package chatroom

import akka.NotUsed
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import chatroom.clients.Client
import chatroom.clients.Client.{ChatRoom, Football, FootballMessage, MasterMessages}
import chatroom.master.ChatRoomMaster

object Main {

  def apply(): Behavior[NotUsed] = Behaviors.setup{ ctx =>

    //spawn master
    val chatRoomMaster = ctx.spawn(ChatRoomMaster(),"Master")

    //val a
    val client = new ClientTests(chatRoomMaster,Football,"Samuel",ctx)


    client.sendToRoom(FootballMessage("Hello from here"))
    client.sendToRoom(FootballMessage("Hello from here 2"))
    client.sendToRoom(FootballMessage("Hello from here 3"))
    client.sendToRoom(FootballMessage("Hello from here 4 "))
    client.sendToRoom(FootballMessage("Hello from here 5"))

    client.disconnect

    //should not work as client has been disconnected
    client.sendToRoom(FootballMessage("Hello from here 6"))

    Behaviors.receiveSignal{
      case (_, Terminated(_)) =>
        Behaviors.stopped
      }

    }


  def main(args: Array[String]): Unit = {
    ActorSystem(Main(),"Start")
  }

}

class ClientTests(chatRoomMaster : ActorRef[MasterMessages], connectTo : ChatRoom,screenName : String,context : ActorContext[NotUsed])
extends Client(chatRoomMaster,connectTo,screenName,context){
  override def onNotificationReceived(from: String, message: String): Unit =
    println(s"message received from $from, message is $message")
}


