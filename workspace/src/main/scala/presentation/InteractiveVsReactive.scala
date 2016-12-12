package presentation

import applied_duality.reactive.Observable

object Interactive extends App {

  val it = Iterable(0, 1, 2, 3)

  val iterator = it.iterator
  while (iterator.hasNext)
    println(iterator.next())

  for (i <- it)
    println(i)

  it.foreach(println)
}

object Reactive extends App {

  val observable = Observable(0, 1, 2, 3)
  observable.subscribe(println(_))
}
