package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.Routes

object Application extends Controller with Secured {

  def index = withUser {
    user =>
      implicit request =>
        Ok(views.html.index(user.name))
  }

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        WorkItems.submitSimpleWorkItem)).as("text/javascript")
  }

}
