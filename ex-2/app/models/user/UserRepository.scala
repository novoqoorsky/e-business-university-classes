package models.user

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class UserTable(tag: Tag) extends Table[DeprecatedUser](tag, "user") {
    
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("user_name")
    def password = column[String]("password")
    def email = column[String]("email")
    def client = column[Long]("client")

    def * = (id, username, password, email, client) <> ((DeprecatedUser.apply _).tupled, DeprecatedUser.unapply)
  }

  private val users = TableQuery[UserTable]

  def create(username: String, password: String, email: String, client: Long): Future[DeprecatedUser] = db.run {
    (users.map(u => (u.username, u.password, u.email, u.client))
      returning users.map(_.id)
      into { case ((username, password, email, client), id) => DeprecatedUser(id, username, password, email, client) }
      ) += (username, password, email, client)
  }

  def list(): Future[Seq[DeprecatedUser]] = db.run {
    users.result
  }

  def getById(id: Long): Future[DeprecatedUser] = db.run {
    users.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[DeprecatedUser]] = db.run {
    users.filter(_.id === id).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run(users.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newUser: DeprecatedUser): Future[Unit] = {
    val userToUpdate: DeprecatedUser = newUser.copy(id)
    db.run(users.filter(_.id === id).update(userToUpdate)).map(_ => ())
  }
}