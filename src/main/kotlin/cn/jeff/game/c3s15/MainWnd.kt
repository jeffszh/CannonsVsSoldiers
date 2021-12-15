package cn.jeff.game.c3s15

import javafx.fxml.FXMLLoader
import javafx.scene.canvas.Canvas
import javafx.scene.layout.BorderPane
import tornadofx.*

class MainWnd : View(GlobalVars.appConf.mainTitle) {

	override val root: BorderPane
	private val j: MainWndJ
	private val chessBoardCanvas: Canvas

	init {
		primaryStage.isResizable = true

		val loader = FXMLLoader()
		root = loader.load(javaClass.getResourceAsStream("/cn/jeff/game/c3s15/MainWnd.fxml"))
		j = loader.getController()
		j.k = this

		chessBoardCanvas = Canvas(300.0, 200.0)
		j.centerPane.add(chessBoardCanvas)
	}

	fun btn01Click() {
		val gc = chessBoardCanvas.graphicsContext2D
		gc.fill = c(0, 160, 255)
		gc.fillRect(0.0, 0.0, chessBoardCanvas.width, chessBoardCanvas.height)
		gc.fill = c(255, 128, 0)
		gc.fillRoundRect(80.0, 60.0, 120.0, 90.0, 8.0, 8.0)
		gc.strokeRoundRect(80.0, 60.0, 120.0, 90.0, 8.0, 8.0)
//		j.centerPane.clear()
//		j.centerPane.children.add(canvas)
//		val pane = Pane()
//		pane.children.add(canvas)
		println(j.centerPane.children[0])
	}

	fun btn02Click() {
		val gc = chessBoardCanvas.graphicsContext2D
		gc.clearRect(0.0, 0.0, chessBoardCanvas.width, chessBoardCanvas.height)
	}

}
