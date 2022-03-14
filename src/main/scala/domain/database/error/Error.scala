package domain.database.error

sealed trait Error
case object NotFound extends Error
case class Unknown(ex: Throwable) extends Error
