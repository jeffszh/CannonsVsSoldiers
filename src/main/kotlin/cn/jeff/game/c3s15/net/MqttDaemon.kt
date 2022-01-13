package cn.jeff.game.c3s15.net

import cn.jeff.game.c3s15.event.ReceivedMqttMsg
import org.fusesource.hawtbuf.UTF8Buffer
import org.fusesource.mqtt.client.BlockingConnection
import org.fusesource.mqtt.client.MQTT
import org.fusesource.mqtt.client.QoS
import org.fusesource.mqtt.client.Topic
import tornadofx.*
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread
import kotlin.random.Random

object MqttDaemon {

	// 服务器地址
//	private const val SERVER_ADDR = "broker.mqttdashboard.com"
//	private const val SERVER_ADDR = "test.mosquitto.org"
//	private const val SERVER_ADDR = "broker.hivemq.com"
	private const val SERVER_ADDR = "broker.emqx.io"

	// 主题前缀
	private const val TOPIC_PREFIX = "cn.jeff.game.C3S15"

	// 通道号
	var channelNum = 1234
		set(value) {
			field = value
			restart()
		}

	// 主题
	private val runningTopic get() = "$TOPIC_PREFIX/$channelNum"

	// QOS
	// private val usingQoS = QoS.AT_MOST_ONCE
	private val usingQoS = QoS.EXACTLY_ONCE

	// MQTT对象
	private val mqtt = MQTT().apply {
		clientId = UTF8Buffer(
			"GU${Random.nextInt(99999999)}".toByteArray()
		)
	}

	/** 给外面读取的 clientId */
	val clientId get() = mqtt.clientId.toString()

	// 发送和接收线程
	private var receiverThread: Thread? = null
	private var senderThread: Thread? = null

	// 发送缓冲区
	private val sendingQueue = LinkedBlockingQueue<String>()

	fun start() {
		thread(name = "MQTT_daemon") {
			println("version=${mqtt.version}")
			println("clientId=${mqtt.clientId}")
			println("keepAlive=${mqtt.keepAlive}")
			println("reconnectDelay=${mqtt.reconnectDelay}")
			println("reconnectDelayMax=${mqtt.reconnectDelayMax}")
			mqtt.setHost(SERVER_ADDR, 1883)
			val conn = mqtt.blockingConnection()
			conn.connect()
			Thread.sleep(1000)
			println("clientId=${mqtt.clientId}")
			println(conn.isConnected)

			receiverThread = thread(name = "MQTT_receiver") {
				try {
					receiver(conn)
				} catch (e: InterruptedException) {
					e.printStackTrace()
					conn.disconnect()
				}
			}
			senderThread = thread(name = "MQTT_sender") {
				try {
					sender(conn)
				} catch (e: InterruptedException) {
					e.printStackTrace()
					conn.disconnect()
				}
			}
		}
	}

	fun stop() {
		receiverThread?.interrupt()
		receiverThread = null
		senderThread?.interrupt()
		senderThread = null
	}

	private fun restart() {
		stop()
		start()
	}

	private fun receiver(conn: BlockingConnection) {
		conn.subscribe(arrayOf(Topic(runningTopic, usingQoS)))
		while (!Thread.interrupted()) {
			val msg = conn.receive()
			val txt = msg.payload.toString(Charsets.UTF_8)
			FX.eventbus.fire(ReceivedMqttMsg(txt))
			msg.ack()
		}
	}

	private fun sender(conn: BlockingConnection) {
		while (!Thread.interrupted()) {
			// val txt = "${mqtt.clientId}: ${sendingQueue.take()}"
			val txt = sendingQueue.take()
			conn.publish(runningTopic, txt.toByteArray(Charsets.UTF_8), usingQoS, false)
		}
	}

	fun sendMsg(txt: String) {
		sendingQueue.offer(txt)
	}

}
