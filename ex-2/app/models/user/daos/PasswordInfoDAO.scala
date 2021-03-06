package models.user.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class PasswordInfoDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext, override val classTag: ClassTag[PasswordInfo])
  extends DelegableAuthInfoDAO[PasswordInfo] with DAOSlick {

  import profile.api._

  protected def passwordInfoQuery(loginInfo: LoginInfo) = for {
    dbLoginInfo <- loginInfoQuery(loginInfo)
    dbPasswordInfo <- slickPasswordInfos if dbPasswordInfo.loginInfoId === dbLoginInfo.id
  } yield dbPasswordInfo

  // Use subquery workaround instead of join to get authinfo because slick only supports selecting
  // from a single table for update/delete queries (https://github.com/slick/slick/issues/684).
  protected def passwordInfoSubQuery(loginInfo: LoginInfo) =
    slickPasswordInfos.filter(_.loginInfoId in loginInfoQuery(loginInfo).map(_.id))

  protected def addAction(loginInfo: LoginInfo, authInfo: PasswordInfo) =
    loginInfoQuery(loginInfo).result.head.flatMap { dbLoginInfo =>
      slickPasswordInfos +=
        DBPasswordInfo(authInfo.hasher, authInfo.password, authInfo.salt, dbLoginInfo.id.get)
    }.transactionally

  protected def updateAction(loginInfo: LoginInfo, authInfo: PasswordInfo) =
    passwordInfoSubQuery(loginInfo).
      map(dbPasswordInfo => (dbPasswordInfo.hasher, dbPasswordInfo.password, dbPasswordInfo.salt)).
      update((authInfo.hasher, authInfo.password, authInfo.salt))

  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    db.run(passwordInfoQuery(loginInfo).result.headOption).map { dbPasswordInfoOption =>
      dbPasswordInfoOption.map(dbPasswordInfo =>
        PasswordInfo(dbPasswordInfo.hasher, dbPasswordInfo.password, dbPasswordInfo.salt))
    }
  }

  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    db.run(addAction(loginInfo, authInfo)).map(_ => authInfo)

  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    db.run(updateAction(loginInfo, authInfo)).map(_ => authInfo)

  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val query = loginInfoQuery(loginInfo).joinLeft(slickPasswordInfos).on(_.id === _.loginInfoId)
    val action = query.result.head.flatMap {
      case (dbLoginInfo, Some(dbPasswordInfo)) => updateAction(loginInfo, authInfo)
      case (dbLoginInfo, None) => addAction(loginInfo, authInfo)
    }
    db.run(action).map(_ => authInfo)
  }

  def remove(loginInfo: LoginInfo): Future[Unit] =
    db.run(passwordInfoSubQuery(loginInfo).delete).map(_ => ())
}
