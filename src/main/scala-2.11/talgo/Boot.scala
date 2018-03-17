package talgo

import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import talgo.http.EventsStatsRoute
import talgo.processing.EventProcessor
import talgo.services.EventStatsService
import talgo.utils.AkkaStreamsUtils

import scala.concurrent.ExecutionContext

object Boot extends App {

  def startApplication() = {
    implicit val actorSystem: ActorSystem = ActorSystem("talgo-akka-http")
    implicit val executor: ExecutionContext = actorSystem.dispatcher
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val host = "127.0.0.1"
    val port = 8080

    val processCmd: String = Paths.get(this.getClass.getResource("/generator-windows-amd64.exe").toURI).toFile.getAbsolutePath

    val fromProcessEventsSource: Source[String, NotUsed] = AkkaStreamsUtils.Source.fromProcessOutput(processCmd)

    val eventStatsService = new EventStatsService(eventsSource = fromProcessEventsSource, eventProcessor = EventProcessor)

    val httpRoute = new EventsStatsRoute(eventStatsService)

    Http().bindAndHandle(httpRoute.route, host, port)
  }

  startApplication()
}