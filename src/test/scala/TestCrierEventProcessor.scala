import java.nio.ByteBuffer

import org.scalatest.{FunSuite, Matchers, MustMatchers}
import com.amazonaws.services.kinesis.model.Record
import com.gu.crier.model.event.v1.{Event, EventType, ItemType}
import org.apache.thrift.transport.TMemoryBuffer
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TTransport
import org.scalatest.OptionValues._
import org.apache.logging.log4j.scala.Logging

class TestCrierEventProcessor extends FunSuite with MustMatchers with Logging {
  test("should decode a simple event with no payload"){
    val testEvent = Event("dummyPayload",
      eventType = EventType.Update,
      itemType = ItemType.Content,
      dateTime = System.currentTimeMillis()/1000
    )

    val transport = new TMemoryBuffer(512)
    val protocol = new TBinaryProtocol(transport)

    testEvent.write(protocol)
    transport.flush()

    println(s"transport array: ${transport.getArray.toString}")
    
    val testRecord = new Record().withData(ByteBuffer.wrap(transport.getArray))

    val handledCount = CrierEventProcessor.process(Seq(testRecord)) { event=>
      event.itemType must equal (ItemType.Atom)
      event.payload.isEmpty must equal(true)
      true
    }
    handledCount must equal(1)

  }
}
