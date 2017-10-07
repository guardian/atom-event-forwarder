import com.gu.contentatom.thrift.atom.media._
import com.gu.contentatom.thrift.{Atom, AtomData, AtomType, ContentChangeDetails}
import com.gu.contentapi.client.model.v1.{Content,ContentType}
import com.gu.crier.model.event.v1._
import org.scalatest.{FunSuite, Matchers, MustMatchers}
import org.apache.logging.log4j.scala.Logging

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

    val maybeJson = SNSHandler.eventToJson(event)
    maybeJson should be(defined)
    val jsonString = maybeJson.get
    jsonString should equal("{\"payloadId\":\"fakeIdString\",\"eventType\":{\"Update\":{}},\"itemType\":{\"Atom\":{}},\"dateTime\":1507221293,\"payload\":{\"atom\":{\"id\":\"someId\",\"atomType\":\"media\",\"labels\":[],\"defaultHtml\":\"<p></p>\",\"data\":{\"media\":{\"assets\":[{\"assetType\":\"audio\",\"version\":1,\"id\":\"SomeAssetId\",\"platform\":\"url\"}],\"title\":\"some title\",\"category\":\"news\"}},\"contentChangeDetails\":{\"revision\":1},\"commissioningDesks\":[]}}}")
  }

  test("eventToJson should serialize a content event"){

    //val contentItem = Content(id="someFakeId",ContentType.Article)

    val contentItem = Content(id="someFakeId",ContentType.Article,webTitle="some web title",webUrl="http://path/to/web",apiUrl="http://path/to/api")

    val payload = EventPayload.Content(contentItem.asInstanceOf[EventPayloadAliases.ContentAlias])

    val event = Event(payloadId = "fakeIdString",
      eventType = EventType.Update,
      itemType = ItemType.Content,
      dateTime = 1507221293,
      payload = Some(payload)
    )

    val maybeJson = SNSHandler.eventToJson(event)
    maybeJson should be(defined)
    val jsonString = maybeJson.get
    jsonString should equal("{\"payloadId\":\"fakeIdString\",\"eventType\":{\"Update\":{}},\"itemType\":{\"Content\":{}},\"dateTime\":1507221293,\"payload\":{\"content\":{\"id\":\"someFakeId\",\"type\":\"article\",\"webTitle\":\"some web title\",\"webUrl\":\"http://path/to/web\",\"apiUrl\":\"http://path/to/api\",\"tags\":[],\"references\":[],\"isHosted\":false}}}")
  }
}
