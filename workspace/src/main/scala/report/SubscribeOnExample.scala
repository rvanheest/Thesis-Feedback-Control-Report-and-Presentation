package report

import applied_duality.reactive.Observable
import applied_duality.reactive.schedulers.NewThreadScheduler
import scala.io.StdIn

object SubscribeOnExample extends App {

	Observable(1, 2, 3, 4)					// thread 1
		.subscribeOn(new NewThreadScheduler)
		.map(i => 2 * i)					// thread 1
		.observeOn(new NewThreadScheduler)
		.map(i => i / 2)					// thread 2
		.subscribe(i => print(i + " "))	// thread 2

	StdIn.readLine()
}