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

  }
}
