package com.tudux

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Boot {
  def main(args: Array[String]): Unit = {
    implicit val system       = ActorSystem()
    implicit val materializer = ActorMaterializer()

    system.actorOf(HttpApi.apply("localhost", 8000, null), HttpApi.Name)
  }
}
