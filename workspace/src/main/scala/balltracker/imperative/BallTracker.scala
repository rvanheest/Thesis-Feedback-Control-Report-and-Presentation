package balltracker.imperative

import scala.annotation.tailrec
import scala.collection.mutable.Queue
import balltracker.Draw
import balltracker.Types._
import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.rx.toHandler
import javafx.rx.toRunnable
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javafx.stage.WindowEvent

// original: github.com/nikital/pid
class BallTracker extends Application {

	def start(stage: Stage) = {
		var x = 20.0
		var y = 20.0
		var vx = 0.0
		var vy = 0.0

		var setpointX = x
		var setpointY = y
		var prevErrorX = 0.0
		var prevErrorY = 0.0
		var integralX = 0.0
		var integralY = 0.0

		var kp = 3.0
		var ki = 0.0001
		var kd = 80.0

		val history = new History
		var historyTick = 0

		def pid: Acceleration = {
			val errorX = setpointX - x
			integralX += errorX
			val derivativeX = errorX - prevErrorX
			prevErrorX = errorX

			val errorY = setpointY - y
			integralY += errorY
			val derivativeY = errorY - prevErrorY
			prevErrorY = errorY

			def pid(error: Double, integral: Double, derivative: Double) = {
				0.001 * (kp * error + ki * integral + kd * derivative)
			}

			(pid(errorX, integralX, derivativeX), pid(errorY, integralY, derivativeY))
		}

		@tailrec
		def update(implicit gc: GraphicsContext): Unit = {
			// going from acceleration to position by a double integral
			var a = pid
			var ax = a._1
			var ay = a._2
			var maxA = 0.2
			ax = math.max(math.min(ax, maxA), -maxA)
			ay = math.max(math.min(ay, maxA), -maxA)
			vx += ax
			vy += ay
			x += vx
			y += vy

			// managing the history
			historyTick += 1
			if (historyTick == 5) {
				historyTick = 0

				if (history.size >= 50)
					history.dequeue
				history.enqueue((x, y))
			}

			// drawing all the elements
			Platform.runLater(() => {
				Draw.drawBackground
				Draw.drawHistory(history)
				Draw.drawLine((x, y), (setpointX, setpointY))
				Draw.drawBall((x, y))
				Draw.drawVectors((x, y), (ax, ay))
			})

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
			setpointX = e.getX
			setpointY = e.getY
		})

		val loop = new Thread(() => update)

		stage.setOnHidden((_: WindowEvent) => System.exit(0))
		stage.setScene(new Scene(root, width, height))
		stage.setTitle("Balltracker")
		stage.show()

		loop.start
	}
}
object BallTracker extends App {
	Application.launch(classOf[BallTracker])
}