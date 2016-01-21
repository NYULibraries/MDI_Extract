package edu.nyu.dlts.mdi.extract.actors

import akka.actor.{ Actor, ActorRef, Props, PoisonPill }
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config._
import org.apache.tika.Tika
import scala.concurrent.duration._
import scala.language.postfixOps

import edu.nyu.dlts.mdi.extract.AMQPSupport
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

  def receive = {

  	case _ =>
  }
}


class Consumer(supervisor: ActorRef) extends Actor with AMQPSupport with AMQPConfiguration {
  
  import java.io.File
  import java.util.UUID
  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  val consumer = getConsumer(conf.getString("rabbitmq.host"), conf.getString("rabbitmq.exchange_name"), conf.getString("rabbitmq.consume_key"))

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