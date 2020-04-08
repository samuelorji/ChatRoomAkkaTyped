package chatroom

import akka.NotUsed
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import chatroom.clients.Client
import chatroom.clients.Client.{ChatRoom, Football, FootballMessage, MasterMessages, PostMessage, Quit}
import chatroom.master.ChatRoomMaster

object Main {

  def apply(): Behavior[NotUsed] = Behaviors.setup{ ctx =>
    //spawn client and master

    val chatRoomMaster = ctx.spawn(ChatRoomMaster(),"Master")
    //val client = ctx.spawn(Client(chatRoomMaster),"Client")
    val client = new ClientTests(chatRoomMaster,Football,"Samuel",ctx)



    //Thread.sleep(100)

    client.sendToRoom(FootballMessage("Hello from here"))
    client.sendToRoom(FootballMessage("Hello from here 2"))
    client.sendToRoom(FootballMessage("Hello from here 3"))
    client.sendToRoom(FootballMessage("Hello from here 4 "))
    client.sendToRoom(FootballMessage("Hello from here 5"))

    client.disconnect
    client.sendToRoom(FootballMessage("Hello from here 6"))

    //ctx.watch(client)


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

class ClientTests(chatRoomMaster : ActorRef[MasterMessages], connectTo : ChatRoom,screenName : String,context : ActorContext[NotUsed])
extends Client(chatRoomMaster,connectTo,screenName,context){
  override def onNotificationReceived(from: String, message: String): Unit =
    println(s"message receoved from $from, message is $message")
}


