package domain.database

import domain.database.error.DatabaseError
import domain.models.{Id, User}
import zio.IO

trait UserDatabaseAccess {
  def all(): IO[DatabaseError, Seq[User]]
  def byId(id: Id): IO[DatabaseError, User]
  def byName(name: String): IO[DatabaseError, User]
  def add(user: User) : IO[DatabaseError, Int]
  def update(id: Id, user: User): IO[DatabaseError, Int]
}
