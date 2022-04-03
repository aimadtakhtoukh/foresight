package implementation.database

import domain.database.error.{DatabaseError, NotFound, Unknown}
import domain.models.{Id, User}
import implementation.database.operations.{AddOne, GetAll, GetById, UpdateOne}
import implementation.database.tables.Users
import implementation.database.utils.ConnectionConfig
import slick.jdbc.JdbcBackend
import slick.jdbc.MySQLProfile.api._
import zio.{IO, ZIO}

import scala.concurrent.{ExecutionContext, Future}

object UserDatabaseAccess extends domain.database.UserDatabaseAccess
  with GetById[User, Users]
  with GetAll[User, Users]
  with AddOne[User, Users]
  with UpdateOne[User, Users] {

  implicit val db: JdbcBackend.Database = ConnectionConfig.database
  implicit val users: TableQuery[Users] = TableQuery[Users]

  override def all() : IO[DatabaseError, Seq[User]] = getAll()

  override def byId(id: Id) : IO[DatabaseError, User] = getById(id)

  override def byName(name: String) : IO[DatabaseError, User] =
    for {
      query <- ZIO.succeed(users.filter(_.name === name).result.headOption)
      resultOption <- ZIO.fromFuture(_ => db.run(query)).mapError(ex => Unknown(ex))
      result <- ZIO.fromOption(resultOption).orElseFail(NotFound)
    } yield result

  override def add(user: User) : IO[DatabaseError, Int] = addOne(user)

  override def update(id: Id, user: User) : IO[DatabaseError, Int] = updateOne(id, user)
//    for {
//      updateQuery <- ZIO.succeed(for(user <- users if user.id === id.value) yield user)
//      result <- ZIO.fromFuture(_ => db.run(updateQuery.update(user))).mapError(ex => Unknown(ex))
//    } yield result
}
