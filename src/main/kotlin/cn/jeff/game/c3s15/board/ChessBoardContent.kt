package cn.jeff.game.c3s15.board

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*
import kotlin.math.abs

/**
 * # 棋盘的内容
 *
 * 之所以单独将此类抽出来，是为了方便日后做AI计算。
 * 此类包含棋盘的当前局面和上一步棋，这两个信息在[ChessBoard]上面也必然要有，
 * 但[ChessBoard]上的内容会在界面上显示出来，
 * 而这里的内容如果没执行[attachToChessCells]之类，则可以用于安静的计算。
 */
class ChessBoardContent {

	private val chessList = List(25) { Chess.EMPTY }.toObservable()
	private val lastMove = SimpleObjectProperty<Move>(null)
	val moveCountProperty = SimpleIntegerProperty(0)
	private var moveCount by moveCountProperty
	val isCannonsTurn get() = (moveCount % 2) == 0
	val gameOverProperty = SimpleBooleanProperty(false)
	var gameOver by gameOverProperty
	val isCannonsWin get() = gameOver && !isCannonsTurn

	operator fun get(x: Int, y: Int) = if (x in 0..4 && y in 0..4) chessList[x + y * 5] else null

	operator fun set(x: Int, y: Int, chess: Chess) {
		if (x in 0..4 && y in 0..4) {
			chessList[x + y * 5] = chess
		}
	}

	class Move(val fromX: Int, val fromY: Int, val toX: Int, val toY: Int)

	/**
	 * 设置成开局局面。
	 */
	fun setInitialContent() {
		val str = """
			11111
			11111
			11111
			00000
			20202
		""".trimIndent().lines().joinToString("")
		println("[$str]")
		val content = str.map { c ->
			Chess.values()[c.toString().toInt()]
		}
		chessList.setAll(content)
		lastMove.value = null
		moveCount = 0
		gameOver = false

		// 试验
		val compress = compressToInt64(chessList)
		println("压缩=$compress")
		val decompress = decompressFromInt64(compress)
		decompress.forEachIndexed { index, chess ->
			print(chess.text)
			if (index % 5 == 4) {
				println()
			}
		}
	}

	/**
	 * 关联到界面上的棋盘格
	 *
	 * @param chessCells 用于在界面上显示的棋盘格
	 */
	fun attachToChessCells(chessCells: List<ChessCell>) {
		chessList.onChange {
			chessCells.forEachIndexed { index, chessCell ->
				chessCell.chessProperty.value = chessList[index]
			}
		}
	}

	/**
	 * 关联到界面上的“上一步棋”
	 *
	 * @param uiLastMove 在界面上显示的“上一步棋”
	 */
	fun attachToLastMove(uiLastMove: ObjectProperty<Move>) {
		lastMove.bindBidirectional(uiLastMove)
	}

	fun compressToInt64() = compressToInt64(chessList)

	private fun compressToInt64(chessList: List<Chess>): Long {
		var result = 0L
		for (i in chessList.indices) {
			result = result.shl(2)
			result += chessList[i].ordinal
		}
		return result
	}

	private fun decompressFromInt64(int64: Long): List<Chess> {
		val result = Array(25) { Chess.EMPTY }
		var i64 = int64
		for (i in result.count() - 1 downTo 0) {
			result[i] = Chess.values()[(i64 and 0x03).toInt()]
			i64 = i64.shr(2)
		}
		return result.toList()
	}

	fun isMoveValid(move: Move) = with(move) {
		val fromChess = this@ChessBoardContent[fromX, fromY] ?: return@with false
		val toChess = this@ChessBoardContent[toX, toY] ?: return@with false
		val dx = abs(toX - fromX)
		val dy = abs(toY - fromY)
		when {
			// 若同行或同列，dx或dy為零。
			dx * dy != 0 -> false
			isCannonsTurn -> {
				when {
					// 移动一格的情形
					fromChess == Chess.CANNON && toChess == Chess.EMPTY ->
						// 同行或同列距离1格
						dx + dy == 1
					// 吃的情形
					fromChess == Chess.CANNON && toChess == Chess.SOLDIER ->
						// 同行或同列距离2格
						dx + dy == 2 &&
								// 中间必须是空格
								this@ChessBoardContent[(fromX + toX) / 2, (fromY + toY) / 2
								] == Chess.EMPTY
					else -> false
				}
			}
			else -> {
				// 兵只有移动一格
				fromChess == Chess.SOLDIER && toChess == Chess.EMPTY &&
						dx + dy == 1
			}
		}
	}

	fun applyMove(move: Move) {
		if (isMoveValid(move)) {
			this[move.toX, move.toY] = this[move.fromX, move.fromY]!!
			this[move.fromX, move.fromY] = Chess.EMPTY
			lastMove.value = move
			moveCount++
			gameOver = livingSoldierCount() == 0 || cannonBreathCount() == 0
		}
	}

	/**
	 * 剩余的【兵】的数量
	 */
	fun livingSoldierCount() =
		chessList.count { it == Chess.SOLDIER }

	/**
	 * 【炮】的“气”的数量
	 */
	fun cannonBreathCount(): Int =
		// 找出所有空位的坐标
		chessList.mapIndexedNotNull { index, chess ->
			if (chess == Chess.EMPTY) {
				listOf(index % 5, index / 5)
			} else {
				null
			}
		}.filter { (x, y) ->
			// 找相邻有炮的
			listOf(
				this[x + 1, y],
				this[x - 1, y],
				this[x, y + 1],
				this[x, y - 1],
			).contains(Chess.CANNON)
		}.count()

	fun clone() = ChessBoardContent().also {
		it.chessList.setAll(chessList)
		it.moveCount = moveCount
	}

}
