package models

import anorm.NotAssigned
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import play.api.test._
import play.api.test.Helpers._

class CustomerSpec extends FlatSpec with ShouldMatchers {

  val testCustomer = Customer(NotAssigned, "exampleTech GmbH","EXA", Address("Brunnenstr.","3","45701","Entenhausen"))
  
  "A Customer" should "be savable" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val customerId = Customer.save(testCustomer)
      customerId should not equal (NotAssigned)
    }
  }
  
  "A Customer" should "be updatable" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val customerId = Customer.save(testCustomer).get
      val customer = Customer.findById(customerId).get
      val newCustomer = customer.copy(invoiceSequence = Some(2))
      Customer.update(customerId, newCustomer)
      val updatedCustomer = Customer.findById(customerId).get
      updatedCustomer.invoiceSequence should equal (Some(2))
    }
  }
  
  "A Customer" should "increment invoiceSequence on get" in {
    running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val customerId = Customer.save(testCustomer).get
      val customer = Customer.findById(customerId).get
      val seq = customer.getAndIncrementSequence
      seq should equal (Some(1))
      val customerWithIncrementedSeq = Customer.findById(customerId).get
      customerWithIncrementedSeq.invoiceSequence should equal (Some(2))
    }
  }
  
}