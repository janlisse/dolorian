package controllers

import play.api.mvc.Controller
import models.InvoiceTemplate

object Application extends Controller with Secured {

  def index = withUser {
    user =>
      implicit request =>
        Ok(views.html.index(user.name))
  }

}
