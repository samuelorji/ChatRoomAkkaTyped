package chatroom

import akka.NotUsed
import akka.actor.typed.{ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import chatroom.clients.Client
import chatroom.clients.Client.Quit
import chatroom.master.ChatRoomMaster

object Main {

  def apply(): Behavior[NotUsed] = Behaviors.setup{ ctx =>
    //spawn client and master

    val chatRoomMaster = ctx.spawn(ChatRoomMaster(),"Master")
    val client = ctx.spawn(Client(chatRoomMaster),"Client")

    ctx.watch(client)

    //client ! Quit

    Behaviors.receiveSignal{
      case (_, Terminated(_)) =>


        Behaviors.same
      }

    }


  def main(args: Array[String]): Unit = {
    ActorSystem(Main(),"Start")
  }

}
