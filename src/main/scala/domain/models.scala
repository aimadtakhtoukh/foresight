package domain

import java.time.LocalDate

object models {
  trait WithId {def id : Option[Id]}

  case class Id(value: Long)

  case class User(id: Option[Id], name: String) extends WithId

  case class SecurityUser(id : Option[Id] = None, userId : Id, securityId : String, `type` : String) extends WithId

  sealed trait Availability
  case object On extends Availability
  case object Off extends Availability
  case object Maybe extends Availability
  case object Planning extends Availability

  case class Entry(id: Option[Id], userId: Id, date: LocalDate, availability : Availability) extends WithId
}
