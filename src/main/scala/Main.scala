import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import domain.models.{Entry, Id, Off, On}
import implementation.database.{EntryDatabaseAccess, UserDatabaseAccess}
import implementation.database.utils.ConnectionConfig
import implementation.web.api.{EntryApi, UserApi}

import java.time.LocalDate
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration.Duration
import scala.io.StdIn

//object Main extends App {
//  implicit val db = ConnectionConfig.database
//  try {
//    val result = Await.result(
////      UserDatabaseAccess.update(Id(2L), User(Some(Id(2L)), "Lucas")),
////      UserDatabaseAccess.all(),
////      EntryDatabaseAccess.add(Entry(None, Id(1L), LocalDate.now(), On)),
////      EntryDatabaseAccess.update(Id(1L), Entry(Some(Id(1L)), Id(1L), LocalDate.now(), Off)),
//      EntryDatabaseAccess.byId(Id(1L)),
//      Duration.Inf
//    )
//    println(result)
//  } catch {
//    case t: Throwable => t.printStackTrace()
//  } finally db.close
//}

object Main extends App {
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "foresight")
  implicit val executionContext: ExecutionContext = system.executionContext

  val route = concat(
    UserApi.routes,
    EntryApi.routes
  )

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

  println(s"Server now online. Please navigate to http://localhost:8080/user/all\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}
