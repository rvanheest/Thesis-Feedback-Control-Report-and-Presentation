package report.balltracker

import scala.collection.mutable

package object imperative_experiments {

  val ballRadius = 20.0
  val width = 1024
  val height = 768

  type Position = (Double, Double)
  type Velocity = (Double, Double)
  type Acceleration = (Double, Double)
  type History = mutable.Queue[Position]

  implicit class Tuple2Math[X: Numeric, Y: Numeric](val src: (X, Y)) {
    import Numeric.Implicits._
    def +(other: (X, Y)) = (src._1 + other._1, src._2 + other._2)
    def -(other: (X, Y)) = (src._1 - other._1, src._2 - other._2)
    def *(scalar: X)(implicit ev: Y =:= X) = (scalar * src._1, scalar * src._2)
    def map[Z](f: X => Z)(implicit ev: Y =:= X): (Z, Z) = (f(src._1), f(src._2))
  }

  case class Ball(acceleration: Acceleration, velocity: Velocity, position: Position) {
    def accelerate(acc: Acceleration): Ball = {
      Ball(acc, velocity + acc, position + velocity + acc)
    }
  }
  object Ball {
    def apply(radius: Double): Ball = {
      Ball((0.0, 0.0), (0.0, 0.0), (radius, radius))
    }
  }
}
