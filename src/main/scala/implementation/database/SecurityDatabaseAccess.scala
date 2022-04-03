package implementation.database

import domain.database.error.DatabaseError
import domain.models.{Id, SecurityUser}
import implementation.database.operations.{AddOne, GetById}
import implementation.database.tables.SecurityUsers
import implementation.database.utils.ConnectionConfig
import slick.jdbc.JdbcBackend
import slick.lifted.TableQuery
import zio.IO

import scala.concurrent.{ExecutionContext, Future}

object SecurityDatabaseAccess extends domain.database.SecurityUserDatabaseAccess
  with GetById[SecurityUser, SecurityUsers]
  with AddOne[SecurityUser, SecurityUsers] {

  implicit val db: JdbcBackend.Database = ConnectionConfig.database
  implicit val securityUsers: TableQuery[SecurityUsers] = TableQuery[SecurityUsers]

  override def byId(id: Id) : IO[DatabaseError, SecurityUser] = getById(id)

  override def add(su: SecurityUser) : IO[DatabaseError, Int] = addOne(su)

}
