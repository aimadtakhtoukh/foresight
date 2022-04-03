package domain.service

import domain.models
import domain.models.{Id, SecurityUser}
import domain.service.error.DomainError
import zio.IO

trait CreateUser {
  def createUser(userId : Id, user: models.User, securityUser: SecurityUser): IO[DomainError, Unit]
}
