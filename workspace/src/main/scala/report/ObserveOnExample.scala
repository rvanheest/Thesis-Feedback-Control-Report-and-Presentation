package report

import applied_duality.reactive.Observable
import applied_duality.reactive.schedulers.NewThreadScheduler
import scala.io.StdIn

object ObserveOnExample extends App {

	Observable(1, 2, 3, 4)
		.map(i => 2 * i) // main
		.observeOn(new NewThreadScheduler)
		.map(i => i / 2) // thread2
		.subscribe(i => print(i + " "))

	StdIn.readLine()
}