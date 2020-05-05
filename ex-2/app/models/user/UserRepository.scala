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

  class UserTable(tag: Tag) extends Table[User](tag, "user") {
    
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("user_name")
    def password = column[String]("password")
    def email = column[String]("email")
    def client = column[Option[Long]]("client")

    def * = (id, username, password, email, client) <> ((User.apply _).tupled, User.unapply)
  }

  private val users = TableQuery[UserTable]

  def create(username: String, password: String, email: String): Future[User] = db.run {
    (users.map(u => (u.username, u.password, u.email))
      returning users.map(_.id)
      into { case ((username, password, email), id) => User(id, username, password, email, None) }
      ) += (username, password, email)
  }

  def list(): Future[Seq[User]] = db.run {
    users.result
  }

  def getById(id: Long): Future[User] = db.run {
    users.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[User]] = db.run {
    users.filter(_.id === id).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run(users.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newUser: User): Future[Unit] = {
    val userToUpdate: User = newUser.copy(id)
    db.run(users.filter(_.id === id).update(userToUpdate)).map(_ => ())
  }
}