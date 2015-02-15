package models

import anorm.SqlParser._
import anorm._
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json.{JsValue, Json}


case class Template(id: Option[Long], name: String, content: JsValue)

object Template  {

  val templateParser = {
    get[Long]("id") ~
      get[String]("name") ~
      get[String]("content") map {
        case (id ~ name ~ content) => Template(Some(id), name, Json.parse(content))
      }
  }
  
  def save(template: Template): Option[Long] = {
    DB.withConnection {
      implicit c =>
        SQL("insert into template(name, content) values ({name},{content})")
          .on("name" -> template.name,
            "content" -> Json.stringify(template.content)).executeInsert()
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