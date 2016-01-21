package edu.nyu.dlts.mdi.extract

import akka.actor.{ ActorSystem, Props }
import edu.nyu.dlts.mdi.extract.actors._

object Main extends App {
	// initialize the actor system and log
	val system = ActorSystem("File-Extract")	

	//initialize supervisor
	val supervisor = system.actorOf(Props[Supervisor], "supervisor")
}
