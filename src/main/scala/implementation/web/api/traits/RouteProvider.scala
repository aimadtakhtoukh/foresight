package implementation.web.api.traits

import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

trait RouteProvider {
  def routes(implicit executionContext: ExecutionContext): Route
}
