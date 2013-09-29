package models

case class Address(street: String, streetNumber: String, zipCode: String, city: String)
case class Customer(id: anorm.Pk[Long], name: String, address: Address)


