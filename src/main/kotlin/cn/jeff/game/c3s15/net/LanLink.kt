package cn.jeff.game.c3s15.net

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class LanLink(initiative: Boolean, op: BaseNetLink.() -> Unit) : BaseNetLink(op) {

	companion object {
		private const val UDP_PORT = 7447
		private const val TCP_PORT = 7448
	}

	private var workThread: Thread? = null
	private var serverSocket: ServerSocket? = null
	private var socket: Socket? = null

	init {
		workThread = thread(name = "LAN_LINK_WORK_THREAD") {
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

	private fun runInitiative() {
		socket = bluetoothDevice.createRfcommSocketToServiceRecord(
			UUID.fromString(GlobalVars.BLUETOOTH_LINK_UUID)
		)
		socket?.connect()

		runConnected()
	}

	private fun runPassive() {
		DatagramSocket(UDP_PORT).use { udpSocket ->
			val buffer = ByteArray(2048)
			val udpPacket = DatagramPacket(buffer, buffer.size)
			val recLen = udpSocket.receive(udpPacket)
		}

		serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
			"C3S15",
			UUID.fromString(GlobalVars.BLUETOOTH_LINK_UUID)
		)
		socket = serverSocket?.accept()

		// 馬上可以關掉，不會影響已連接好的socket。
		serverSocket?.close()
		serverSocket = null

		runConnected()
	}

	private fun runConnected() {
		doOnConnect()
		val input = socket?.inputStream ?: return
		val buffer = ByteArray(2048)
		do {
			val recLen = input.read(buffer)
			val txt = String(buffer, 0, recLen, Charsets.UTF_8)
			doOnReceived(txt)
		} while (recLen > 0)
	}

	override fun sendData(data: String) {
		socket?.outputStream?.write(data.toByteArray())
	}

	override fun close() {
		workThread?.interrupt()
		workThread = null
		socket?.close()
		socket = null
		serverSocket?.close()
		serverSocket = null
	}

}
