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

  def totalTime(): Period = {
    val endTimeMinusBreak = endTime.minusMinutes(breakTime)
    new Period(startTime, endTimeMinusBreak)
  }
}

object Project {

  val projectParser = {
    get[Pk[Long]]("id") ~
      get[String]("name") ~
      get[Option[String]]("number") map {
      case (id ~ name ~ number) => {
        Project(id, name, number)
      }
    }
  }

  def getAll: List[Project] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("Select * from project")
        select.as(projectParser *)
    }
  }


  def save(project: Project): Option[Long] = {
    DB.withConnection {
      implicit c =>
        SQL("insert into project(name, number) values ({name},{number})")
          .on("name" -> project.name, "number" -> project.number).executeInsert()
    }
  }

  def delete(id: Long) {
    DB.withConnection {
      implicit connection =>
        SQL( """
          DELETE FROM project where id = {id}
             """).on(
          'id -> id
        ).executeUpdate
    }
  }

  def findById(id: Long): Project = {
    DB.withConnection {
      implicit c =>
        SQL("Select * from project p where p.id = {id}").on("id" -> id) as projectParser.single
    }
  }

}

object WorkItem {

  val SpreadsheetFeedUrl = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full")

  val workItemParser = {
    get[Pk[Long]]("id") ~
      get[Long]("projectId") ~
      get[DateTime]("startTime") ~
      get[DateTime]("endTime") ~
      get[Int]("breakTime") ~
      get[String]("description") map {
      case (id ~ projectId ~ startTime ~ endTime ~ breakTime ~ description) => WorkItem(id, projectId, startTime, endTime, breakTime, description)
    }
  }

  val projectWorkItemMap = {
    get[Long]("projectId") ~ workItemParser
  }

  def getAll: List[WorkItem] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("Select * from work_item order by startTime DESC")
        select.as(workItemParser *)
    }
  }

  def getByProject(projectId: Long): List[WorkItem] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("Select * from work_item w where w.projectId = {projectId} order by startTime DESC")
        select.on("projectId" -> projectId).as(workItemParser *)
    }
  }

  def groupedByProjectId: Map[Long, List[WorkItem]] = {
    getAll.groupBy(_.projectId)
  }

  def delete(id: Long) {
    DB.withConnection {
      implicit connection =>
        SQL( """
          DELETE FROM work_item where id = {id}
             """).on(
          'id -> id
        ).executeUpdate
    }
  }

  def save(workItem: WorkItem): Option[Long] = {
    DB.withConnection {
      implicit c =>
        SQL("insert into work_item(projectId, startTime,endTime,breakTime,description) values ({projectId},{startTime},{endTime},{breakTime},{description})")
          .on("projectId" -> workItem.projectId,
          "startTime" -> workItem.startTime,
          "endTime" -> workItem.endTime,
          "breakTime" -> workItem.breakTime,
          "description" -> workItem.description
        ).executeInsert()
    }
  }
}
