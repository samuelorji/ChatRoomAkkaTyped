package chatroom.master

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import chatroom.clients.Client
import chatroom.rooms.{BasketBallRoom, FootballRoom}

object ChatRoomMaster {
  import chatroom.clients.Client._
   final case class SessionMessage(message : ChatRoomMessage, screenName :String, client : ActorRef[ClientMessages]) extends ChatRoomMessage
   case class AddClient(client : ActorRef[ClientMessages],pipe : ActorRef[ChatRoomMessage] ) extends ChatRoomMessage
   case class RemoveClient(client : ActorRef[ChatRoomMessage],master : ActorRef[MasterMessages]) extends ChatRoomMessage

  private var rooms : Map[ChatRoom, ActorRef[ChatRoomMessage]]  = Map.empty

  def apply(): Behavior[MasterMessages] = Behaviors.setup{ctx =>

   rooms =  Map(
      Football -> ctx.spawn(FootballRoom(),"FootballRoom"),
      BasketBall -> ctx.spawn(BasketBallRoom(),"BasketBallRoom")
    )

    Behaviors.receiveMessage{
      case GetSession(screenName, room, client) =>
        rooms.get(room).map { rm =>
          //create a pipe and send back to client
          val pipe = ctx.spawn(
            getRoomPipe(rm,client,screenName),
            screenName + "handler"
          )
          rm ! AddClient(client,pipe)
          client ! SessionGranted(pipe)
        }.getOrElse{
          client ! UnProcessedMessage("No Room Found", room)
        }

        Behaviors.same


      case EndSession(pipe, room) =>
        rooms.get(room).foreach{ room =>
          room ! RemoveClient(pipe,ctx.self)
        }
        Behaviors.same

      case ClientRemoved(client) =>
        println(s"$client removed")
        client ! SessionEnded
        Behaviors.same

    }
  }

  private def getRoomPipe(
   room : ActorRef[ChatRoomMessage],
   client: ActorRef[ClientMessages],
   screenName : String
   ) : Behavior[ChatRoomMessage] = Behaviors.receiveMessage{
    case PostMessage(message, replyTo) =>
      room ! SessionMessage(message,screenName,replyTo)
      Behaviors.same

  }

}
