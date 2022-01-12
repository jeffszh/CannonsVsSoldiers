package cn.jeff.game.c3s15.net

import cn.jeff.game.c3s15.GlobalVars
import cn.jeff.game.c3s15.board.ChessBoardContent
import cn.jeff.game.c3s15.brain.PlayerType
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
	private var state = NetGameState.OFFLINE

	@Volatile
	private var restarting = false

	private val cannonsPlayerType get() = GlobalVars.cannonsPlayerType.value
	private val soldiersPlayerType get() = GlobalVars.soldiersPlayerType.value

	private val localId get() = MqttDaemon.clientId
	private var pairedRemoteId = ""

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
				NetGameState.LOCAL_TURN -> TODO()
				NetGameState.REMOTE_TURN -> TODO()
				NetGameState.GAME_OVER -> TODO()
				NetGameState.LOST_CONN -> TODO()
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
		sendGameMsg(GameMessage(NetGameState.INVITING, localId, ""))
		do {
			val receivedMsg = gameMsgQueue.poll(2500, TimeUnit.MILLISECONDS) ?: break
			if (receivedMsg.state == NetGameState.WAIT_INV) {
				if (receivedMsg.remoteId.isEmpty()) {
					// 若对方仍然未配对好，与之配对。
					sendGameMsg(
						GameMessage(
							NetGameState.INVITING,
							localId, receivedMsg.localId
						)
					)
					continue
				} else if (receivedMsg.remoteId == localId) {
					// 若已跟自己配对，进入游戏。
					pairedRemoteId = receivedMsg.localId
					state = NetGameState.REMOTE_TURN
				}
			}
		} while (false)
	}

	private fun doWaitInvite() {
		gameMsgQueue.poll(6400, TimeUnit.MILLISECONDS)?.also { receivedMsg ->
			when (receivedMsg.state) {
				NetGameState.INVITING -> {
					if (receivedMsg.remoteId.isEmpty()) {
						// 收到邀请，作出回应。
						sendGameMsg(
							GameMessage(
								NetGameState.WAIT_INV, localId, ""
							)
						)
					} else if (receivedMsg.remoteId == localId) {
						// 收到配對消息，回應確認配對。
						sendGameMsg(
							GameMessage(
								NetGameState.WAIT_INV, localId, receivedMsg.remoteId
							)
						)
					}
				}
				NetGameState.REMOTE_TURN -> {
				}
				else -> {
					// do nothing
				}
			}
		}
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

}
