package controllers

import play.api.mvc.Controller
import models.Customer
import play.api.data.Form
import play.api.data.Forms._
import anorm.NotAssigned
import models.Address

object Customers extends Controller with Secured {

  val customerForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: anorm.Pk[Long]),
      "name" -> nonEmptyText,
      "shortName" -> nonEmptyText(3, 3),
      "address" -> mapping(
        "street" -> nonEmptyText,
        "streetNumber" -> nonEmptyText,
        "city" -> nonEmptyText,
        "zipCode" -> nonEmptyText)(Address.apply)(Address.unapply),
      "invoiceSequence" -> optional(number))(Customer.apply)(Customer.unapply))

  def list = withAuth {
    username =>
      implicit request =>
        Ok(views.html.customerList(Customer.getAll))
  }

  def create = withAuth {
    username =>
      implicit request =>
        Ok(views.html.customerCreate(customerForm))
  }

  def save = withAuth {
    username =>
      implicit request =>
        customerForm.bindFromRequest.fold(
          errors => BadRequest(views.html.customerCreate(errors)),
          customer => {
            Customer.save(customer)
            Redirect(routes.Customers.list)
          })
  }

  def edit(id: Long) = withAuth {
    username =>
      implicit request =>
        Customer.findById(id).map { customer =>
          Ok(views.html.customerEdit(id, customerForm.fill(customer)))
        }.getOrElse(NotFound)
  }

  def update(id: Long) = withAuth {
    username =>
      implicit request =>
        customerForm.bindFromRequest.fold(
          errors => BadRequest(views.html.customerEdit(id, errors)),
          customer => {
            Customer.update(id, customer)
            Redirect(routes.Customers.list).flashing("success" -> "Kunde %s erfolgreich geändert".format(customer.name))
          })
  }

  def delete(id: Long) = withAuth {
    username =>
      implicit request =>
        Customer.delete(id)
        Redirect(routes.Projects.list).flashing("success" -> "Kunde erfolgreich gelöscht!")
  }

}