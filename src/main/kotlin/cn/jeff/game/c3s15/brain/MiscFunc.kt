package cn.jeff.game.c3s15.brain

import javafx.geometry.Point2D

fun calcArrowPolygonPoints(
	fromX: Double, fromY: Double, toX: Double, toY: Double,
	d1: Double, d2: Double
): List<Point2D> {
	val midX = toX - (toX - fromX) * .25
	val midY = toY - (toY - fromY) * .25

	// 這段代碼可以用矩陣來優化，懶得慢慢想，用無腦辦法算了。
	val (d1X, d1Y, d2X, d2Y) = if (fromX == toX) {
		listOf(d1, .0, d2, .0)
	} else {
		listOf(.0, d1, .0, d2)
	}
	val tipX1 = midX + d2X
	val tipX2 = midX - d2X
	val tipY1 = midY + d2Y
	val tipY2 = midY - d2Y
	val midX1 = midX + d1X
	val midX2 = midX - d1X
	val midY1 = midY + d1Y
	val midY2 = midY - d1Y
	val stX1 = fromX + d1X
	val stX2 = fromX - d1X
	val stY1 = fromY + d1Y
	val stY2 = fromY - d1Y
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
