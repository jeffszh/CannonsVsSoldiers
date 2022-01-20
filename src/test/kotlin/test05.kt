import cn.jeff.game.c3s15.net.MqttDaemon
import cn.jeff.game.c3s15.net.MqttLink
import java.util.concurrent.Semaphore

fun main() {
	println("开始。")
	MqttDaemon.start()
	val semConnected = Semaphore(0)
	MqttLink(false) {
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
		Thread.sleep(500)
		mqttLink.sendData("你好！我是李四。")
		Thread.sleep(2000)
		mqttLink.sendData("将残的灯火他不吹灭。")
		Thread.sleep(15000)
	}
	MqttDaemon.stop()
	println("结束")
}
