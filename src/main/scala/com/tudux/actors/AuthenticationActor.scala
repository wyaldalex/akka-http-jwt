package com.tudux.actors

import akka.actor.ActorLogging
import akka.persistence.PersistentActor

class AuthenticationActor extends PersistentActor with ActorLogging {

  override def persistenceId: String = "auth-actor"

  override def receiveCommand: Receive = ???

  override def receiveRecover: Receive = ???

}
