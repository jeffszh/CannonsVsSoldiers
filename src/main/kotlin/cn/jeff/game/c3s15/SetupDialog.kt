package cn.jeff.game.c3s15

import cn.jeff.game.c3s15.brain.PlayerType
import javafx.fxml.FXMLLoader
import javafx.scene.control.ToggleButton
import javafx.scene.layout.BorderPane
import tornadofx.*

class SetupDialog : Fragment("设置") {

	override val root: BorderPane
	private val j: SetupDialogJ

	private val cannonText get() = GlobalVars.appConf.cannonText
	private val soldierText get() = GlobalVars.appConf.soldierText

	private val tbList: List<ToggleButton>
	private val playerPairList = listOf(
		PlayerType.HUMAN to PlayerType.AI,
		PlayerType.AI to PlayerType.HUMAN,
		PlayerType.HUMAN to PlayerType.HUMAN,
		PlayerType.AI to PlayerType.AI,
		PlayerType.HUMAN to PlayerType.NET,
		PlayerType.NET to PlayerType.HUMAN,
	)

	init {
		val loader = FXMLLoader()
		root = loader.load(javaClass.getResourceAsStream("/cn/jeff/game/c3s15/SetupDialog.fxml"))
		j = loader.getController()
		j.k = this

		val items = listOf(
			"${cannonText}：人腦  ${soldierText}：電腦",
			"${cannonText}：電腦  ${soldierText}：人腦",
			"${cannonText}：人腦  ${soldierText}：人腦",
			"${cannonText}：電腦  ${soldierText}：電腦",
			"${cannonText}：自己  ${soldierText}：网友",
			"${cannonText}：网友  ${soldierText}：自己",
		)
		tbList = listOf(j.tb01, j.tb02, j.tb03, j.tb04, j.tb05, j.tb06)
		tbList.forEachIndexed { ind, tb ->
			tb.text = items[ind]
		}

		val selection = playerPairList.indexOf(
			GlobalVars.cannonsPlayerType.value to GlobalVars.soldiersPlayerType.value
		)
		j.tgPlayerSetting.selectToggle(tbList[selection])
	}

	fun confirmBtnClicked() {
		val selection = listOf(
			j.tb01, j.tb02, j.tb03, j.tb04, j.tb05, j.tb06
		).indexOf(j.tgPlayerSetting.selectedToggle)
		GlobalVars.cannonsPlayerType.value = playerPairList[selection].first
		GlobalVars.soldiersPlayerType.value = playerPairList[selection].second
		close()
	}

	fun cancelBtnClicked() {
		close()
	}

}
