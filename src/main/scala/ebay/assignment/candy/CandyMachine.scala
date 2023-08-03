package ebay.assignment.candy

import ebay.assignment.machine.{Event, Machine, State}

object CandyMachine extends App {
  sealed trait CandyEvent extends Event

  case object RedCandy extends CandyEvent
  case object YellowCandy extends CandyEvent

  sealed trait CandyState extends State[CandyEvent, CandyState] {
    override def canHandle(evt: CandyEvent): Boolean = true
  }

  sealed trait ErrorCandyState

  final case object InitState extends CandyState {
    override def handle(evt: CandyEvent): CandyState = evt match {
      case RedCandy    => RedState(1)
      case YellowCandy => YellowState(1)
    }
  }

  final case class RedState(count: Int) extends CandyState {
    override def handle(evt: CandyEvent): CandyState = evt match {
      case RedCandy    => if (count == 2) RedError else RedState(count + 1)
      case YellowCandy => YellowState(1)
    }
  }

  final case class YellowState(count: Int) extends CandyState {
    override def handle(evt: CandyEvent): CandyState = evt match {
      case RedCandy    => RedState(1)
      case YellowCandy => if (count == 2) YellowError else YellowState(count + 1)
    }
  }

  final case object RedError extends CandyState with ErrorCandyState {
    override def handle(evt: CandyEvent): CandyState = evt match {
      case RedCandy    => RedError
      case YellowCandy => YellowState(1)
    }
  }

  final case object YellowError extends CandyState with ErrorCandyState {
    override def handle(evt: CandyEvent): CandyState = evt match {
      case RedCandy    => RedState(1)
      case YellowCandy => YellowError
    }
  }

  val stateChecker: PartialFunction[(CandyState, CandyEvent, CandyState), Machine.PedalAction] = {
    case (RedState(_), evt, RedError) =>
      println(s"$evt => Alert!!!. Red Error")
      Machine.KeepMoving

    case (YellowState(_), evt, YellowError) =>
      println(s"$evt => Alert!!!. Yellow Error")
      Machine.KeepMoving

    case (_, evt, current) =>
      println(s"$evt => $current")
      Machine.KeepMoving
  }


  val machine = new Machine[CandyEvent, CandyState](InitState)(stateChecker)

  println("Good run:")
  List(RedCandy, YellowCandy, RedCandy, YellowCandy, RedCandy, YellowCandy).foreach(machine.put)
  println(s"Completed with state: ${machine.state}")
  println("")

  println("Almost good run:")
  machine.reset(InitState)

  List(RedCandy, YellowCandy, YellowCandy, RedCandy, YellowCandy, RedCandy, RedCandy, YellowCandy).foreach(machine.put)
  println(s"Completed with state: ${machine.state}")
  println("")

  println("Bad run:")
  machine.reset(InitState)

  List(RedCandy, YellowCandy, YellowCandy, YellowCandy, YellowCandy, RedCandy, RedCandy, RedCandy, RedCandy).foreach(machine.put)
  println(s"Completed with state: ${machine.state}")
}
