package controllers

import java.util.UUID

import com.mohiva.play.silhouette.api._
import javax.inject.Inject
import models.user.services._
import play.api.i18n.I18nSupport
import play.api.mvc._
import utils.silhouette.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

class SignUpController @Inject()(components: ControllerComponents,
                                 silhouette: Silhouette[DefaultEnv],
                                 signUpService: SignUpService,
                                )(implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  def submit: Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignUpForm.form.bindFromRequest.fold(
      _ => Future.successful(BadRequest),
      data => {
        val activationUrlProvider: UUID => String = authTokenId => authTokenId.toString
        println(data)

        signUpService.signUpByCredentials(data, request.remoteAddress, activationUrlProvider).map {
          case UserCreated(user) =>
            silhouette.env.eventBus.publish(SignUpEvent(user, request))
            Ok
          case UserAlreadyExists =>
            Conflict
          case InvalidRecaptchaCode =>
            BadRequest("Captcha code is not correct")
        }
      }
    )
  }
}

import play.api.data.Form
import play.api.data.Forms._

object SignUpForm {

  val form = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> email,
      "password" -> nonEmptyText,
      "captchaResponse" -> nonEmptyText
    )(CredentialsSingUpData.apply)(CredentialsSingUpData.unapply)
  )
}
