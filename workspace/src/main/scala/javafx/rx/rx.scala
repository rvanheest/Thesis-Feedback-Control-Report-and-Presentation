package javafx

import applied_duality.reactive.Observable
import applied_duality.reactive.Subscription
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Node
import javafx.scene.input.InputEvent
import javafx.scene.input.MouseEvent

package object rx {

	implicit def toHandler[T <: Event](action: T => Unit): EventHandler[T] = {
		new EventHandler[T] { override def handle(e: T): Unit = action(e) }
	}

	implicit def toRunnable(runnable: () => Unit): Runnable = {
		new Runnable { override def run() = runnable() }
	}

	implicit class Events(val node: Node) extends AnyVal {

		def getEvent[T <: InputEvent](event: EventType[T]): Observable[T] = Observable.create[T](observer => {
			val handler = (e: T) => observer.onNext(e)
			node.addEventHandler(event, handler)
			observer += Subscription { node.removeEventHandler(event, handler) }
		})

		def mouseClicked: Observable[MouseEvent] = getEvent(MouseEvent.MOUSE_CLICKED)
	}
}
