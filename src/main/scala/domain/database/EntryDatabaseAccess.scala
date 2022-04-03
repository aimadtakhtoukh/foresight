package domain.database

import domain.database.error.DatabaseError
import domain.models.{Entry, Id}
import zio.IO

import java.time.LocalDate

trait EntryDatabaseAccess {
  def all(): IO[DatabaseError, Seq[Entry]]
  def byId(id: Id): IO[DatabaseError, Entry]
  def byUserId(userId: Id): IO[DatabaseError, Seq[Entry]]
  def add(entry: Entry): IO[DatabaseError, Int]
  def update(id: Id, entry: Entry): IO[DatabaseError, Int]
  def between(before: LocalDate, after: LocalDate): IO[DatabaseError, Seq[Entry]]
}
