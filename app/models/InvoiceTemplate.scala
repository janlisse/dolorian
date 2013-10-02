package models

import anorm._
import play.api.db.DB
import java.math.BigDecimal
import anorm.SqlParser._
import anorm.~
import play.api.Play
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{ObjectMetadata, CannedAccessControlList, PutObjectRequest}
import java.util.UUID
import java.io.File


case class InvoiceTemplate(id: Pk[Long], templateFileKey: Option[String], projectId: Long, hourlyRate: BigDecimal, invoiceNumber: String)


object InvoiceTemplate extends S3Support {

  import play.api.Play.current

  val invoiceTemplateParser = {
    get[Pk[Long]]("id") ~
      get[Option[String]]("templateFile") ~
      get[Long]("projectId") ~
      get[BigDecimal]("hourlyRate") ~
      get[String]("invoiceNumber") map {
      case (id ~ templateFileKey ~ projectId ~ hourlyRate ~ invoiceNumber) => {
        InvoiceTemplate(id, templateFileKey, projectId, hourlyRate, invoiceNumber)
      }
    }
  }

  def save(invoiceTemplate: InvoiceTemplate, file: File): Option[Long] = {
    val key = saveToS3(file)
    DB.withConnection {
      implicit c =>
        SQL("insert into invoice_template(templateFile, projectId, hourlyRate, invoiceNumber) values ({templateFile},{projectId},{hourlyRate},{invoiceNumber})")
          .on("templateFile" -> key,
          "projectId" -> invoiceTemplate.projectId,
          "hourlyRate" -> invoiceTemplate.hourlyRate,
          "invoiceNumber" -> invoiceTemplate.invoiceNumber
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
}

trait S3Support {

  import Play.current

  val bucket = Play.configuration.getString("aws.s3.bucket").get
  val secretKey = Play.configuration.getString("aws.secret.key").get
  val accessKey = Play.configuration.getString("aws.access.key").get

  val awsCredentials = new BasicAWSCredentials(accessKey, secretKey)
  val amazonS3 = new AmazonS3Client(awsCredentials)


  def saveToS3(file: File): String = {
    val key = UUID.randomUUID.toString + ".odt"
    val objectMetadata = new ObjectMetadata()
    objectMetadata.setContentType("application/vnd.oasis.opendocument.text")

    val request = new PutObjectRequest(bucket, key, file)
    request.withCannedAcl(CannedAccessControlList.AuthenticatedRead)
    amazonS3.putObject(request)
    key
  }

  def deleteFromS3(key: String) {
    amazonS3.deleteObject(bucket, key)
  }

}