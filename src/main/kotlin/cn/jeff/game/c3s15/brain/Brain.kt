package cn.jeff.game.c3s15.brain

import cn.jeff.game.c3s15.GlobalVars
import cn.jeff.game.c3s15.board.Chess
import cn.jeff.game.c3s15.board.ChessBoardContent
import tornadofx.*

class Brain(private val chessBoardContent: ChessBoardContent) {

	private var currentTask: FXTask<*>? = null
	private var currentSide = Chess.EMPTY
	private var aiSettingChanging = false

	init {
		GlobalVars.cannonsUseAIProperty.onChange {
			changeAiSetting()
		}
		GlobalVars.soldiersUseAIProperty.onChange {
			changeAiSetting()
		}
		chessBoardContent.gameOverProperty.onChange {
			changeAiSetting()
		}
		chessBoardContent.moveCountProperty.onChange {
			changeAiSetting()
		}
		changeAiSetting()
	}

	fun startRunning() {
		/*
			println("===================================================")
			val vv = GlobalVars.cannonsUseAIProperty.objectBinding(GlobalVars.soldiersUseAIProperty) {
				when {
					GlobalVars.cannonsUseAI -> Chess.CANNON
					GlobalVars.soldiersUseAI -> Chess.SOLDIER
					else -> Chess.EMPTY
				}
			}
	//		object : ObjectBinding<Chess>() {
	//			init {
	//				bind(GlobalVars.cannonsUseAIProperty, GlobalVars.soldiersUseAIProperty)
	//			}
	//
	//			override fun computeValue(): Chess =
	//				when {
	//					GlobalVars.cannonsUseAI -> Chess.CANNON
	//					GlobalVars.soldiersUseAI -> Chess.SOLDIER
	//					else -> Chess.EMPTY
	//				}
	//		}
			vv.onChange {
				println("--------------------- $it ----------------------------")
			}
			val ww = SimpleObjectProperty(Chess.EMPTY)
			ww.bind(vv)
			ww.onChange {
				println("---------------------==== $it ====----------------------------")
			}

		 */

		/*
		val xx = GlobalVars.soldiersUseAIProperty and GlobalVars.cannonsUseAIProperty
		println(xx)
		val runOnWhichSide = SimpleObjectProperty<Chess>().objectBinding(
			GlobalVars.soldiersUseAIProperty,
			GlobalVars.cannonsUseAIProperty,
			chessBoardContent.moveCountProperty,
			chessBoardContent.gameOverProperty
		) {
			println("改............................................")
			if (chessBoardContent.gameOver) {
				Chess.EMPTY
			} else {
				if (chessBoardContent.isCannonsTurn) {
					if (GlobalVars.cannonsUseAI) Chess.CANNON
					else Chess.EMPTY
				} else {
					if (GlobalVars.soldiersUseAI) Chess.SOLDIER
					else Chess.EMPTY
				}
			}
		}
		val rs = SimpleObjectProperty<Chess>()
		rs.bind(runOnWhichSide)
		rs.onChange {
			println("改变！ $it")
		}

		 */

//		runAsync(true) {
//			while (true) {
//				Thread.sleep(3000)
//				println("时间：${Date()}")
//			}
//		}
	}

	private fun changeAiSetting() {
		aiSettingChanging = true
		// 运用技巧，避免重复触发。
		runLater {
			println("-----===== 改变AI设置 =====-----")
			currentSide = if (chessBoardContent.gameOver) {
				Chess.EMPTY
			} else {
				if (chessBoardContent.isCannonsTurn) {
					if (GlobalVars.cannonsUseAI) Chess.CANNON
					else Chess.EMPTY
				} else {
					if (GlobalVars.soldiersUseAI) Chess.SOLDIER
					else Chess.EMPTY
				}
			}
			println("-----===== $currentSide =====-----")
			aiSettingChanging = false
		}
	}

}
