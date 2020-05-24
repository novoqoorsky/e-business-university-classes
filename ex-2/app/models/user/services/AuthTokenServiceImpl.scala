package models.user.services

import java.time.ZonedDateTime
import java.util.UUID

import javax.inject.Inject
import models.user.AuthToken
import models.user.daos.AuthTokenDAO

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class AuthTokenServiceImpl @Inject()(authTokenDAO: AuthTokenDAO)(implicit ex: ExecutionContext) extends AuthTokenService {

  def create(userID: UUID, expiry: FiniteDuration = 12 hours): Future[AuthToken] = {
    val token = AuthToken(UUID.randomUUID(), userID, ZonedDateTime.now().plusSeconds(expiry.toSeconds))
    authTokenDAO.save(token).map(_ => token)
  }

  def validate(id: UUID): Future[Option[AuthToken]] = authTokenDAO.find(id)

  def clean: Future[Seq[AuthToken]] = authTokenDAO.findExpired().flatMap { tokens =>
    Future.sequence(tokens.map { token =>
      authTokenDAO.remove(token.id).map(_ => token)
    })
  }
}
