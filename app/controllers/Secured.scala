package controllers

import play.api.mvc._
import models.User

/**
 * Provide security features
 */
trait Secured {

  /**
   * Retrieve the connected user's email
   */
  def username(request: RequestHeader) = request.session.get("email")

  /**
   * Not authorized, forward to login
   */
  private def onUnauthorized(request: RequestHeader) = {
    Results.Redirect(routes.Authentication.login)
  }

  /**
   * Action for authenticated users.
   */
  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  def withUser(f: User => Request[AnyContent] => Result) = withAuth { username => implicit request =>
    User.findOneByUsername(username).map { user =>
      f(user)(request)
    }.getOrElse(onUnauthorized(request))
  }
}
