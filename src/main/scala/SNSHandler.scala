import java.util.Base64

import com.amazonaws.SdkBaseException
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.kinesis.model.Record
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object SNSHandler extends Logging {
  def getClient:AmazonSNSClient = new AmazonSNSClient()

  def tellSNS(record:Record):Future[Boolean] = Future {
    sys.env.get("DESTINATION_TOPIC_ARN") match {
      case Some(topicArn)=>
        logger.info(s"Passing message from record ${record.getSequenceNumber} onto topic $topicArn")
        val base64EncodedContent = Base64.getEncoder.encodeToString(record.getData.array())
        val rq = new PublishRequest().withTopicArn(topicArn).withMessage(base64EncodedContent)
        try {
          val result = getClient.publish(rq)
          logger.info(s"Message has been sent with ID ${result.getMessageId}")
          true
        } catch {
          case e:SdkBaseException=>
            logger.error(s"Unable to send message: ${e.getMessage}")
            false
        }
      case None=>
        logger.info("No topic ARN configured so not sending to SNS")
        false
    }
  }
}
