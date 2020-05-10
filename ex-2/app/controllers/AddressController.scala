package controllers

import javax.inject.{Inject, Singleton}
import models.address.{Address, AddressRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressController @Inject()(addressRepository: AddressRepository,
                                  messagesControllerComponents: MessagesControllerComponents)(implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(messagesControllerComponents) {

  val addressForm: Form[CreateAddressForm] = Form {
    mapping(
      "city" -> nonEmptyText,
      "streetName" -> nonEmptyText,
      "houseNumber" -> number,
      "postalCode" -> nonEmptyText
    )(CreateAddressForm.apply)(CreateAddressForm.unapply)
  }

  val updateAddressForm: Form[UpdateAddressForm] = Form {
    mapping(
      "id" -> longNumber,
      "city" -> nonEmptyText,
      "streetName" -> nonEmptyText,
      "houseNumber" -> number,
      "postalCode" -> nonEmptyText
    )(UpdateAddressForm.apply)(UpdateAddressForm.unapply)
  }

  def addAddress(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.addressadd(addressForm))
  }

  def updateAddress(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>

    addressRepository.getById(id).map(address => {
      val filledUpdateForm = updateAddressForm.fill(UpdateAddressForm(address.id, address.city, address.streetName, address.houseNumber, address.postalCode))
      Ok(views.html.addressupdate(filledUpdateForm))
    })

  }

  def deleteAddress(id: Long): Action[AnyContent] = Action {
    addressRepository.delete(id)
    Redirect("/display-addresses")
  }

  def displayAddress(id: Long): Action[AnyContent] = Action.async { implicit request =>
    addressRepository.getByIdOption(id).map {
      case Some(a) => Ok("Address " + a.city + ", " + a.streetName + " " + a.houseNumber)
      case None => Redirect(routes.HomeController.index())
    }
  }

  def displayAddresses(): Action[AnyContent] = Action.async { implicit request =>
    addressRepository.list().map(addresses => Ok(views.html.addressesdisplay(addresses)))
  }

  def saveAddedAddress(): Action[AnyContent] = Action.async { implicit request =>

    addressForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.addressadd(errorForm))
        )
      },
      address => {
        addressRepository.create(address.city, address.streetName, address.houseNumber, address.postalCode).map { _ =>
          routes.AddressController.addAddress()
          Redirect("/display-addresses")
        }
      }
    )
  }

  def saveUpdatedAddress(): Action[AnyContent] = Action.async { implicit request =>
    updateAddressForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.addressupdate(errorForm))
        )
      },
      address => {
        addressRepository.update(address.id, Address(address.id, address.city, address.streetName, address.houseNumber, address.postalCode)).map { _ =>
          routes.AddressController.updateAddress(address.id)
          Redirect("/display-addresses")
        }
      }
    )
  }
}

case class CreateAddressForm(city: String, streetName: String, houseNumber: Int, postalCode: String)
case class UpdateAddressForm(id: Long, city: String, streetName: String, houseNumber: Int, postalCode: String)
