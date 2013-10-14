package models

import anorm.Id
import anorm.NotAssigned
import org.joda.time.DateTime
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import play.api.test._
import play.api.test.Helpers._
import java.math.BigDecimal

class WorkItemSpec extends FlatSpec with ShouldMatchers {

  "A WorkItem" should "be savable" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val customerId = Customer.save(Customer(NotAssigned, "big company", "BIG", Address("mainstreet", "45", "12345", "denver"))).get
      val projectId = Project.save(Project(NotAssigned, "testProject", "1234-xc", customerId, 1l, 1l,new BigDecimal(50)))
      val id = WorkItem.save(WorkItem(NotAssigned, projectId.get, new DateTime(), new DateTime(), 30, "description"))
      id should not equal (None)
    }
  }

  "WorkItems" should "be retrievable by projectId" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val customerId = Customer.save(Customer(NotAssigned, "big company", "BIG", Address("mainstreet", "45", "12345", "denver"))).get
      val projectId = Project.save(Project(NotAssigned, "testProject", "1234-xc", customerId, 1l, 1l,new BigDecimal(50)))
      val projectId2 = Project.save(Project(NotAssigned, "testProject2", "666-cv", customerId, 1l, 1l,new BigDecimal(50)))
      val id = WorkItem.save(WorkItem(NotAssigned, projectId.get, new DateTime(), new DateTime(), 30, "description"))
      val id2 = WorkItem.save(WorkItem(NotAssigned, projectId2.get, new DateTime(), new DateTime(), 30, "task1"))
      val workItems = WorkItem.getByProject(projectId.get)
      workItems.size should equal(1)
    }
  }

  "WorkItems" should "be retrieved grouped by Project" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val customerId = Customer.save(Customer(NotAssigned, "big company", "BIG", Address("mainstreet", "45", "12345", "denver"))).get
      val projectId = Project.save(Project(NotAssigned, "testProject", "1234-xc", customerId, 1l, 1l,new BigDecimal(50)))
      val projectId2 = Project.save(Project(NotAssigned, "testProject2", "666-cv", customerId, 1l, 1l,new BigDecimal(50)))
      val id = WorkItem.save(WorkItem(NotAssigned, projectId.get, new DateTime(), new DateTime(), 30, "description"))
      val id2 = WorkItem.save(WorkItem(NotAssigned, projectId2.get, new DateTime(), new DateTime(), 30, "task1"))
      val id3 = WorkItem.save(WorkItem(NotAssigned, projectId2.get, new DateTime(), new DateTime(), 10, "task2"))
      val groupedMap = WorkItem.groupedByProjectId
      groupedMap.size should equal(2)
      (groupedMap get projectId.get).get.size should equal(1)
      (groupedMap get projectId2.get).get.size should equal(2)

    }
  }

  "WorkItems" should "be retrieved by Project, Month and Year" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val customerId = Customer.save(Customer(NotAssigned, "big company", "BIG", Address("mainstreet", "45", "12345", "denver"))).get
      val projectId = Project.save(Project(NotAssigned, "testProject", "1234-xc", customerId, 1l, 1l,new BigDecimal(50))).get
      val id = WorkItem.save(WorkItem(NotAssigned, projectId, new DateTime(2013, 9, 10, 10, 0), new DateTime(), 30, "description"))
      val id2 = WorkItem.save(WorkItem(NotAssigned, projectId, new DateTime(2013, 8, 10, 10, 0), new DateTime(), 30, "task1"))
      val selected = WorkItem.getByProjectMonthAndYear(projectId, 9, 2013)
      selected.size should equal(1)
    }
  }

}

