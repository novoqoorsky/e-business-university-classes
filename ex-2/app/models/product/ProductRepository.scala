package models.product

import javax.inject.{Inject, Singleton}
import models.category.CategoryRepository
import models.producer.ProducerRepository
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductRepository @Inject()(dbConfigProvider: DatabaseConfigProvider,
                                  val categoryRepository: CategoryRepository,
                                  val producerRepository: ProducerRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class ProductTable(tag: Tag) extends Table[Product](tag, "product") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def description = column[String]("description")
    def category = column[Long]("category")
    def producer = column[Long]("producer")
    def price = column[Int]("price")

    def categoryFk = foreignKey("category_fk", category, categories)(_.id)
    def producerFk = foreignKey("producer_fk", producer, producers)(_.id)

    def * = (id, name, description, category, producer, price) <> ((Product.apply _).tupled, Product.unapply)
  }

  import categoryRepository.CategoryTable
  import producerRepository.ProducerTable

  private val products = TableQuery[ProductTable]
  private val categories = TableQuery[CategoryTable]
  private val producers = TableQuery[ProducerTable]

  def create(name: String, description: String, category: Long, producer: Long, price: Int): Future[Product] = db.run {
    (products.map(p => (p.name, p.description, p.category, p.producer, p.price))
      returning products.map(_.id)
      into { case ((name, description, category, producer, price), id) => Product(id, name, description, category, producer, price) }
      ) += (name, description, category, producer, price)
  }

  def list(): Future[Seq[Product]] = db.run {
    products.result
  }

  def getByCategory(categoryId: Long): Future[Seq[Product]] = db.run {
    products.filter(_.category === categoryId).result
  }

  def getById(id: Long): Future[Product] = db.run {
    products.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Product]] = db.run {
    products.filter(_.id === id).result.headOption
  }

  def getByCategories(categoryIds: List[Long]): Future[Seq[Product]] = db.run {
    products.filter(_.category inSet categoryIds).result
  }

  def delete(id: Long): Future[Unit] = db.run(products.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newProduct: Product): Future[Unit] = {
    val productToUpdate: Product = newProduct.copy(id)
    db.run(products.filter(_.id === id).update(productToUpdate)).map(_ => ())
  }
}
