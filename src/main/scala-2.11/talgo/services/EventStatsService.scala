package talgo.services

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import talgo.WordCount
import talgo.actors.ObserveStateActor
import talgo.processing.EventProcessorLike

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by TG on 17/03/2018.
  */
trait EventStatsServiceLike {
  def getWordCountsStats: Future[Map[String, WordCount]]
}

class EventStatsService(eventsSource: Source[String, NotUsed], eventProcessor: EventProcessorLike)
                       (implicit materializer: Materializer, ec: ExecutionContext, actorSystem: ActorSystem)
  extends EventStatsServiceLike {
  /**
    * initialization phase
    */
  val (observeStateSink, observeState) = ObserveStateActor.asSinkAndObserveState[Map[String, WordCount]](initState = Map.empty)
  eventProcessor.getRunnableGraph(eventsSource)(observeStateSink).run()
  /**
    * End of initialization phase
    */

  override def getWordCountsStats: Future[Map[String, WordCount]] = observeState.apply()
}