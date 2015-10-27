package presentation

import applied_duality.reactive.Observable
import scala.collection.mutable.Queue
import applied_duality.reactive.Subject
import applied_duality.reactive.Observer

/*
 * Immutable Component interface
 */
trait ImmutableComponent[I, O] {
	def update(i: I): ImmutableComponent[I, O]
	def action: O
}
class ImmutableRunningAverage(n: Int, queue: Queue[Double]) extends ImmutableComponent[Double, Double] {

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

	val queue = Queue[Double]()

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

	val queue = Queue[Double]()
	val output = Subject[Double]

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
	val subject = Subject[I]
	val observable = subject.publish(transform)

	def transform(i: Observable[I]): Observable[O]
	def asObservable: Observable[O] = observable

	override def onNext(i: I) = subject.onNext(i)
	override def onError(e: Throwable) = subject.onError(e)
	override def onCompleted = subject.onCompleted
}
class RunningAverage(n: Int) extends Component[Double, Double] {
	val queue = Queue[Double]()

	def transform(ts: Observable[Double]) = ts.tee(_ => if (queue.length == n) queue.dequeue)
		.tee(queue.enqueue(_))
		.map(t => queue.sum / queue.size)
}

/*
 * Running average with operators
 */
class RunningAverageWithOperators[X] {

	val runningAverage = (src: fbc.Component[X, Double], n: Int) => 
		src.scan(Queue[Double]())((queue, d) => {
			if (queue.length == n)
				queue.dequeue()
			queue.enqueue(d)
			
			queue
		})
		.map(queue => queue.sum / queue.size)
}
