package cn.jeff.game.c3s15.brain

import javafx.geometry.Point2D

fun calcArrowPolygonPoints(
	fromX: Double, fromY: Double, toX: Double, toY: Double,
	d1: Double, d2: Double
): List<Point2D> {
	val midX = toX - (toX - fromX) * .25
	val midY = toY - (toY - fromY) * .25
	val tipX1: Double
	val tipX2: Double
	val tipY1: Double
	val tipY2: Double
	val midX1: Double
	val midX2: Double
	val midY1: Double
	val midY2: Double
	val stX1: Double
	val stX2: Double
	val stY1: Double
	val stY2: Double
	if (fromX == toX) {
		tipY1 = midY
		tipY2 = midY
		tipX1 = midX + d2
		tipX2 = midX - d2
		midY1 = midY
		midY2 = midY
		midX1 = midX + d1
		midX2 = midX - d1
		stY1 = fromY
		stY2 = fromY
		stX1 = fromX + d1
		stX2 = fromX - d1
	} else {
		tipX1 = midX
		tipX2 = midX
		tipY1 = midY + d2
		tipY2 = midY - d2
		midX1 = midX
		midX2 = midX
		midY1 = midY + d1
		midY2 = midY - d1
		stX1 = fromX
		stX2 = fromX
		stY1 = fromY + d1
		stY2 = fromY - d1
	}
	return listOf(
		Point2D(toX, toY),
		Point2D(tipX1, tipY1),
		Point2D(midX1, midY1),
		Point2D(stX1, stY1),
		Point2D(stX2, stY2),
		Point2D(midX2, midY2),
		Point2D(tipX2, tipY2)
	)
}
