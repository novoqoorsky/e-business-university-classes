package controllers

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import models.user.services._
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Request}
import utils.silhouette.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

class SignInController @Inject()(authenticateService: AuthenticateService,
                                 silhouette: Silhouette[DefaultEnv],
                                 configuration: Configuration,
                                 clock: Clock)(implicit ex: ExecutionContext)
  extends AbstractAuthController(silhouette, configuration, clock) with I18nSupport with Logger {

  def submit: Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignInForm.form.bindFromRequest.fold(
      _ => Future.successful(BadRequest),
      data => {
        authenticateService.credentials(data.email, data.password).flatMap {
          case Success(user) =>
            val loginInfo = LoginInfo(CredentialsProvider.ID, user.email.get)
            authenticateUser(user, loginInfo, data.rememberMe)
          case InvalidPassword(attemptsAllowed) =>
            Future.successful(Forbidden(Json.obj("errorCode" -> "InvalidPassword", "attemptsAllowed" -> attemptsAllowed)))
          case NonActivatedUserEmail =>
            Future.successful(Forbidden(Json.obj("errorCode" -> "NonActivatedUserEmail")))
          case UserNotFound =>
            Future.successful(Forbidden(Json.obj("errorCode" -> "UserNotFound")))
          case ToManyAuthenticateRequests(nextAllowedAttemptTime) =>
            Future.successful(TooManyRequests(Json.obj("errorCode" -> "TooManyRequests", "nextAllowedAttemptTime" -> nextAllowedAttemptTime)))
        }
          .recover {
            case e =>
              logger.error(s"Sign in error email = ${data.email}", e)
              InternalServerError(Json.obj("errorCode" -> "SystemError"))
          }
      }
    )
  }
}

object SignInForm {

  val form = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText,
      "rememberMe" -> boolean
    )(Data.apply)(Data.unapply)
  )

  case class Data(email: String, password: String, rememberMe: Boolean)
}
