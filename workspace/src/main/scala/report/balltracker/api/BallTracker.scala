package report.balltracker.api

import javafx.application.Application
import javafx.rx.Events
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.StackPane
import javafx.stage.Stage

import applied_duality.reactive.Observable
import applied_duality.reactive.schedulers.JavaFxScheduler
import fbc.Component
import fbc.commons.Controllers

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class BallTracker extends Application {

  val kp = 3.0
  val ki = 0.0001
  val kd = 80.0

  val pidX = Controllers.pidController(kp, ki, kd)
  val pidY = Controllers.pidController(kp, ki, kd)

  def feedback(clicks: Observable[Position]): Component[Unit, Ball2D] = {
    val fbcX = Component.create[Position, Pos](_._1) >>> feedbackSystem(pidX)
    val fbcY = Component.create[Position, Pos](_._2) >>> feedbackSystem(pidY)
    Component.from(clicks) >>> fbcX.combine(fbcY)(Ball2D(_, _))
  }

  def feedbackSystem(pid: Component[Double, Double]): BallFeedbackSystem = {
    pid.map(d => math.max(math.min(d * 0.001, 0.2), -0.2))
      .scan(new AccVel)(_ accelerate _).drop(1)
      .scan(Ball1D(ballRadius))(_ move _)
      .sample(16 milliseconds)
      .feedback(_ position)
  }

  def start(stage: Stage) = {
    val canvas = new Canvas(width, height)
    implicit val gc = canvas.getGraphicsContext2D
    Draw.drawInit

    val root = new StackPane(canvas)
    root setAlignment javafx.geometry.Pos.TOP_LEFT

    val history = new History

    root.mouseClicked
      .map(event => (event.getX, event.getY))
      .publish(clicks => feedback(clicks).asObservable.withLatestFrom(clicks)((_, _)))
      .observeOn(JavaFxScheduler())
      .tee(x => {
        val (ball, goal) = x
        Draw.draw(ball.position, goal, ball.acceleration, history)
      })
      .map(_._1.position)
      .buffer(5)
      .map(_.last)
      .subscribe(pos => {
        history.synchronized {
          if (history.size >= 50)
            history dequeue()
          history enqueue pos
        }
      })

    stage setScene new Scene(root, width, height)
    stage setTitle "Balltracker"
    stage show()
  }
}
object BallTracker extends App {
  Application.launch(classOf[BallTracker])
}
