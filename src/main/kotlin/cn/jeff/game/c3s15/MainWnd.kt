package cn.jeff.game.c3s15

import cn.jeff.game.c3s15.board.ChessBoardContent
import javafx.fxml.FXMLLoader
import javafx.scene.layout.BorderPane
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
	}

	fun btnRestartClick() {
		j.chessBoard.content.setInitialContent()
	}

	fun btn02Click() {
//		j.chessBoard.content.lastMove.value = ChessBoardContent.Move(
//			3, 1, 1, 1
//		)
	}

}
