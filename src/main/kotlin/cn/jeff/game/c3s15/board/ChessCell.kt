package cn.jeff.game.c3s15.board

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import tornadofx.*

class ChessCell : Canvas() {

	companion object {
		/** 边界留白 */
		private const val borderPadding = 10.0

		private val fontRefSize = SimpleDoubleProperty(0.0)

		private var fontSize = 0.0

		init {
			fontRefSize.onChange { refSize ->
				println("字体改变")
				val text = Text("字")
				text.font = Font(120.0)
				val fontWidth = text.boundsInLocal.width
				fontSize = text.font.size / fontWidth * refSize
				println("字体大小：$fontSize")
			}
		}
	}

	/** 棋盘格的尺寸 */
	val cellSizeProperty = SimpleDoubleProperty(0.0)

	/** 棋子 */
	val chessProperty = SimpleObjectProperty(Chess.CANNON)

	private var needRepaint = false

	init {
		cellSizeProperty.onChange {
			triggerRepaint()
		}
		chessProperty.onChange {
			triggerRepaint()
		}
	}

	private fun triggerRepaint() {
		needRepaint = true
		runLater {
			if (needRepaint) {
				repaint()
				needRepaint = false
			}
		}
	}

	private fun repaint() {
		width = cellSizeProperty.value
		height = width
		fontRefSize.value = width * .75 - borderPadding - 4.0
		graphicsContext2D.apply {
			clearRect(0.0, 0.0, width, height)
			stroke = chessProperty.value.color
			lineWidth = 3.0
			fill = Color.WHITE
			val internalWidth = width - 2 * borderPadding
			fillOval(borderPadding, borderPadding, internalWidth, internalWidth)
			strokeOval(borderPadding, borderPadding, internalWidth, internalWidth)
			font = Font(fontSize)
			fill = stroke
			fillText(
				chessProperty.value.text,
				(cellSizeProperty.value - fontRefSize.value) / 2,
				cellSizeProperty.value - (cellSizeProperty.value - fontRefSize.value) / 2 -
						fontSize * .16
			)
		}
	}

}
