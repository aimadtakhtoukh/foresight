package implementation.database.operations

import domain.database.error.{DatabaseError, Unknown}
import domain.models.{Id, WithId}
import implementation.database.utils.WithRepId
import slick.jdbc.MySQLProfile.api._
import zio.{IO, ZIO}

trait UpdateOne[Value <: WithId, Tables <: Table[Value] with WithRepId] {

  def updateOne(id: Id, value: Value)(implicit db: Database, tableQuery: TableQuery[Tables]): IO[DatabaseError, Int] =
    for {
      updateQuery <- ZIO.succeed(for(value <- tableQuery if value.id === id.value) yield value)
      result <- ZIO.fromFuture(_ => db.run(updateQuery.update(value))).mapError(ex => Unknown(ex))
    } yield result
}
