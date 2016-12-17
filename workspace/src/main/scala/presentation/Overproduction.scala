package presentation

import fbc.overproduction.rxscala.Requestable
import rx.lang.scala.Observable
import rx.lang.scala.schedulers.ComputationScheduler

import scala.concurrent.duration.DurationInt
import scala.io.StdIn
import scala.language.postfixOps

object Overproduction extends App {

  Requestable.from(0 until 133701)
    .observe(1 millisecond)
    .filter(_ % 2 == 0)
    .map(2 *)
    .drop(10)
    .take(20)
    .subscribe(
      i => println(s"> $i"),
      _.printStackTrace(),
      () => println("completed"))


  Observable[Int](subscriber => {
    (0 until 133701).foreach(subscriber.onNext)
    subscriber.onCompleted()
  })
    .observeOn(ComputationScheduler())
    .filter(_ % 2 == 0)
    .map(2 *)
    .drop(10)
    .take(20)
    .subscribe(
      i => println(s"> $i"),
      _.printStackTrace(),
      () => println("completed"))

  StdIn.readLine()
}
