package models

import java.net.URL
import org.joda.time.{ Period, DateTime }
import anorm._
import play.api.db.DB
import models.AnormExtension._
import anorm.SqlParser._
import play.api.Play.current
import scala.reflect._
import org.joda.time.MutableDateTime

case class WorkItem(id: anorm.Pk[Long], projectId: Long, startTime: DateTime, endTime: DateTime, breakTime: Int, description: String) {

  def totalTime: Period = {
    val endTimeMinusBreak = endTime.minusMinutes(breakTime)
    new Period(startTime, endTimeMinusBreak)
  }

  val totalTimeFormatted = {
    val total = totalTime
    val hours = total.getHours
    val minutes = total.getMinutes
    f"$hours%02d:$minutes%02d"
  }

  val date = startTime.toString("dd.MM.YYYY")
}

object WorkItem {

  val workItemParser = {
    get[Pk[Long]]("id") ~
      get[Long]("project_id") ~
      get[DateTime]("start_time") ~
      get[DateTime]("end_time") ~
      get[Int]("break_time") ~
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
        val select = SQL("Select * from work_item order by start_time DESC")
        select.as(workItemParser *)
    }
  }

  def getByProject(projectId: Long): List[WorkItem] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("Select * from work_item w where w.project_id = {projectId} order by start_time DESC")
        select.on("projectId" -> projectId).as(workItemParser *)
    }
  }

  def getByProjectMonthAndYear(projectId: Long, month: Int, year: Int): List[WorkItem] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("""
          SELECT * FROM work_item w WHERE w.project_id = {projectId}
          AND EXTRACT(month FROM w.start_time) = {month}
          AND EXTRACT(year FROM w.start_time) = {year}
          order by w.start_time DESC""")
        select.on("projectId" -> projectId, "month" -> month, "year" -> year).as(workItemParser *)
    }
  }

  def groupedByProjectId: Map[Long, List[WorkItem]] = {
    getAll.groupBy(_.projectId)
  }
  
  def getByRange(range: (DateTime,DateTime)): List[WorkItem] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("""
          SELECT * FROM work_item w WHERE w.start_time >= {start}
          AND w.end_time < {end} order by w.start_time DESC""")
        select.on("start" -> range._1,"end" -> range._2).as(workItemParser *)
    }
  }

  def delete(id: Long) {
    DB.withConnection {
      implicit connection =>
        SQL("DELETE FROM work_item where id = {id}").
          on('id -> id).executeUpdate
    }
  }

  def save(workItem: WorkItem): Option[Long] = {
    DB.withConnection {
      implicit c =>
        SQL("insert into work_item(project_id, start_time,end_time,break_time,description) values ({projectId},{startTime},{endTime},{breakTime},{description})")
          .on("projectId" -> workItem.projectId,
            "startTime" -> workItem.startTime,
            "endTime" -> workItem.endTime,
            "breakTime" -> workItem.breakTime,
            "description" -> workItem.description).executeInsert()
    }
  }

  def totalHours(workItems: List[WorkItem]) = {
    var hoursTotal: Int = 0
    var minutesTotal: Int = 0
    for (workItem <- workItems) {
      val hours = workItem.totalTime.getHours
      hoursTotal += hours
      val minutes = workItem.totalTime.getMinutes
      minutesTotal += minutes
    }
    hoursTotal += minutesTotal / 60
    minutesTotal %= 60
    f"$hoursTotal%02d:$minutesTotal%02d"
  }

}

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}


case class SearchQuery()

object SearchQuery {

 
}

