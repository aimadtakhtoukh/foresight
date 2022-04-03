package implementation.web.api

import akka.actor.typed.ActorSystem
import akka.http.interop.ZIOSupport
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, OK}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MarshallingDirectives
import domain.database.error.{DatabaseError, NotFound, Unknown}
import domain.models._
import domain.web.api.route.RouteProvider
import implementation.database.EntryDatabaseAccess
import implementation.web.api.json.CirceSupport._
import io.circe.generic.auto._
import io.circe.syntax._

import java.time.LocalDate
import scala.concurrent.ExecutionContext

object EntryApi extends RouteProvider with ZIOSupport {
  override def routes(implicit actorSystem: ActorSystem[Nothing], ec: ExecutionContext): Route =
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
        complete(
          EntryDatabaseAccess.all()
            .mapBoth(
              handleDatabaseError,
              entries => HttpResponse(status = OK, entity = HttpEntity(ContentTypes.`application/json`, entries.map(toDTO).asJson.noSpaces))
            )
        )
        /*
        onComplete(EntryDatabaseAccess.all()) {
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          case Success(Right(entries)) =>
            complete(HttpEntity(ContentTypes.`application/json`, entries.map(toDTO).asJson.noSpaces))
        }*/
      }
    }

  private def allEntriesByUser(implicit ec: ExecutionContext): Route =
    path("all" / "byUser" / LongNumber) { userId =>
      get {
        complete(
          EntryDatabaseAccess.byUserId(Id(userId))
            .mapBoth(
              handleDatabaseError,
              entries => HttpResponse(status = OK, entity = HttpEntity(ContentTypes.`application/json`, entries.map(toDTO).groupBy(_.userId).asJson.noSpaces))
            )
        )
//        onComplete(EntryDatabaseAccess.all()) {
//          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//          case Success(Right(entries)) =>
//            complete(HttpEntity(ContentTypes.`application/json`, entries.map(toDTO).groupBy(_.userId).asJson.noSpaces))
//        }
      }
    }

  private def byId(implicit ec: ExecutionContext): Route =
    path(LongNumber) {id: Long =>
      get {
        complete(
          EntryDatabaseAccess.byId(Id(id))
            .mapBoth(
              handleDatabaseError,
              entry => HttpResponse(status = OK, entity = HttpEntity(ContentTypes.`application/json`, toDTO(entry).asJson.noSpaces))
            )
        )
//        onComplete(EntryDatabaseAccess.byId(Id(id))) {
//          case Failure(ex) => complete(InternalServerError, ErrorResponse(s"An error occurred: ${ex.getMessage}").asJson)
//          case Success(Left(domain.database.error.NotFound)) => complete(NotFound, s"No entry with id $id")
//          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//          case Success(Right(entry)) =>
//            complete(HttpEntity(ContentTypes.`application/json`, toDTO(entry).asJson.noSpaces))
//        }
      }
    }

  private def byUserId(implicit ec: ExecutionContext): Route =
    path("user" / LongNumber) {id: Long =>
      get {
        complete(
          EntryDatabaseAccess.byUserId(Id(id))
            .mapBoth(
              handleDatabaseError,
              entries => HttpResponse(status = OK, entity = HttpEntity(ContentTypes.`application/json`, entries.map(toDTO).asJson.noSpaces))
            )
        )
//        onComplete(EntryDatabaseAccess.byUserId(Id(id))) {
//          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//          case Success(Left(domain.database.error.NotFound)) => complete(NotFound, s"No user with id $id")
//          case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//          case Success(Right(entries)) =>
//            complete(HttpEntity(ContentTypes.`application/json`, entries.map(toDTO).asJson.noSpaces))
//        }
      }
    }

  private def add(implicit ec: ExecutionContext): Route = {
    path("add") {
      post {
        entity(MarshallingDirectives.as[EntryDTO]) { dto =>
          complete(
            EntryDatabaseAccess.add(toModel(dto))
              .mapBoth(
                handleDatabaseError,
                _ => HttpResponse(OK)
              )
          )
//          onComplete(EntryDatabaseAccess.add(toModel(dto))) {
//            case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//            case Success(Left(domain.database.error.Unknown(ex))) =>
//              complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//            case Success(Right(_)) => complete(StatusCodes.OK)
//          }
        }
      }
    }
  }


  private def update(implicit ec: ExecutionContext): Route =
    path("update" / LongNumber) { id =>
      post {
        entity(MarshallingDirectives.as[EntryDTO]) { dto =>
          complete(
            EntryDatabaseAccess.update(Id(id), toModel(dto).copy(id = Some(Id(id))))
              .mapBoth(
                handleDatabaseError,
                _ => HttpResponse(OK)
              )
          )
//          onComplete(EntryDatabaseAccess.update(Id(id), toModel(dto).copy(id = Some(Id(id))))) {
//            case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//            case Success(Left(domain.database.error.Unknown(ex))) =>
//              complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//            case Success(Right(_)) => complete(StatusCodes.OK)
//          }
        }
      }
    }

  private def between(implicit ec: ExecutionContext): Route =
    path("between") {
      get {
        parameters("before", "after") { (before, after) =>
          complete(
            EntryDatabaseAccess.between(LocalDate.parse(before).minusDays(1), LocalDate.parse(after).plusDays(1))
              .mapBoth(
                handleDatabaseError,
                entries => HttpResponse(status = OK, entity = HttpEntity(ContentTypes.`application/json`, entries.map(toDTO).groupBy(_.userId).asJson.noSpaces))
              )
          )
//          onComplete(EntryDatabaseAccess.between(LocalDate.parse(before).minusDays(1), LocalDate.parse(after).plusDays(1))) {
//            case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//            case Success(Left(domain.database.error.Unknown(ex))) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//            case Success(Right(entries)) =>
//              complete(HttpEntity(ContentTypes.`application/json`, entries.map(toDTO).groupBy(_.userId).asJson.noSpaces))
//          }
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

  private def handleDatabaseError: DatabaseError => HttpResponse = {
    case Unknown(ex) => HttpResponse(status = InternalServerError, entity = ex.getMessage)
    case NotFound => HttpResponse(status = InternalServerError, entity = "Not found")
  }
}
