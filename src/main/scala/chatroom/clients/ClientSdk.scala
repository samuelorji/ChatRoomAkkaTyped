package chatroom.clients

import akka.NotUsed
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import chatroom.clients.Client.{ChatRoom, ChatRoomMessage, ClientMessages, EndSession, Football, GetSession, MasterMessages, MessagePosted, NotificationReceived, PostMessage, Quit, SessionEnded, SessionGranted, SportMessages, UnProcessedMessage}

object ClientSdk {

  abstract class Client(chatRoomMaster : ActorRef[MasterMessages], connectTo : ChatRoom,screenName : String,context : ActorContext[NotUsed]){

    private [this] var internalActor : Option[ActorRef[ClientMessages]] = Some(context.spawn(props,screenName))

    //encapsulate the actor
    private def props : Behavior[ClientMessages] = Behaviors.setup { ctx =>
      chatRoomMaster ! GetSession(screenName, connectTo, ctx.self)

      Behaviors.receiveMessage{
        case SessionEnded =>
          Behaviors.stopped

        case SessionGranted(handler) =>
          println("session granted")
          roomHandler = Some(handler)
          Behaviors.same

        case Quit =>
          disconnectFromRoom
          Behaviors.same

        case NotificationReceived(from, message) =>
          onNotificationReceived(from,message)
          Behaviors.same

        case UnProcessedMessage(message, room) =>
          println(s"Cannot post $message to ${room.toString}")
          Behaviors.same

        case MessagePosted(msg) =>
          println(s"msg was posted ")
          Behaviors.same

      }


    }

    final def disconnect : Unit = {
      roomHandler = None
      internalActor.foreach(_ ! Quit)
      internalActor = None
    }

    def sendToRoom(msg : SportMessages) : Unit = {
      if(roomHandler.isEmpty){
        Thread.sleep(200)
        //should use a future for this instead
      }
      if(internalActor.isDefined) {
        roomHandler.foreach(_ ! PostMessage(msg, internalActor.get))
      }
    }


    protected def onUnprocessedMessage(msg : String, room : ChatRoom) : Unit  = {
      println(s"Cannot post $msg to ${room.toString}", msg, room.toString)

    }

    protected def onMessagePosted(msg : String) : Unit  = {
      println(s"Message Posted", msg)

    }

    private var roomHandler : Option[ActorRef[ChatRoomMessage]] = None

    def onNotificationReceived(from: String, message: String): Unit

    private def disconnectFromRoom = {
      roomHandler.foreach{ pipe =>
        chatRoomMaster ! EndSession(pipe,Football)
      }
    }
  }
}
