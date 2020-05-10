package controllers

import javax.inject.{Inject, Singleton}
import models.client.{Client, ClientRepository}
import models.user.{User, UserRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class UserController @Inject()(userRepository: UserRepository,
                                  clientRepository: ClientRepository,
                                  messagesControllerComponents: MessagesControllerComponents)(implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(messagesControllerComponents) {

  var clients: Seq[Client] = Seq[Client]()

  val userForm: Form[CreateUserForm] = Form {
    mapping(
      "userName" -> nonEmptyText,
      "password" -> nonEmptyText,
      "email" -> nonEmptyText,
      "client" -> longNumber
    )(CreateUserForm.apply)(CreateUserForm.unapply)
  }

  val updateUserForm: Form[UpdateUserForm] = Form {
    mapping(
      "id" -> longNumber,
      "userName" -> nonEmptyText,
      "password" -> nonEmptyText,
      "email" -> nonEmptyText,
      "client" -> longNumber
    )(UpdateUserForm.apply)(UpdateUserForm.unapply)
  }

  def addUser(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val fClients = clientRepository.list()

    for {
      clients <- fClients
    } yield Ok(views.html.useradd(userForm, clients))
  }

  def updateUser(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    retrieveForeignProperties()

    userRepository.getById(id).map(user => {
      val filledUpdateForm = updateUserForm.fill(UpdateUserForm(user.id, user.userName, user.password, user.email, user.client))
      Ok(views.html.userupdate(filledUpdateForm, clients))
    })

  }

  def deleteUser(id: Long): Action[AnyContent] = Action {
    userRepository.delete(id)
    Redirect("/display-users")
  }

  def displayUser(id: Long): Action[AnyContent] = Action.async { implicit request =>
    userRepository.getByIdOption(id).map {
      case Some(p) => Ok("User " + p.userName)
      case None => Redirect(routes.HomeController.index())
    }
  }

  def displayUsers(): Action[AnyContent] = Action.async { implicit request =>
    userRepository.list().map(users => Ok(views.html.usersdisplay(users)))
  }

  def saveAddedUser(): Action[AnyContent] = Action.async { implicit request =>
    retrieveForeignProperties()

    userForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.useradd(errorForm, clients))
        )
      },
      user => {
        userRepository.create(user.userName, user.password, user.email, user.client).map { _ =>
          routes.UserController.addUser()
          Redirect("/display-users")
        }
      }
    )
  }

  def saveUpdatedUser(): Action[AnyContent] = Action.async { implicit request =>
    retrieveForeignProperties()

    updateUserForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.userupdate(errorForm, clients))
        )
      },
      user => {
        userRepository.update(user.id, User(user.id, user.userName, user.password, user.email, user.client)).map { _ =>
          routes.UserController.updateUser(user.id)
          Redirect("/display-users")
        }
      }
    )
  }

  def retrieveForeignProperties(): Unit = {
    clientRepository.list().onComplete {
      case Success(c) => clients = c
      case Failure(_) => print("Failure on retrieving clients")
    }
  }
}

case class CreateUserForm(userName: String, password: String, email: String, client: Long)
case class UpdateUserForm(id: Long, userName: String, password: String, email: String, client: Long)