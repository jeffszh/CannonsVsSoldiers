package cn.jeff.game.c3s15.board

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*

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
	val lastMove = SimpleObjectProperty<Move>(null)
	private val moveCountProperty = SimpleIntegerProperty(0)
	var moveCount by moveCountProperty
	val isCannonsTurn get() = (moveCount / 2) == 0

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

}
