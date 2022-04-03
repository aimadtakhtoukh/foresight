package implementation.database.operations

import domain.database.error.{DatabaseError, Unknown}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery
import zio.{IO, ZIO}

trait AddOne[Value, Tables <: Table[Value]] {
  def addOne(value: Value)
            (implicit db : Database,
             tableQuery : TableQuery[Tables]
            ): IO[DatabaseError, Int] =
    ZIO
      .fromFuture(_ => db.run(tableQuery += value))
      .mapError(Unknown)

}
