package chatroom.rooms

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import chatroom.clients.Client.ChatRoomMessage

object BasketBallRoom {

  def apply() : Behavior[ChatRoomMessage] = Behaviors.receive{(ctx,msg) =>
    Behaviors.same
  }

}
