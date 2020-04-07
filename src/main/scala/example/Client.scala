package example

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Client {

  import ChatRoom._

  def apply(): Behavior[SessionEvent] =  Behaviors.receive{(context,msg) =>

    msg match {
      case SessionGranted(handle) =>

        handle ! PostMessage(" Hello from Client ")
        handle ! PostMessage(" Hello from Client 1")
        handle ! PostMessage(" Hello from Client 2")
        handle ! PostMessage(" Hello from Client 3")
        handle ! PostMessage(" Hello from Client 4")
        handle ! PostMessage(" Hello from Client 5")
        handle ! PostMessage(" Hello from Client 6")
        handle ! PostMessage(" Hello from Client 7")
        handle ! PostMessage(" Hello from Client 8")
        handle ! PostMessage(" Hello from Client 9")
        handle ! PostMessage(" Hello from Client 10")
        Behaviors.same

      case MessagePosted(screenName,message) =>
        context.log.info(s"{} just posted {}",screenName,message)

        Behaviors.same
    }

  }

}
