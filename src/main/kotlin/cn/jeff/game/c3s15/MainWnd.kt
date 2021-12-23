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
		j.statusLabel.textProperty().bind(j.chessBoard.content.moveCountProperty.stringBinding(
			j.chessBoard.content.gameOverProperty,
			GlobalVars.cannonsUseAIProperty,
			GlobalVars.soldiersUseAIProperty,
			GlobalVars.aiTraversalCountProperty
		) {
			if (j.chessBoard.content.gameOver) {
				if (j.chessBoard.content.isCannonsWin)
					"【${GlobalVars.appConf.cannonText}】获胜！"
				else
					"【${GlobalVars.appConf.soldierText}】获胜！"
			} else {
				if (j.chessBoard.content.isCannonsTurn) {
					if (GlobalVars.cannonsUseAI)
						"电脑【${GlobalVars.appConf.cannonText}】" +
								"正在思考：${GlobalVars.aiTraversalCount}"
					else
						"轮到玩家【${GlobalVars.appConf.cannonText}】走棋"
				} else {
					if (GlobalVars.soldiersUseAI)
						"电脑【${GlobalVars.appConf.soldierText}】" +
								"正在思考：${GlobalVars.aiTraversalCount}"
					else
						"轮到玩家【${GlobalVars.appConf.soldierText}】走棋"
				}
			}
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
