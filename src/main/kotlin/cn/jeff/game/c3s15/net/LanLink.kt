package cn.jeff.game.c3s15.net

import java.io.IOException
import java.net.*
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
		repeat(5) {
			try {
				DatagramSocket().use { udpSocket ->
					val data = "hello".toByteArray()
					val udpPacket = DatagramPacket(
						data, data.size,
						InetAddress.getByName("255.255.255.255"), UDP_PORT
					)
					udpSocket.broadcast = true
					udpSocket.send(udpPacket)
					udpSocket.soTimeout = 2000
					udpSocket.receive(udpPacket)
					// 若成功收到回應，用對方的地址建立TCP連接。
					socket = Socket().apply {
						// connect(udpPacket.socketAddress, 3000)
						connect(InetSocketAddress(udpPacket.address, TCP_PORT), 3000)
					}
				}
				if (socket?.isConnected == true) {
					return runConnected()
				}
			} catch (e: SocketTimeoutException) {
				// do nothing
			}
		}
		throw IOException("连接失败！")
	}

	private fun runPassive() {
		DatagramSocket(UDP_PORT).use { udpSocket ->
			val buffer = ByteArray(2048)
			val udpPacket = DatagramPacket(buffer, buffer.size)
			udpSocket.receive(udpPacket)
			// 不必理會收到什麼，隨便回應什麼都可以，目的是把自己的地址發給對方。
			udpSocket.send(udpPacket)
		}

		serverSocket = ServerSocket(TCP_PORT)
		socket = serverSocket?.accept()

		// TCP跟藍牙不一樣，不能關掉serverSocket，否則已建立的連接會斷開。
		// serverSocket?.close()
		// serverSocket = null

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
