package presentation

import applied_duality.reactive.Subject
import fbc2.Component

import scala.annotation.tailrec

object SqrtApproxFeedback extends App {

	type Number = Double
	type Guess = Double
	type Accuracy = Double
	type GoodEnough = Boolean

	def sqrtSystem(x: Number, firstGuess: Guess = 1.0): Component[Number, Guess] = {
		def isGoodEnough(guess: Guess, setpoint: Accuracy): GoodEnough = {
			scala.math.abs(guess * guess - x) / x < setpoint
		}

		def goodEnough: Component[(Guess, Accuracy), (Guess, GoodEnough)] = {
			Component.create { case (guess, setpoint) => (guess, isGoodEnough(guess, setpoint)) }
		}

		goodEnough.takeWhile { case (_, goodEnough) => !goodEnough }
			.map { case (guess, _) => (guess + x / guess) / 2 }
			.startWith(firstGuess)
			.feedbackWith(y => y)((_, _))
	}

	val subj = Subject[Accuracy]()
	sqrtSystem(4)
		.run(subj)
		.subscribe(println(_), e => println(e.getMessage), () => println("completed"))
	subj.onNext(0.001)
}

object SqrtApprox extends App {

	type Number = Double
	type Guess = Double
	type Accuracy = Double
	type GoodEnough = Boolean

	def sqrt(x: Number, accuracy: Accuracy = 0.001, firstGuess: Guess = 1.0): Number = {

		@tailrec
		def sqrtIter(guess: Guess): Number = {
			if (isGoodEnough(guess))
				guess
			else
				sqrtIter(improve(guess))
		}

		def improve(guess: Guess): Guess = {
			(guess + x / guess) / 2
		}

		def isGoodEnough(guess: Guess): GoodEnough = {
			scala.math.abs(guess * guess - x) / x < accuracy
		}

		sqrtIter(firstGuess)
	}

	println(sqrt(2.0))
	println(sqrt(4.0))
	println(sqrt(1e-6))
	println(sqrt(1e60))
}
