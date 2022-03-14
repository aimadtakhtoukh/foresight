package domain.database

import domain.models.{Id, User}
import domain.database.error.Error

import scala.concurrent.{ExecutionContext, Future}

trait UserDatabaseAccess {
  def all()(implicit executionContext: ExecutionContext): Future[Either[Error, Seq[User]]]
  def byId(id: Id)(implicit executionContext: ExecutionContext): Future[Either[Error, User]]
  def byName(name: String)(implicit executionContext: ExecutionContext): Future[Either[Error, User]]
  def add(user: User)(implicit executionContext: ExecutionContext) : Future[Either[Error, Int]]
  def update(id: Id, user: User)(implicit executionContext: ExecutionContext): Future[Either[Error, Int]]
}
