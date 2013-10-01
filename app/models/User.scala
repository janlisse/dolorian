package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class User(name: String, email: String, password: String)


object User {


  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("name") ~
      get[String]("email") ~
      get[String]("password") map {
      case name ~ email ~ password => User(name, email, password)
    }
  }

  /**
   * Retrieve all users.
   */
  def findAll: Seq[User] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from app_user").as(User.simple *)
    }
  }


  /**
   * Authenticate a User.
   */
  def authenticate(email: String, password: String): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from app_user where email = {email} and password = {password}").
          on(
          'email -> email,
          'password -> password).as(User.simple.singleOpt)
    }
  }

  def findOneByUsername(username: String): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from app_user where email = {username}").
          on('username -> username).as(User.simple.singleOpt)
    }
  }

  /**
   * Create a User.
   */
  def create(user: User): User = {
    DB.withConnection {
      implicit connection =>
        SQL("insert into app_user values ({name},{email},{password})").on(
          'name -> user.name,
          'email -> user.email,
          'password -> user.password).executeUpdate()
        user
    }
  }
}