package models

import java.net.URL
import org.joda.time.{Period, DateTime}


import anorm._
import play.api.db.DB
import models.AnormExtension._
import anorm.SqlParser._
import play.api.Play.current

case class Project(id: anorm.Pk[Long], name: String, number: Option[String] = None)

case class WorkItem(id: anorm.Pk[Long], projectId: Long, startTime: DateTime, endTime: DateTime, breakTime: Int, description: String) {

  def totalTime() : Period = {
    val endTimeMinusBreak = endTime.minusMinutes(breakTime)
    new Period(startTime, endTimeMinusBreak)
  }
}

object Project {

  val projectParser = {
    get[Pk[Long]]("id")~
      get[String]("name")~
      get[Option[String]]("number") map {
      case (id~name~number) => {
        Project(id, name, number)
      }
    }
  }

  def getAll(): List[Project] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("Select * from project")
        select.as(projectParser *)
    }
  }

  def save(project: Project) : Option[Long] = {
    DB.withConnection {
      implicit c =>
        SQL("insert into project(name, number) values ({name},{number})")
          .on("name" -> project.name, "number" -> project.number).executeInsert()
    }
  }

  def delete(id: Long) {
    DB.withConnection { implicit connection =>
      SQL("""
          DELETE FROM project where id = {id}
          """).on(
        'id -> id
      ).executeUpdate
    }
  }

}

object WorkItem {

  val SpreadsheetFeedUrl = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full")


  def getAll(): List[WorkItem] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("Select * from work")
        select().map(row =>
          WorkItem(row[anorm.Pk[Long]]("id"), row[Long]("projectId"), row[DateTime]("startTime"), row[DateTime]("endTime"), row[Int]("breakTime"), row[String]("description"))
        ).toList
    }
  }

  def delete(id: Long) {
    DB.withConnection { implicit connection =>
      SQL("""
          DELETE FROM work where id = {id}
          """).on(
        'id -> id
      ).executeUpdate
    }
  }

  def save(workItem: WorkItem) : Option[Long] = {
    DB.withConnection {
      implicit c =>
        SQL("insert into work(projectId, startTime,endTime,breakTime,description) values ({projectId},{startTime},{endTime},{breakTime},{description},)")
          .on("projectId" -> workItem.projectId,
          "startTime" -> workItem.startTime,
          "endTime" -> workItem.endTime,
          "breakTime" -> workItem.breakTime,
          "description" -> workItem.description
        ).executeInsert()
    }
  }
}
