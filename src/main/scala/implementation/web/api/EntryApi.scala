package implementation.web.api

import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import domain.models._
import implementation.database.EntryDatabaseAccess
import implementation.web.api.json.CirceSupport._
import implementation.web.api.traits.RouteProvider
import io.circe.generic.auto._
import io.circe.syntax._

import java.time.LocalDate
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object EntryApi extends RouteProvider {
  override def routes(implicit executionContext: ExecutionContext): Route =
    pathPrefix("entry") {
      concat(
        allEntries,
        byId,
        byUserId,
        add,
        update
      )
    }

  case class EntryDTO(id: Option[Long], userId: Long, date: LocalDate, availability: String)

  import ModelToDTO._

  private def allEntries(implicit executionContext: ExecutionContext): Route =
    path("all") {
      get {
        onComplete(EntryDatabaseAccess.all()) {
          case Success(Right(entries)) =>
            complete(HttpEntity(ContentTypes.`application/json`, entries.map(toDTO).asJson.noSpaces))
          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      }
    }

  private def byId(implicit executionContext: ExecutionContext): Route =
    path(LongNumber) {id: Long =>
      get {
        onComplete(EntryDatabaseAccess.byId(Id(id))) {
          case Success(Right(entry)) =>
            complete(HttpEntity(ContentTypes.`application/json`, toDTO(entry).asJson.noSpaces))
          case Success(Left(domain.database.error.NotFound)) => complete(NotFound, s"No entry with id $id")
          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      }
    }

  private def byUserId(implicit executionContext: ExecutionContext): Route =
    path("user" / LongNumber) {id: Long =>
      get {
        onComplete(EntryDatabaseAccess.byUserId(Id(id))) {
          case Success(Right(entries)) =>
            complete(HttpEntity(ContentTypes.`application/json`, entries.map(toDTO).asJson.noSpaces))
          case Success(Left(domain.database.error.NotFound)) => complete(NotFound, s"No user with id $id")
          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      }
    }

  private def add(implicit executionContext: ExecutionContext): Route = {
    path("add") {
      post {
        entity(as[EntryDTO]) { dto =>
          onComplete(EntryDatabaseAccess.add(toModel(dto))) {
            case Success(Right(_)) => complete(StatusCodes.OK)
            case Success(Left(domain.database.error.Unknown(ex))) =>
              complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
            case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          }
        }
      }
    }
  }


  private def update(implicit executionContext: ExecutionContext): Route =
    path("update" / LongNumber) { id =>
      post {
        entity(as[EntryDTO]) { dto =>
          onComplete(EntryDatabaseAccess.update(Id(id), toModel(dto).copy(id = Some(Id(id))))) {
            case Success(Right(_)) => complete(StatusCodes.OK)
            case Success(Left(domain.database.error.Unknown(ex))) =>
              complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
            case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          }
        }
      }
    }

  object ModelToDTO {
    private def availabilityToString: Availability => String = {
      case On => "On"
      case Off => "Off"
      case Maybe => "Maybe"
      case Planning => "Planning"
    }

    private def stringToAvailability: String => Availability = {
      case "On" => On
      case "Off" => Off
      case "Planning" => Planning
      case _ => Maybe
    }

    def toDTO(model : Entry) : EntryDTO = EntryDTO(
      id = model.id.map(_.value),
      userId = model.userId.value,
      date = model.date,
      availability = availabilityToString(model.availability)
    )
    def toModel(dto: EntryDTO) : Entry = Entry(
      id = dto.id.map(Id),
      userId = Id(dto.userId),
      date = dto.date,
      availability = stringToAvailability(dto.availability)
    )
  }
}
