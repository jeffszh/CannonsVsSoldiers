package cn.jeff.game.c3s15.net

import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

class MqttLink(initiative: Boolean, op: BaseNetLink.() -> Unit) : BaseNetLink(op) {

//	private const val TXT_OFFLINE = "离线"
//	private const val TXT_WAITING_CONNECTION = "正在等待连接……"
//	private const val TXT_CONNECTING = "正在连接……"
//	private const val TXT_CONN_DROP = "掉线了……"
//	private const val TXT_CONNECTED = "已跟对方建立连接。"

	private var workThread: Thread? = null
	private var heartBeatThread: Thread? = null

	private val localId get() = MqttDaemon.clientId
	private var remoteId = ""

	init {
		workThread = thread(name = "MQTT_LINK_WORK_THREAD") {
			MqttDaemon.clearReceivingQueue()
			try {
				if (initiative) {
					runInitiative()
				} else {
					runPassive()
				}
			} catch (e: InterruptedException) {
				// do nothing
			} catch (e: Exception) {
				doOnError(e)
			}
		}
	}

	override fun close() {
		workThread?.interrupt()
		workThread = null
		heartBeatThread?.interrupt()
		heartBeatThread = null
	}

	override fun sendData(data: String) {
		sendPacket(LinkPacket.PacketType.DATA, localId, remoteId, data)
	}

	private fun sendPacket(
		packetType: LinkPacket.PacketType,
		localId: String, remoteId: String,
		data: String
	) {
		val packet = LinkPacket(packetType, localId, remoteId, data)
		val txt = gson.toJson(packet)
		MqttDaemon.sendMsg(txt)
	}

	private fun receivePacket(timeOut: Long): LinkPacket? {
		val txt = MqttDaemon.receiveMsg(timeOut) ?: return null
		return try {
			gson.fromJson(txt, LinkPacket::class.java)
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}
	}

	private fun runInitiative() {
		while (true) {
			sendPacket(LinkPacket.PacketType.CONNECT, localId, "", "")
			nullPacket@ while (true) {
				val packet = receivePacket(1500) ?: break@nullPacket
				if (packet.packetType == LinkPacket.PacketType.CONN_ACK &&
					packet.remoteId == localId
				) {
					val remoteId = packet.localId
					sendPacket(LinkPacket.PacketType.CONNECTED, localId, remoteId, "")
					return runConnected(remoteId)
				}
			}
		}
	}

	private fun runPassive() {
		while (true) {
			val packet = receivePacket(20000)
			if (packet != null) {
				when (packet.packetType) {
					LinkPacket.PacketType.CONNECT -> {
						sendPacket(
							LinkPacket.PacketType.CONN_ACK,
							localId, packet.localId, ""
						)
					}
					LinkPacket.PacketType.CONNECTED -> {
						if (packet.remoteId == localId) {
							return runConnected(packet.localId)
						}
					}
					else -> {
						// do nothing
					}
				}
			}
		}
	}

	private fun runConnected(remoteId: String) {
		this.remoteId = remoteId
		doOnConnect()
		heartBeatThread = thread(name = "MQTT_LINK_HEARTBEAT_THREAD") {
			runHeartBeat()
		}
		try {
			var lastReceiveTime = Date().time
			while (true) {
				if ((Date().time - lastReceiveTime) > LINK_TIMEOUT)
					throw IOException("MqttLink超时断线！")
				val packet = receivePacket(LINK_TIMEOUT) ?: throw IOException("MqttLink超时断线！")
				if (packet.remoteId == localId) {
					lastReceiveTime = Date().time
				} else {
					continue
				}
				if (packet.packetType == LinkPacket.PacketType.DATA) {
					doOnReceived(packet.data)
				}
			}
		} catch (e: InterruptedException) {
			// do nothing
		} catch (e: Exception) {
			doOnError(e)
		}
	}

	private fun runHeartBeat() {
		try {
			while (true) {
				Thread.sleep(3000)
				sendPacket(LinkPacket.PacketType.HEARTBEAT, localId, remoteId, "")
			}
		} catch (e: InterruptedException) {
			// do nothing
		} catch (e: Exception) {
			doOnError(e)
		}
	}

}
