package chatroom
package rooms

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import chatroom.clients.Client.ChatRoomMessage

object FootballRoom {
  import clients.Client._
  import master.ChatRoomMaster._


  def apply() : Behavior[ChatRoomMessage] =
    footballRoom(Map.empty)

  private def footballRoom(clients : Map[ActorRef[ChatRoomMessage], ActorRef[ClientMessages]]) : Behavior[ChatRoomMessage] = {
    Behaviors.receiveMessage{
      case AddClient(client,pipe) =>
        println(s"new client added ")
        footballRoom(clients.updated(pipe , client))

      case SessionMessage(message, screenName, client) =>
        message match {
          case FootballMessage(msg) =>
            //send client that we received his message,
            client ! MessagePosted(msg)

            //notify all currently connected clients
            clients.values.foreach(_ ! NotificationReceived(screenName,msg))
            Behaviors.same
          case m =>
            client ! UnProcessedMessage(s"Cannot Send [$m] to FootballRoom", Football)
            Behaviors.same
        }


      case RemoveClient(pipe,chatMaster) =>
        //You can notify all clients that someone left

        clients.get(pipe).map { client =>
          chatMaster ! ClientRemoved(client)
          footballRoom(clients.removed(pipe))
        }.getOrElse(Behaviors.same)





        //clear cache or important details ):







    }
  }

}
