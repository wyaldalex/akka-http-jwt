package com.tudux.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor

import scala.util.{Failure, Success}

sealed trait AuthRole
object AuthRoles {
  case object GeneralUser extends AuthRole
  case object Admin extends AuthRole
}

case class User(user: String, password: String, role: AuthRole)

sealed trait AuthActorCommand
object AuthActorCommand{
  case class CreateUser(user: User) extends AuthActorCommand
  case class CreateSuperUser(user: User) extends AuthActorCommand
  case class ValidateUser(user: String, password: String) extends AuthActorCommand
}

sealed trait AuthActorEvent
object AuthActorEvent {
  case class UserCreatedEvent(user: User) extends AuthActorEvent
}

sealed trait AuthActorResponse
object AuthActorResponse {
  case class ValidUser()
}

//TODO: Password Encryption
object AuthenticationActor {
  def props: Props = Props(new AuthenticationActor)
}

class AuthenticationActor extends PersistentActor with ActorLogging {

  import AuthActorCommand._
  import AuthActorEvent._

  var state = Map[String,User]().empty

  override def persistenceId: String = "auth-actor"

  override def receiveCommand: Receive = {
    case CreateUser(user) =>
      log.info(s"message received $user")
    case CreateSuperUser(user) =>
      if(!state.contains(user.user)) {
        log.info(s"Received CreateSuperUser $user")
        persist(UserCreatedEvent(user)) { e =>
          state = state + (user.user -> user)
          log.info(s"Modified State + $state")
        }
      } else {
        log.info(s"Super user already created")
      }
    case ValidateUser(user,password) =>
      log.info("Validating user")
      if(state.contains(user) && state(user).password == password) sender() ! Right("Authorized")
      else sender() ! Left("Unauthorized")
  }

  override def receiveRecover: Receive = {
    case UserCreatedEvent(user) =>
      state = state + (user.user -> user)
      log.info(s"Recovered State + $state")
    case _ =>
      log.info("Pending implementation")
  }

}
