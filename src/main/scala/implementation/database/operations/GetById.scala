package implementation.database.operations

import domain.database.error.{Error, NotFound, Unknown}
import domain.models.{Id, WithId}
import implementation.database.utils.WithRepId
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

trait GetById[Value <: WithId, Tables <: Table[Value] with WithRepId] {
  def getById(id: Id)
             (implicit db : Database,
              tableQuery : TableQuery[Tables],
              executionContext: ExecutionContext
             ): Future[Either[Error, Value]] =
    db.run(tableQuery.filter(_.id === id.value).result.headOption)
      .map {
        case Some(value : Value) => Right(value)
        case None => Left(NotFound)
      }.recover {
        case ex : Throwable => Left(Unknown(ex))
      }
}
