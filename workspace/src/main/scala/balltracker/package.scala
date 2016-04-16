import fbc.Component

import scala.collection.mutable

package object balltracker {

  val ballRadius = 20
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

  case class AccVel(acceleration: Acc = 0.0, velocity: Vel = 0.0)

  def AccVel(old: AccVel, acc: Double) = {
    new AccVel(acc, old.velocity + acc)
  }

  case class AccVelPos(acceleration: Acc, velocity: Vel, position: Pos)

  def AccVelPos(old: AccVelPos, av: AccVel) = {
    new AccVelPos(av.acceleration, av.velocity, old.position + av.velocity)
  }

  def AccVelPos(position: Pos) = {
    new AccVelPos(0.0, 0.0, position)
  }

  case class AccVelPosPair(position: Position, velocity: Velocity, acceleration: Acceleration)

  def AccVelPosPair(x: AccVelPos, y: AccVelPos) = {
    new AccVelPosPair((x.position, y.position), (x.velocity, y.velocity), (x.acceleration, y.acceleration))
  }

  case class AccVelPosGoalPair(position: Position, velocity: Velocity, acceleration: Acceleration, goal: Position)

  def AccVelPosGoalPair(avp: AccVelPosPair, goal: Position) = {
    new AccVelPosGoalPair(avp.position, avp.velocity, avp.acceleration, goal)
  }
}
