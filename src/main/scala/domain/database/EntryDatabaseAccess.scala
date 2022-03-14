package domain.database

import domain.models.{Entry, Id}

import scala.concurrent.{ExecutionContext, Future}
import error.Error

trait EntryDatabaseAccess {
  def all()(implicit executionContext: ExecutionContext): Future[Either[Error, Seq[Entry]]]
  def byId(id: Id)(implicit executionContext: ExecutionContext): Future[Either[Error, Entry]]
  def byUserId(userId: Id)(implicit executionContext: ExecutionContext): Future[Either[Error, Seq[Entry]]]
  def add(entry: Entry)(implicit executionContext: ExecutionContext): Future[Either[Error, Int]]
  def update(id: Id, entry: Entry)(implicit executionContext: ExecutionContext): Future[Either[Error, Int]]
}
