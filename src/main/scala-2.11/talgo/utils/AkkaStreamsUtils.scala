package talgo.utils

import akka.NotUsed
import akka.stream.scaladsl.Source
import scala.sys.process.Process

/**
  * Created by TG on 17/03/2018.
  */
object AkkaStreamsUtils {
  object Source {
    def fromProcessOutput(process: String): Source[String, NotUsed] = {
      akka.stream.scaladsl.Source.fromIterator[String](() => Process(process).lineStream.toIterator)
    }
  }
}
