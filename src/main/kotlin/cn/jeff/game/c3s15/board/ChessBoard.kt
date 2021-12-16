package cn.jeff.game.c3s15.board

import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.canvas.Canvas
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import tornadofx.*
import kotlin.math.floor
import kotlin.math.min

/**
 * # 棋盘
 *
 * 由于棋盘是正方形的，有些东西可以简便处理。
 */
class ChessBoard : Pane() {

	companion object {
		/** 边界留白 */
		private const val borderPadding = 10.0
	}

	/** 棋盘格的尺寸 */
	private val cellSizeProperty = SimpleDoubleProperty(0.0)

	private val backgroundCanvas = Canvas()
	private var isSizeChanged = false

	init {
		add(backgroundCanvas)
		cellSizeProperty.onChange { cellSize ->
			repaintBackground(cellSize)
		}
		widthProperty().onChange {
			sizeChanged()
		}
		heightProperty().onChange {
			sizeChanged()
		}
	}

	private fun sizeChanged() {
		isSizeChanged = true
		// 这里运用了延迟运行的技巧，可以使width和height连续改变的时候只触发一次。
		runLater {
			if (isSizeChanged) {
				println("新大小 = $width x $height")
				cellSizeProperty.value = floor((min(width, height) - borderPadding * 2) / 20) * 4
				isSizeChanged = false
			}
		}
	}

	private fun repaintBackground(cellSize: Double) {
		backgroundCanvas.apply {
			width = cellSize * 5 + borderPadding * 2
			height = width
			// 居中
			layoutX = (this@ChessBoard.width - width) / 2
			layoutY = (this@ChessBoard.height - height) / 2
			graphicsContext2D.apply {
				clearRect(0.0, 0.0, width, height)
				stroke = Color.BLACK
				// 画最外面的线，稍粗一点。
				lineWidth = 3.0
				val internalWidth = width - 2 * borderPadding
				strokeRect(borderPadding, borderPadding, internalWidth, internalWidth)
				// 画里面的线，稍细一点。
				lineWidth = 2.0
				for (i in 1..4) {
					// 正方形，横线和竖线同理，每次循环同时画一条横线和一条竖线。
					val d = borderPadding + i * cellSize
					strokeLine(borderPadding, d, width - borderPadding, d)
					strokeLine(d, borderPadding, d, height - borderPadding)
				}
			}
		}
	}

}
