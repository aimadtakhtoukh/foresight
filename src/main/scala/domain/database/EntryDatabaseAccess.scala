package domain.database

import domain.models.{Entry, Id}

import scala.concurrent.{ExecutionContext, Future}
import error.Error

import java.time.LocalDate

trait EntryDatabaseAccess {
  def all()(implicit ec: ExecutionContext): Future[Either[Error, Seq[Entry]]]
  def byId(id: Id)(implicit ec: ExecutionContext): Future[Either[Error, Entry]]
  def byUserId(userId: Id)(implicit ec: ExecutionContext): Future[Either[Error, Seq[Entry]]]
  def add(entry: Entry)(implicit ec: ExecutionContext): Future[Either[Error, Int]]
  def update(id: Id, entry: Entry)(implicit ec: ExecutionContext): Future[Either[Error, Int]]
  def between(before: LocalDate, after: LocalDate)(implicit ec: ExecutionContext): Future[Either[Error, Seq[Entry]]]
}
