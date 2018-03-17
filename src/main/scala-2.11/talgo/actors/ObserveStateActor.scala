package talgo.actors

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink}
import akka.util.Timeout
import talgo.actors.ObserveStateActor.{ObserveStateRequest, ObserveStateResponse, SetState}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/**
  * Created by TG on 17/03/2018.
  */

/**
  * ObserveStateActor is a generic implementation for an actor which keeps state S
  * It provides get current state and set state functionality
  *
  * The messages of this Actor are private and not to be used from outside
  * Instead, the Actor provides an high-level API to update the state using the Akka Streams Sink interface
  * and ObserveState which generates a Future of the state value S (implemented by Ask pattern)
  */
object ObserveStateActor {
  // an higher-level API for getting the current state
  type ObserveState[S] = () => Future[S]

  def asSinkAndObserveState[S: ClassTag](initState: S)
                                        (implicit actorSystem: ActorSystem,
                                         ec: ExecutionContext): (Sink[S, NotUsed], ObserveState[S]) = {
    val actorRef = actorSystem.actorOf(Props.apply(new ObserveStateActor[S](initState)))

    (asSink(actorRef), asObserveState(actorRef))
  }

  // Actor internal messages
  private case class SetState[S](s: S)
  private case object ObserveStateRequest
  private case class ObserveStateResponse[S](s: S)


  private def asSink[S](actorRef: ActorRef): Sink[S, NotUsed] = {
    Flow[S].map(SetState(_))
      .to(Sink.actorRef(actorRef, OverflowStrategy.fail))
  }

  private def asObserveState[S: ClassTag](actorRef: ActorRef)(implicit ec: ExecutionContext): ObserveState[S] = {
    import akka.pattern.ask

    import scala.concurrent.duration._
    implicit val askTimeout: Timeout = Timeout(30 seconds)

    () => for {
      ObserveStateResponse(state: S) <- actorRef ? ObserveStateRequest
    } yield state
  }
}

class ObserveStateActor[S: ClassTag](initState: S) extends Actor {
  override def receive: Receive = receiveWithState(state = initState)

  private def receiveWithState(state: S): Receive = {
    case SetState(s: S) =>
      context.become(receiveWithState(s))
    case ObserveStateRequest =>
      sender ! ObserveStateResponse(state)
  }
}
