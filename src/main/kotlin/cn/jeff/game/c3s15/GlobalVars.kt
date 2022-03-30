package cn.jeff.game.c3s15

import cn.jeff.game.c3s15.board.GameRecord
import cn.jeff.game.c3s15.brain.PlayerType
import cn.jeff.game.c3s15.event.NetStatusChangeEvent
import cn.jeff.game.c3s15.net.BaseNetLink
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
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

	//	private val gson = GsonBuilder().setPrettyPrinting().create()
	private val yaml = Yaml(DumperOptions().apply {
		lineBreak = DumperOptions.LineBreak.WIN
		defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
	})
	private val yamlMapper = YAMLMapper.builder().build()
	private val yamlWriter = yamlMapper.writerWithDefaultPrettyPrinter()
	private val yamlReader = yamlMapper.reader()
	var appConf = AppConf()

	fun loadConf(filename: String = confFilename) {
		try {
			FileReader(filename).use { reader ->
				appConf = yamlReader.readValue(reader, AppConf::class.java)
			}
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	fun saveConf(filename: String = confFilename) {
		FileWriter(filename).use { writer ->
//			yamlWriter.writeValue(writer, appConf)
			// 使用Jackson的writer可确保字段顺序跟类定义的顺序相同（但无法指定换行符格式）。
			val appConfString = yamlWriter.writeValueAsString(appConf)
			// SnakeYaml的Yaml对象可以控制换行格式，确保输出的yaml文件一定用回车换行。
			// 注：Jackson内部其实也是使用SnakeYaml，但不知怎样操控换行符的格式，只好绕个圈来做。
			// 另：如果直接用SnakeYaml来将appConf写入，则无法操控字段顺序（总是以字母顺序排序，无法定制），
			// 所以要先用Jackson转为字符串，再用SnakeYaml读入再输出。
			yaml.dump(yaml.load(appConfString), writer)
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
