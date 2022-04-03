package implementation.database.operations

import domain.database.error.{DatabaseError, Unknown}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery
import zio.{IO, ZIO}

trait GetAll[Value, Tables <: Table[Value]] {
  def getAll()
            (implicit db : Database,
             tableQuery : TableQuery[Tables]
            ): IO[DatabaseError, Seq[Value]] =
    ZIO
      .fromFuture(_ => db.run(tableQuery.result))
      .mapError(Unknown)
}