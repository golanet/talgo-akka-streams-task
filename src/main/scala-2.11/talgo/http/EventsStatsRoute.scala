package talgo.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import talgo.services.EventStatsServiceLike

import scala.concurrent.ExecutionContext

/**
  * Created by TG on 15/03/2018.
  */
case class EventsStatsRoute(eventStatsService: EventStatsServiceLike)
                           (implicit executionContext: ExecutionContext)
  extends SprayJsonSupport with JsonSupportProtocols {

  val route: Route = pathPrefix("events") {
    path("stats") {
      get {
        onSuccess(eventStatsService.getWordCountsStats) {
          stats => complete(stats)
        }
      }
    }

  }
}
