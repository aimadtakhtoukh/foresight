package implementation.database.operations

import domain.database.error.{DatabaseError, NotFound, Unknown}
import domain.models.{Id, WithId}
import implementation.database.utils.WithRepId
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery
import zio.{IO, ZIO}

import scala.concurrent.{ExecutionContext, Future}

trait GetById[Value <: WithId, Tables <: Table[Value] with WithRepId] {
  def getById(id: Id)
             (implicit db : Database,
              tableQuery : TableQuery[Tables],
             ): IO[DatabaseError, Value] =
    for {
      query <- ZIO.succeed(tableQuery.filter(_.id === id.value).result.headOption)
      resultOption <- ZIO.fromFuture(_ => db.run(query)).mapError(ex => Unknown(ex))
      result <- ZIO.fromOption(resultOption).orElseFail(NotFound)
    } yield result
}
