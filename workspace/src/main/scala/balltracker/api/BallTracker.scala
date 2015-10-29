package balltracker.api

import scala.concurrent.duration.DurationInt

import applied_duality.reactive.Observable
import applied_duality.reactive.ObservableX
import applied_duality.reactive.Observer
import applied_duality.reactive.schedulers.JavaFxScheduler
import applied_duality.reactive.schedulers.NewThreadScheduler
import balltracker.AccVel
import balltracker.AccVelPos
import balltracker.AccVelPosGoalPair
import balltracker.AccVelPosPair
import balltracker.Draw
import balltracker.Types._
import fbc.Component
import fbc.Operators
import fbc.commons.Controllers
import javafx.application.Application
import javafx.rx.Events
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.StackPane
import javafx.stage.Stage

class BallTracker extends Application {

	val kp = 3
	val ki = 0.0001
	val kd = 80

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
				val fbc = Component.from(clicks) >>> fbcX.zip(fbcY)(new AccVelPosPair(_, _))

				fbc.asObservable
					.withLatestFrom(clicks)(new AccVelPosGoalPair(_, _))
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
				.scan(new AccVel)(new AccVel(_, _)).drop(1)
				.scan(new AccVelPos(ballRadius))(new AccVelPos(_, _))
				.sample(16 milliseconds)
				.feedback(_.position)
	}
}
object BallTracker extends App {
	Application.launch(classOf[BallTracker])
}
