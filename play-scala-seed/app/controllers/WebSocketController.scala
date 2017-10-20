package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ BroadcastHub, Flow, Keep, MergeHub, Sink }
import akka.stream.{ ActorMaterializer, KillSwitches, UniqueKillSwitch }
import play.api.mvc._

import scala.concurrent.duration._

class WebSocketController @Inject()(cc: ControllerComponents) extends AbstractController(cc){

  def connect() = WebSocket.accept[String, String] { req =>
    Flow.fromFunction(txt => s"flow passed! $txt")
  }

  def dynamic() = WebSocket.accept[String, String] { req =>
    DynamicStream.publishSubscribeFlow
  }

}

object DynamicStream {

  implicit val as = ActorSystem()
  implicit val mat = ActorMaterializer()

  lazy val publishSubscribeFlow: Flow[String, String, UniqueKillSwitch] = {

    val (sink, source) =
      MergeHub.source[String](perProducerBufferSize = 16)
          .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
          .run()

    source.runWith(Sink.ignore)

    val busFlow: Flow[String, String, UniqueKillSwitch] =
    Flow.fromSinkAndSource(sink, source)
        .joinMat(KillSwitches.singleBidi[String, String])(Keep.right)
        .backpressureTimeout(3.seconds)

    busFlow
  }
}
