package implementation.database.operations

import domain.database.error.{Error, Unknown}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

trait GetAll[Value, Tables <: Table[Value]] {
  def getAll()
            (implicit db : Database,
             tableQuery : TableQuery[Tables],
             executionContext: ExecutionContext
            ): Future[Either[Error, Seq[Value]]] =
    db.run(tableQuery.result)
      .map(Right(_))
      .recover {
        case ex: Throwable => Left(Unknown(ex))
      }
}