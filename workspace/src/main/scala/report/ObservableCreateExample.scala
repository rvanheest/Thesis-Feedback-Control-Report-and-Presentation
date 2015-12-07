package report

import applied_duality.reactive.Observable
import applied_duality.reactive.Observer

object ObservableCreateExample extends App {

	val xs: Observable[Int] = Observable.create((obv: Observer[Int]) => {
		obv.onNext(1)
		obv.onNext(2)
		obv.onNext(3)
		obv.onCompleted()
	})
	val observer: Observer[Int] = Observer((x: Int) => print(s"$x "),
		(e: Throwable) => println(e),
		() => println("completed"))

	xs.subscribe(observer)
	
	val ys = Observable(1, 2, 3)
	val observer2: Observer[Int] = Observer((x: Int) => print(s"$x "),
		(e: Throwable) => println(e),
		() => println("completed"))
	ys.subscribe(observer2)
}