package controllers

import play.api.mvc.Controller

object Application extends Controller with Secured {

  def index = withUser {
    user =>
      implicit request =>
        Ok(views.html.index(user.name))
  }

}
