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
  override def routes(implicit ec: ExecutionContext): Route =
    pathPrefix("entry") {
      concat(
        allEntries,
        allEntriesByUser,
        byId,
        byUserId,
        add,
        update,
        between
      )
    }

  case class EntryDTO(id: Option[Long], userId: Long, date: LocalDate, availability: String)

  import ModelToDTO._

  private def allEntries(implicit ec: ExecutionContext): Route =
    path("all") {
      get {
        onComplete(EntryDatabaseAccess.all()) {
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Right(entries)) =>
            complete(HttpEntity(ContentTypes.`application/json`, entries.map(toDTO).asJson.noSpaces))
        }
      }
    }

  private def allEntriesByUser(implicit ec: ExecutionContext): Route =
    path("all" / "byUser") {
      get {
        onComplete(EntryDatabaseAccess.all()) {
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Right(entries)) =>
            complete(HttpEntity(ContentTypes.`application/json`, entries.map(toDTO).groupBy(_.userId).asJson.noSpaces))
        }
      }
    }

  private def byId(implicit ec: ExecutionContext): Route =
    path(LongNumber) {id: Long =>
      get {
        onComplete(EntryDatabaseAccess.byId(Id(id))) {
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Left(domain.database.error.NotFound)) => complete(NotFound, s"No entry with id $id")
          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Right(entry)) =>
            complete(HttpEntity(ContentTypes.`application/json`, toDTO(entry).asJson.noSpaces))
        }
      }
    }

  private def byUserId(implicit ec: ExecutionContext): Route =
    path("user" / LongNumber) {id: Long =>
      get {
        onComplete(EntryDatabaseAccess.byUserId(Id(id))) {
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Left(domain.database.error.NotFound)) => complete(NotFound, s"No user with id $id")
          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Right(entries)) =>
            complete(HttpEntity(ContentTypes.`application/json`, entries.map(toDTO).asJson.noSpaces))
        }
      }
    }

  private def add(implicit ec: ExecutionContext): Route = {
    path("add") {
      post {
        entity(as[EntryDTO]) { dto =>
          onComplete(EntryDatabaseAccess.add(toModel(dto))) {
            case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
            case Success(Left(domain.database.error.Unknown(ex))) =>
              complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
            case Success(Right(_)) => complete(StatusCodes.OK)
          }
        }
      }
    }
  }


  private def update(implicit ec: ExecutionContext): Route =
    path("update" / LongNumber) { id =>
      post {
        entity(as[EntryDTO]) { dto =>
          onComplete(EntryDatabaseAccess.update(Id(id), toModel(dto).copy(id = Some(Id(id))))) {
            case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
            case Success(Left(domain.database.error.Unknown(ex))) =>
              complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
            case Success(Right(_)) => complete(StatusCodes.OK)
          }
        }
      }
    }

  private def between(implicit ec: ExecutionContext): Route =
    path("between") {
      get {
        parameters("before", "after") { (before, after) =>
          onComplete(EntryDatabaseAccess.between(LocalDate.parse(before).minusDays(1), LocalDate.parse(after).plusDays(1))) {
            case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
            case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
            case Success(Right(entries)) =>
              complete(HttpEntity(ContentTypes.`application/json`, entries.map(toDTO).groupBy(_.userId).asJson.noSpaces))
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
