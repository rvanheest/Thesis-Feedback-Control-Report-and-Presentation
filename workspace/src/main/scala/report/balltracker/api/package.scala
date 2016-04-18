package report.balltracker

import fbc.Component

import scala.collection.mutable

package object api {

  val ballRadius = 20.0
  val width = 1024
  val height = 768

  type Pos = Double
  type Vel = Double
  type Acc = Double
  type Position = (Pos, Pos)
  type Velocity = (Vel, Vel)
  type Acceleration = (Acc, Acc)
  type History = mutable.Queue[Position]
  type BallFeedbackSystem = Component[Double, Ball1D]

  implicit class Tuple2Math[X: Numeric, Y: Numeric](val src: (X, Y)) {
    import Numeric.Implicits._
    def +(other: (X, Y)) = (src._1 + other._1, src._2 + other._2)
  }

  case class AccVel(acceleration: Acc = 0.0, velocity: Vel = 0.0) {
    def accelerate(acc: Acc): AccVel = {
      AccVel(acc, velocity + acc)
    }
  }

  case class Ball1D(acceleration: Acc, velocity: Vel, position: Pos) {
    def move(av: AccVel): Ball1D = {
      Ball1D(av.acceleration, av.velocity, position + av.velocity)
    }
  }
  object Ball1D {
    def apply(position: Pos): Ball1D = Ball1D(0.0, 0.0, position)
  }

  case class Ball2D(acceleration: (Acc, Acc), velocity: (Vel, Vel), position: (Pos, Pos)) {
    def move(acc: Acceleration): Ball2D = {
      Ball2D(acc, velocity + acc, position + velocity + acc)
    }
  }
  object Ball2D {
    def apply(x: Ball1D, y: Ball1D): Ball2D = {
      Ball2D((x.acceleration, y.acceleration), (x.velocity, y.velocity), (x.position, y.position))
    }
  }
}
