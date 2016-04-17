package report.balltracker

import scala.collection.mutable

package object imperative {

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
    def *[Z](scalar: Double) = (src._1.toDouble * scalar, src._2.toDouble * scalar)
    def map[Z](f: X => Z)(implicit ev: Y =:= X): (Z, Z) = {
      ((x: X, y: Y) => (f(x), f.compose(ev)(y))).tupled(src)
    }
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
