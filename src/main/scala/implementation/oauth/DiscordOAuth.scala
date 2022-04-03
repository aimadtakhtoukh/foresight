package implementation.oauth

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.unmarshalling.Unmarshal
import io.circe.generic.auto._
import io.circe.parser.decode
import pureconfig.ConfigReader.Result
import pureconfig._
import pureconfig.generic.auto._
import zio.{IO, ZIO}

case class OAuthApp(id : String, clientId : String, clientSecret : String, authorizeUrl : String, tokenUrl : String, userInfoUrl : String, redirectUrl : Option[String])
case class DiscordUser(id : String, username : String, discriminator : String, avatar: String, bot : Option[Boolean], mfa_enabled : Boolean, locale : String, verified : Option[String])

object DiscordOAuth {
  def getDiscordUserFromToken(token : String)(implicit actorSystem: ActorSystem[Nothing]) : IO[Throwable, DiscordUser] =
    (for {
      discordConf <- ZIO.fromEither(getDiscordConf)
      discordResponse <- ZIO.fromFuture(_ => Http().singleRequest(HttpRequest(uri = discordConf.userInfoUrl, headers = Seq(Authorization(OAuth2BearerToken(token))))))
      entity <- ZIO.effect(discordResponse.entity)
      entityAsString <- ZIO.fromFuture(_ => Unmarshal(entity).to[String])
      json <- ZIO.fromEither(decode[DiscordUser](entityAsString))
    } yield json).mapError(error => new IllegalStateException(error.toString))
//    Future { getDiscordConf }
//      .collect { case Right(conf) => Http().singleRequest(HttpRequest(uri = conf.userInfoUrl, headers = Seq(Authorization(OAuth2BearerToken(token)))))}
//      .flatten
//      .map(_.entity)
//      .flatMap(Unmarshal(_).to[String])
//      .map(decode[DiscordUser](_))

  def getDiscordConf: Result[OAuthApp] =
    ConfigSource.resources("conf/default/discord.conf").load[OAuthApp]

}
