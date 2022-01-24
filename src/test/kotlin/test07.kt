import cn.jeff.game.c3s15.board.ChessBoardContent.Move
import cn.jeff.game.c3s15.board.GameRecord
import java.text.SimpleDateFormat
import java.util.*

fun main() {
	val m1 = Move.fromString("A3-C2")
	println(m1)
	println()
	println()
	val txt1 = """
		C5-C3 E3-E4
		A5-A3 D3-D4
		A3-E4
	""".trimIndent()
	val lines = txt1.lines()
	val moves = lines.flatMap { line ->
		val tokens = line.split(" ")
		tokens.map { token ->
			Move.fromString(token)
		}
	}
	moves.forEach {
		println(it)
	}

	val gameRecord = GameRecord()
	gameRecord.title = "试一下"
	gameRecord.startTime = SimpleDateFormat("YYYY-MM-DD hh:mm:ss").format(
		Date()
	)
	gameRecord.moves.addAll(moves)
	gameRecord.saveToFile("game-record.txt")

	val record2 = GameRecord()
	record2.loadFromFile("game-record.txt")
	record2.moves.forEach {
		println(it)
	}
	record2.saveToFile()
}
