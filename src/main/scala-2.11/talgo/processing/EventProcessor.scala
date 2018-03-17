package talgo.processing

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import spray.json._
import talgo.WordCount
import talgo.processing.Event._
import scalaz.Scalaz._

/**
  * Created by TG on 16/03/2018.
  */
trait EventProcessorLike {
  def getRunnableGraph[T](source: Source[String, NotUsed])
                         (sink: Sink[Map[String, WordCount], T]): RunnableGraph[T]
}

object EventProcessor extends EventProcessorLike {

  override def getRunnableGraph[T](source: Source[String, NotUsed])
                                  (sink: Sink[Map[String, WordCount], T]): RunnableGraph[T] = {
    source.log("before-processing")
      .via(parseJsonFlow).log("parse-event-json")
      .via(wordCountFlow).log("calc-word-count")
      .toMat(sink)(Keep.right)
  }

  object WordCount {
    def mergeWordCounts(wcs: Map[String, WordCount]*) = wcs.reduce(_ |+| _)
  }

  val parseJsonFlow = Flow[String].map { line => line.parseJson.convertTo[Event] }
    // drop corrupted events
    .withAttributes(ActorAttributes.supervisionStrategy(Supervision.resumingDecider))
    .log("json parser")

  val wordCountFlow = Flow[Event].scan(Map.empty[String, WordCount])(
    (acc: Map[String, WordCount], e: Event) =>
      WordCount.mergeWordCounts(acc, eventWordCounts(e))
  ).log("word count flow")

  private def eventWordCounts(e: Event): Map[String, WordCount] = {
    val EVENT_TYPE = "EVENT_TYPE"
    val DATA = "DATA"

    val eventTypeWordCount: WordCount = Map(e.event_type -> 1)
    val dataWordCount: WordCount = e.data.split(" ")
      .groupBy(w => w)
      .mapValues { words => words.length.toLong }

    Map(
      EVENT_TYPE -> eventTypeWordCount,
      DATA -> dataWordCount
    )
  }

  //  def createRunnableGraph(inputSource: Source[String, NotUsed])
  //                         (eventTypeCounterActor: ActorRef, dataWordCounterActor: ActorRef)
  //                         (implicit actorSystem: ActorSystem): RunnableGraph[NotUsed] = {
  //    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
  //      import GraphDSL.Implicits._
  //      import spray.json._
  //
  //      val in = inputSource
  //
  //      in.log("before-processing")
  //
  //      val parseJson = Flow[String].map { line => line.parseJson.convertTo[Event] }
  //        // drop corrupted events
  //        .withAttributes(ActorAttributes.supervisionStrategy(Supervision.resumingDecider))
  //        .log("json parser")
  //
  //      val bcast = builder.add(Broadcast[Event](2))
  //
  //      val createIncrementForEventType = Flow[Event].map { msg => IncrementWord(msg.event_type) }
  //      val createIncrementForDataWord = Flow[Event].mapConcat[IncrementWord] { msg =>
  //        val tokens = msg.data.split(" ")
  //        val incrementWords: Array[IncrementWord] = tokens.map(IncrementWord(_))
  //
  //        incrementWords.to[collection.immutable.Iterable]
  //      }
  //
  //      val eventTypeCounterSink = Sink.actorRef(eventTypeCounterActor, OverflowStrategy.fail)
  //      val dataWordCounterSink = Sink.actorRef(dataWordCounterActor, OverflowStrategy.fail)
  //
  //      in ~> parseJson ~> bcast
  //
  //      bcast ~> createIncrementForEventType ~> eventTypeCounterSink
  //      bcast ~> createIncrementForDataWord ~> dataWordCounterSink
  //
  //      ClosedShape
  //    })
  //  }
  //
}
