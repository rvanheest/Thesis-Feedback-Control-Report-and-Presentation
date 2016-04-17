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

	var avp2d = AccVelPos2D(AccVelPos(ballRadius), AccVelPos(ballRadius))

	var setpoint = avp2d.position
	var prevError = (0.0, 0.0)
	var integral = (0.0, 0.0)

	val history = new History
	var historyTick = 0
	
	def start(stage: Stage) = {

		def pid: Acceleration = {
			val kp = 3.0
			val ki = 0.0001
			val kd = 80.0

			val error = setpoint - avp2d.position
			val derivative = error - prevError

			integral = integral + error
			prevError = error

			(error * kp + integral * ki + derivative * kd) * 0.001
		}

		@tailrec
		def update(implicit gc: GraphicsContext): Unit = {
			val acc = pid.map(a => math.max(math.min(a, 0.2), -0.2))
			avp2d = avp2d.move(acc)

			// managing the history
			historyTick += 1
			if (historyTick == 5) {
				historyTick = 0

				if (history.size >= 50)
					history.dequeue
				history enqueue avp2d.position
			}

			// drawing all the elements
			Platform.runLater(() => Draw.draw(avp2d.position, setpoint, avp2d.acceleration, history))

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
