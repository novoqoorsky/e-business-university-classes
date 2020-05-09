package models.clientorders

import javax.inject.{Inject, Singleton}
import models.client.ClientRepository
import models.order.OrderRepository
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ClientOrdersRepository @Inject() (dbConfigProvider: DatabaseConfigProvider,
                                        val clientRepository: ClientRepository,
                                        val orderRepository: OrderRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class ClientOrdersTable(tag: Tag) extends Table[ClientOrders](tag, "client_orders") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def client = column[Long]("client")
    def order = column[Long]("order")

    def category_fk = foreignKey("category_fk", client, clients)(_.id)
    def producer_fk = foreignKey("producer_fk", order, orders)(_.id)

    def * = (id, client, order) <> ((ClientOrders.apply _).tupled, ClientOrders.unapply)
  }

  import clientRepository.ClientTable
  import orderRepository.OrderTable

  private val clientOrders = TableQuery[ClientOrdersTable]
  private val clients = TableQuery[ClientTable]
  private val orders = TableQuery[OrderTable]

  def create(client: Long, order: Long): Future[ClientOrders] = db.run {
    (clientOrders.map(co => (co.client, co.order))
      returning clientOrders.map(_.id)
      into { case ((client, order), id) => ClientOrders(id, client, order) }
      ) += (client, order)
  }

  def list(): Future[Seq[ClientOrders]] = db.run {
    clientOrders.result
  }

  def getById(id: Long): Future[ClientOrders] = db.run {
    clientOrders.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[ClientOrders]] = db.run {
    clientOrders.filter(_.id === id).result.headOption
  }

  def getByClient(client_id: Long): Future[Seq[ClientOrders]] = db.run {
    clientOrders.filter(_.client === client_id).result
  }

  def delete(id: Long): Future[Unit] = db.run(clientOrders.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newClientOrders: ClientOrders): Future[Unit] = {
    val clientOrdersToUpdate: ClientOrders = newClientOrders.copy(id)
    db.run(clientOrders.filter(_.id === id).update(clientOrdersToUpdate)).map(_ => ())
  }
}

