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
		var (x, y) = (20.0, 20.0)
		var (vx, vy) = (0.0, 0.0)

		var (setpointX, setpointY) = (x, y)
		var (prevErrorX, prevErrorY) = (0.0, 0.0)
		var (integralX, integralY) = (0.0, 0.0)

		val kp = 3.0
		val ki = 0.0001
		val kd = 80.0

		val history = new History
		var historyTick = 0

		def pid: Acceleration = {
			val (errorX, errorY) = (setpointX - x, setpointY - y)
			val (derivativeX, derivativeY) = (errorX - prevErrorX, errorY - prevErrorY)

			integralX += errorX
			integralY += errorY

			prevErrorX = errorX
			prevErrorY = errorY

			def pid(error: Double, integral: Double, derivative: Double) = {
				0.001 * (kp * error + ki * integral + kd * derivative)
			}

			(pid(errorX, integralX, derivativeX), pid(errorY, integralY, derivativeY))
		}

		@tailrec
		def update(implicit gc: GraphicsContext): Unit = {
			// going from acceleration to position by a double integral
			val maxA = 0.2
			def minMaxAcc(a: Acc) = math.max(math.min(a, maxA), -maxA)

			val (a1, a2) = pid
			val acc@(ax, ay) = (minMaxAcc(a1), minMaxAcc(a2))

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
			Platform.runLater(() => draw((x, y), (setpointX, setpointY), acc))

			// sleep and call again
			Thread.sleep(16)
			update
		}

		def draw(pos: Position, setpoint: Position, acc: Acceleration)(implicit gc: GraphicsContext) = {
			Draw.drawBackground
			Draw.drawHistory(history)
			Draw.drawLine(pos, setpoint)
			Draw.drawBall(pos)
			Draw.drawVectors(pos, acc)
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