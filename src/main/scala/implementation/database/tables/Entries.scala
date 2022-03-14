package implementation.database.tables

import domain.models.{Availability, Entry, Id, Maybe, Off, On, Planning, User}
import implementation.database.UserDatabaseAccess
import implementation.database.utils.WithRepId
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.lifted.{ForeignKeyQuery, ProvenShape, Tag}

import java.time.LocalDate
import slick.jdbc.MySQLProfile.api._

class Entries(tag: Tag) extends Table[Entry](tag, "entry") with WithRepId {
  implicit val availabilityMapper: JdbcType[Availability] with BaseTypedType[Availability] =
    MappedColumnType.base[Availability, String](
      {
        case On => "On"
        case Off => "Off"
        case Maybe => "Maybe"
        case Planning => "Planning"
      },
      {
        case "On" => On
        case "Off" => Off
        case "Maybe" => Maybe
        case "Planning" => Planning
        case _ => Maybe
      }
    )

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def date = column[LocalDate]("date")
  def dispo = column[Availability]("dispo")

  def userId = column[Long]("user_id")
  def userFk: ForeignKeyQuery[Users, User] =
    foreignKey("USER_FK", userId, UserDatabaseAccess.users)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

  override def * : ProvenShape[Entry] =
    (id.?, userId, date, dispo) <> (toEntry.tupled, fromEntry)

  private def toEntry: (Option[Long], Long, LocalDate, Availability) => Entry =
    (idOpt, userId, date, dispo) => Entry(idOpt.map(Id), Id(userId), date, dispo)

  private def fromEntry(entry: Entry) : Option[(Option[Long], Long, LocalDate, Availability)] =
    Some((entry.id.map(_.value), entry.userId.value, entry.date, entry.availability))
}
