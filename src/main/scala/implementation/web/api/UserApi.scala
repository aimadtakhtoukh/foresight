package implementation.web.api

import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError, NotFound}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.LongNumber
import akka.http.scaladsl.server.Route
import domain.models.{Id, User}
import implementation.database.UserDatabaseAccess
import implementation.web.api.json.CirceSupport._
import implementation.web.api.traits.RouteProvider
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object UserApi extends RouteProvider {
  override def routes(implicit ec: ExecutionContext): Route =
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

  private def allUsers(implicit ec: ExecutionContext) : Route =
    path("all") {
      get {
        onComplete(UserDatabaseAccess.all()) {
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Right(users)) =>
            complete(HttpEntity(ContentTypes.`application/json`, users.map(toDTO).asJson.noSpaces))
        }
      }
    }

  private def byId(implicit ec: ExecutionContext) : Route =
    path(LongNumber) {id: Long =>
      get {
        onComplete(UserDatabaseAccess.byId(Id(id))) {
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Left(domain.database.error.NotFound)) => complete(NotFound, s"No user with id $id")
          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Right(user)) =>
            complete(HttpEntity(ContentTypes.`application/json`, toDTO(user).asJson.noSpaces))
        }
      }
    }

  private def byName(implicit ec: ExecutionContext) : Route =
    path("name" / Segment) { name : String =>
      get {
        onComplete(UserDatabaseAccess.byName(name)) {
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Left(domain.database.error.NotFound)) => complete(NotFound, s"No user with name $name")
          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Right(user)) =>
            complete(HttpEntity(ContentTypes.`application/json`, toDTO(user).asJson.noSpaces))
        }
      }
    }

  private def add(implicit ec: ExecutionContext): Route = {
    path("add") {
      post {
        entity(as[UserDTO]) { dto =>
          toModel(dto) match {
            case None => complete(BadRequest, s"User is empty")
            case Some(user) =>
              onComplete(UserDatabaseAccess.add(user)) {
                case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
                case Success(Left(domain.database.error.Unknown(ex))) =>
                  complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
                case Success(Right(_)) => complete(StatusCodes.OK)
              }
          }
        }
      }
    }
  }

  private def update(implicit ec: ExecutionContext): Route =
    path("update" / LongNumber) {id =>
      post {
        entity(as[UserDTO]) { dto =>
          toModel(dto) match {
            case None => complete(BadRequest, s"User is empty")
            case Some(user) =>
              onComplete(UserDatabaseAccess.update(Id(id), user.copy(id = Some(Id(id))))) {
                case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
                case Success(Left(domain.database.error.Unknown(ex))) =>
                  complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
                case Success(Right(_)) => complete(StatusCodes.OK)
              }
          }
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
}
