import models.User
import play.api.GlobalSettings

import play.api.Application

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    InitialData.insert()
  }


}

/**
 * Initialize test data
 */
object InitialData {

  def insert() = {
    if (User.findAll.isEmpty) {
      Seq(
        User("jan@test.de", "secret")
      ).foreach(User.create)
    }
  }

}

