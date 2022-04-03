package implementation.service
import domain.database.error.{NotFound, Unknown}
import domain.models
import domain.models.{Id, SecurityUser}
import domain.service.error.{DomainError, Unexpected}
import implementation.database.{SecurityDatabaseAccess, UserDatabaseAccess}
import zio.IO

object CreateUser extends domain.service.CreateUser {
  override def createUser(userId : Id, user: models.User, securityUser: SecurityUser): IO[DomainError, Unit] =
    (for {
      _ <- UserDatabaseAccess.add(user)
      _ <- SecurityDatabaseAccess.add(securityUser.copy(userId = userId))
    } yield ())
    .mapError {
      case Unknown(ex) => Unexpected(ex)
      case NotFound => Unexpected(new IllegalStateException("wut?"))
    }
}
