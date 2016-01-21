package edu.nyu.dlts.mdi.extract

import java.util.UUID
import java.io.File

object Protocol {
  case class MetadataExtractRequest(uuid: UUID, path: File)
  case object Listen
}