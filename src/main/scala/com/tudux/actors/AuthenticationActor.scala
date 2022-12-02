package com.tudux.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor

sealed trait AuthRole
object AuthRoles {
  case object GeneralUser extends AuthRole
  case object Admin extends AuthRole
}

sealed trait AuthActorCommand
object AuthActorCommand{
  case class CreateUser(user: String, password: String, role: AuthRole) extends AuthActorCommand
}


object AuthenticationActor {
  def props: Props = Props(new AuthenticationActor)
}

class AuthenticationActor extends PersistentActor with ActorLogging {

  import AuthActorCommand._

  override def persistenceId: String = "auth-actor"

  override def receiveCommand: Receive = {
    case createUser@CreateUser(user,password,role) =>
      log.info(s"message received $createUser")
  }

  override def receiveRecover: Receive = {
    case _ =>
      log.info("Pending implementation")
  }

}
