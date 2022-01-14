import cn.jeff.game.c3s15.net.MqttLink

fun main() {
	println("开始。")
	MqttLink(false) {
		onReceive {
		}
		onError {
		}
	}.use {
		Thread.sleep(10000)
	}
	println("结束")
}
