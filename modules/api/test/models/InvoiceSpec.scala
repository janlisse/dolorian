package models

import java.math.BigDecimal

import org.joda.time.LocalDate
import org.scalatest.{FlatSpec, Matchers}
import play.api.test.Helpers._
import play.api.test._

class InvoiceSpec extends FlatSpec with Matchers {

  "invoice" should "be savable" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val templateId = Template.save(Template(None, "defaultInvoiceTemplate", "key")).get
      val customerId = Customer.save(Customer(None, "big company", "BIG", Address("mainstreet", "45", "12345", "denver"))).get
      val projectId = Project.save(Project(None, "testProject", "1234-xc", customerId, templateId, templateId, new BigDecimal(50))).get
      val invoiceId = Invoice.save(Invoice(None, projectId, new LocalDate, new BigDecimal(100), InvoiceStatus.Created, Some("123"))).get
      invoiceId should not equal None
    }
  }
  
  it should "be updatable" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val templateId = Template.save(Template(None, "defaultInvoiceTemplate", "key")).get
      val customerId = Customer.save(Customer(None, "big company", "BIG", Address("mainstreet", "45", "12345", "denver"))).get
      val projectId = Project.save(Project(None, "testProject", "1234-xc", customerId, templateId, templateId, new BigDecimal(50))).get
      val invoiceId = Invoice.save(Invoice(None, projectId, new LocalDate, new BigDecimal(100), InvoiceStatus.Created, Some("123"))).get
      Invoice.updateStatus(invoiceId, InvoiceStatus.Sent)
      val updatedInvoice = Invoice.findById(invoiceId).get
      updatedInvoice.status should equal (InvoiceStatus.Sent)
    }
  }

}