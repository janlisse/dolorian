package models

import java.math.BigDecimal

import org.joda.time.{DateTime, Duration, LocalDate, Period}
import org.scalatest.{FlatSpec, Matchers}
import play.api.test.Helpers._
import play.api.test._

class WorkItemSpec extends FlatSpec with Matchers {

  
  "WorkItem" should "have a rounded duration" in {
    running(FakeApplication()) {
      val workitem = SimpleWorkItem(None, 1l, new LocalDate(), Period.minutes(26).withSeconds(40),"test")
      workitem.roundedDuration.getMinutes should equal (30)
      
      val workitem2 = SimpleWorkItem(None, 1l, new LocalDate(), Period.hours(2).withMinutes(59).withSeconds(15),"test")
      workitem2.roundedDuration.normalizedStandard().getHours should equal (3)
      workitem2.roundedDuration.normalizedStandard().getMinutes should equal (0)
      
      val workitem3 = SimpleWorkItem(None, 1l, new LocalDate(), Period.hours(2).withMinutes(4).withSeconds(15),"test")
      workitem3.roundedDuration.normalizedStandard().getHours should equal (2)
    }
  }
  
  it should "be savable" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val templateId = Template.save(Template(None, "defaultInvoiceTemplate", "key")).get
      val customerId = Customer.save(Customer(None, "big company", "BIG", Address("mainstreet", "45", "12345", "denver"))).get
      val projectId = Project.save(Project(None, "testProject", "1234-xc", customerId, templateId, templateId,new BigDecimal(50)))
      val id = WorkItem.save(SimpleWorkItem(None, projectId.get,  new LocalDate, Duration.standardMinutes(10).toPeriod,"description"))
      id should not equal None
    }
  }

  it should "be retrievable by projectId" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val templateId = Template.save(Template(None, "defaultInvoiceTemplate", "key")).get
      val customerId = Customer.save(Customer(None, "big company", "BIG", Address("mainstreet", "45", "12345", "denver"))).get
      val projectId = Project.save(Project(None, "testProject", "1234-xc", customerId, templateId, templateId,new BigDecimal(50)))
      val projectId2 = Project.save(Project(None, "testProject2", "666-cv", customerId, templateId, templateId,new BigDecimal(50)))
      val id = WorkItem.save(DetailedWorkItem(None, projectId.get, new DateTime(), new DateTime(), Some(30),new LocalDate(), "description"))
      val id2 = WorkItem.save(DetailedWorkItem(None, projectId2.get, new DateTime(), new DateTime(), Some(30),new LocalDate(), "task1"))
      val workItems = WorkItem.getByProject(projectId.get)
      workItems.size should equal(1)
    }
  }

  it should "be retrieved grouped by Project" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val templateId = Template.save(Template(None, "defaultInvoiceTemplate", "key")).get
      val customerId = Customer.save(Customer(None, "big company", "BIG", Address("mainstreet", "45", "12345", "denver"))).get
      val projectId = Project.save(Project(None, "testProject", "1234-xc", customerId, templateId, templateId,new BigDecimal(50)))
      val projectId2 = Project.save(Project(None, "testProject2", "666-cv", customerId, templateId, templateId,new BigDecimal(50)))
      val id = WorkItem.save(DetailedWorkItem(None, projectId.get, new DateTime(), new DateTime(), Some(30), new LocalDate(), "description"))
      val id2 = WorkItem.save(DetailedWorkItem(None, projectId2.get, new DateTime(), new DateTime(), Some(30), new LocalDate(), "task1"))
      val id3 = WorkItem.save(DetailedWorkItem(None, projectId2.get, new DateTime(), new DateTime(), Some(30), new LocalDate(), "task2"))
      val groupedMap = WorkItem.groupedByProjectId
      groupedMap.size should equal(2)
      (groupedMap get projectId.get).get.size should equal(1)
      (groupedMap get projectId2.get).get.size should equal(2)

    }
  }

  it should "be retrieved by Project, Month and Year" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val templateId = Template.save(Template(None, "defaultInvoiceTemplate", "key")).get
      val customerId = Customer.save(Customer(None, "big company", "BIG", Address("mainstreet", "45", "12345", "denver"))).get
      val projectId = Project.save(Project(None, "testProject", "1234-xc", customerId, 1l, 1l,new BigDecimal(50))).get
      val id = WorkItem.save(DetailedWorkItem(None, projectId, new DateTime(2013, 9, 10, 10, 0), new DateTime(), Some(30), new LocalDate(), "description"))
      val id2 = WorkItem.save(DetailedWorkItem(None, projectId, new DateTime(2013, 8, 10, 10, 0), new DateTime(), Some(30), new LocalDate(), "task1"))
      val selected = WorkItem.getByProjectMonthAndYear(projectId, 9, 2013)
      selected.size should equal(1)
    }
  }
  
  it should "calculate sum of durations" in {
    running(FakeApplication()) {
      val workitem = SimpleWorkItem(None, 1l, new LocalDate(), Period.minutes(6),"test")
      val workitem2 = SimpleWorkItem(None, 1l, new LocalDate(), Period.hours(2).withMinutes(59),"test")
      val workitem3 = SimpleWorkItem(None, 1l, new LocalDate(), Period.hours(2).withMinutes(4),"test")
      val total = WorkItem.totalHours(List(workitem, workitem2, workitem3))
      total should equal("05:09")
    }
  }
  
  it should "calculate sum of durations without normalizing hours" in {
    running(FakeApplication()) {
      val workitem = SimpleWorkItem(None, 1l, new LocalDate(), Period.hours(15).withMinutes(20),"test")
      val workitem2 = SimpleWorkItem(None, 1l, new LocalDate(), Period.hours(20),"test")
      val workitem3 = SimpleWorkItem(None, 1l, new LocalDate(), Period.hours(60),"test")
      val workitem4 = SimpleWorkItem(None, 1l, new LocalDate(), Period.hours(15),"test")
      val total = WorkItem.totalHours(List(workitem, workitem2, workitem3, workitem4))
      total should equal("110:20")
    }
  }

}

