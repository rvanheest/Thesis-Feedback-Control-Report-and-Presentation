package presentation

import applied_duality.reactive.Observable

import applied_duality.reactive.Subject
import applied_duality.reactive.Observer

import scala.collection.mutable

/*
 * Immutable Component interface
 */
trait ImmutableComponent[I, O] {
	def update(i: I): ImmutableComponent[I, O]
	def action: O
}
class ImmutableRunningAverage(n: Int, queue: mutable.Queue[Double]) extends ImmutableComponent[Double, Double] {

	def update(u: Double): ImmutableRunningAverage = {
		if (queue.length == n) queue.dequeue
		queue.enqueue(u)

		new ImmutableRunningAverage(n, queue)
	}

	def action: Double = queue.sum / queue.length
}

/*
 * Mutable Component interface
 */
trait MutableComponent[I, O] {
	def update(i: I): Unit
	def action: O
}
class MutableRunningAverage(n: Int) extends MutableComponent[Double, Double] {

	val queue = mutable.Queue[Double]()

	def update(u: Double): Unit = {
		if (queue.size == n) queue.dequeue
		queue.enqueue(u)
	}

	def action: Double = queue.sum / queue.length
}

/*
 * Reactive Component interface
 */
trait ReactiveComponent[I, O] {
	def in(i: I): Unit
	def out: Observable[O]
}
class ReactiveRunningAverage(n: Int) extends ReactiveComponent[Double, Double] {

	val queue = mutable.Queue[Double]()
	val output = Subject[Double]()

	def in(u: Double): Unit = {
		if (queue.size == n) queue.dequeue()
		queue.enqueue(u)

		output.onNext(queue.sum / queue.length)
	}
	def out: Observable[Double] = output
}

/*
 * The real Component interface
 */
trait Component[I, O] extends Observer[I] {
	val subject = Subject[I]()

	def transform(i: Observable[I]): Observable[O]
	def asObservable: Observable[O] = transform(subject)

	override def onNext(i: I) = subject.onNext(i)
	override def onError(e: Throwable) = subject.onError(e)
	override def onCompleted() = subject.onCompleted()
}
class RunningAverage(n: Int) extends Component[Double, Double] {
	val queue = mutable.Queue[Double]()

	def transform(ts: Observable[Double]) = ts.tee(_ => if (queue.length == n) queue.dequeue)
		.tee(queue.enqueue(_))
		.map(t => queue.sum / queue.size)
}

/*
 * Running average with operators
 */
class RunningAverageWithOperators(n: Int) extends fbc.Component[Double, Double] {

	def transform(input: Observable[Double]): Observable[Double] = {
		input
			.scanLeft(new mutable.Queue[Double])((queue, d) => {
				if (queue.length == n)
					queue.dequeue()
				queue.enqueue(d)

				queue
			})
			.drop(1)
			.map(queue => queue.sum / queue.size)
	}
}

object Test extends App {
	val comp = new RunningAverageWithOperators(3)
	comp.asObservable.subscribe(println(_))

	comp.onNext(5)
	comp.onNext(3)
	comp.onNext(2)
	comp.onNext(8)
	comp.onNext(5)
}
