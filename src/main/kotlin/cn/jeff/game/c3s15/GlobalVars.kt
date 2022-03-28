package cn.jeff.game.c3s15

import cn.jeff.game.c3s15.board.GameRecord
import cn.jeff.game.c3s15.brain.PlayerType
import cn.jeff.game.c3s15.event.NetStatusChangeEvent
import cn.jeff.game.c3s15.net.BaseNetLink
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import tornadofx.*
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

object GlobalVars {

	private const val confFilename = "c3s15.conf.yaml"
	private val yaml = Yaml(DumperOptions().apply {
		defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
		lineBreak = DumperOptions.LineBreak.WIN
	})
	// private val gson = GsonBuilder().setPrettyPrinting().create()

	var appConf = AppConf()

	fun loadConf(filename: String = confFilename) {
		try {
			FileReader(filename).use { reader ->
				appConf = yaml.loadAs(reader, AppConf::class.java)
				// appConf = gson.fromJson(reader, AppConf::class.java)
			}
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	fun saveConf(filename: String = confFilename) {
		FileWriter(filename).use { writer ->
			yaml.dump(appConf, writer)
			// gson.toJson(appConf, writer)
		}
	}

//	val cannonsUseAIProperty = SimpleBooleanProperty(false)
//	var cannonsUseAI: Boolean
//		get() = cannonsUseAIProperty.value
//		set(value) {
//			cannonsUseAIProperty.value = value
//		}
//
//	val soldiersUseAIProperty = SimpleBooleanProperty(true)
//	var soldiersUseAI: Boolean
//		get() = soldiersUseAIProperty.value
//		set(value) {
//			soldiersUseAIProperty.value = value
//		}

	val cannonsPlayerType = SimpleObjectProperty(PlayerType.HUMAN)
	val soldiersPlayerType = SimpleObjectProperty(PlayerType.AI)

	val aiTraversalCountProperty = SimpleIntegerProperty(0)
	val aiTraversalCount: Int get() = aiTraversalCountProperty.value

//	val netGameStateProperty = SimpleObjectProperty(NetGameState.OFFLINE)

	var netLink: BaseNetLink? = null
		set(value) {
			if (field != value) {
				field = value
				FX.eventbus.fire(NetStatusChangeEvent())
			}
		}

	val gameRecord = GameRecord()

}
