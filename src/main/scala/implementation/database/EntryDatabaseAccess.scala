package implementation.database

import domain.database.error.{Error, Unknown}
import domain.models._
import implementation.database.operations.{AddOne, GetAll, GetById}
import implementation.database.tables.Entries
import implementation.database.utils.ConnectionConfig
import slick.jdbc.JdbcBackend
import slick.jdbc.MySQLProfile.api._

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

object EntryDatabaseAccess extends domain.database.EntryDatabaseAccess
  with GetById[Entry, Entries]
  with GetAll[Entry, Entries]
  with AddOne[Entry, Entries] {

  implicit val db: JdbcBackend.Database = ConnectionConfig.database
  implicit val entries: TableQuery[Entries] = TableQuery[Entries]

  override def all()(implicit ec: ExecutionContext): Future[Either[Error, Seq[Entry]]] = getAll()

  override def byId(id: Id)(implicit ec: ExecutionContext): Future[Either[Error, Entry]] = getById(id)

  override def byUserId(userId: Id)(implicit ec: ExecutionContext): Future[Either[Error, Seq[Entry]]] = {
    db.run(entries.filter(_.userId === userId.value).result)
      .map(Right(_))
      .recover { case ex: Throwable => Left(Unknown(ex))}
  }

  override def add(entry: Entry)(implicit ec: ExecutionContext): Future[Either[Error, Int]] = addOne(entry)

  override def update(id: Id, entry: Entry)(implicit ec: ExecutionContext): Future[Either[Error, Int]] = {
    val updateQuery: Query[Entries, Entry, Seq] = for(entry <- entries if entry.id === id.value) yield entry
    db.run(updateQuery.update(entry))
      .map(Right(_))
      .recover { case ex: Throwable => Left(Unknown(ex))}
  }

  override def between(before: LocalDate, after: LocalDate)(implicit ec: ExecutionContext): Future[Either[Error, Seq[Entry]]] = {
    db.run(entries.filter(_.date > before).result)
      .map(Right(_))
      .recover { case ex: Throwable => Left(Unknown(ex))}
  }
}
