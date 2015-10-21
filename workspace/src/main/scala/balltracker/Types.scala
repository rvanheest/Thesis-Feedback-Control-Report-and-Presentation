package balltracker

import scala.collection.mutable.Queue

import balltracker.Types.Acc
import balltracker.Types.Acceleration
import balltracker.Types.Pos
import balltracker.Types.Position
import balltracker.Types.Vel
import balltracker.Types.Velocity
import fbc.Component

object Types {

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
	type History = Queue[Position]
}

case class AccVel(acceleration: Acc = 0.0, velocity: Vel = 0.0) {
	def this(old: AccVel, acc: Double) = this(acc, old.velocity + acc)
}

case class AccVelPos(acceleration: Acc, velocity: Vel, position: Pos) {
	def this(old: AccVelPos, av: AccVel) = this(av.acceleration, av.velocity, old.position + av.velocity)
	def this(position: Pos) = this(0.0, 0.0, position)
}

case class AccVelPosPair(position: Position, velocity: Velocity, acceleration: Acceleration) {
	def this(x: AccVelPos, y: AccVelPos) = this((x.position, y.position), (x.velocity, y.velocity), (x.acceleration, y.acceleration))
}

case class AccVelPosGoalPair(position: Position, velocity: Velocity, acceleration: Acceleration, goal: Position) {
	def this(avp: AccVelPosPair, goal: Position) = this(avp.position, avp.velocity, avp.acceleration, goal)
}
