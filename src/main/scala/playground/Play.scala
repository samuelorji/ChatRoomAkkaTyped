package playground

object Play {

  import Stuff._
  def createMoney(nsm : String) : Car  = {
    Car(nsm)
  }

}


  object Stuff {
    case class Money(name : String)
    case class Car(name : String){
      def ! (stuff: Int) = name
    }
  }

//
//object Testing extends App {
//  Pla
//}