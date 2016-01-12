package report

import applied_duality.reactive.Observable
import scala.concurrent.duration.DurationInt
import applied_duality.reactive.Subject
import scala.io.StdIn

object CallstackBlocking extends App {

	def now() = System.currentTimeMillis()

	val start = now()

	val timer = Observable(1, 2, 3, 4)
		.tee(i => println("[" + (now() - start) + "] emitted - " + i))
	val subject = Subject[Int]

	subject.tee(i => println("[" + (now() - start) + "] before - " + i))
		.tee(_ => Thread.sleep(1000))
		.subscribe(i => println("[" + (now() - start) + "] after - " + i))
	timer.subscribe(subject)
}
