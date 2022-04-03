package domain.database.error

sealed trait DatabaseError
case object NotFound extends DatabaseError
case class Unknown(ex: Throwable) extends DatabaseError
