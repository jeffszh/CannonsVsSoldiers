import cn.jeff.game.c3s15.net.MqttDaemon

fun main() {
	MqttDaemon.start()
	println("MQTT 的 clientId = ${MqttDaemon.clientId}")
	Thread.sleep(5000)
	MqttDaemon.stop()
}
