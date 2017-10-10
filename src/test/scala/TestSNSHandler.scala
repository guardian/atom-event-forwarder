import com.gu.contentatom.thrift.atom.media._
import com.gu.contentatom.thrift.{Atom, AtomData, AtomType, ContentChangeDetails}
import com.gu.crier.model.event.v1._
import org.scalatest.{FunSuite, Matchers}
import org.apache.logging.log4j.scala.Logging
import io.circe.parser._
import gnieh.diffson.circe._

class TestSNSHandler extends FunSuite with Matchers with Logging {
  test("eventToJson should serialize an atom event"){
    val asset = Asset(AssetType.Audio, version=1,id="SomeAssetId",platform=Platform.Url)
    val mediaAtom = MediaAtom(Seq(asset),title="some title",category = Category.News)

    val atom = Atom(id = "someId", atomType = AtomType.Media, defaultHtml = "<p></p>", data = AtomData.Media(mediaAtom), contentChangeDetails = ContentChangeDetails(revision = 1))
    val payload = EventPayload.Atom(atom.asInstanceOf[EventPayloadAliases.AtomAlias])

    val event = Event(payloadId = "fakeIdString",
      eventType = EventType.Update,
      itemType = ItemType.Atom,
      dateTime = 1507221293,
      payload = Some(payload)
    )

    val expectedJson = parse(
      """
        |{
        |  "payloadId" : "fakeIdString",
        |  "eventType" : "update",
        |  "itemType" : "atom",
        |  "dateTime" : 1507221293,
        |  "payload" : {
        |    "atom" : {
        |      "id" : "someId",
        |      "atomType" : "media",
        |      "labels" : [
        |      ],
        |      "defaultHtml" : "<p></p>",
        |      "data" : {
        |        "media" : {
        |          "assets" : [
        |            {
        |              "assetType" : "audio",
        |              "version" : 1,
        |              "id" : "SomeAssetId",
        |              "platform" : "url"
        |            }
        |          ],
        |          "title" : "some title",
        |          "category" : "news"
        |        }
        |      },
        |      "contentChangeDetails" : {
        |        "revision" : 1
        |      },
        |      "commissioningDesks" : [
        |      ]
        |    }
        |  }
        |}
      """.stripMargin).toOption.get

    val actualJson = SNSHandler.eventToJson(event)
    val diff = JsonDiff.diff(expectedJson, actualJson, false)
    diff should be(JsonPatch(Nil))
    if (diff != JsonPatch(Nil)) println(diff)
  }

}
