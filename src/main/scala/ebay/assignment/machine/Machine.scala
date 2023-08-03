package ebay.assignment.machine

import org.slf4j.LoggerFactory

import java.io.{File, FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import scala.util.Try

class Machine[EventT <: Event, StateT <: State[EventT, StateT]](init: StateT, saveTo: Option[File] = None)
                                                               (onTransition: PartialFunction[(StateT, EventT, StateT), Machine.PedalAction]) {
  require(init != null, "Init state must be not null.")
  require(onTransition != null, "onTransition must be not null.")

  private [this] val logger = LoggerFactory.getLogger(Machine.getClass)

  private [this] val putLock = new ReentrantLock
  private [this] val current = new AtomicReference[StateT](init)

  storeState(state)

  def state: StateT = current.get()

  def reset(toState: StateT): Unit = {
    putLock.lockInterruptibly()
    current.set(toState)
    putLock.unlock()
  }

  def recoverStateFrom(file: File): Unit = {
    require(init != null, "File must be not null.")

    using(new ObjectInputStream(new FileInputStream(file))) { is =>
      val state = is.readObject().asInstanceOf[StateT]
      reset(state)
    }
  }

  def put(evt: EventT): Boolean = {
    putLock.lockInterruptibly()

    val curr = current.get()

    val res = Try {
      val next = if (curr.canHandle(evt)) curr.handle(evt) else curr
      val transitionRes = onTransition.apply(curr, evt, next)
      (next, transitionRes)
    }
    .map {
      case (next, Machine.KeepMoving) =>
        storeState(next)
        current.set(next)
        logger.debug(s"going to state $next")
        true

      case (_, Machine.Skip)          =>
        logger.debug(s"skipping event $evt")
        false
    }
    .recover { t =>
      logger.error("Error happened", t)
      false
    }

    putLock.unlock()
    res.get
  }

  private def storeState(state: StateT): Unit = {
    saveTo.foreach { file =>
      using(new ObjectOutputStream(new FileOutputStream(file))) { os =>
        os.writeObject(state)
      }
    }
  }

  private def using[A, B <: {def close(): Unit}](closeable: B)(f: B => A): A =
    try { f(closeable) } finally { closeable.close() }
}

object Machine {
  sealed trait PedalAction

  case object KeepMoving extends PedalAction
  case object Skip extends PedalAction
}
