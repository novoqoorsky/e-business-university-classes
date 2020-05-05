package models.product

import javax.inject.{Inject, Singleton}
import models.category.CategoryRepository
import models.producer.ProducerRepository
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductRepository @Inject()(dbConfigProvider: DatabaseConfigProvider,
                                  categoryRepository: CategoryRepository,
                                  producerRepository: ProducerRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class ProductTable(tag: Tag) extends Table[Product](tag, "product") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def description = column[String]("description")
    def category = column[Long]("category")
    def producer = column[Long]("producer")
    def price = column[Double]("price")

    //def category_fk = foreignKey("category_fk", category, categories)(_.id)
    //def producer_fk = foreignKey("producer_fk", producer, producers)(_.id)

    def * = (id, name, description, category, producer, price) <> ((Product.apply _).tupled, Product.unapply)
  }

  private val products = TableQuery[ProductTable]
  //private val categories = TableQuery[CategoryTable]
  //private val producers = TableQuery[ProducerTable]

  def create(name: String, description: String, category: Long, producer: Long, price: Double): Future[Product] = db.run {
    (products.map(p => (p.name, p.description, p.category, p.producer, p.price))
      returning products.map(_.id)
      into { case ((name, description, category, producer, price), id) => Product(id, name, description, category, producer, price) }
      ) += (name, description, category, producer, price)
  }

  def list(): Future[Seq[Product]] = db.run {
    products.result
  }

  def getByCategory(category_id: Long): Future[Seq[Product]] = db.run {
    products.filter(_.category === category_id).result
  }

  def getById(id: Long): Future[Product] = db.run {
    products.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Product]] = db.run {
    products.filter(_.id === id).result.headOption
  }

  def getByCategories(category_ids: List[Long]): Future[Seq[Product]] = db.run {
    products.filter(_.category inSet category_ids).result
  }

  def delete(id: Long): Future[Unit] = db.run(products.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, new_product: Product): Future[Unit] = {
    val productToUpdate: Product = new_product.copy(id)
    db.run(products.filter(_.id === id).update(productToUpdate)).map(_ => ())
  }
}
