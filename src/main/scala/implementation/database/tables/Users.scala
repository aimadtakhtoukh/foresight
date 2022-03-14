package implementation.database.tables

import domain.models.{Id, User}
import implementation.database.utils.WithRepId
import slick.lifted.ProvenShape
import slick.jdbc.MySQLProfile.api._

class Users(tag: Tag) extends Table[User](tag, "user") with WithRepId {
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name: Rep[String] = column[String]("name", O.Unique)

  override def * : ProvenShape[User] = (id.?, name) <> (toUser.tupled, fromUser)

  private def toUser: (Option[Long], String) => User = (idOpt, name) => User(idOpt.map(Id), name)
  private def fromUser(user: User): Option[(Option[Long], String)] = Some((user.id.map(_.value), user.name))
}