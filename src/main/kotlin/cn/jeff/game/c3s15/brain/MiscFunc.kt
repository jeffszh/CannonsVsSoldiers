package cn.jeff.game.c3s15.brain

import javafx.geometry.Point2D

fun calcArrowPolygonPoints(
	fromX: Double, fromY: Double, toX: Double, toY: Double,
	d1: Double, d2: Double
): List<Point2D> {
	val (d1X, d1Y, d2X, d2Y) = if (fromX == toX)
		listOf(d1, .0, d2, .0)
	else
		listOf(.0, d1, .0, d2)

	val midX = toX - (toX - fromX) * .25
	val midY = toY - (toY - fromY) * .25
	return listOf(
		Point2D(toX, toY),
		Point2D(midX + d2X, midY + d2Y),
		Point2D(midX + d1X, midY + d1Y),
		Point2D(fromX + d1X, fromY + d1Y),
		Point2D(fromX - d1X, fromY - d1Y),
		Point2D(midX - d1X, midY - d1Y),
		Point2D(midX - d2X, midY - d2Y)
	)
}
