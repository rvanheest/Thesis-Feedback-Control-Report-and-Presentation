package report.balltracker.imperative

import java.io.File
import java.util.UUID
import javafx.application.{Application, Platform}
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Pos
import javafx.rx.{toHandler, toRunnable}
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.input.{KeyEvent, MouseEvent}
import javafx.scene.layout.StackPane
import javafx.scene.{Scene, SnapshotParameters}
import javafx.stage.{Stage, WindowEvent}
import javax.imageio.ImageIO

class BallTracker extends Application {

  lazy val snapshots = {
    val file = new File(new File("").getAbsoluteFile, "snapshots")
    if (!file.exists) file.mkdirs()
    file
  }

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
    val acceleration = pid map (a => math.max(math.min(a, 0.2), -0.2))
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
    Platform runLater (() => Draw.draw(ball.position, setpoint, ball.acceleration, history))
  }

  def snapshot(canvas: Canvas): Unit = {
    val snapshot = canvas.snapshot(new SnapshotParameters, null)
    val img = SwingFXUtils.fromFXImage(snapshot, null)
    ImageIO.write(img, "png", new File(snapshots, s"${UUID.randomUUID()}.png"))
  }

  def start(stage: Stage) = {
    val canvas = new Canvas(width, height)
    implicit val gc = canvas getGraphicsContext2D

    val root = new StackPane(canvas)
    root setAlignment Pos.TOP_LEFT
    root.addEventHandler(MouseEvent.MOUSE_CLICKED, (e: MouseEvent) => setpoint = (e.getX, e.getY))

    val loop = new Thread(() => while (true) { update; Thread.sleep(16) })

    stage setOnHidden ((_: WindowEvent) => System.exit(0))
    val scene = new Scene(root, width, height)
    stage setScene scene
    stage setTitle "Balltracker"
    stage.show()

    scene.addEventHandler(KeyEvent.KEY_PRESSED, (e: KeyEvent) => if (e.getText == "s") snapshot(canvas))

    loop.start()
  }
}
object BallTracker extends App {
  Application.launch(classOf[BallTracker])
}
