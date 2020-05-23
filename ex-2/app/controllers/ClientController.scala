package controllers

import javax.inject.{Inject, Singleton}
import models.address.{Address, AddressRepository}
import models.cart.{Cart, CartRepository}
import models.client.{Client, ClientRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ClientController @Inject()(clientRepository: ClientRepository,
                                 addressRepository: AddressRepository,
                                 cartRepository: CartRepository,
                                 messagesControllerComponents: MessagesControllerComponents)(implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(messagesControllerComponents) {

  val DISPLAY_CLIENTS_URL = "/display-clients"

  var addresses: Seq[Address] = Seq[Address]()
  var carts: Seq[Cart] = Seq[Cart]()

  val clientForm: Form[CreateClientForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "address" -> longNumber,
      "cart" -> longNumber,
    )(CreateClientForm.apply)(CreateClientForm.unapply)
  }

  val updateClientForm: Form[UpdateClientForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "address" -> longNumber,
      "cart" -> longNumber,
    )(UpdateClientForm.apply)(UpdateClientForm.unapply)
  }

  def addClient(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val fAddresses = addressRepository.list()
    val fCarts = cartRepository.list()

    for {
      addresses <- fAddresses
      carts <- fCarts
    } yield Ok(views.html.clientadd(clientForm, addresses, carts))
  }

  def updateClient(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    retrieveForeignProperties()

    clientRepository.getById(id).map(client => {
      val filledUpdateForm = updateClientForm.fill(UpdateClientForm(client.id, client.name, client.lastName, client.address, client.cart))
      Ok(views.html.clientupdate(filledUpdateForm, addresses, carts))
    })

  }

  def deleteClient(id: Long): Action[AnyContent] = Action {
    clientRepository.delete(id)
    Redirect(DISPLAY_CLIENTS_URL)
  }

  def displayClient(id: Long): Action[AnyContent] = Action.async { implicit request =>
    clientRepository.getByIdOption(id).map {
      case Some(p) => Ok("Client " + p.name)
      case None => Redirect(routes.HomeController.index())
    }
  }

  def displayClients(): Action[AnyContent] = Action.async { implicit request =>
    clientRepository.list().map(clients => Ok(views.html.clientsdisplay(clients)))
  }

  def saveAddedClient(): Action[AnyContent] = Action.async { implicit request =>
    retrieveForeignProperties()

    clientForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.clientadd(errorForm, addresses, carts))
        )
      },
      client => {
        clientRepository.create(client.name, client.lastName, client.address, client.cart).map { _ =>
          routes.ClientController.addClient()
          Redirect(DISPLAY_CLIENTS_URL)
        }
      }
    )
  }

  def saveUpdatedClient(): Action[AnyContent] = Action.async { implicit request =>
    retrieveForeignProperties()

    updateClientForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.clientupdate(errorForm, addresses, carts))
        )
      },
      client => {
        clientRepository.update(client.id, Client(client.id, client.name, client.lastName, client.address, client.cart)).map { _ =>
          routes.ClientController.updateClient(client.id)
          Redirect(DISPLAY_CLIENTS_URL)
        }
      }
    )
  }

  def retrieveForeignProperties(): Unit = {
    addressRepository.list().onComplete {
      case Success(a) => addresses = a
      case Failure(_) => print("Failure on retrieving addresses")
    }
    cartRepository.list().onComplete{
      case Success(c) => carts = c
      case Failure(_) => print("Failure on retrieving carts")
    }
  }

  // REACT

  def clients(): Action[AnyContent] = Action.async {
    clientRepository.list().map(clients => Ok(Json.toJson(clients)))
  }

  def postClient(): Action[AnyContent] = Action { implicit request =>
    val c = request.body.asJson.get.asInstanceOf[JsObject].value
    clientRepository.create(
      c("name").asInstanceOf[JsString].value,
      c("lastName").asInstanceOf[JsString].value,
      c("address").asInstanceOf[JsString].value.toLong,
      c("cart").asInstanceOf[JsString].value.toLong
    )
    Created("Client created")
  }

  def putClient(): Action[AnyContent] = Action { implicit request =>
    val c = request.body.asJson.get.asInstanceOf[JsObject].value
    val id = c("id").asInstanceOf[JsString].value.toLong
    val client = Client(
      id,
      c("name").asInstanceOf[JsString].value,
      c("lastName").asInstanceOf[JsString].value,
      c("address").asInstanceOf[JsString].value.toLong,
      c("cart").asInstanceOf[JsString].value.toLong
    )
    clientRepository.update(id, client)
    Created(Json.toJson(client))
  }

  def deleteClientExternal(id: Long): Action[AnyContent] = Action { implicit request =>
    deleteClient(id)
    Ok("Cart deleted")
  }
}

case class CreateClientForm(name: String, lastName: String, address: Long, cart: Long)
case class UpdateClientForm(id: Long, name: String, lastName: String, address: Long, cart: Long)