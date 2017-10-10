import java.io.{InputStream, OutputStream}

import org.apache.commons.codec.digest.DigestUtils
import com.amazonaws.services.kinesis.clientlibrary.types.UserRecord
import com.amazonaws.services.kinesis.model.Record
import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.lambda.runtime
import com.gu.contentatom.thrift.Atom
import com.gu.crier.model.event.v1._
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import org.apache.logging.log4j.scala.Logging

import scala.collection.JavaConverters._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try

class AtomForwarderLambda extends RequestHandler[KinesisEvent, Unit] with Logging {
  implicit val ec:ExecutionContext = ThreadExecContext.ec

  def itemTypeAsString(itemType: ItemType): String = itemType match {
    case ItemType.Content=>"Content"
    case ItemType.Section=>"Section"
    case ItemType.Tag=>"Tag"
    case ItemType.StoryPackage=>"Story Package"
    case _=>"unknown"
  }

  def eventTypeAsString(eventType: EventType):String = eventType match {
    case EventType.Update=>"Update"
    case EventType.Delete=>"Delete"
    case EventType.RetrievableUpdate=>"Retrievable Update"
    case _=>"unknown"
  }

  override def handleRequest(event:KinesisEvent, context: Context): Unit = {
    val rawRecords: List[Record] = event.getRecords.asScala.map(_.getKinesis).toList
    val userRecords = UserRecord.deaggregate(rawRecords.asJava)

    logger.info(s"${context.getAwsRequestId} Processing ${userRecords.size} records ...")

    val processingFuture = CrierEventProcessor.process(userRecords.asScala) { (event, record)=>
      event.itemType match {
        case ItemType.Atom=>
          event.payload.map({
            case EventPayload.Atom(atom)=>
              logger.info(s"${context.getAwsRequestId} Got an atom payload, forwarding on...")
              SNSHandler.tellSNS(event, context.getAwsRequestId)
            case _=>
              Future(false)
          }).getOrElse(Future(false))
        case _=>
          logger.warn(s"${context.getAwsRequestId} This event is for a ${itemTypeAsString(event.itemType)} ${eventTypeAsString(event.eventType)}, not going to do anything.")
          Future(false)
      }
    }

    processingFuture.onComplete({ totalProcessed:Try[Int]=>
      logger.info(s"${context.getAwsRequestId} Processed a total of ${totalProcessed.get} records successfully")
    })

    logger.info(s"${context.getAwsRequestId} waiting for threads")
    Await.result(processingFuture, (context.getRemainingTimeInMillis-100) millis)
    logger.info(s"${context.getAwsRequestId} completed")
  }
}
