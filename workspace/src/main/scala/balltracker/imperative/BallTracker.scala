package balltracker.imperative

import javafx.application.{Application, Platform}
import javafx.geometry.Pos
import javafx.rx.{toHandler, toRunnable}
import javafx.scene.Scene
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.stage.{Stage, WindowEvent}

import balltracker._

import scala.annotation.tailrec

// original: github.com/nikital/pid
class BallTracker extends Application {

	implicit class Tuple2Math[X: Numeric, Y: Numeric](val src: (X, Y)) {
		import Numeric.Implicits._
		def +(other: (X, Y)) = (src._1 + other._1, src._2 + other._2)
		def -(other: (X, Y)) = (src._1 - other._1, src._2 - other._2)
		def *[Z](scalar: Double) = (src._1.toDouble * scalar, src._2.toDouble * scalar)
	}

	var position = (20.0, 20.0)
	var velocity = (0.0, 0.0)

	var setpoint = position
	var prevError = (0.0, 0.0)
	var integral = (0.0, 0.0)

	val kp = 3.0
	val ki = 0.0001
	val kd = 80.0

	val history = new History
	var historyTick = 0
	
	def start(stage: Stage) = {

		def pid: Acceleration = {
			val error = setpoint - position
			val derivative = error - prevError

			integral = integral + error
			prevError = error
			
			(error * kp + integral * ki + derivative * kd) * 0.001
		}

		@tailrec
		def update(implicit gc: GraphicsContext): Unit = {
			// going from acceleration to position by a double integral
			val maxA = 0.2
			def minMaxAcc(a: Acc) = math.max(math.min(a, maxA), -maxA)

			val (a1, a2) = pid
			val acc@(ax, ay) = (minMaxAcc(a1), minMaxAcc(a2))

			velocity = velocity + acc
			position = position + velocity

			// managing the history
			historyTick += 1
			if (historyTick == 5) {
				historyTick = 0

				if (history.size >= 50)
					history.dequeue
				history.enqueue(position)
			}

			// drawing all the elements
			Platform.runLater(() => Draw.draw(position, setpoint, acc, history))

			// sleep and call again
			Thread.sleep(16)
			update
		}

		val canvas = new Canvas(width, height)
		implicit val gc = canvas.getGraphicsContext2D

		val root = new StackPane(canvas)
		root.setAlignment(Pos.TOP_LEFT)
		root.addEventHandler(MouseEvent.MOUSE_CLICKED, (e: MouseEvent) => {
			// change setpoint on click
			setpoint = (e.getX, e.getY)
		})

		val loop = new Thread(() => update)

		stage.setOnHidden((_: WindowEvent) => System.exit(0))
		stage.setScene(new Scene(root, width, height))
		stage.setTitle("Balltracker")
		stage.show()

		loop.start()
	}
}
object BallTracker extends App {
	Application.launch(classOf[BallTracker])
}
