package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import javax.inject.{Inject, Singleton}
import models.category.{Category, CategoryRepository}
import models.producer.{Producer, ProducerRepository}
import models.product.{Product, ProductRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc._
import utils.silhouette.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ProductController @Inject()(productRepository: ProductRepository,
                                  categoryRepository: CategoryRepository,
                                  producerRepository: ProducerRepository,
                                  messagesControllerComponents: MessagesControllerComponents,
                                  silhouette: Silhouette[DefaultEnv])(implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(messagesControllerComponents) {

  val DISPLAY_PRODUCTS_URL = "/display-products"

  var categories: Seq[Category] = Seq[Category]()
  var producers: Seq[Producer] = Seq[Producer]()

  val productForm: Form[CreateProductForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> longNumber,
      "producer" -> longNumber,
      "price" -> number
    )(CreateProductForm.apply)(CreateProductForm.unapply)
  }

  val updateProductForm: Form[UpdateProductForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> longNumber,
      "producer" -> longNumber,
      "price" -> number
    )(UpdateProductForm.apply)(UpdateProductForm.unapply)
  }

  def addProduct(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val fCategories = categoryRepository.list()
    val fProducers = producerRepository.list()

    for {
      categories <- fCategories
      producers <- fProducers
    } yield Ok(views.html.productadd(productForm, categories, producers))
  }

  def updateProduct(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    retrieveForeignProperties()

    productRepository.getById(id).map(product => {
      val filledUpdateForm = updateProductForm.fill(UpdateProductForm(product.id, product.name, product.description, product.category, product.producer, product.price))
      Ok(views.html.productupdate(filledUpdateForm, categories, producers))
    })

  }

  def deleteProduct(id: Long): Action[AnyContent] = Action {
    productRepository.delete(id)
    Redirect(DISPLAY_PRODUCTS_URL)
  }

  def displayProduct(id: Long): Action[AnyContent] = Action.async { implicit request =>
    productRepository.getByIdOption(id).map {
      case Some(p) => Ok("Product " + p.name)
      case None => Redirect(routes.HomeController.index())
    }
  }

  def displayProducts(): Action[AnyContent] = Action.async { implicit request =>
    productRepository.list().map(products => Ok(views.html.productsdisplay(products)))
  }

  def saveAddedProduct(): Action[AnyContent] = Action.async { implicit request =>
    retrieveForeignProperties()

    productForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productadd(errorForm, categories, producers))
        )
      },
      product => {
        productRepository.create(product.name, product.description, product.category, product.producer, product.price).map { _ =>
          routes.ProductController.addProduct()
          Redirect(DISPLAY_PRODUCTS_URL)
        }
      }
    )
  }

  def saveUpdatedProduct(): Action[AnyContent] = Action.async { implicit request =>
    retrieveForeignProperties()

    updateProductForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productupdate(errorForm, categories, producers))
        )
      },
      product => {
        productRepository.update(product.id, Product(product.id, product.name, product.description, product.category, product.producer, product.price)).map { _ =>
          routes.ProductController.updateProduct(product.id)
          Redirect(DISPLAY_PRODUCTS_URL)
        }
      }
    )
  }

  def retrieveForeignProperties(): Unit = {
    categoryRepository.list().onComplete {
      case Success(c) => categories = c
      case Failure(_) => print("Failure on retrieving categories")
    }
    producerRepository.list().onComplete{
      case Success(p) => producers = p
      case Failure(_) => print("Failure on retrieving products")
    }
  }

  // REACT

  def products(): Action[AnyContent] = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    productRepository.list().map(products => Ok(Json.toJson(products)))
  }

  def postProduct(): Action[AnyContent] = Action { implicit request =>
    val p = request.body.asJson.get.asInstanceOf[JsObject].value
    productRepository.create(
      p("name").asInstanceOf[JsString].value,
      p("description").asInstanceOf[JsString].value,
      p("category").asInstanceOf[JsString].value.toLong,
      p("producer").asInstanceOf[JsString].value.toLong,
      p("price").asInstanceOf[JsString].value.toInt
    )
    Created("Product created")
  }

  def putProduct(): Action[AnyContent] = Action { implicit request =>
    val p = request.body.asJson.get.asInstanceOf[JsObject].value
    val id = p("id").asInstanceOf[JsString].value.toLong
    val product = Product(
      id,
      p("name").asInstanceOf[JsString].value,
      p("description").asInstanceOf[JsString].value,
      p("category").asInstanceOf[JsString].value.toLong,
      p("producer").asInstanceOf[JsString].value.toLong,
      p("price").asInstanceOf[JsString].value.toInt
    )
    productRepository.update(id, product)
    Created(Json.toJson(product))
  }

  def deleteProductExternal(id: Long): Action[AnyContent] = Action { implicit request =>
    deleteProduct(id)
    Ok("Product deleted")
  }
}

case class CreateProductForm(name: String, description: String, category: Long, producer: Long, price: Int)
case class UpdateProductForm(id: Long, name: String, description: String, category: Long, producer: Long, price: Int)