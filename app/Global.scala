import anorm.Id
import models.{WorkItem, Project, User}
import org.joda.time.DateTime
import play.api.{Play, GlobalSettings, Application}
import play.api.Play.current

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    TestData.insert()
  }
}

/**
 * Initialize test data
 */
object TestData {

  def insert() = {
    if (Play.isDev && User.findAll.isEmpty) {
      Seq(
        User("Joshi", "jan@test.de", "secret")
      ).foreach(User.create)
      Seq(
        Project(Id(1l), "IS24", Some("xyz-123"))
      ).foreach(Project.save)
      Seq(
        WorkItem(Id(1l), 1l, new DateTime(2013,7,12,10,0),new DateTime(2013,7,12,20,20), 30,"something"),
        WorkItem(Id(2l), 1l, new DateTime(2013,7,14,10,15),new DateTime(2013,7,14,18,0), 30,"anything")
      ).foreach(WorkItem.save)
    }
  }

}

