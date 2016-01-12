package report

import applied_duality.reactive.Observable
import applied_duality.reactive.schedulers.NewThreadScheduler
import scala.io.StdIn

object ObserveOnExample extends App {

	Observable(1, 2, 3, 4)				// main thread
		.map(i => 2 * i)				// main thread
		.observeOn(new NewThreadScheduler)
		.map(i => i / 2)				// thread 2
		.subscribe(i => print(i + " "))	// thread 2

	StdIn.readLine()
}