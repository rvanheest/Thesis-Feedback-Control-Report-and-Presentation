package report.balltracker.imperative

import javafx.application.{Application, Platform}
import javafx.geometry.Pos
import javafx.rx.{toHandler, toRunnable}
import javafx.scene.Scene
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.stage.{Stage, WindowEvent}

class BallTracker extends Application {

  var ball = Ball(ballRadius)

  var setpoint = ball.position
  var prevError = (0.0, 0.0)
  var integral = (0.0, 0.0)

  val history = new History
  var historyTick = 0

  def pid: Acceleration = {
    val kp = 3.0
    val ki = 0.0001
    val kd = 80.0

    val error = setpoint - ball.position
    val derivative = error - prevError

    integral = integral + error
    prevError = error

    (error * kp + integral * ki + derivative * kd) * 0.001
  }

  def update(implicit gc: GraphicsContext): Unit = {
    val acceleration = pid.map(a => math.max(math.min(a, 0.2), -0.2))
    ball = ball accelerate acceleration

    // managing the history
    historyTick += 1
    if (historyTick == 5) {
      historyTick = 0

      if (history.size >= 50)
        history.dequeue
      history enqueue ball.position
    }

    // drawing all the elements
    Platform.runLater(() => Draw.draw(ball.position, setpoint, ball.acceleration, history))

    // sleep and call again
    Thread.sleep(16)
    update
  }

  def start(stage: Stage) = {
    val canvas = new Canvas(width, height)
    implicit val gc = canvas.getGraphicsContext2D

    val root = new StackPane(canvas)
    root.setAlignment(Pos.TOP_LEFT)
    root.addEventHandler(MouseEvent.MOUSE_CLICKED, (e: MouseEvent) => setpoint = (e.getX, e.getY))

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
