import com.amazonaws.SdkBaseException
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.kinesis.model.Record
import com.gu.crier.model.event.v1.{Event, EventPayload, ItemType, _}
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.auto
import com.gu.contentapi.json.CirceEncoders
import com.gu.contentatom.thrift.Atom
import com.gu.contentapi.client.model.v1.Content

object SNSHandler extends CrossAccount with Logging {
/*
  def payloadId : _root_.scala.Predef.String
  def eventType : com.gu.crier.model.event.v1.EventType
  def itemType : com.gu.crier.model.event.v1.ItemType
  def dateTime : scala.Long
  def payload : scala.Option[com.gu.crier.model.event.v1.EventPayload]
 */
  implicit val encodeAtom:Encoder[Atom] = CirceEncoders.atomEncoder
  implicit val encodeContent:Encoder[Content] = CirceEncoders.contentEncoder

  implicit val encodePayload: Encoder[Option[EventPayload]] = new Encoder[Option[EventPayload]] {
    def getPayloadData:Option[EventPayload] => Json = {
      case Some(payload)=>
        payload match {
          case EventPayload.Atom(atom)=>
            Json.obj(("atom", atom.asInstanceOf[Atom].asJson))
          case EventPayload.Content(content)=>
            Json.obj(("content", content.asInstanceOf[Content].asJson))
          case _=>
            Json.Null
        }
      case None=>
        Json.Null
    }

    override final def apply(a: Option[EventPayload]): Json = getPayloadData(a)
//    final def apply(maybePayload: Option[EventPayload]): Json = Json.obj(
//      ("content",getPayloadData(maybePayload))
//    )
  }

  implicit val encodeEvent: Encoder[Event] = new Encoder[Event] {
    final def apply(event: Event): Json = Json.obj(
      ("payloadId", Json.fromString(event.payloadId)),
      ("eventType", event.eventType.asJson),
      ("itemType", event.itemType.asJson),
      ("dateTime", Json.fromLong(event.dateTime)),
      ("payload", event.payload.asJson)
    )
  }

  def getClient:AmazonSNSClient = new AmazonSNSClient(assumeRoleCredentials)

  def eventToJson(event:Event):Option[String] = {
    event.itemType match {
      case ItemType.Atom=>
        event.payload.map({
          case EventPayload.Atom(atom)=>
            event.asJson.noSpaces
        })
      case ItemType.Content=>
        event.payload.map({
          case EventPayload.Content(content)=>
            event.asJson.noSpaces
        })
      case _=>None
    }
  }

  def tellSNS(event:Event, awsId: String):Future[Boolean] = Future {
    sys.env.get("DESTINATION_TOPIC_ARN") match {
      case Some(topicArn)=>
        eventToJson(event) match {
          case Some(jsonContent)=>
            val rq = new PublishRequest().withTopicArn(topicArn).withMessage(jsonContent)
            try {
              val result = getClient.publish(rq)
              logger.info(s"$awsId Message has been sent with ID ${result.getMessageId}")
              true
            } catch {
              case e:SdkBaseException=>
                logger.error(s"$awsId Unable to send message: ${e.getMessage}")
                false
            }
          case None=>
            logger.info(s"$awsId This wasn't a known event so not touching it.")
            false
        }

      case None=>
        logger.info(s"$awsId No topic ARN configured so not sending to SNS")
        false
    }
  }
}
