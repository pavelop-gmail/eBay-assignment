package ebay.assignment.machine

import ebay.assignment.machine.TestEvent._

sealed trait NeoState extends State[TestEvent, NeoState] {
  override def canHandle(evt: TestEvent): Boolean = evt match {
    case BotsShot => false
    case _ => true
  }

  override def handle(evt: TestEvent): NeoState = evt match {
    case BotsShot => throw new Exception("I'm killed'!!!")
    case RedPill     => NeoState.RealWorld
    case BluePill    => NeoState.MatrixWorld
  }
}

object NeoState {
  case object RealWorld extends NeoState
  case object MatrixWorld extends NeoState
}
