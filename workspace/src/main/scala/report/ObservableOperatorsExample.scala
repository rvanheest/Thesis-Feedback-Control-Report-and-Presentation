package report

import applied_duality.reactive.Observable

object ObservableOperatorsExample extends App {

	Observable(1, 2, 3, 4, 5)       		// emits:    1, 2, 3, 4, 5
		.filter(x => x % 2 == 1)    		// emits:    1,    3,    5
		.map(x => x * 2)            		// emits:    2,    6,	  10
		.scanLeft(0)((sum, x) => sum + x)	// emits: 0, 2,    8,     18
		.drop(1)							// emits:    2,    8,     18
		.take(2)							// emits:    2,    8
		.subscribe(x => println(x))
}