package beginning

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import beginning.HelloWorld.{Greet, Greeted}
import beginning.HelloWorldMain.SayHello
import beginning.TestNamedActor.Message

object HelloWorld {

  final case class Greet(whom: String, replyTo: ActorRef[Greeted])

  final case class Greeted(whom: String, from: ActorRef[Greet])

  def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
    context.log.info("Hello {}!", message.whom)
    message.replyTo ! Greeted(message.whom, context.self)
    Behaviors.same
  }
}

object HelloWorldBot {
  // can only receive greeted from HellowWorld.Greet


  def apply(maxGreeting: Int): Behavior[Greeted] =
    bot(0, maxGreeting)


  private def bot(greetingCounter: Int, max: Int): Behavior[Greeted] = Behaviors.receive { (context, message) => {
    println(s"greeting counter is $greetingCounter")
    println(s"max greeting is $max")
    val n = greetingCounter + 1
    context.log.info("Greeting {} for {}", n, message.whom)

      if (n >= max) {
        Behaviors.stopped
      } else {
        message.from ! Greet(message.whom, context.self)
        bot(n,max)
      }
  }
  }
}


object HelloWorldMain {
  final case class SayHello(msg : String)

  def apply() : Behavior[SayHello] = Behaviors.setup{cotx =>
    val helloWorld = cotx.spawn(HelloWorld(),"Greeter")

    Behaviors.receiveMessage{(msg) =>
      val bot = cotx.spawn(HelloWorldBot(3),msg.msg)

      helloWorld ! Greet(msg.msg,bot)
      Behaviors.same
    }
  }
}



object TestStopped {
  case object Stop
  def apply() : Behavior[Stop.type] = x

//    Behaviors.receiveMessage{msg =>
//    println(s"received message $msg, stopping actor")
//    Behaviors.stopped
//  }

  val x : Behavior[Stop.type] = Behaviors.receive{(ctx,msg) =>
    println(ctx.self.path.name)
    println(s"received message $msg, stopping actor")
    Behaviors.stopped

  }
}

object TestNamedActor {


  case class Message(msg : String)

  def apply(name : String) : Behavior[Message] = Behaviors.receiveMessage{
    case Message(g) =>
      println(s"$name just received $g")
      Behaviors.same
  }
}


object main{
  val system = ActorSystem(TestNamedActor("samuel"), "j")


  //(1 to Integer.MAX_VALUE).foreach(n => system ! Message(s"Hello $n"))


  def sum(list : List[Int]) : Int = {
    list match {
      case Nil => 0
      case h :: t => h + sum(t)
    }
  }

  sum((1 to 6750).toList)
//  system ! Message("Hello")
//  system ! Message("World")

  system.terminate()
}