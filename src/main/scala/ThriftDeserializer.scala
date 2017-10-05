/* taken verbatim from https://github.com/guardian/fastly-cache-purger/blob/master/src/main/scala/com/gu/fastly/ThriftDeserializer.scala,
thanks Chris Birchall & Tom Forbes
 */
import java.nio.ByteBuffer

import com.twitter.scrooge.ThriftStruct
import org.apache.thrift.TBaseHelper
import org.apache.thrift.protocol.{ TProtocol, TCompactProtocol }
import org.apache.thrift.transport.TMemoryInputTransport

object ThriftDeserializer {
  private val protocolFactory = new TCompactProtocol.Factory()

  def inputProtocolFrom(byteBuffer: ByteBuffer): TProtocol =
    inputProtocolFrom(TBaseHelper.byteBufferToByteArray(byteBuffer))

  def inputProtocolFrom(bytes: Array[Byte]): TProtocol =
    protocolFactory.getProtocol(new TMemoryInputTransport(bytes))

  def fromByteBuffer[T <: ThriftStruct](byteBuffer: ByteBuffer)(decoder: TProtocol => T): T =
    decoder(inputProtocolFrom(byteBuffer))

}