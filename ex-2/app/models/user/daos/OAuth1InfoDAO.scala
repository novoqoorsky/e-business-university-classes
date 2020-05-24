package models.user.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth1Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class OAuth1InfoDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext, override val classTag: ClassTag[OAuth1Info])
  extends DelegableAuthInfoDAO[OAuth1Info] with DAOSlick {

  import profile.api._

  protected def oAuth1InfoQuery(loginInfo: LoginInfo) = for {
    dbLoginInfo <- loginInfoQuery(loginInfo)
    dbOAuth1Info <- slickOAuth1Infos if dbOAuth1Info.loginInfoId === dbLoginInfo.id
  } yield dbOAuth1Info

  // Use subquery workaround instead of join to get authinfo because slick only supports selecting
  // from a single table for update/delete queries (https://github.com/slick/slick/issues/684).
  protected def oAuth1InfoSubQuery(loginInfo: LoginInfo) =
    slickOAuth1Infos.filter(_.loginInfoId in loginInfoQuery(loginInfo).map(_.id))

  protected def addAction(loginInfo: LoginInfo, authInfo: OAuth1Info) =
    loginInfoQuery(loginInfo).result.head.flatMap { dbLoginInfo =>
      slickOAuth1Infos += DBOAuth1Info(None, authInfo.token, authInfo.secret, dbLoginInfo.id.get)
    }.transactionally

  protected def updateAction(loginInfo: LoginInfo, authInfo: OAuth1Info) =
    oAuth1InfoSubQuery(loginInfo).
      map(dbOAuthInfo => (dbOAuthInfo.token, dbOAuthInfo.secret)).
      update((authInfo.token, authInfo.secret))

  def find(loginInfo: LoginInfo): Future[Option[OAuth1Info]] = {
    val result = db.run(oAuth1InfoQuery(loginInfo).result.headOption)
    result.map { dbOAuth1InfoOption =>
      dbOAuth1InfoOption.map(dbOAuth1Info => OAuth1Info(dbOAuth1Info.token, dbOAuth1Info.secret))
    }
  }

  def add(loginInfo: LoginInfo, authInfo: OAuth1Info): Future[OAuth1Info] =
    db.run(addAction(loginInfo, authInfo)).map(_ => authInfo)

  def update(loginInfo: LoginInfo, authInfo: OAuth1Info): Future[OAuth1Info] =
    db.run(updateAction(loginInfo, authInfo)).map(_ => authInfo)

  def save(loginInfo: LoginInfo, authInfo: OAuth1Info): Future[OAuth1Info] = {
    val query = loginInfoQuery(loginInfo).joinLeft(slickOAuth1Infos).on(_.id === _.loginInfoId)
    val action = query.result.head.flatMap {
      case (dbLoginInfo, Some(dbOAuth1Info)) => updateAction(loginInfo, authInfo)
      case (dbLoginInfo, None) => addAction(loginInfo, authInfo)
    }.transactionally
    db.run(action).map(_ => authInfo)
  }

  def remove(loginInfo: LoginInfo): Future[Unit] =
    db.run(oAuth1InfoSubQuery(loginInfo).delete).map(_ => ())
}