package models

import java.net.URL
import org.joda.time._
import anorm._
import play.api.db.DB
import models.AnormExtension._
import anorm.SqlParser._
import play.api.Play.current
import scala.reflect._
import play.api.Play
import play.api.i18n.Messages
import org.joda.time.format.{DateTimeFormatterBuilder, PeriodFormatterBuilder}

trait WorkItem extends TimeSupport {

  def id: anorm.Pk[Long]
  def duration: Period
  def date: LocalDate
  def description: String
  def projectId: Long
  val durationFormatted = duration.toString(timeParser)
  val dateFormatted = date.toString("dd.MM.YYYY")

}

case class SimpleWorkItem(id: anorm.Pk[Long], projectId: Long,
  date: LocalDate, duration: Period, description: String) extends WorkItem {

  import SimpleWorkItem._

  def roundedDuration = {
    require(roundingFactor > 1 || 60 % roundingFactor != 0, "RoundingFactor must be a fraction of 60")
    val mod = duration.getMinutes % roundingFactor
    duration.withSeconds(0).plusMinutes(if (mod < roundingFactor / 2) -mod else (roundingFactor - mod)).normalizedStandard
  }

}

case class DetailedWorkItem(id: anorm.Pk[Long], projectId: Long, startTime: DateTime,
  endTime: DateTime, breakTime: Option[Int] = Some(0), date: LocalDate,
  description: String) extends WorkItem {

  def duration: Period = {
    val endTimeMinusBreak = endTime.minusMinutes(breakTime.getOrElse(0))
    new Period(startTime, endTimeMinusBreak).normalizedStandard()
  }

  val startTimeFormatted = startTime.toString("HH:mm")
  val endTimeFormatted = endTime.toString("HH:mm")
  val breakTimeFormatted = Duration.standardMinutes(breakTime.getOrElse(0): Int).toPeriod(PeriodType.time()).toString(timeParser)

}

object SimpleWorkItem extends TimeSupport {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._
  import play.api.libs.json.Reads._
  import play.api.data.validation.ValidationError
  import org.joda.time.format.PeriodFormatterBuilder

  val roundingFactor = Play.application.configuration.getInt("workitem.duration.roundingFactor").getOrElse(15)

  implicit val jodaPeriodReads: Reads[Period] = new Reads[Period] {

    def reads(json: JsValue): JsResult[Period] = {
      json match {
        case JsString(periodString) => parsePeriod(periodString) match {
          case Some(period) => {
            if (period.toStandardMinutes().getMinutes() < roundingFactor / 2) {
              JsError(Seq(JsPath() -> Seq(ValidationError("validate.error.duration.min.length", SimpleWorkItem.roundingFactor / 2))))
            } else JsSuccess(period)
          }
          case None => JsError(Seq(JsPath() -> Seq(ValidationError("validate.error.duration.format"))))
        }
        case _ => JsError(Seq(JsPath() -> Seq(ValidationError("validate.error.duration"))))
      }
    }
  }

  implicit val simpleWorkItemReads: Reads[SimpleWorkItem] = (
    (JsPath \ "projectId").read[Long] and
    (JsPath \ "date").read[LocalDate] and
    (JsPath \ "duration").read[org.joda.time.Period] and
    (JsPath \ "description").read[String](minLength[String](3)))((projectId, date, duration, description) => SimpleWorkItem(NotAssigned, projectId, date, duration, description))

}

object WorkItem extends TimeSupport {

  val workItemParser = {
    get[Pk[Long]]("id") ~
      get[Long]("project_id") ~
      get[Option[DateTime]]("start_time") ~
      get[Option[DateTime]]("end_time") ~
      get[Option[Int]]("break_time") ~
      get[Option[Long]]("duration") ~
      get[LocalDate]("date") ~
      get[String]("description") map {
        case (id ~ projectId ~
          Some(startTime) ~ Some(endTime) ~ breakTime ~
          None ~ date ~ description) => DetailedWorkItem(id, projectId, startTime, endTime, breakTime, date, description)
      }
  }

  val projectWorkItemMap = {
    get[Long]("projectId") ~ workItemParser
  }

  def getAll: List[WorkItem] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("Select * from work_item order by date DESC")
        select.as(workItemParser *)
    }
  }

  def getByProject(projectId: Long): List[WorkItem] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("Select * from work_item w where w.project_id = {projectId} order by date DESC")
        select.on("projectId" -> projectId).as(workItemParser *)
    }
  }

  def getByProjectMonthAndYear(projectId: Long, month: Int, year: Int): List[WorkItem] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("""
          SELECT * FROM work_item w WHERE w.project_id = {projectId}
          AND EXTRACT(month FROM w.date) = {month}
          AND EXTRACT(year FROM w.date) = {year}
          order by w.date DESC""")
        select.on("projectId" -> projectId, "month" -> month, "year" -> year).as(workItemParser *)
    }
  }

  def groupedByProjectId: Map[Long, List[WorkItem]] = {
    getAll.groupBy(_.projectId)
  }

  def getByRange(range: (DateTime, DateTime)): List[WorkItem] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("""
          SELECT * FROM work_item w WHERE w.date >= {start}
          AND w.date < {end} order by w.date DESC""")
        select.on("start" -> range._1, "end" -> range._2).as(workItemParser *)
    }
  }

  def delete(id: Long) {
    DB.withConnection {
      implicit connection =>
        SQL("DELETE FROM work_item where id = {id}").
          on('id -> id).executeUpdate
    }
  }
  
  def findById(id: Long): Option[DetailedWorkItem] = {
    DB.withConnection {
      implicit c =>
        SQL("Select * from work_item w where w.id = {id}").on("id" -> id) as workItemParser.singleOpt
    }
  }

  def save(workItem: DetailedWorkItem): Option[Long] = {
    DB.withConnection {
      implicit c =>
        SQL("insert into work_item(project_id, start_time,end_time,break_time, date, description) values ({projectId},{startTime},{endTime},{breakTime},{date},{description})")
          .on("projectId" -> workItem.projectId,
            "startTime" -> workItem.startTime,
            "endTime" -> workItem.endTime,
            "breakTime" -> workItem.breakTime,
            "date" -> workItem.startTime.toLocalDate(),
            "description" -> workItem.description).executeInsert()
    }
  }

  def save(workItem: SimpleWorkItem): Option[Long] = {
    DB.withConnection {
      implicit c =>
        SQL("insert into work_item(project_id, duration, date, description) values ({projectId},{duration},{date},{description})")
          .on("projectId" -> workItem.projectId,
            "duration" -> workItem.duration.toStandardDuration().getMillis(),
            "date" -> workItem.date,
            "description" -> workItem.description).executeInsert()
    }
  }

  def update(id: Long, item: DetailedWorkItem) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update work_item
          set project_id = {project_id}, start_time = {start_time}, end_time = {end_time}, break_time = {break_time},
          date = {date}, description = {description}
          where id = {id}
        """).on(
          'id -> id,
          "project_id" -> item.projectId,
          "start_time" -> item.startTime,
          "end_time" -> item.endTime,
          "break_time" -> item.breakTime,
          "date" -> item.date,
          "description" -> item.description).executeUpdate()
    }
  }

  def totalHours(workItems: List[WorkItem]) = {
    def sum(workItems: List[WorkItem]): Period = workItems match {
      case x :: tail => x.duration.plus(sum(tail))
      case Nil => Period.ZERO
    }
    sum(workItems).normalizedStandard(PeriodType.time).toString(timeParser)
  }
}

trait TimeSupport {

  val timeParser = new PeriodFormatterBuilder()
    .printZeroAlways()
    .minimumPrintedDigits(2)
    .appendHours
    .appendSeparator(":")
    .printZeroAlways()
    .minimumPrintedDigits(2)
    .appendMinutes
    .toFormatter

  def parsePeriod(input: String): Option[Period] = {
    scala.util.control.Exception.allCatch[Period] opt (timeParser.parsePeriod(input).normalizedStandard())
  }
}
