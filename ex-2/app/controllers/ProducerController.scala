package controllers

import javax.inject.{Inject, Singleton}
import models.address.{Address, AddressRepository}
import models.producer.{Producer, ProducerRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ProducerController @Inject()(producerRepository: ProducerRepository,
                                   addressRepository: AddressRepository,
                                   messagesControllerComponents: MessagesControllerComponents)(implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(messagesControllerComponents) {

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
    val fProducers = producerRepository.list()

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
    Redirect("/display-producers")
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
          Redirect("/display-producers")
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
          Redirect("/display-producers")
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
}

case class CreateProducerForm(name: String, address: Long)
case class UpdateProducerForm(id: Long, name: String, address: Long)