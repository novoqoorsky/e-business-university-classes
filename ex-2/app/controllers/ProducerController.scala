package controllers

import javax.inject.{Inject, Singleton}
import models.address.{Address, AddressRepository}
import models.producer.{Producer, ProducerRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ProducerController @Inject()(producerRepository: ProducerRepository,
                                   addressRepository: AddressRepository,
                                   messagesControllerComponents: MessagesControllerComponents)(implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(messagesControllerComponents) {

  val DISPLAY_PRODUCERS_URL = "/display-producers"

  var addresses: Seq[Address] = Seq[Address]()

  val producerForm: Form[CreateProducerForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "address" -> longNumber,
    )(CreateProducerForm.apply)(CreateProducerForm.unapply)
  }

  val updateProducerForm: Form[UpdateProducerForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "address" -> longNumber
    )(UpdateProducerForm.apply)(UpdateProducerForm.unapply)
  }

  def addProducer(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val fAddresses = addressRepository.list()

    for {
      addresses <- fAddresses
    } yield Ok(views.html.produceradd(producerForm, addresses))
  }

  def updateProducer(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    retrieveForeignProperties()

    producerRepository.getById(id).map(producer => {
      val filledUpdateForm = updateProducerForm.fill(UpdateProducerForm(producer.id, producer.name, producer.address))
      Ok(views.html.producerupdate(filledUpdateForm, addresses))
    })

  }

  def deleteProducer(id: Long): Action[AnyContent] = Action {
    producerRepository.delete(id)
    Redirect(DISPLAY_PRODUCERS_URL)
  }

  def displayProducer(id: Long): Action[AnyContent] = Action.async { implicit request =>
    producerRepository.getByIdOption(id).map {
      case Some(p) => Ok("Producer " + p.name)
      case None => Redirect(routes.HomeController.index())
    }
  }

  def displayProducers(): Action[AnyContent] = Action.async { implicit request =>
    producerRepository.list().map(producers => Ok(views.html.producersdisplay(producers)))
  }

  def saveAddedProducer(): Action[AnyContent] = Action.async { implicit request =>
    retrieveForeignProperties()

    producerForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.produceradd(errorForm, addresses))
        )
      },
      producer => {
        producerRepository.create(producer.name, producer.address).map { _ =>
          routes.ProducerController.addProducer()
          Redirect(DISPLAY_PRODUCERS_URL)
        }
      }
    )
  }

  def saveUpdatedProducer(): Action[AnyContent] = Action.async { implicit request =>
    retrieveForeignProperties()

    updateProducerForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.producerupdate(errorForm, addresses))
        )
      },
      producer => {
        producerRepository.update(producer.id, Producer(producer.id, producer.name, producer.address)).map { _ =>
          routes.ProducerController.updateProducer(producer.id)
          Redirect(DISPLAY_PRODUCERS_URL)
        }
      }
    )
  }

  def retrieveForeignProperties(): Unit = {
    addressRepository.list().onComplete {
      case Success(a) => addresses = a
      case Failure(_) => print("Failure on retrieving addresses for producers")
    }
  }

  // REACT

  def producers(): Action[AnyContent] = Action.async {
    producerRepository.list().map(producers => Ok(Json.toJson(producers)))
  }

  def postProducer(): Action[AnyContent] = Action { implicit request =>
    val p = request.body.asJson.get.asInstanceOf[JsObject].value
    producerRepository.create(
      p("name").asInstanceOf[JsString].value,
      p("address").asInstanceOf[JsString].value.toLong
    )
    Created("Producer created")
  }

  def putProducer(): Action[AnyContent] = Action { implicit request =>
    val p = request.body.asJson.get.asInstanceOf[JsObject].value
    val id = p("id").asInstanceOf[JsString].value.toLong
    val producer = Producer(
      id,
      p("name").asInstanceOf[JsString].value,
      p("address").asInstanceOf[JsString].value.toLong
    )
    producerRepository.update(id, producer)
    Created(Json.toJson(producer))
  }

  def deleteProducerExternal(id: Long): Action[AnyContent] = Action { implicit request =>
    deleteProducer(id)
    Ok("Cart deleted")
  }
}

case class CreateProducerForm(name: String, address: Long)
case class UpdateProducerForm(id: Long, name: String, address: Long)