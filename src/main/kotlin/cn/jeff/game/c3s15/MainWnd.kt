package cn.jeff.game.c3s15

import cn.jeff.game.c3s15.board.ChessCell
import javafx.fxml.FXMLLoader
import javafx.scene.canvas.Canvas
import javafx.scene.image.WritableImage
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.layout.BorderPane
import javafx.scene.text.Font
import tornadofx.*
import kotlin.math.abs

class MainWnd : View(GlobalVars.appConf.mainTitle) {

	override val root: BorderPane
	private val j: MainWndJ
	private val chessBoardCanvas: ChessCell

	init {
		primaryStage.isResizable = true

		val loader = FXMLLoader()
		root = loader.load(javaClass.getResourceAsStream("/cn/jeff/game/c3s15/MainWnd.fxml"))
		// val img = Image(javaClass.getResourceAsStream("/image/block.png"))
		val img = Canvas(80.0, 80.0).apply {
			val gc = graphicsContext2D
			isMouseTransparent = true
			gc.clearRect(0.0, 0.0, 80.0, 80.0)
			gc.fill = c(160, 160, 2)
			gc.fillRoundRect(2.0, 2.0, 76.0, 76.0, 82.0, 82.0)
			gc.strokeRoundRect(2.0, 2.0, 76.0, 76.0, 82.0, 82.0)
			gc.font = Font.font(50.0)
			gc.strokeText(GlobalVars.appConf.cannonText, 20.0, 60.0)
		}.snapshot(null, WritableImage(80, 80))
		j = loader.getController()
		j.k = this
		chessBoardCanvas = j.chessBoardCanvas
		chessBoardCanvas.width = 300.0
		chessBoardCanvas.height = 200.0

		var mx = 0.0
		var my = 0.0
		j.centerPane.setOnDragDetected { e ->
			val drg = j.centerPane.startDragAndDrop(TransferMode.MOVE)
			drg.setDragView(img, 24.0, 24.0)
			// 坑！必须设置内容，而且内容不为空，才能真正发起拖拽，否则没反应！
			drg.setContent(ClipboardContent().apply {
				putString("AbCd")
			})
			mx = e.x
			my = e.y
			println("start drag")
		}
		j.centerPane.setOnDragEntered { e ->
			println("enter: ${e.y}, ${e.y}")
		}
		j.centerPane.setOnDragOver { e ->
			println("drag: ${e.x}, ${e.y}")
			chessBoardCanvas.relocate(e.x, e.y)
			if (abs(mx - e.x) > 100 || abs(my - e.y) > 100) {
				e.acceptTransferModes(TransferMode.MOVE)
			}
		}
		j.centerPane.setOnDragDropped { e ->
			e.isDropCompleted = true
			e.consume()
			println("drop! ${e.x}, ${e.y}")
		}
		j.centerPane.setOnDragDone { e ->
			e.consume()
			println("done! ${e.x}, ${e.y}")
		}
	}

	fun btn01Click() {
		val gc = chessBoardCanvas.graphicsContext2D
		gc.fill = c(0, 160, 255)
		gc.fillRect(0.0, 0.0, chessBoardCanvas.width, chessBoardCanvas.height)
		gc.fill = c(255, 128, 0)
		gc.fillRoundRect(-80.0, 60.0, 120.0, 90.0, 8.0, 8.0)
		gc.strokeRoundRect(-80.0, 60.0, 120.0, 90.0, 8.0, 8.0)
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
