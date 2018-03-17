package talgo.processing

import spray.json.DefaultJsonProtocol

/**
  * Created by TG on 16/03/2018.
  */
object Event extends DefaultJsonProtocol {
  implicit val eventJsonConverter = jsonFormat3(Event.apply)
}

case class Event(event_type: String, data: String, timestamp: Long)
