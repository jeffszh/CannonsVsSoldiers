package cn.jeff.game.c3s15.net

import cn.jeff.game.c3s15.GlobalVars
import cn.jeff.game.c3s15.MainWnd
import cn.jeff.game.c3s15.board.ChessBoardContent
import cn.jeff.game.c3s15.brain.PlayerType
import cn.jeff.game.c3s15.event.MoveChessEvent
import com.google.gson.GsonBuilder
import tornadofx.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object NetworkGameProcessor {

	private val gson = GsonBuilder().setPrettyPrinting().create()

	private val gameMsgQueue = LinkedBlockingQueue<GameMessage>()
	private val moveChessQueue = SynchronousQueue<Pair<Long, ChessBoardContent.Move>>()
	// private val moveChessQueue = Exchanger<Pair<Long, ChessBoardContent.Move>>()

	private val semPlayerSettingChange = Semaphore(0)

	@Volatile
	private var state = NetGameState.OFFLINE
		private set(value) {
			field = value
			runLater {
				GlobalVars.netGameStateProperty.value = value
			}
		}

	@Volatile
	private var restarting = false

	private val cannonsPlayerType get() = GlobalVars.cannonsPlayerType.value
	private val soldiersPlayerType get() = GlobalVars.soldiersPlayerType.value
	private val localChessBoard get() = find<MainWnd>().j.chessBoard

	private val localId get() = MqttDaemon.clientId
	private var pairedRemoteId = ""
	private var remoteNoResponseCount = 0
	private const val MAX_NO_RESPONSE_COUNT = 10

	private var localPackedChessBoardContent = 0L
	private var localLastMove: ChessBoardContent.Move? = null

	init {
		GlobalVars.cannonsPlayerType.onChange {
			semPlayerSettingChange.release()
		}
		GlobalVars.soldiersPlayerType.onChange {
			semPlayerSettingChange.release()
		}
	}

	fun start() {
		workThread.start()
	}

	fun stop() {
		restarting = false
		workThread.interrupt()
	}

	fun restart() {
		restarting = true
		workThread.interrupt()
		semPlayerSettingChange.release()
	}

	private val workThread = thread(start = false, name = this.javaClass.simpleName) {
		do {
			try {
				state = NetGameState.OFFLINE
				process()
			} catch (e: InterruptedException) {
				// e.printStackTrace()
			}
		} while (restarting)
	}

	/**
	 * # 握手流程
	 *
	 * * 首先，等待玩家配置改变，如果符合网络对战的设置，炮方进入被邀请状态，兵方开始邀请。
	 * * 兵方发出邀请，填写自己的ID，留空对方ID；炮方收到后，作出回应，同样填写自己ID，留空对方ID。
	 * * 兵方收到回应，同时填写自己ID和对方ID再次发出；炮方收到后，双方ID填好回应。
	 * * 兵方收到双方ID都填好的回应，作为已建立好连接的标志，进入对战状态。
	 * * 炮方收到对方要求走棋的消息（[NetGameState.REMOTE_TURN]），进入对战状态。
	 * * 之后双方不断交替要求走棋和回应未走棋或已走棋，游戏进行下去。
	 * * 直到双方分出胜负，或出现网络异常。
	 *
	 * ` 注意：此流程策略是隨意配對，基於本遊戲就幾個相熟的人玩，不可作為參考。
	 * 若要完善，必須要有房間的概念。 `
	 */
	private fun process() {
		// println("thread name is ${Thread.currentThread().name}")
		while (!Thread.interrupted()) {
			when (state) {
				NetGameState.OFFLINE -> doOffline()
				NetGameState.INVITING -> doInviting()
				NetGameState.WAIT_INV -> doWaitInvite()
				NetGameState.LOCAL_TURN -> doLocalTurn()
				NetGameState.REMOTE_TURN -> doRemoteTurn()
				NetGameState.GAME_OVER -> doIdle()
				NetGameState.LOST_CONN -> doIdle()
			}
		}
	}

	private fun doOffline() {
		semPlayerSettingChange.acquire()
		if (cannonsPlayerType == PlayerType.HUMAN &&
			soldiersPlayerType == PlayerType.NET
		) {
			// 炮方作为被邀请方
			state = NetGameState.WAIT_INV
		} else if (cannonsPlayerType == PlayerType.NET &&
			soldiersPlayerType == PlayerType.HUMAN
		) {
			// 兵方作为邀请方
			state = NetGameState.INVITING
		}
	}

	private fun doInviting() {
		println("发出邀请。")
		sendGameMsg(GameMessage(state, localId, ""))
		do {
			val receivedMsg = gameMsgQueue.poll(2500, TimeUnit.MILLISECONDS) ?: break
			println("收到消息……${receivedMsg.state}")
			if (receivedMsg.state == NetGameState.WAIT_INV) {
				println("localId=$localId remoteId=${receivedMsg.remoteId}")
				if (receivedMsg.remoteId.isEmpty()) {
					// 若对方仍然未配对好，与之配对。
					println("对方仍未配对，要求与之配对。")
					sendGameMsg(GameMessage(state, localId, receivedMsg.localId))
				} else if (receivedMsg.remoteId == localId) {
					// 若已跟自己配对，进入游戏。
					println("已配对，进入游戏。")
					pairedRemoteId = receivedMsg.localId
					state = NetGameState.REMOTE_TURN
					break
				}
			}
		} while (true)
	}

	private fun doWaitInvite() {
		// val receivedMsg = gameMsgQueue.poll(6400, TimeUnit.MILLISECONDS) ?: return
		val receivedMsg = gameMsgQueue.take()
		when (receivedMsg.state) {
			NetGameState.INVITING -> {
				if (receivedMsg.remoteId.isEmpty()) {
					// 收到邀请，作出回应。
					println("收到邀请消息。")
					sendGameMsg(GameMessage(state, localId, ""))
				} else if (receivedMsg.remoteId == localId) {
					// 收到配對消息，回應確認配對。
					println("收到配对邀请。")
					sendGameMsg(GameMessage(state, localId, receivedMsg.localId))
				}
			}
			NetGameState.REMOTE_TURN -> {
				if (receivedMsg.remoteId == localId) {
					pairedRemoteId = receivedMsg.localId
					localLastMove = null
					state = NetGameState.LOCAL_TURN
				}
			}
			else -> {
				// do nothing
			}
		}
	}

	private fun doRemoteTurn() {
		println("催促对方走棋。")
		sendGameMsg(GameMessage(state, localId, pairedRemoteId))
		while (true) {
			val receivedMsg = gameMsgQueue.poll(2000, TimeUnit.MILLISECONDS)
			if (receivedMsg == null) {
				remoteNoResponseCount++
				if (remoteNoResponseCount > MAX_NO_RESPONSE_COUNT) {
					state = NetGameState.LOST_CONN
				}
				// 收到空消息，就是已经过去两秒了，跳出，再次发出催促消息。
				break
			}
			if (receivedMsg.state == NetGameState.LOCAL_TURN &&
				receivedMsg.remoteId == localId &&
				receivedMsg.localId == pairedRemoteId
			) {
				remoteNoResponseCount = 0
				val remoteLastMove = receivedMsg.lastMove
				if (remoteLastMove != null) {
					// 若对方有走棋，判断跟本地棋盘是否一致。
					val newChessBoardContent = localChessBoard.content.clone()
					newChessBoardContent.applyMove(remoteLastMove)
					if (newChessBoardContent.compressToInt64() == receivedMsg.packedChessCells) {
						// 棋盘一致，确认走棋。
						FX.eventbus.fire(MoveChessEvent(remoteLastMove))
						println("改状态")
						state = if (newChessBoardContent.gameOver) {
							NetGameState.GAME_OVER
						} else {
							localLastMove = null
							NetGameState.LOCAL_TURN
						}
						println("状态为：$state")
						break
					}
				}
			} else if (receivedMsg.state == NetGameState.REMOTE_TURN &&
				receivedMsg.remoteId == localId &&
				receivedMsg.localId == pairedRemoteId &&
				localLastMove != null
			) {
				// 若对方仍然处于[NetGameState.REMOTE_TURN]状态，
				// 即上次的走棋消息对方仍未收到，补发给对方。
				sendGameMsg(
					GameMessage(
						NetGameState.LOCAL_TURN, localId, pairedRemoteId,
						localPackedChessBoardContent, localLastMove
					)
				)
			}
		}
	}

	private fun doLocalTurn() {
		// 轮到本地走棋的时候，其实根本不需要接收对方发来的消息，没有真正有意义的消息。
		// 因此，专门等待本地玩家走棋就可以了。
		val playerMove = moveChessQueue.poll(1300, TimeUnit.MILLISECONDS)
		if (playerMove == null) {
			// 还没走棋，发走棋为空的消息给对方。
			sendGameMsg(GameMessage(state, localId, pairedRemoteId))
		} else {
			// 已经走棋了，先存起来，然后发给对方。
			localPackedChessBoardContent = playerMove.first
			localLastMove = playerMove.second
			sendGameMsg(
				GameMessage(
					state, localId, pairedRemoteId,
					localPackedChessBoardContent, localLastMove
				)
			)
			// 先清空接收队列，然后切换到对方走棋状态。
			gameMsgQueue.clear()
			state = NetGameState.REMOTE_TURN
		}
		// 空读，避免消息堆积太多。
		gameMsgQueue.poll()
	}

	private fun doIdle() {
		while (semPlayerSettingChange.tryAcquire()) {
			// do nothing
		}
		state = NetGameState.OFFLINE
	}

	private fun sendGameMsg(msg: GameMessage) {
		MqttDaemon.sendMsg(gson.toJson(msg))
	}

	/** 收到 MQTT 消息时，从这里通知进来。 */
	fun onMqttReceived(txtPayload: String) {
		val msg = try {
			gson.fromJson(txtPayload, GameMessage::class.java)
		} catch (e: Exception) {
			e.printStackTrace()
			return
		}
		gameMsgQueue.offer(msg)
	}

	/** 本地走棋，从这里通知进来。 */
	fun applyLocalMove(packedChessCells: Long, move: ChessBoardContent.Move) {
		if (state == NetGameState.LOCAL_TURN) {
			moveChessQueue.put(packedChessCells to move)
		}
	}

}
