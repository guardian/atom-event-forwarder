import java.io.{InputStream, OutputStream}

import org.apache.commons.codec.digest.DigestUtils
import com.amazonaws.services.kinesis.clientlibrary.types.UserRecord
import com.amazonaws.services.kinesis.model.Record
import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.lambda.runtime
import com.gu.contentatom.thrift.Atom
import com.gu.crier.model.event.v1._
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class AtomForwarderLambda extends RequestHandler[KinesisEvent, Unit] {
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

    println(s"Processing ${userRecords.size} records ...")
    CrierEventProcessor.process(userRecords.asScala) { (event, record)=>
      event.itemType match {
        case ItemType.Atom=>
          event.payload.map({
            case EventPayload.Atom(atom)=>
              println("Got an atom payload, forwarding on...")
              Future.sequence(Seq(
                SNSHandler.tellSNS(event),
                KinesisHandler.tellKinesis(record)
              )).map({ futureSeq=>
                //each of the handlers return Future[Bool]. We're happy if either succeeds, right now.
                futureSeq.head || futureSeq(1)
              })
            case _=>
              Future(false)
          }).getOrElse(Future(false))
        case _=>
          println(s"This event is for a ${itemTypeAsString(event.itemType)} ${eventTypeAsString(event.eventType)}, not going to do anything.")
          Future(false)
      }
    }.onComplete({ totalProcessed:Try[Int]=>
      println(s"Processed a total of ${totalProcessed.get} records successfully")
    })
  }
}
