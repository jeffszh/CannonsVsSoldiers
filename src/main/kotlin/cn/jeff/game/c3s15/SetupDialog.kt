package cn.jeff.game.c3s15

import javafx.fxml.FXMLLoader
import javafx.scene.layout.BorderPane
import tornadofx.*

class SetupDialog : Fragment("设置") {

	override val root: BorderPane
	private val j: SetupDialogJ

	init {
		val loader = FXMLLoader()
		root = loader.load(javaClass.getResourceAsStream("/cn/jeff/game/c3s15/SetupDialog.fxml"))
		j = loader.getController()
		j.k = this

		j.tb01.text = "${GlobalVars.appConf.cannonText}由人脑控制"
		j.tb02.text = "${GlobalVars.appConf.cannonText}由电脑控制"
		j.tb03.text = "${GlobalVars.appConf.soldierText}由人脑控制"
		j.tb04.text = "${GlobalVars.appConf.soldierText}由电脑控制"

		j.tgCannon.selectToggle(
			if (GlobalVars.cannonsUseAI)
				j.tb02
			else
				j.tb01
		)
		j.tgSoldier.selectToggle(
			if (GlobalVars.soldiersUseAI)
				j.tb04
			else
				j.tb03
		)
	}

	fun confirmBtnClicked() {
		GlobalVars.cannonsUseAI = j.tgCannon.selectedToggle == j.tb02
		GlobalVars.soldiersUseAI = j.tgSoldier.selectedToggle == j.tb04
		close()
	}

	fun cancelBtnClicked() {
		close()
	}

}
