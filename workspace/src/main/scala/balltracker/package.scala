import fbc.Component

import scala.collection.mutable

package object balltracker {

  val ballRadius = 20.0
  val width = 1024
  val height = 768

  type Pos = Double
  type Vel = Double
  type Acc = Double
  type Position = (Pos, Pos)
  type Velocity = (Vel, Vel)
  type Acceleration = (Acc, Acc)
  type BallFeedbackSystem = Component[Double, AccVelPos]
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

  case class AccVel(acceleration: Acc = 0.0, velocity: Vel = 0.0) {
    def accelerate(acc: Acc): AccVel = {
      AccVel(acc, velocity + acc)
    }
  }

  case class AccVelPos(acceleration: Acc, velocity: Vel, position: Pos) {
    def move(av: AccVel): AccVelPos = {
      AccVelPos(av.acceleration, av.velocity, position + av.velocity)
    }
  }
  object AccVelPos {
    def apply(position: Pos): AccVelPos = AccVelPos(0.0, 0.0, position)
  }

  case class AccVelPos2D(acceleration: (Acc, Acc), velocity: (Vel, Vel), position: (Pos, Pos)) {
    def move(acc: Acceleration): AccVelPos2D = {
      AccVelPos2D(acc, velocity + acc, position + velocity + acc)
    }
  }
  object AccVelPos2D {
    def apply(x: AccVelPos, y: AccVelPos): AccVelPos2D = {
      AccVelPos2D((x.acceleration, y.acceleration), (x.velocity, y.velocity), (x.position, y.position))
    }
  }

  case class AccVelPosGoal2D(position: Position, velocity: Velocity, acceleration: Acceleration, goal: Position)
  object AccVelPosGoal2D {
    def apply(avp: AccVelPos2D, goal: Position) = {
      new AccVelPosGoal2D(avp.position, avp.velocity, avp.acceleration, goal)
    }
  }
}
