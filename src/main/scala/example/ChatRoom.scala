package example

import akka.NotUsed
import akka.actor.Actor
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import example.ChatRoom.GetSession

object ChatRoom {


  sealed trait RoomCommand
  final case class GetSession(screenName: String, replyTo: ActorRef[SessionEvent]) extends RoomCommand
  private final case class PublishSessionMessage(screenName : String, message :String) extends RoomCommand
  sealed trait SessionEvent
  final case class SessionGranted(handle: ActorRef[PostMessage]) extends SessionEvent
  final case class SessionDenied(reason: String) extends SessionEvent
  final case class MessagePosted(screenName: String, message: String) extends SessionEvent

  trait SessionCommand
  final case class PostMessage(message: String) extends SessionCommand
  private final case class NotifyClient(message: MessagePosted) extends SessionCommand

  def apply(): Behavior[RoomCommand] =
    chat(List.empty)

  private def chat(session: List[ActorRef[ChatRoom.SessionCommand]]) : Behavior[RoomCommand] = Behaviors.receive{(ctx,msg) =>
    msg match {
      case GetSession(screenName,client) =>
        //create actorPipe
       val pipe =  ctx.spawn(
          getChatRoomPipe(ctx.self,client,screenName),
          screenName
        )

        client ! SessionGranted(pipe)
        chat(pipe :: session)

      case PublishSessionMessage(screenName, message) =>
        val notification = NotifyClient(MessagePosted(screenName,message))

        session.foreach(_ ! notification)
        Behaviors.same

    }
  }

  private def getChatRoomPipe(
                             room : ActorRef[RoomCommand],
                             client : ActorRef[SessionEvent],
                             screenName : String
                             ) : Behavior[SessionCommand] = {

    println(s"screen name is $screenName")
    Behaviors.receive { (_, msg) =>
      msg match {
        case PostMessage(msg) =>
          println(s"pipe just received a message, screen name is $screenName")
          room ! PublishSessionMessage(screenName, msg)
          getChatRoomPipe(room,client,"changed screen name")

        case NotifyClient(message) =>
          client ! message
          Behaviors.same

      }
    }

  }
}


object Main {

  def apply() : Behavior[NotUsed]= Behaviors.setup{(ctx) =>
    val chatRoom = ctx.spawn(ChatRoom(),"ChatRoom")
    val client = ctx.spawn(Client(),"Client")

    chatRoom ! GetSession("samuel",client)
    ctx.watch(client)

    Behaviors.receiveSignal{
      case (_,Terminated(_)) =>
        Behaviors.same
    }
  }



  def main(args : Array[String]) : Unit = {
    val system = ActorSystem(Main(),"MainSystem")
  }

}
