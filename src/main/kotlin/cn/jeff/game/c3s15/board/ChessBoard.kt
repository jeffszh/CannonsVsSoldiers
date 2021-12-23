package cn.jeff.game.c3s15.board

import cn.jeff.game.c3s15.GlobalVars
import cn.jeff.game.c3s15.brain.Brain
import cn.jeff.game.c3s15.brain.calcArrowPolygonPoints
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.canvas.Canvas
import javafx.scene.input.TransferMode
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
	private val chessCells = List(25) {
		ChessCell()
	}
	private var isSizeChanged = false

	private val lastMoveIndicator = Canvas()
	private val lastMove = SimpleObjectProperty<ChessBoardContent.Move>(null)

	/** 棋盘的内容 */
	val content = ChessBoardContent()

	init {
		add(backgroundCanvas)
		chessCells.forEach {
			add(it)
		}
		add(lastMoveIndicator)
		cellSizeProperty.onChange { cellSize ->
			repaintBackground(cellSize)
			updateLastMoveIndicator()
		}
		widthProperty().onChange {
			sizeChanged()
		}
		heightProperty().onChange {
			sizeChanged()
		}
		lastMove.onChange {
			updateLastMoveIndicator()
		}

		content.attachToChessCells(chessCells)
		content.attachToLastMove(lastMove)
		content.setInitialContent()

		setupDragDrop()
		Brain(content)
	}

	private fun sizeChanged() {
		isSizeChanged = true
		// 这里运用了延迟运行的技巧，可以使width和height连续改变的时候只触发一次。
		runLater {
			if (isSizeChanged) {
				println("新大小 = $width x $height")
				cellSizeProperty.value = floor((min(width, height) - borderPadding * 2) / 20) * 4
				rearrangeCells()
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

	private fun rearrangeCells() {
		chessCells.forEachIndexed { index, chessCell ->
			val iX = index % 5
			val iY = index / 5
			chessCell.layoutX = borderPadding + iX * cellSizeProperty.value +
					backgroundCanvas.layoutX
			chessCell.layoutY = borderPadding + iY * cellSizeProperty.value +
					backgroundCanvas.layoutY
			chessCell.cellSizeProperty.value = cellSizeProperty.value
		}
	}

	private fun updateLastMoveIndicator() {
		lastMoveIndicator.apply {
			width = backgroundCanvas.width
			height = backgroundCanvas.height
			layoutX = backgroundCanvas.layoutX
			layoutY = backgroundCanvas.layoutY
			graphicsContext2D.apply {
				clearRect(0.0, 0.0, width, height)
				val cellSize = cellSizeProperty.value
				val lastMoveValue = lastMove.value
				if (lastMoveValue != null) {
					val (fromX, fromY, toX, toY) = with(lastMoveValue) {
						listOf(fromX, fromY, toX, toY)
					}.map { intVal ->
						intVal * cellSize + borderPadding + cellSize / 2.0
					}
					val polygonPoints = calcArrowPolygonPoints(
						fromX, fromY, toX, toY,
						cellSize * .15, cellSize * .25
					)
					val xArray = polygonPoints.map { it.x }.toDoubleArray()
					val yArray = polygonPoints.map { it.y }.toDoubleArray()
					val nPoints = polygonPoints.count()
					fill = c(255, 160, 0, .65)
					stroke = c(80, 250, 0, .65)
					lineWidth = 3.0
					fillPolygon(xArray, yArray, nPoints)
					strokePolygon(xArray, yArray, nPoints)
				}
			}
		}
	}

	private fun setupDragDrop() {
		var startDragX = -1
		var startDragY = -1
		setOnDragDetected { e ->
			if (content.gameOver) {
				return@setOnDragDetected
			}
			val (x, y) = mouseXyToChessBoardXy(e.x, e.y)
			val chess = content[x, y]
			println("${e.x},${e.y} --> $x,$y  $chess")
			if (if (content.isCannonsTurn) {
					chess == Chess.CANNON
				} else {
					chess == Chess.SOLDIER
				}
			) {
				startDragAndDrop(TransferMode.MOVE).setContent {
					chess ?: kotlin.error("不可能！")
					putString("$x $y ${chess.text}")
					startDragX = x
					startDragY = y
				}
			}
		}
		setOnDragEntered {
		}
		setOnDragOver { e ->
			val offset = -cellSizeProperty.value / 2
			chessCells[startDragX + startDragY * 5].apply {
				relocate(e.x + offset, e.y + offset)
				toFront()
			}
			val (x, y) = mouseXyToChessBoardXy(e.x, e.y)
			if (content.isMoveValid(ChessBoardContent.Move(startDragX, startDragY, x, y))) {
				e.acceptTransferModes(TransferMode.MOVE)
			}
		}
		setOnDragDropped { e ->
			val (x, y) = mouseXyToChessBoardXy(e.x, e.y)
			applyMove(ChessBoardContent.Move(startDragX, startDragY, x, y))
			e.isDropCompleted = true
			e.consume()
		}
		setOnDragDone { e ->
			e.consume()
			rearrangeCells()
		}
	}

	fun applyMove(move: ChessBoardContent.Move) {
		content.applyMove(move)
		lastMoveIndicator.toFront()
		rearrangeCells()
		showDialogIfGameOver()
	}

	private fun mouseXyToChessBoardXy(mX: Double, mY: Double) = listOf(
		floor((mX - backgroundCanvas.layoutX - borderPadding) / cellSizeProperty.value).toInt(),
		floor((mY - backgroundCanvas.layoutY - borderPadding) / cellSizeProperty.value).toInt()
	)

	private fun showDialogIfGameOver() {
		if (content.gameOver) {
			val winSideText = if (content.isCannonsWin)
				GlobalVars.appConf.cannonText
			else
				GlobalVars.appConf.soldierText
			information("【$winSideText】获胜！")
		}
	}

}
