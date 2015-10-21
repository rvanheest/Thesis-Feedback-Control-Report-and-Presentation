package balltracker.api

import javafx.application.Application
import balltracker.Types._
import javafx.stage.Stage
import javafx.scene.canvas.Canvas
import balltracker.Draw
import javafx.scene.layout.StackPane
import javafx.geometry.Pos
import fbc.commons.Controllers
import javafx.scene.Scene
import javafx.rx.Events
import applied_duality.reactive.Observable
import applied_duality.reactive.Observer
import balltracker._
import fbc.Component
import scala.concurrent.duration.DurationInt
import applied_duality.reactive.schedulers.NewThreadScheduler
import applied_duality.reactive.schedulers.JavaFxScheduler

class BallTracker extends Application {

	val kp = 3.0
	val ki = 0.001
	val kd = 80

	val maxA = 0.2

	def start(stage: Stage) = {
		val canvas = new Canvas(width, height)
		implicit val gc = canvas.getGraphicsContext2D
		Draw.drawBackground
		Draw.drawBall(ballRadius, ballRadius)

		val root = new StackPane(canvas)
		root.setAlignment(Pos.TOP_LEFT)

		val modelX = new BallModel(ballRadius)
		val modelY = new BallModel(ballRadius)
		val pidX = Controllers.pidController(kp, ki, kd)
		val pidY = Controllers.pidController(kp, ki, kd)
		val history = new History

		root.mouseClicked
			.map(event => (event.getX, event.getY))
			.publish(clicks => Observable.create((observer: Observer[AccVelPosGoalPair]) => {
				val fbcX = feedbackSystem(pidX, modelX)
				val fbcY = feedbackSystem(pidY, modelY)

				clicks.map(_._1).subscribe(fbcX)
				clicks.map(_._2).subscribe(fbcY)

				val fbcXOut = fbcX.asObservable.observeOn(new NewThreadScheduler)
				val fbcYOut = fbcY.asObservable.observeOn(new NewThreadScheduler)

				fbcXOut.zip(fbcYOut)(new AccVelPosPair(_, _))
					.withLatestFrom(clicks)(new AccVelPosGoalPair(_, _))
					.subscribe(observer)

				// start both feedback systems
				modelX.onNext(0.0)
				modelY.onNext(0.0)
			}))
			.observeOn(JavaFxScheduler())
			.tee(pair => {
				Draw.drawBackground
				Draw.drawLine(pair.position, pair.goal)
				Draw.drawHistory(history)
				Draw.drawBall(pair.position)
				Draw.drawVectors(pair.position, pair.acceleration)
			})
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

	def feedbackSystem(pid: Component[Double, Double], model: BallModel): BallFeedbackSystem = {
		pid.map(d => math.max(math.min(d * 0.001, maxA), -maxA)).concat(model).feedback(_.position)
	}
}
object BallTracker extends App {
	Application.launch(classOf[BallTracker])
}

class BallModel(ballRadius: Double) extends Component[Acc, AccVelPos] {
	def transform(acceleration: Observable[Acc]): Observable[AccVelPos] = {
		acceleration
			.scanLeft(new AccVel)(new AccVel(_, _)).drop(1)
			.scanLeft(new AccVelPos(ballRadius))(new AccVelPos(_, _)).drop(1)
			.sample(16 milliseconds)
	}
}