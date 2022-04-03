package domain.service.error

sealed trait DomainError
case class Unexpected(t: Throwable) extends DomainError
