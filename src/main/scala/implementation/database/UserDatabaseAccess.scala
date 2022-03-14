package implementation.database

import domain.database.error.{Error, NotFound, Unknown}
import domain.models.{Id, User}
import implementation.database.operations.{AddOne, GetAll, GetById}
import implementation.database.tables.Users
import implementation.database.utils.ConnectionConfig
import slick.jdbc.JdbcBackend
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}

object UserDatabaseAccess extends domain.database.UserDatabaseAccess
  with GetById[User, Users]
  with GetAll[User, Users]
  with AddOne[User, Users] {

  implicit val db: JdbcBackend.Database = ConnectionConfig.database
  implicit val users: TableQuery[Users] = TableQuery[Users]

  override def all()(implicit executionContext: ExecutionContext): Future[Either[Error, Seq[User]]] = getAll()

  override def byId(id: Id)(implicit executionContext: ExecutionContext): Future[Either[Error, User]] = getById(id)

  override def byName(name: String)(implicit executionContext: ExecutionContext): Future[Either[Error, User]] =
    db.run(users.filter(_.name === name).result.headOption)
      .map {
        case Some(user : User) => Right(user)
        case None => Left(NotFound)
      }
      .recover { case ex: Throwable => Left(Unknown(ex))}

  override def add(user: User)(implicit executionContext: ExecutionContext): Future[Either[Error, Int]] = addOne(user)

  override def update(id: Id, user: User)(implicit executionContext: ExecutionContext): Future[Either[Error, Int]] = {
    val updateQuery: Query[Users, User, Seq] = for(user <- users if user.id === id.value) yield user
    db.run(updateQuery.update(user))
      .map(Right(_))
      .recover { case ex: Throwable => Left(Unknown(ex))}
  }
}
