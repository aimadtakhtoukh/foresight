package domain.web.api.route

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

trait RouteProvider {
  def routes(implicit actorSystem: ActorSystem[Nothing], ec : ExecutionContext): Route
}
