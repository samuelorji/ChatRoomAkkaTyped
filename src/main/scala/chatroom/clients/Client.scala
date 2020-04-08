package chatroom.clients

import akka.NotUsed
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import chatroom.clients.Client.{ChatRoom, ChatRoomMessage, ClientMessages, EndSession, Football, GetSession, MasterMessages, MessagePosted, NotificationReceived, PostMessage, Quit, SessionEnded, SessionGranted, SportMessages, UnProcessedMessage}

object Client {
  trait ClientMessages

  trait ChatRoomMessage

  trait MasterMessages

  trait ChatRoom

  case object Football extends ChatRoom

  case object BasketBall extends ChatRoom

  case class GetSession(screenName: String, room: ChatRoom, client: ActorRef[ClientMessages]) extends MasterMessages
  case class EndSession(client: ActorRef[ChatRoomMessage], room: ChatRoom) extends MasterMessages
  case class ClientRemoved(client : ActorRef[ClientMessages]) extends MasterMessages

  case class SessionGranted(handler: ActorRef[ChatRoomMessage]) extends ClientMessages

  case object SessionEnded extends ClientMessages

  case class MessagePosted(message: String) extends ClientMessages

  case class NotificationReceived(from: String, message: String) extends ClientMessages

  case class PostMessage(message: ChatRoomMessage, replyTo: ActorRef[ClientMessages]) extends ChatRoomMessage

  case class UnProcessedMessage(message: String, room: ChatRoom) extends ClientMessages


  case object Quit extends ClientMessages

  trait SportMessages extends ChatRoomMessage
  case class FootballMessage(msg: String) extends SportMessages

  case class BasketBallMessage(msg: String) extends SportMessages


  private def logInfo(msg: String, args: String*)(implicit ctx: ActorContext[ClientMessages]): Unit = {
    ctx.log.info(msg, args)

  }

  def apply(chatRoomMaster: ActorRef[MasterMessages],chatRoomPipe : Option[ActorRef[ChatRoomMessage]] = None): Behavior[ClientMessages] =
    Behaviors.setup { ctx =>
      chatRoomMaster ! GetSession("ClientFootball", Football, ctx.self)

      def clientBehavior(handle : Option[ActorRef[ChatRoomMessage]]) : Behavior[ClientMessages] = Behaviors.receiveMessage {
        case SessionGranted(handler) =>

          handler ! PostMessage(FootballMessage("Hello from this client 1"), ctx.self)
          handler ! PostMessage(FootballMessage("Hello from this client 2"), ctx.self)
          handler ! PostMessage(FootballMessage("Hello from this client 3"), ctx.self)
          handler ! PostMessage(FootballMessage("Hello from this client 4"), ctx.self)
          handler ! PostMessage(FootballMessage("Hello from this client 5"), ctx.self)
          handler ! PostMessage(FootballMessage("Hello from this client 6"), ctx.self)
          handler ! PostMessage(FootballMessage("Hello from this client 7"), ctx.self)
          handler ! PostMessage(BasketBallMessage("Hello from this client 8"), ctx.self)
          handler ! PostMessage(FootballMessage("Hello from this client 9"), ctx.self)
          handler ! PostMessage(FootballMessage("Hello from this client 10"), ctx.self)
          handler ! PostMessage(FootballMessage("Hello from this client 11"), ctx.self)
          handler ! PostMessage(FootballMessage("Hello from this client 12"), ctx.self)
          handler ! PostMessage(FootballMessage("Hello from this client 13"), ctx.self)


          chatRoomMaster ! EndSession(handler,Football)
          clientBehavior(Some(handler))

        case Quit =>
          chatRoomPipe.foreach{ pipe =>
            println("Quitting Football chat room ")
            chatRoomMaster ! EndSession(pipe,Football)
          }
          Behaviors.same


        case SessionEnded =>
          println("Client is done")

          chatRoomPipe.foreach(_ ! PostMessage(FootballMessage("Hello from this client 7"), ctx.self))
          Behaviors.stopped


        case NotificationReceived(from, message) =>
          ctx.log.info("{} just posted {}", from, message)
          //logInfo("{} just posted {}", from,message)
          Behaviors.same

        case UnProcessedMessage(message, room) =>
          ctx.log.info(s"Cannot post {} to {}", message, room.toString)
          Behaviors.same

        case MessagePosted(msg) =>
          println(s"msg was posted ")
          Behaviors.same

      }


      clientBehavior(chatRoomPipe)

    }

}


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
        ctx.log.info(s"Cannot post {} to {}", message, room.toString)
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
      Thread.sleep(50)
      //should use a future for this instead
    }
    if(internalActor.isDefined) {
      roomHandler.foreach(_ ! PostMessage(msg, internalActor.get))
    }
  }


  protected def onUnprocessedMessage(msg : String, room : ChatRoom) : Unit  = {
    println(s"Cannot post {} to {}", msg, room.toString)

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


