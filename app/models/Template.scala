package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import java.math.BigDecimal
import anorm.SqlParser._
import play.api.Play
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{ GetObjectRequest, ObjectMetadata, CannedAccessControlList, PutObjectRequest }
import java.util.UUID
import java.io.InputStream
import java.io.File

case class Template(id: Pk[Long], name: String, key: String) {
  
  def inputStream = Template.loadFromS3(key)
}

object Template extends S3Support {
  import play.api.Play.current

  val MIME_TYPE = "application/vnd.oasis.opendocument.text"

  val templateParser = {
    get[Pk[Long]]("id") ~
      get[String]("name") ~
      get[String]("key") map {
        case (id ~ name ~ key) => {
          Template(id, name, key)
        }
      }
  }

  def save(template: Template, file: File, fileName: String): Option[Long] = {
    val key = saveToS3(file, fileName)
    DB.withConnection {
      implicit c =>
        SQL("insert into template(name, key) values ({name},{key})")
          .on("name" -> template.name,
            "key" -> key).executeInsert()
    }
  }

  def delete(id: Long) {
    val template = Template.findById(id)
    deleteFromS3(template.key)
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

trait S3Support {

  import Play.current

  val bucket = Play.configuration.getString("aws.s3.bucket").get
  val secretKey = Play.configuration.getString("aws.secret.key").get
  val accessKey = Play.configuration.getString("aws.access.key").get

  val awsCredentials = new BasicAWSCredentials(accessKey, secretKey)
  val amazonS3 = new AmazonS3Client(awsCredentials)

  def saveToS3(file: File, fileName: String): String = {
    val key = UUID.randomUUID.toString + "/" + fileName
    val objectMetadata = new ObjectMetadata()
    objectMetadata.setContentType(Template.MIME_TYPE)

    val request = new PutObjectRequest(bucket, key, file)
    request.withCannedAcl(CannedAccessControlList.AuthenticatedRead)
    amazonS3.putObject(request)
    key
  }

  def deleteFromS3(key: String) = {
    amazonS3.deleteObject(bucket, key)
  }

  def loadFromS3(key: String): InputStream = {
    amazonS3.getObject(new GetObjectRequest(bucket, key)).getObjectContent
  }

}