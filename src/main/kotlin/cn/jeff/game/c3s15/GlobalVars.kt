package cn.jeff.game.c3s15

import com.google.gson.GsonBuilder
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

object GlobalVars {

	private const val confFilename = "c3s15.conf.json"
	private val gson = GsonBuilder().setPrettyPrinting().create()
	var appConf = AppConf()

	fun loadConf(filename: String = confFilename) {
		try {
			FileReader(filename).use { reader ->
				appConf = gson.fromJson(reader, AppConf::class.java)
			}
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	fun saveConf(filename: String = confFilename) {
		FileWriter(filename).use { writer ->
			gson.toJson(appConf, writer)
		}
	}

	var cannonsUseAI = false
	var soldiersUseAI = true

}
