package implementation.web.api

import akka.actor.typed.ActorSystem
import akka.http.interop.ZIOSupport
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import domain.models.{Id, SecurityUser}
import domain.web.api.route.RouteProvider
import implementation.database.SecurityDatabaseAccess
import implementation.oauth.{DiscordOAuth, DiscordUser}
import implementation.web.api.json.CirceSupport._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

object SecurityApi extends RouteProvider with ZIOSupport {
  override def routes(implicit actorSystem: ActorSystem[Nothing], ec : ExecutionContext): Route =
    pathPrefix("security") {
      concat(
        conf()
      )
    }

  private def conf()(implicit actorSystem: ActorSystem[Nothing], ec : ExecutionContext): Route =
    path("conf") {
      get {
        headerValueByName("Authorization") { code =>
          complete(
            (for {
              discordUser <- DiscordOAuth.getDiscordUserFromToken(code.split(" ")(1))
              _ <- SecurityDatabaseAccess.add(toSecurityUser(discordUser))
            } yield ())
              .mapBoth(
                _ => HttpResponse(OK),
                _ => HttpResponse(OK)
              )
          )
//          onComplete(DiscordOAuth.getDiscordUserFromToken(code.split(" ")(1))) {
//            case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//            case Success(Left(e)) => complete(InternalServerError, s"An error occured: ${e.getMessage}")
//            case Success(Right(discordUser)) =>
//              onComplete(
//                SecurityDatabaseAccess.add(toSecurityUser(discordUser))
//                  .map(_ => discordUser.asJson.noSpaces)
//              )(_ => complete(discordUser))
//          }
        }
      }
    }

  private def toSecurityUser(discordUser: DiscordUser): SecurityUser = {
    SecurityUser(id = None, userId = Id(1L), securityId = discordUser.id, `type` = "DISCORD")
  }
}
