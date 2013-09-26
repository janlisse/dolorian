import anorm.NotAssigned
import models.{Project, WorkItem}

import org.joda.time.DateTime
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import org.squeryl.PrimitiveTypeMode.inTransaction

import play.api.test._
import play.api.test.Helpers._

class WorkItemSpec extends FlatSpec with ShouldMatchers {

  "A WorkItem" should "be savable" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      inTransaction {
        val projectId = Project.save("test")
        val id = WorkItem.save(WorkItem(NotAssigned, projectId.get, new DateTime(), new DateTime(), 30, "description"))
        id should not equal (None)
      }
    }
  }

}

