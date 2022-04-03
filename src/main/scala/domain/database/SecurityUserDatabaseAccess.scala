package domain.database

import domain.database.error.DatabaseError
import domain.models
import zio.IO

trait SecurityUserDatabaseAccess {

  def byId(id: models.Id): IO[DatabaseError, models.SecurityUser]

  def add(su: models.SecurityUser): IO[DatabaseError, Int]
}
