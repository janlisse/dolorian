package models

import anorm.SqlParser._
import anorm._
import play.api.Play.current
import play.api.db.DB

case class Template(id: Option[Long], name: String, key: String)

object Template  {

  val MIME_TYPE = "application/vnd.oasis.opendocument.text"
  
  val templateParser = {
    get[Long]("id") ~
      get[String]("name") ~
      get[String]("key") map {
        case (id ~ name ~ key) => Template(Some(id), name, key)
      }
  }
  
  def save(template: Template) = {
    DB.withConnection {
      implicit c =>
        SQL("insert into template(name, key) values ({name},{key})")
          .on("name" -> template.name,
            "key" -> template.key).executeInsert()
    }
  }

  def delete(id: Long) {
    DB.withConnection {
      implicit connection =>
        SQL("DELETE FROM template where id = {id}").on('id -> id).executeUpdate
    }
  }

  def getAll = {
    DB.withConnection {
      implicit c =>
        SQL("Select * from template") as (templateParser *)
    }
  }

  def findById(id: Long) = {
    DB.withConnection {
      implicit c =>
        SQL("Select * from template t where t.id = {id}").on("id" -> id) as templateParser.single
    }
  }

  def options: Seq[(String, String)] = {
    getAll map {
      c => c.id.toString -> c.name
    }
  }
}