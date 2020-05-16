package controllers

import javax.inject.{Inject, Singleton}
import models.category.{Category, CategoryRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryController @Inject()(categoryRepository: CategoryRepository, messagesControllerComponents: MessagesControllerComponents)(implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(messagesControllerComponents) {

  val categoryForm: Form[CreateCategoryForm] = Form {
    mapping(
      "name" -> nonEmptyText
    )(CreateCategoryForm.apply)(CreateCategoryForm.unapply)
  }

  val updateCategoryForm: Form[UpdateCategoryForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText
    )(UpdateCategoryForm.apply)(UpdateCategoryForm.unapply)
  }

  def addCategory(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.categoryadd(categoryForm))
  }

  def updateCategory(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>

    categoryRepository.getById(id).map(category => {
      val filledUpdateForm = updateCategoryForm.fill(UpdateCategoryForm(category.id, category.name))
      Ok(views.html.categoryupdate(filledUpdateForm))
    })

  }

  def deleteCategory(id: Long): Action[AnyContent] = Action {
    categoryRepository.delete(id)
    Redirect("/display-categories")
  }

  def displayCategory(id: Long): Action[AnyContent] = Action.async { implicit request =>
    categoryRepository.getByIdOption(id).map {
      case Some(c) => Ok("Category " + c.name)
      case None => Redirect(routes.HomeController.index())
    }
  }

  def displayCategories(): Action[AnyContent] = Action.async { implicit request =>
    categoryRepository.list().map(categories => Ok(views.html.categoriesdisplay(categories)))
  }

  def saveAddedCategory(): Action[AnyContent] = Action.async { implicit request =>

    categoryForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.categoryadd(errorForm))
        )
      },
      category => {
        categoryRepository.create(category.name).map { _ =>
          routes.CategoryController.addCategory()
          Redirect("/display-categories")
        }
      }
    )
  }

  def saveUpdatedCategory(): Action[AnyContent] = Action.async { implicit request =>
    updateCategoryForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.categoryupdate(errorForm))
        )
      },
      category => {
        categoryRepository.update(category.id, Category(category.id, category.name)).map { _ =>
          routes.CategoryController.updateCategory(category.id)
          Redirect("/display-categories")
        }
      }
    )
  }

  // REACT

  def categories(): Action[AnyContent] = Action.async {
    categoryRepository.list().map(categories => Ok(Json.toJson(categories)))
  }

  def postCategory(): Action[AnyContent] = Action { implicit request =>
    val c = request.body.asJson.get.asInstanceOf[JsObject].value
    categoryRepository.create(
      c("name").asInstanceOf[JsString].value
    )
    Created("Category created")
  }

  def putCategory(): Action[AnyContent] = Action { implicit request =>
    val c = request.body.asJson.get.asInstanceOf[JsObject].value
    val id = c("id").asInstanceOf[JsString].value.toLong
    val category = Category(
      id,
      c("name").asInstanceOf[JsString].value
    )
    categoryRepository.update(id, category)
    Created(Json.toJson(category))
  }

  def deleteCategoryExternal(id: Long): Action[AnyContent] = Action { implicit request =>
    deleteCategory(id)
    Ok("Cart deleted")
  }
}

case class CreateCategoryForm(name: String)
case class UpdateCategoryForm(id: Long, name: String)
