package domain.database

import domain.models.{Id, User}
import domain.database.error.Error

import scala.concurrent.{ExecutionContext, Future}

trait UserDatabaseAccess {
  def all()(implicit ec: ExecutionContext): Future[Either[Error, Seq[User]]]
  def byId(id: Id)(implicit ec: ExecutionContext): Future[Either[Error, User]]
  def byName(name: String)(implicit ec: ExecutionContext): Future[Either[Error, User]]
  def add(user: User)(implicit ec: ExecutionContext) : Future[Either[Error, Int]]
  def update(id: Id, user: User)(implicit ec: ExecutionContext): Future[Either[Error, Int]]
}
