package implementation.web.api

import akka.actor.typed.ActorSystem
import akka.http.interop.ZIOSupport
import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError, NotFound, OK}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.LongNumber
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MarshallingDirectives
import domain.database.error.{DatabaseError, NotFound, Unknown}
import domain.models.{Id, User}
import domain.web.api.route.RouteProvider
import implementation.database.UserDatabaseAccess
import implementation.web.api.json.CirceSupport._
import io.circe.generic.auto._
import io.circe.syntax._
import zio.ZIO

import scala.concurrent.ExecutionContext

object UserApi extends RouteProvider with ZIOSupport {
  override def routes(implicit actorSystem: ActorSystem[Nothing], ec : ExecutionContext): Route =
    pathPrefix("user") {
      concat(
        allUsers,
        byId,
        byName,
        add,
        update
      )
    }

  case class UserDTO(id: Option[Long], name: String)

  import ModelToDTO._

  def allUsers(implicit ec: ExecutionContext) : Route =
    path("all") {
      get {
        complete(
          UserDatabaseAccess.all()
            .mapBoth(
              handleDatabaseError,
              users => HttpResponse(status = OK, entity = HttpEntity(ContentTypes.`application/json`, users.map(toDTO).asJson.noSpaces))
            )
        )
//        onComplete(UserDatabaseAccess.all()) {
//          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//          case Success(Right(users)) =>
//            complete(HttpEntity(ContentTypes.`application/json`, users.map(toDTO).asJson.noSpaces))
//        }
      }
    }

  def byId(implicit ec: ExecutionContext) : Route =
    path(LongNumber) {id: Long =>
      get {
        complete(
          UserDatabaseAccess.byId(Id(id))
            .mapBoth(
              handleDatabaseError,
              user => HttpResponse(status = OK, entity = HttpEntity(ContentTypes.`application/json`, toDTO(user).asJson.noSpaces))
            )
        )
//        onComplete(UserDatabaseAccess.byId(Id(id))) {
//          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//          case Success(Left(domain.database.error.NotFound)) => complete(NotFound, s"No user with id $id")
//          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//          case Success(Right(user)) =>
//            complete(HttpEntity(ContentTypes.`application/json`, toDTO(user).asJson.noSpaces))
//        }
      }
    }

  def byName(implicit ec: ExecutionContext) : Route =
    path("name" / Segment) { name : String =>
      get {
        complete(
          UserDatabaseAccess.byName(name)
            .mapBoth(
              handleDatabaseError,
              user => HttpResponse(status = OK, entity = HttpEntity(ContentTypes.`application/json`, toDTO(user).asJson.noSpaces))
            )
        )
//        onComplete(UserDatabaseAccess.byName(name)) {
//          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//          case Success(Left(domain.database.error.NotFound)) => complete(NotFound, s"No user with name $name")
//          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//          case Success(Right(user)) =>
//            complete(HttpEntity(ContentTypes.`application/json`, toDTO(user).asJson.noSpaces))
//        }
      }
    }

  def add(implicit ec: ExecutionContext): Route = {
    path("add") {
      post {
        entity(MarshallingDirectives.as[UserDTO]) { dto =>
          complete(
            (for {
              userOption <- ZIO.effect(toModel(dto))
              user <- ZIO.fromOption(userOption).orElseFail(HttpResponse(BadRequest, entity = "User is empty"))
              _ <- UserDatabaseAccess.add(user)
            } yield ())
              .mapBoth({
                case hr : HttpResponse => hr
                case db : DatabaseError => handleDatabaseError(db)
              },
                _ => HttpResponse(OK)
              )
          )
//          toModel(dto) match {
//            case None => complete(BadRequest, s"User is empty")
//            case Some(user) =>
//              onComplete(UserDatabaseAccess.add(user)) {
//                case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//                case Success(Left(domain.database.error.Unknown(ex))) =>
//                  complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//                case Success(Right(_)) => complete(StatusCodes.OK)
//              }
//          }
        }
      }
    }
  }

  def update(implicit ec: ExecutionContext): Route =
    path("update" / LongNumber) {id =>
      post {
        entity(MarshallingDirectives.as[UserDTO]) { dto =>
          complete(
            (for {
              userOption <- ZIO.effect(toModel(dto))
              user <- ZIO.fromOption(userOption).orElseFail(HttpResponse(BadRequest, entity = "User is empty"))
              _ <- UserDatabaseAccess.update(Id(id), user.copy(id = Some(Id(id))))
            } yield ())
              .mapBoth({
                case hr : HttpResponse => hr
                case db : DatabaseError => handleDatabaseError(db)
              },
                _ => HttpResponse(OK)
              )
          )
//          toModel(dto) match {
//            case None => complete(BadRequest, s"User is empty")
//            case Some(user) =>
//              onComplete(UserDatabaseAccess.update(Id(id), user.copy(id = Some(Id(id))))) {
//                case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//                case Success(Left(domain.database.error.Unknown(ex))) =>
//                  complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//                case Success(Right(_)) => complete(StatusCodes.OK)
//              }
//          }
        }
      }
    }

  object ModelToDTO {
    def toDTO(model : User) : UserDTO = UserDTO(id = model.id.map(_.value), name = model.name)
    def toModel(dto: UserDTO): Option[User] = dto match {
      case UserDTO(_, "") => None
      case UserDTO(id, name) => Some(User(id = id.map(Id), name = name))
    }
  }

  private def handleDatabaseError: DatabaseError => HttpResponse = {
    case Unknown(ex) => HttpResponse(status = InternalServerError, entity = ex.getMessage)
    case domain.database.error.NotFound => HttpResponse(status = InternalServerError, entity = "Not found")
  }
}
