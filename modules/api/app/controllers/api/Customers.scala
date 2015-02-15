package controllers.api

import models.Customer
import play.api.libs.json.{JsError, Json}
import play.api.mvc.Controller

object Customers extends Controller {

  def list = Authenticated {
      val json = Json.toJson(Customer.getAll)
      Ok(json)
  }

  def save = Authenticated(parse.json) {
    request =>
      val customerResult = request.body.validate[Customer]
      customerResult.fold(
        errors => {
          BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors)))
        },
        customer => {
          Customer.save(customer)
          Ok(Json.obj("status" -> "OK", "message" -> ("Customer '" + customer.name + "' saved.")))
        }
      )
  }
}
