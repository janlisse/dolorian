package models

import java.io.{File, InputStream}
import java.util.UUID

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{
  CannedAccessControlList,
  GetObjectRequest,
  ObjectMetadata,
  PutObjectRequest
}
import play.api.Play
import play.api.Play.current

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
    S3Configuration.amazonS3
      .getObject(new GetObjectRequest(S3Configuration.awsBucket, key))
      .getObjectContent
  }
}

object S3Configuration {
  val awsBucket = Play.application.configuration
    .getString("aws.s3.bucket")
    .getOrElse(throw new RuntimeException("aws.s3.bucket must be configured"))
  val amazonS3 = AmazonS3ClientBuilder
    .standard()
    .withCredentials(new DefaultAWSCredentialsProviderChain())
    .build()
}
