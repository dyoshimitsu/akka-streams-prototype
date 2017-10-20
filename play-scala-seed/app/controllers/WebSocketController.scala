package controllers

import javax.inject.Inject

import akka.stream.scaladsl.Flow
import play.api.mvc._

class WebSocketController @Inject()(cc: ControllerComponents) extends AbstractController(cc){

  def connect() = WebSocket.accept[String, String] { req =>
    Flow.fromFunction(txt => s"flow passed! $txt")
  }

}
