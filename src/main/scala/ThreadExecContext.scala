import java.util.concurrent.Executors
import scala.concurrent._
import org.apache.logging.log4j.scala.Logging

object ThreadExecContext extends Logging {
  implicit val ec: ExecutionContext = new ExecutionContext {
    private val threadPool = Executors.newFixedThreadPool(sys.env.getOrElse("THREADS", 1).toString.toInt)

    override def execute(runnable: Runnable): Unit = {
      threadPool.submit(runnable)
    }

    override def reportFailure(cause: Throwable): Unit = {
      logger.error(cause.getMessage)
      logger.error(cause.getStackTrace)
    }

    def shutdown:Unit = threadPool.shutdown()

  }
}