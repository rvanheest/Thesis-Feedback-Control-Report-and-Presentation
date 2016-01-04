package report

import applied_duality.reactive.Observable

object MonadicOperatorsExample extends App {

	Observable(1, 2, 3, 4)
		.flatMap(x => Observable(x, x*x))
		.subscribe(x => print(x + " "))
}