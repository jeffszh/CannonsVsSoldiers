import javafx.geometry.Point2D

fun main() {
	val p1 = Point2D(1.0, 0.0)
	val p2 = Point2D(0.0, -1.0)
	val p3 = Point2D(0.0, 2.0)
	println(p1.angle(p2))
	println(p1.angle(p3))
	println(p2.angle(p3))
}
