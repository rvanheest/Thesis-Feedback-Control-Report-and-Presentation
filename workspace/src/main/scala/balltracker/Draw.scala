package balltracker

import balltracker.Types.Acceleration
import balltracker.Types.History
import balltracker.Types.Position
import balltracker.Types.ballRadius
import balltracker.Types.height
import balltracker.Types.width
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.shape.StrokeLineCap

object Draw {

	def drawBall(point: Position)(implicit gc: GraphicsContext) = {
		val (x, y) = point
		val diameter = 2 * ballRadius

		gc.setFill(Color.rgb(123, 87, 71))
		gc.fillOval(x - ballRadius, y - ballRadius, diameter, diameter)
	}

	def drawBackground(implicit gc: GraphicsContext) = {
		gc.setFill(Color.rgb(231, 212, 146))
		gc.fillRect(0, 0, width, height)
	}

	def drawLine(ball: Position, setpoint: Position)(implicit gc: GraphicsContext) = {
		val (bx, by) = ball
		val (sx, sy) = setpoint

		gc.setStroke(Color.rgb(96, 185, 154))
		gc.setLineWidth(1.0)
		gc.setLineDashes(8.0, 14.0)

		gc.beginPath()
		gc.moveTo(sx, sy)
		gc.lineTo(bx, by)
		gc.stroke()
		gc.setLineDashes()
	}

	def drawVectors(pos: Position, acc: Acceleration)(implicit gc: GraphicsContext) = {
		val (px, py) = pos
		val (ax, ay) = acc

		gc.setStroke(Color.rgb(247, 120, 37))
		gc.setLineWidth(8)
		gc.setLineCap(StrokeLineCap.ROUND)

		gc.beginPath()
		gc.moveTo(px, py)
		gc.lineTo(px - ax * 300, py)
		gc.stroke()

		gc.beginPath()
		gc.lineTo(px, py)
		gc.lineTo(px, py - ay * 300)
		gc.stroke()
	}

	def drawHistory(history: History)(implicit gc: GraphicsContext) = {
		history.synchronized {
			history.zipWithIndex.foreach(item => {
				val (pos, index) = item
				val (x, y) = pos
				val size = history.size
				val alpha = (index: Double) / size

				gc.setFill(Color.rgb(96, 185, 154, alpha))
				gc.fillOval(x, y, 10, 10)
			})
		}
	}
}