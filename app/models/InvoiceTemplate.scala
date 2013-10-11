package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import java.math.BigDecimal
import anorm.SqlParser._
import anorm.~
import play.api.Play
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{GetObjectRequest, ObjectMetadata, CannedAccessControlList, PutObjectRequest}
import java.util.UUID
import java.io.InputStream
import java.io.File

case class InvoiceTemplate(id: Pk[Long], templateFileKey: Option[String], projectId: Long, hourlyRate: BigDecimal) {

  lazy val project: Project = DB.withConnection { implicit connection =>
    SQL("SELECT * FROM project p WHERE p.id = {id}").on(
      'id -> projectId).as(Project.projectParser.single)
  } 
  
  def inputStream = InvoiceTemplate.loadFromS3(templateFileKey.get)
}


object InvoiceTemplate extends S3Support {

  import play.api.Play.current

  val MIME_TYPE = "application/vnd.oasis.opendocument.text"

  val invoiceTemplateParser = {
    get[Pk[Long]]("id") ~
      get[Option[String]]("template_file") ~
      get[Long]("project_id") ~
      get[BigDecimal]("hourly_rate") map {
      case (id ~ templateFileKey ~ projectId ~ hourlyRate) => {
        InvoiceTemplate(id, templateFileKey, projectId, hourlyRate)
      }
    }
  }

  def save(invoiceTemplate: InvoiceTemplate, file: File, fileName: String): Option[Long] = {
    val key = saveToS3(file, fileName)
    DB.withConnection {
      implicit c =>
        SQL("insert into invoice_template(template_file, project_id, hourly_rate) values ({templateFile},{projectId},{hourlyRate})")
          .on("templateFile" -> key,
          "projectId" -> invoiceTemplate.projectId,
          "hourlyRate" -> invoiceTemplate.hourlyRate
        ).executeInsert()
    }
  }

  def delete(id: Long) {
    val template = InvoiceTemplate.findById(id)
    template.templateFileKey map {
      key => deleteFromS3(key)
    }
    DB.withConnection {
      implicit connection =>
        SQL( """
          DELETE FROM invoice_template where id = {id}
             """).on(
          'id -> id
        ).executeUpdate
    }
  }

  def getAll: List[InvoiceTemplate] = {
    DB.withConnection {
      implicit c =>
        SQL("Select * from invoice_template") as (invoiceTemplateParser *)
    }
  }

  def findById(id: Long): InvoiceTemplate = {
    DB.withConnection {
      implicit c =>
        SQL("Select * from invoice_template t where t.id = {id}").on("id" -> id) as invoiceTemplateParser.single
    }
  }

  def options: Seq[(String,String)]  = {
    getAll map {
      c => c.id.toString -> c.templateFileKey.getOrElse("-")
    }
  }


}

trait S3Support {

  import Play.current

  val bucket = Play.configuration.getString("aws.s3.bucket").get
  val secretKey = Play.configuration.getString("aws.secret.key").get
  val accessKey = Play.configuration.getString("aws.access.key").get

  val awsCredentials = new BasicAWSCredentials(accessKey, secretKey)
  val amazonS3 = new AmazonS3Client(awsCredentials)


  def saveToS3(file: File, fileName: String): String = {
    val key = UUID.randomUUID.toString +"/"+fileName
    val objectMetadata = new ObjectMetadata()
    objectMetadata.setContentType(InvoiceTemplate.MIME_TYPE)

    val request = new PutObjectRequest(bucket, key, file)
    request.withCannedAcl(CannedAccessControlList.AuthenticatedRead)
    amazonS3.putObject(request)
    key
  }

  def deleteFromS3(key: String) = {
    amazonS3.deleteObject(bucket, key)
  }

  def loadFromS3(key: String) : InputStream = {
    amazonS3.getObject(new GetObjectRequest(bucket, key)).getObjectContent
  }

}