package models.client

import javax.inject.{Inject, Singleton}
import models.address.AddressRepository
import models.cart.CartRepository
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ClientRepository @Inject() (dbConfigProvider: DatabaseConfigProvider,
                                  val addressRepository: AddressRepository,
                                  val cartRepository: CartRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class ClientTable(tag: Tag) extends Table[Client](tag, "client") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def lastName = column[String]("last_name")
    def email = column[String]("email")
    def address = column[Long]("address")
    def cart = column[Long]("cart")

    def addressFk = foreignKey("address_fk", address, addresses)(_.id)

    def * = (id, name, lastName, email, address, cart) <> ((Client.apply _).tupled, Client.unapply)
  }

  import addressRepository.AddressTable

  private val clients = TableQuery[ClientTable]
  private val addresses = TableQuery[AddressTable]

  def create(name: String, lastName: String, email: String, address: Long, cart: Long): Future[Client] = db.run {
    (clients.map(c => (c.name, c.lastName, c.email, c.address, c.cart))
      returning clients.map(_.id)
      into { case ((name, lastName, email, address, cart), id) => Client(id, name, lastName, email, address, cart) }
      ) += (name, lastName, email, address, cart)
  }

  def list(): Future[Seq[Client]] = db.run {
    clients.result
  }

  def getById(id: Long): Future[Client] = db.run {
    clients.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Client]] = db.run {
    clients.filter(_.id === id).result.headOption
  }

  def getByEmailOption(email: String): Future[Option[Client]] = db.run {
    clients.filter(_.email === email).result.headOption
  }


  def delete(id: Long): Future[Unit] = db.run(clients.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newClient: Client): Future[Unit] = {
    val clientToUpdate: Client = newClient.copy(id)
    db.run(clients.filter(_.id === id).update(clientToUpdate)).map(_ => ())
  }
}
