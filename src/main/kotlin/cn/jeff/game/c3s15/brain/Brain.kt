package cn.jeff.game.c3s15.brain

import cn.jeff.game.c3s15.GlobalVars
import cn.jeff.game.c3s15.MainWnd
import cn.jeff.game.c3s15.board.Chess
import cn.jeff.game.c3s15.board.ChessBoardContent
import cn.jeff.game.c3s15.event.MoveChessEvent
import javafx.concurrent.Task
import tornadofx.*

class Brain(private val chessBoardContent: ChessBoardContent) {

	private var currentTask: Task<Unit>? = null
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

	private fun changeAiSetting() {
		aiSettingChanging = true
		// 运用技巧，避免重复触发。
		runLater {
			if (aiSettingChanging) {
				println("-----===== 改变AI设置 =====-----")
				val runOnSide = if (chessBoardContent.gameOver) {
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
				println("-----===== $runOnSide =====-----")
				startRunning(runOnSide)
				aiSettingChanging = false
			}
		}
	}

	private fun stopPreviousTask() {
		currentTask?.cancel()
	}

	private fun startRunning(runOnSide: Chess) {
		stopPreviousTask()
		when (runOnSide) {
			Chess.SOLDIER, Chess.CANNON -> currentTask = runAsync(true) {
				aiRoutine(runOnSide)
			}
			Chess.EMPTY -> {
				// do nothing
			}
		}
	}

	private fun aiRoutine(runOnSide: Chess) {
		Thread.sleep(3000)
		find<MainWnd>().fire(
			MoveChessEvent(
				ChessBoardContent.Move(
					2, 4, 2, 2
				)
			)
		)
	}

}
