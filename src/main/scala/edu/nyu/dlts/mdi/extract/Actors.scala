package edu.nyu.dlts.mdi.extract.actors

import akka.actor.{ Actor, ActorRef, Props, PoisonPill }
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.apache.tika.Tika
import scala.concurrent.duration._
import scala.language.postfixOps

import edu.nyu.dlts.mdi.extract.{ CommonUtils, AMQPSupport }
import edu.nyu.dlts.mdi.extract.Protocol._

trait AMQPConfiguration { 
  val conf = ConfigFactory.load() 
}

class Supervisor() extends Actor {
  
  implicit val timeout = new Timeout(5 seconds)
  val tika = new Tika

  val consumerProps = Props(new Consumer(self))
  val consumer = context.actorOf(consumerProps, "Consumer")

  consumer ! Listen

  val publisherProps = Props(new Publisher(self))
  val publisher = context.actorOf(publisherProps, "Publisher")

  def receive = {
  	case pub: Publish => publisher ! pub
  	case mer: MetadataExtractRequest => {
  		val metadataExtractorProps = Props(new MetadataExtractor(self, tika))
  		val metadataExtractor = context.actorOf(metadataExtractorProps, "MetadataExtractor")
  		metadataExtractor ! mer
  	}

  	case _ =>
  }
}

class MetadataExtractor(supervisor: ActorRef, tika: Tika) extends Actor with CommonUtils {
	import java.io.FileInputStream
	import org.apache.tika.metadata.Metadata
	import org.apache.tika.parser.ParseContext
	import org.xml.sax.helpers.DefaultHandler
	import scala.collection.immutable.ListMap

  def receive = {
  	case mer: MetadataExtractRequest => {
  		var response = createNewResponse(mer.id)
  		val metadata = new Metadata
  		val parser = tika.getParser
  		parser.parse(new FileInputStream(mer.file), new DefaultHandler, metadata, new ParseContext)
  		var map = Map.empty[String, String]
  		metadata.names.foreach { i => map += (i -> metadata.get(i)) }
  		map = ListMap(map.toSeq.sortBy(_._1):_*)
  		response = response.copy(outcome = Some("success"), end_time = Some(now()), data = Some(render(map).asInstanceOf[JObject]))
  		supervisor ! Publish(convertResponseToJson(response))
  	}

  	case _ =>
  }
}

class Consumer(supervisor: ActorRef) extends Actor with AMQPSupport with AMQPConfiguration {
  
  import java.io.File
  import java.util.UUID


  val consumer = getConsumer(conf.getString("rabbitmq.host"), conf.getString("rabbitmq.exchange_name"), conf.getString("rabbitmq.consume_request_key"))

  implicit val formats = DefaultFormats

  def receive = {

  	case Listen => {
 	  val delivery = consumer.nextDelivery()
      val message = new String(delivery.getBody())
      val json = parse(message)
      val request_id = UUID.fromString((json \ "request_id").extract[String])
      val request_path = ((json \ "params") \ "request_path").extract[String]
 
 	    //do something with a message
      supervisor ! new MetadataExtractRequest(request_id, new File(request_path))
	    
      self ! Listen 
  	}

  	case _ => 
  }
}

class Publisher(supervisor: ActorRef) extends Actor with AMQPSupport with AMQPConfiguration {
  val publisher = getPublisher(conf.getString("rabbitmq.host"))
  implicit val formats = DefaultFormats

  def receive = {	
  	case p: Publish => {
  		publisher.basicPublish(conf.getString("rabbitmq.exchange_name"), conf.getString("rabbitmq.publish_result_key"), null, p.message.getBytes())
  	}
  	case _ => 
  }
}