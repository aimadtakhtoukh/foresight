package implementation.database

import domain.database.error.{DatabaseError, Unknown}
import domain.models._
import implementation.database.operations.{AddOne, GetAll, GetById, UpdateOne}
import implementation.database.tables.Entries
import implementation.database.utils.ConnectionConfig
import slick.jdbc.JdbcBackend
import slick.jdbc.MySQLProfile.api._
import zio.{IO, ZIO}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

object EntryDatabaseAccess extends domain.database.EntryDatabaseAccess
  with GetById[Entry, Entries]
  with GetAll[Entry, Entries]
  with AddOne[Entry, Entries]
  with UpdateOne[Entry, Entries] {

  implicit val db: JdbcBackend.Database = ConnectionConfig.database
  implicit val entries: TableQuery[Entries] = TableQuery[Entries]

  override def all(): IO[DatabaseError, Seq[Entry]] = getAll()

  override def byId(id: Id): IO[DatabaseError, Entry] = getById(id)

  override def byUserId(userId: Id): IO[DatabaseError, Seq[Entry]] =
    for {
      query <- ZIO.succeed(entries.filter(_.userId === userId.value).result)
      result <- ZIO.fromFuture(_ => db.run(query)).mapError(ex => Unknown(ex))
    } yield result

  override def add(entry: Entry): IO[DatabaseError, Int] = addOne(entry)

  override def update(id: Id, entry: Entry): IO[DatabaseError, Int] = updateOne(id, entry)
//    for {
//      updateQuery <- ZIO.succeed(for(entry <- entries if entry.id === id.value) yield entry)
//      result <- ZIO.fromFuture(_ => db.run(updateQuery.update(entry))).mapError(ex => Unknown(ex))
//    } yield result

  override def between(before: LocalDate, after: LocalDate): IO[DatabaseError, Seq[Entry]] =
    for {
      query <- ZIO.succeed(entries.filter(_.date > before).result)
      result <- ZIO.fromFuture(_ => db.run(query)).mapError(ex => Unknown(ex))
    } yield result
}
