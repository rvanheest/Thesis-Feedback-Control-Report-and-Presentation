package report

import scala.util.Try

object IterableObservable {

	trait Iterable[T] {
		def iterator(): Iterator[T]
	}
	trait Iterator[T] {
		def moveNext(): Boolean
		def current: T
		
		def getNext(): Try[Option[T]] = {
			Try(moveNext()).filter(identity).map(_ => Option(current))
		}
	}
}
