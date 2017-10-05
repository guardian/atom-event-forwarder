import java.util.Base64

import com.amazonaws.SdkBaseException
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.amazonaws.services.kinesis.model.{PutRecordRequest, Record}
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object KinesisHandler extends CrossAccount with Logging {

  def tellKinesis(record: Record):Future[Boolean] = Future {
    val client = AmazonKinesisClientBuilder.standard().withCredentials(assumeRoleCredentials).build()

    sys.env.get("DESTINATION_STREAM_NAME") match {
      case Some(streamName)=>
        val rq = new PutRecordRequest().withData(record.getData)
          .withPartitionKey(record.getPartitionKey)
          .withSequenceNumberForOrdering(record.getSequenceNumber)
          .withStreamName(streamName)
        try {
          val result = client.putRecord(rq)
          logger.info(s"Posted message ${result.toString}")
          true
        } catch {
          case e:SdkBaseException=>
            logger.error(s"Unable to post message to $streamName: ${e.getMessage}")
            false
        }
      case None=>
        false
    }
  }
}
