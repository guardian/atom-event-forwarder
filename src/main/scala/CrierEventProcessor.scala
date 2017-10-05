/* based on https://github.com/guardian/fastly-cache-purger/blob/master/src/main/scala/com/gu/fastly/CrierEventProcessor.scala */
import com.amazonaws.services.kinesis.model.Record
import com.gu.crier.model.event.v1.Event
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

object CrierEventProcessor extends Logging {
  def process(records:Seq[Record])(func: (Event,Record)=>Future[Boolean]):Future[Int] = {
    /*
    Asynchronously processes a batch of records.  Returns a future that will evaluate to the total number correctly processed
    when processing is complete
     */
    val processingResults:Iterable[Future[Boolean]] = records.flatMap { record=>
      val event = eventFromRecord(record)
      event.map { e=>
        func(e, record)
      }.recover {
        case error=>
          logger.error(s"Unable to deserialise event from stream: $error, skipping")
          Future(false)
      }.toOption
    }

    Future.sequence(processingResults).map(_.count(_==true))
//    val handledCount: Int = processingResults.count(_ == true)
//    println(s"Successfully handled $handledCount pieces of content")
//    handledCount
  }

  def eventFromRecord(record: Record):Try[Event] = {
    val buffer = record.getData
    Try(ThriftDeserializer.fromByteBuffer(buffer)(Event.decode))
  }
}
