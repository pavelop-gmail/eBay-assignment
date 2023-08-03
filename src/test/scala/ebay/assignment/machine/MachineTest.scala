package ebay.assignment.machine

import ebay.assignment.machine.NeoState._
import ebay.assignment.machine.TestEvent._
import org.junit.runner.RunWith
import org.scalatest.matchers.should._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.junit.JUnitRunner

import java.nio.file.Files
import java.util.UUID

@RunWith(classOf[JUnitRunner])
class MachineTest extends AnyWordSpec with Matchers {
  val stateChecker: PartialFunction[(NeoState, TestEvent, NeoState), Machine.PedalAction] = {
    case (_, BotsShot, _) =>
      Machine.Skip
    case _ =>
      Machine.KeepMoving
  }

  val tempFile = Files.createTempFile(UUID.randomUUID().toString, ".state").toFile
  tempFile.deleteOnExit()

  val machine = new Machine[TestEvent, NeoState](MatrixWorld)(stateChecker)

  "state machine" should {
    "has init state" in {
      System.out.flush()
      machine.state shouldBe MatrixWorld
    }

    "move state with event" in {
      machine.put(RedPill)
      machine.state shouldBe RealWorld
    }

    "skip events with state checker" in {
      machine.put(BotsShot)
      machine.state shouldBe RealWorld
    }

    "be reset" in {
      machine.reset(MatrixWorld)
      machine.state shouldBe MatrixWorld
    }

    "persist state" in {
      val persistedMachine = new Machine[TestEvent, NeoState](MatrixWorld, Some(tempFile))(stateChecker)
      persistedMachine.put(RedPill)
      persistedMachine.state shouldBe RealWorld
    }

    "recover state" in {
      machine.recoverStateFrom(tempFile)
      machine.state shouldBe RealWorld
    }
  }
}