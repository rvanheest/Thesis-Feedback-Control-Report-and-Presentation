package report

import applied_duality.reactive.Observable
import applied_duality.reactive.schedulers.NewThreadScheduler

object FromList extends App {
	def fromSeq[T](list: Seq[T]): Observable[T] = {
		Observable.create(observer => {
			list.foreach(observer.onNext)
			observer.onCompleted()
		})
	}

	fromSeq(0 to 10)
		.tee(i => println(s"before - $i"))
		.observeOn(new NewThreadScheduler)
		.tee(i => println(s"after - $i"))
		.subscribe()
}