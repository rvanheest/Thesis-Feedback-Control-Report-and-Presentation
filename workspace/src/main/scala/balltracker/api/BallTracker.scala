package balltracker.api

import javafx.application.Application
import javafx.rx.Events
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.StackPane
import javafx.stage.Stage

import applied_duality.reactive.{Observable, ObservableX, Observer}
import applied_duality.reactive.schedulers.JavaFxScheduler
import balltracker._
import fbc.{Component, Operators}
import fbc.commons.Controllers

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class BallTracker extends Application {

	val kp = 3.0
	val ki = 0.0001
	val kd = 80.0

	val maxA = 0.2

	def start(stage: Stage) = {
		val canvas = new Canvas(width, height)
		implicit val gc = canvas.getGraphicsContext2D
		Draw.drawBackground
		Draw.drawBall(ballRadius, ballRadius)

		val root = new StackPane(canvas)
		root.setAlignment(javafx.geometry.Pos.TOP_LEFT)

		val pidX = Controllers.pidController(kp, ki, kd)
		val pidY = Controllers.pidController(kp, ki, kd)
		val history = new History

		root.mouseClicked
			.map(event => (event.getX, event.getY))
			.publish(clicks => Observable.create((observer: Observer[AccVelPosGoalPair]) => {
				val fbcX = Component[Position, Pos](_._1) >>> feedbackSystem(pidX)
				val fbcY = Component[Position, Pos](_._2) >>> feedbackSystem(pidY)
				val fbc = Component.from(clicks) >>> fbcX.zip(fbcY)(AccVelPosPair(_, _))

				fbc.asObservable
					.withLatestFrom(clicks)(AccVelPosGoalPair(_, _))
					.subscribe(observer)
			}))
			.observeOn(JavaFxScheduler())
			.tee(pair => Draw.draw(pair.position, pair.goal, pair.acceleration, history))
			.map(_.position)
			.buffer(5)
			.map(_(4))
			.subscribe(pos => {
				history.synchronized {
					if (history.size >= 50)
						history.dequeue()
					history.enqueue(pos)
				}
			})

		stage.setScene(new Scene(root, width, height))
		stage.setTitle("Balltracker")
		stage.show()
	}

	def feedbackSystem(pid: Component[Double, Double]): BallFeedbackSystem = {
		pid.map(d => math.max(math.min(d * 0.001, maxA), -maxA))
				.scan(new AccVel)(AccVel(_, _)).drop(1)
				.scan(AccVelPos(ballRadius))(AccVelPos(_, _))
				.sample(16 milliseconds)
				.feedback(_.position)
	}
}
object BallTracker extends App {
	Application.launch(classOf[BallTracker])
}
