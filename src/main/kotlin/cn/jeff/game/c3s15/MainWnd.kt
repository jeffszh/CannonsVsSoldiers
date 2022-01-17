package cn.jeff.game.c3s15

import cn.jeff.game.c3s15.brain.PlayerType
import cn.jeff.game.c3s15.event.MoveChessEvent
import cn.jeff.game.c3s15.event.NetStatusChangeEvent
import cn.jeff.game.c3s15.net.MqttDaemon
import cn.jeff.game.c3s15.net.MqttLink
import cn.jeff.game.c3s15.net.NetworkGameProcessor
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.stage.StageStyle
import tornadofx.*

class MainWnd : View(GlobalVars.appConf.mainTitle) {

	override val root: BorderPane
	val j: MainWndJ

	init {
		primaryStage.isResizable = true

		val loader = FXMLLoader()
		root = loader.load(javaClass.getResourceAsStream("/cn/jeff/game/c3s15/MainWnd.fxml"))
		j = loader.getController()
		j.k = this

		j.label01.textProperty().bind(GlobalVars.cannonsPlayerType.stringBinding {
			"${GlobalVars.appConf.cannonText}：${it!!.text}"
		})
		j.label02.textProperty().bind(GlobalVars.soldiersPlayerType.stringBinding {
			"${GlobalVars.appConf.soldierText}：${it!!.text}"
		})
		j.statusLabel.textProperty().bind(j.chessBoard.content.moveCountProperty.stringBinding(
			j.chessBoard.content.gameOverProperty,
			GlobalVars.cannonsPlayerType,
			GlobalVars.soldiersPlayerType,
			GlobalVars.aiTraversalCountProperty
		) {
			if (j.chessBoard.content.gameOver) {
				if (j.chessBoard.content.isCannonsWin)
					"【${GlobalVars.appConf.cannonText}】获胜！"
				else
					"【${GlobalVars.appConf.soldierText}】获胜！"
			} else {
				if (j.chessBoard.content.isCannonsTurn) {
					when (GlobalVars.cannonsPlayerType.value!!) {
						PlayerType.HUMAN -> "轮到玩家【${GlobalVars.appConf.cannonText}】走棋"
						PlayerType.AI -> "电脑【${GlobalVars.appConf.cannonText}】" +
								"正在思考：${GlobalVars.aiTraversalCount}"
						PlayerType.NET -> "等待网友【${GlobalVars.appConf.cannonText}】走棋"
					}
				} else {
					when (GlobalVars.soldiersPlayerType.value!!) {
						PlayerType.HUMAN -> "轮到玩家【${GlobalVars.appConf.soldierText}】走棋"
						PlayerType.AI -> "电脑【${GlobalVars.appConf.soldierText}】" +
								"正在思考：${GlobalVars.aiTraversalCount}"
						PlayerType.NET -> "等待网友【${GlobalVars.appConf.soldierText}】走棋"
					}
				}
			}
		})
//		j.netStatusLabel.textProperty().bind(GlobalVars.netGameStateProperty.asString())

		subscribe<MoveChessEvent> { e ->
			j.chessBoard.applyMove(e.move, e.byRemote)
		}
//		subscribe<ReceivedMqttMsg> { e ->
//			NetworkGameProcessor.onMqttReceived(e.msg)
//		}
		subscribe<NetStatusChangeEvent> {
			j.netStatusLabel.text = if (GlobalVars.mqttLink == null)
				"未连线"
			else
				"已连线"
		}
	}

	fun btnRestartClick() {
		j.chessBoard.content.setInitialContent()
		// NetworkGameProcessor.restart()
		showConnectDialogIfNeed()
	}

	private fun showConnectDialogIfNeed() {
		if (GlobalVars.cannonsPlayerType.value == PlayerType.NET ||
			GlobalVars.soldiersPlayerType.value == PlayerType.NET
		) {
			GlobalVars.mqttLink?.close()
			GlobalVars.mqttLink = null
			dialog("连接方式") {
				style = "-fx-font-family: 'Courier New'; -fx-font-size: 20;"
				alignment = Pos.CENTER
				hbox {
					spacing = 10.0
					button("蓝牙") {
						action { }
					}
					button("互联网") {
						action {
							close()
							showMqttWaitConnectDialog()
						}
					}
					button("取消") {
						action { close() }
					}
				}
			}
		}
	}

	private fun showMqttWaitConnectDialog() {
		val (title, initiative) = if (GlobalVars.cannonsPlayerType.value == PlayerType.NET) {
			"正在等待网友连接……" to false
		} else {
			"正在连接网友……" to true
		}
		dialog(title) {
			style = "-fx-font-family: 'Courier New'; -fx-font-size: 20;"
			alignment = Pos.CENTER
			GlobalVars.mqttLink?.close()
			GlobalVars.mqttLink = null
			val mqttLink = MqttLink(initiative) {
				onConnect {
					runLater {
						this@dialog.close()
						information("成功连接网友。")
						GlobalVars.mqttLink = this
					}
				}
				onError {
					runLater {
						this@dialog.close()
						warning("出错：${it.message}")
						GlobalVars.mqttLink?.close()
						GlobalVars.mqttLink = null
					}
				}
				onReceive {
					NetworkGameProcessor.onMqttReceived(it)
				}
			}
			style = "-fx-font-family: 'Courier New'; -fx-font-size: 20;"
			button("取消") {
				action {
					mqttLink.close()
					close()
				}
			}
		}
	}

	fun btnSetupClick() {
		find(SetupDialog::class).openModal(StageStyle.UTILITY, resizable = false)
	}

	fun btnSetChannelClick() {
		dialog("对战通道") {
			style = "-fx-font-family: 'Courier New'; -fx-font-size: 20;"
			alignment = Pos.CENTER
			spacing = 10.0
			paddingAll = 20.0
			val inputText = textfield(MqttDaemon.channelNum.toString())
			hbox {
				alignment = Pos.CENTER
				spacing = 10.0
				label("通道号")
				add(inputText)
			}
			hbox {
				alignment = Pos.CENTER
				spacing = 10.0
				button("确定") {
					isDefaultButton = true
					action {
						val txt = inputText.text
						val num = if (txt.isInt()) txt.toInt() else -1
						if (num in 0..99999) {
							MqttDaemon.channelNum = num
							close()
						} else {
							information("请输入5位以内的数字。")
						}
					}
				}
				button("取消") {
					action {
						close()
					}
				}
			}
		}
	}

	fun btnSetAiDepth() {
		dialog("AI設置") {
			style = "-fx-font-family: 'Courier New'; -fx-font-size: 20;"
			alignment = Pos.CENTER
			spacing = 10.0
			paddingAll = 20.0
			val inputText = textfield(GlobalVars.appConf.aiDepth.toString())
			hbox {
				alignment = Pos.CENTER
				spacing = 10.0
				label("AI计算深度")
				add(inputText)
			}
			hbox {
				alignment = Pos.CENTER
				spacing = 10.0
				button("确定") {
					isDefaultButton = true
					action {
						val txt = inputText.text
						val num = if (txt.isInt()) txt.toInt() else -1
						if (num in 1..9) {
							GlobalVars.appConf.aiDepth = num
							GlobalVars.saveConf()
							close()
						} else {
							information("请输入1-9。")
						}
					}
				}
				button("取消") {
					action {
						close()
					}
				}
			}
		}
	}

}
