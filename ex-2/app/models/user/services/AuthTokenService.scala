package models.user.services

import java.util.UUID

import models.user.AuthToken

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

trait AuthTokenService {

  def create(userID: UUID, expiry: FiniteDuration = 5 minutes): Future[AuthToken]
  def validate(id: UUID): Future[Option[AuthToken]]
  def clean: Future[Seq[AuthToken]]
}
