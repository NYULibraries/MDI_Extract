package edu.nyu.dlts.mdi.extract

import java.util.UUID
import java.io.File
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

object Protocol {
  case class MetadataExtractRequest(id: UUID, file: File)
  case object Listen
  case class Publish(message: String)

  
  case class Agent(
  	agent: String,
  	version: String,
  	host: String
  )

  case class Response(
  	version: String,
  	request_id: UUID,
  	outcome: Option[String],
  	start_time: String,
  	end_time: Option[String],
  	agent: Agent,
  	data: Option[JObject]
  )
}