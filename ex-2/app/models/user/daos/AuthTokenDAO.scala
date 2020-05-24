package models.user.daos

import java.time.ZonedDateTime
import java.util.UUID

import javax.inject.Inject
import models.user.AuthToken
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future

class AuthTokenDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends DAOSlick {

  import profile.api._

  def find(id: UUID): Future[Option[AuthToken]] = {
    db.run(slickAuthTokens.filter(_.id === id).result.headOption)
  }

  def findExpired(): Future[Seq[AuthToken]] = {
    db.run(slickAuthTokens.filter(_.expiry < ZonedDateTime.now()).result)
  }

  def save(token: AuthToken): Future[Int] = {
    db.run(slickAuthTokens.insertOrUpdate(token))
  }

  def remove(id: UUID): Future[Int] = {
    db.run(slickAuthTokens.filter(_.id === id).delete)
  }
}
