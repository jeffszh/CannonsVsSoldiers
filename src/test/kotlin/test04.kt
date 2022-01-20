import cn.jeff.game.c3s15.net.MqttDaemon
import cn.jeff.game.c3s15.net.MqttLink
import java.util.concurrent.Semaphore

fun main() {
	println("开始。")
	MqttDaemon.start()
	val semConnected = Semaphore(0)
	MqttLink(true) {
		onConnect {
			semConnected.release()
		}
//		onReceive {
//			println("收到数据：$it")
//		}
		onError { e ->
			println("=======================================================")
			println(e.message)
			println("=======================================================")
		}
	}.use { mqttLink ->
		semConnected.acquire()
		mqttLink.sendData("你好！我是张三。")
		Thread.sleep(2000)
		mqttLink.sendData("压伤的芦苇他不折断。")
		Thread.sleep(2000)
	}
	MqttDaemon.stop()
	println("结束")
}
