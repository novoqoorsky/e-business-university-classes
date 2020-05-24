package modules

import com.google.inject.AbstractModule
import models.user.daos.{LoginInfoDAO, LoginInfoDAOImpl}
import models.user.services.{AuthTokenService, AuthTokenServiceImpl}
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport

class BaseModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bind[AuthTokenService].to[AuthTokenServiceImpl]
    bind[LoginInfoDAO].to[LoginInfoDAOImpl]
  }
}