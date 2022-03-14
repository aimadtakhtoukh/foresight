package implementation.database.operations

import domain.database.error.{Error, Unknown}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

trait AddOne[Value, Tables <: Table[Value]] {
  def addOne(value: Value)
            (implicit db : Database,
             tableQuery : TableQuery[Tables],
             executionContext: ExecutionContext
            ): Future[Either[Error, Int]] =
    db.run(tableQuery += value)
      .map(Right(_))
      .recover {
        case ex : Throwable => Left(Unknown(ex))
      }
}
