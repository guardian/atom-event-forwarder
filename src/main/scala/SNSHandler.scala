import com.amazonaws.SdkBaseException
import com.amazonaws.services.sns.{AmazonSNS, AmazonSNSClientBuilder}
import com.amazonaws.services.sns.model.PublishRequest
import com.gu.crier.model.event.v1.{Event, EventPayload, ItemType, _}
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.{ExecutionContext, Future}
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import com.gu.contentapi.json.CirceEncoders._
import com.gu.fezziwig.CirceScroogeMacros.{encodeThriftStruct, encodeThriftUnion}

object SNSHandler extends CrossAccount with Logging {
/*
  def payloadId : _root_.scala.Predef.String
  def eventType : com.gu.crier.model.event.v1.EventType
  def itemType : com.gu.crier.model.event.v1.ItemType
  def dateTime : scala.Long
  def payload : scala.Option[com.gu.crier.model.event.v1.EventPayload]
 */
  implicit val ec:ExecutionContext = ThreadExecContext.ec

  //for some reason SNS gives a "Bad request" if you don't give a specific region to operate in and rely on auto-detection.
  def getClient:AmazonSNS = sys.env.get("EXPLICIT_REGION") match {
    case Some(region_name)=>AmazonSNSClientBuilder.standard ().withRegion(region_name).withCredentials (assumeRoleCredentials).build ()
    case None=>AmazonSNSClientBuilder.standard ().withCredentials (assumeRoleCredentials).build ()
  }

  def eventToJson(event: Event): Json = event.asJson

  def tellSNS(event:Event, awsId: String):Future[Boolean] = Future {
    sys.env.get("DESTINATION_TOPIC_ARN") match {
      case Some(topicArn)=>
        val json = eventToJson(event)
        val rq = new PublishRequest().withTopicArn(topicArn).withMessage(json.noSpaces)
        try {
          val result = getClient.publish(rq)
          logger.info(s"$awsId Message has been sent with ID ${result.getMessageId}")
          true
        } catch {
          case e:SdkBaseException=>
            logger.error(s"$awsId Unable to send message: ${e.getMessage}")
            throw e
        }

      case None=>
        logger.info(s"$awsId No topic ARN configured so not sending to SNS")
        false
    }
  }
}
