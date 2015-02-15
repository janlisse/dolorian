package models

import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.test.Helpers._
import play.api.test._

class CustomerSpec extends PlaySpec with OneAppPerSuite {

  val testCustomer = Customer("exampleTech GmbH", "foo", Address("Brunnenstr.", "3", "45701", "Entenhausen"))

  implicit override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = inMemoryDatabase())

  "Customer should be savable" in  {
    val customerId = Customer.save(testCustomer)
    customerId must not equal None
  }

  "Customer should be updatable" in  {
    val customerId = Customer.save(testCustomer).get
    val customer = Customer.findById(customerId).get
    val newCustomer = customer.copy(shortName = "bar")
    Customer.update(customerId, newCustomer)
    val updatedCustomer = Customer.findById(customerId).get
    updatedCustomer.shortName must equal("bar")
  }


}