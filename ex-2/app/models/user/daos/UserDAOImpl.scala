package models.user.daos

import java.time.ZonedDateTime
import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import models.user.{User, UserRoles}
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

class UserDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, userRoleDAO: UserRoleDAO)(implicit ec: ExecutionContext) extends UserDAO with DAOSlick {
  import profile.api._

  def find(loginInfo: LoginInfo) = {
    val userQuery = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.loginInfoId === dbLoginInfo.id)
      dbUser <- slickUsers.filter(_.id === dbUserLoginInfo.userID)
    } yield dbUser
    db.run(userQuery.result.headOption).map { dbUserOption =>
      dbUserOption.map { user =>
        User(user.userID, user.firstName, user.lastName, user.email, user.avatarURL, user.activated, UserRoles(user.roleId))
      }
    }
  }

  def find(userID: UUID) = {
    val query = slickUsers.filter(_.id === userID)

    db.run(query.result.headOption).map { resultOption =>
      resultOption.map(DBUser.toUser)
    }
  }

  def save(user: User) = {
    // combine database actions to be run sequentially
    val actions = (for {
      userRoleId <- userRoleDAO.getUserRole()
      dbUser = DBUser(user.userID, user.firstName, user.lastName, user.email, user.avatarURL, user.activated, userRoleId, ZonedDateTime.now())
      _ <- slickUsers.insertOrUpdate(dbUser)
    } yield ()).transactionally
    // run actions and return user afterwards
    db.run(actions).map(_ => user)
  }

  override def updateUserRole(userId: UUID, role: UserRoles.UserRole): Future[Boolean] = {
    db.run(slickUsers.filter(_.id === userId).map(_.roleId).update(role.id)).map(_ > 0)
  }

  def findByEmail(email: String): Future[Option[User]] = {
    db.run(slickUsers.filter(_.email === email).take(1).result.headOption).map(_ map DBUser.toUser)
  }
}