package ebay.assignment.machine

sealed trait TestEvent extends Event

object TestEvent {
  case object RedPill     extends TestEvent
  case object BluePill    extends TestEvent
  case object BotsShot    extends TestEvent
}
