package cn.jeff.game.c3s15.board

import java.io.*

class GameRecord {
	var title = ""
	var startTime = ""
	val moves = mutableListOf<ChessBoardContent.Move>()

	fun saveToFile() {
		makeSureFileNotOverride(0)
		saveToFile("record0.txt")
	}

	private fun makeSureFileNotOverride(index: Int) {
		val file1 = File("record$index.txt")
		if (file1.exists()) {
			if (index < 9) {
				makeSureFileNotOverride(index + 1)
				file1.renameTo(File("record${index + 1}.txt"))
			} else {
				// 最多保留最近10个记录。
				file1.delete()
			}
		}
	}

	fun saveToFile(filename: String) {
		PrintWriter(
			OutputStreamWriter(FileOutputStream(filename), Charsets.UTF_8)
		).use { writer ->
			writer.println(title)
			writer.println(startTime)
			val eachMove = moves.iterator()
			while (true) {
				if (!eachMove.hasNext()) break
				writer.print(eachMove.next())
				if (!eachMove.hasNext()) break
				writer.println(" ${eachMove.next()}")
			}
			writer.println()
		}
	}

	fun loadFromFile(filename: String) {
		BufferedReader(InputStreamReader(FileInputStream(filename), Charsets.UTF_8)).use { reader ->
			title = reader.readLine() ?: return
			startTime = reader.readLine() ?: return
			while (true) {
				val line = reader.readLine() ?: return
				val tokens = line.split(" ")
				(0..1).forEach { i ->
					if (tokens.count() > i) {
						val token = tokens[i]
						if (token.isNotEmpty()) {
							moves.add(ChessBoardContent.Move.fromString(token))
						}
					}
				}
			}
		}
	}

}
