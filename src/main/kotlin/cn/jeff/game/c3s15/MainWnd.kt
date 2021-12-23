package cn.jeff.game.c3s15

import cn.jeff.game.c3s15.event.MoveChessEvent
import javafx.fxml.FXMLLoader
import javafx.scene.layout.BorderPane
import javafx.stage.StageStyle
import tornadofx.*

class MainWnd : View(GlobalVars.appConf.mainTitle) {

	override val root: BorderPane
	private val j: MainWndJ

	init {
		primaryStage.isResizable = true

		val loader = FXMLLoader()
		root = loader.load(javaClass.getResourceAsStream("/cn/jeff/game/c3s15/MainWnd.fxml"))
		j = loader.getController()
		j.k = this

		j.label01.textProperty().bind(GlobalVars.cannonsUseAIProperty.stringBinding {
			"${GlobalVars.appConf.cannonText}：${if (it == true) "電腦" else "人腦"}"
		})
		j.label02.textProperty().bind(GlobalVars.soldiersUseAIProperty.stringBinding {
			"${GlobalVars.appConf.soldierText}：${if (it == true) "電腦" else "人腦"}"
		})

		subscribe<MoveChessEvent> { e ->
			j.chessBoard.applyMove(e.move)
		}
	}

	fun btnRestartClick() {
		j.chessBoard.content.setInitialContent()
	}

	fun btnSetupClick() {
		find(SetupDialog::class).openModal(StageStyle.UTILITY, resizable = false)
	}

}
