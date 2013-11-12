package models

import java.io.File
import java.io.InputStream
import java.util.UUID
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.CannedAccessControlList
import play.api.Play
import com.amazonaws.auth.BasicAWSCredentials
import play.api.Play.current
import com.amazonaws.services.s3.AmazonS3Client

trait TemplateStorage {
  def save(file: File, fileName: String, mimeType: String): String
  def delete(key: String)
  def load(key: String): InputStream
}

class S3TemplateStorage extends TemplateStorage {

  def save(file: File, fileName: String, mimeType: String): String = {
    val key = UUID.randomUUID.toString + "/" + fileName
    val objectMetadata = new ObjectMetadata
    objectMetadata.setContentType(mimeType)

    val request = new PutObjectRequest(S3Configuration.awsBucket, key, file)
    request.withCannedAcl(CannedAccessControlList.AuthenticatedRead)
    S3Configuration.amazonS3.putObject(request)
    key
  }

  def delete(key: String) = {
    S3Configuration.amazonS3.deleteObject(S3Configuration.awsBucket, key)
  }

  def load(key: String): InputStream = {
    S3Configuration.amazonS3.getObject(new GetObjectRequest(S3Configuration.awsBucket, key)).getObjectContent
  }
}

object S3Configuration {
  val awsBucket = Play.application.configuration.getString("aws.s3.bucket").getOrElse(throw new RuntimeException("aws.s3.bucket must be configured"))
  val awsAccessKey = Play.application.configuration.getString("aws.access.key").getOrElse(throw new RuntimeException("aws.access.key must be configured"))
  val awsSecretKey = Play.application.configuration.getString("aws.secret.key").getOrElse(throw new RuntimeException("aws.secret.key must be configured"))
  val awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey)
  val amazonS3 = new AmazonS3Client(awsCredentials)
}
