package implementation.database.tables

import domain.models.{Id, SecurityUser}
import implementation.database.UserDatabaseAccess
import implementation.database.utils.WithRepId
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

class SecurityUsers(tag: Tag) extends Table[SecurityUser](tag, "security_user") with WithRepId {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def securityId = column[String]("security_id")
  def origin = column[String]("type")

  def userId = column[Long]("user_id")
  def userFk =
    foreignKey("USER_FK", userId, UserDatabaseAccess.users)(_.id, ForeignKeyAction.Restrict, ForeignKeyAction.Cascade)

  override def * : ProvenShape[SecurityUser] =
    (id.?, userId, securityId, origin) <> (toSecurityUser.tupled, fromSecurityUser)

  private def toSecurityUser: (Option[Long], Long, String, String) => SecurityUser =
    (idOpt, userId, securityId, origin) => SecurityUser(idOpt.map(Id), Id(userId), securityId, origin)

  private def fromSecurityUser(su: SecurityUser): Option[(Option[Long], Long, String, String)] =
    Some((su.id.map(_.value), su.userId.value, su.securityId, su.`type`))
}
