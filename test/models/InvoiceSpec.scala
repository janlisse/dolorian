package models

import anorm.NotAssigned
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import play.api.test._
import play.api.test.Helpers._
import org.joda.time.LocalDate
import java.math.BigDecimal

class InvoiceSpec extends FlatSpec with ShouldMatchers {

  "An Invoice" should "be savable" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val templateId = Template.saveToDB(ClasspathTemplate(NotAssigned, "defaultInvoiceTemplate", "key")).get
      val customerId = Customer.save(Customer(NotAssigned, "big company", "BIG", Address("mainstreet", "45", "12345", "denver"))).get
      val projectId = Project.save(Project(NotAssigned, "testProject", "1234-xc", customerId, templateId, templateId, new BigDecimal(50))).get
      val invoiceId = Invoice.save(Invoice(NotAssigned, projectId, new LocalDate, new BigDecimal(100), InvoiceStatus.Created, Some("123"))).get
      invoiceId should not equal (NotAssigned)
    }
  }
  
  "An Invoice status" should "be updatable" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val templateId = Template.saveToDB(ClasspathTemplate(NotAssigned, "defaultInvoiceTemplate", "key")).get
      val customerId = Customer.save(Customer(NotAssigned, "big company", "BIG", Address("mainstreet", "45", "12345", "denver"))).get
      val projectId = Project.save(Project(NotAssigned, "testProject", "1234-xc", customerId, templateId, templateId, new BigDecimal(50))).get
      val invoiceId = Invoice.save(Invoice(NotAssigned, projectId, new LocalDate, new BigDecimal(100), InvoiceStatus.Created, Some("123"))).get
      Invoice.updateStatus(invoiceId, InvoiceStatus.Sent)
      val updatedInvoice = Invoice.findById(invoiceId).get
      updatedInvoice.status should equal (InvoiceStatus.Sent)
    }
  }

}