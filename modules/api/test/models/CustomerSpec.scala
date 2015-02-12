package models

import org.scalatest.{FlatSpec, Matchers}
import play.api.test.Helpers._
import play.api.test._

class CustomerSpec extends FlatSpec with Matchers {

  val testCustomer = Customer(None, "exampleTech GmbH", "EXA", Address("Brunnenstr.", "3", "45701", "Entenhausen"))

  val appWithMemoryDatabase = FakeApplication(additionalConfiguration = inMemoryDatabase("test"))


  "A Customer" should "be savable" in {
    running(appWithMemoryDatabase) {
      val customerId = Customer.save(testCustomer)
      customerId should not equal None
    }
  }

  "A Customer" should "be updatable" in {
    running(appWithMemoryDatabase) {
      val customerId = Customer.save(testCustomer).get
      val customer = Customer.findById(customerId).get
      val newCustomer = customer.copy(invoiceSequence = Some(2))
      Customer.update(customerId, newCustomer)
      val updatedCustomer = Customer.findById(customerId).get
      updatedCustomer.invoiceSequence should equal(Some(2))
    }
  }

  "A Customer" should "increment invoiceSequence" in {
    running(appWithMemoryDatabase) {
      val customerId = Customer.save(testCustomer).get
      val customer = Customer.findById(customerId).get
      customer.incrementSequence
      val customerWithIncrementedSeq = Customer.findById(customerId).get
      customerWithIncrementedSeq.invoiceSequence should equal(Some(2))
    }
  }

}