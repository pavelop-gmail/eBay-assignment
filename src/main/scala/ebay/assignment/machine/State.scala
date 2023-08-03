package ebay.assignment.machine

trait Event

trait State[EventT <: Event, StateT <: State[EventT, StateT]] extends Serializable {
  def canHandle(evt: EventT): Boolean
  def handle(evt: EventT): StateT
}