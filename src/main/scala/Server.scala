import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import implementation.web.api.{EntryApi, SecurityApi, UserApi}

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object Server extends App {
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "foresight")
  implicit val executionContext: ExecutionContext = system.executionContext

  val routes = concat(
    UserApi.routes,
    EntryApi.routes,
    SecurityApi.routes
  )

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes)

  println(s"Server now online. Please navigate to http://localhost:8080/user/all\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}
