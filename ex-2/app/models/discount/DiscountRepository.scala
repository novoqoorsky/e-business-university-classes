package models.discount

import javax.inject.{Inject, Singleton}
import models.product.ProductRepository
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DiscountRepository @Inject() (dbConfigProvider: DatabaseConfigProvider, productRepository: ProductRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DiscountTable(tag: Tag) extends Table[Discount](tag, "discount") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def product = column[Long]("product")
    def percentage = column[Int]("percentage")

    //def product_fk = foreignKey("product_fk", product, products)(_.id)

    def * = (id, product, percentage) <> ((Discount.apply _).tupled, Discount.unapply)
  }

  private val discounts = TableQuery[DiscountTable]
  //private val products = TableQuery[ProductTable]

  def create(product: Long, percentage: Int): Future[Discount] = db.run {
    (discounts.map(d => (d.product, d.percentage))
      returning discounts.map(_.id)
      into { case ((product, percentage), id) => Discount(id, product, percentage) }
      ) += (product, percentage)
  }

  def list(): Future[Seq[Discount]] = db.run {
    discounts.result
  }

  def getById(id: Long): Future[Discount] = db.run {
    discounts.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Discount]] = db.run {
    discounts.filter(_.id === id).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run(discounts.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newDiscount: Discount): Future[Unit] = {
    val discountToUpdate: Discount = newDiscount.copy(id)
    db.run(discounts.filter(_.id === id).update(discountToUpdate)).map(_ => ())
  }
}

