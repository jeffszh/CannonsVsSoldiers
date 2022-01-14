package cn.jeff.game.c3s15.net

class MqttLink(initiative: Boolean, op: MqttLink.() -> Unit) : AutoCloseable {

//	private const val TXT_OFFLINE = "离线"
//	private const val TXT_WAITING_CONNECTION = "正在等待连接……"
//	private const val TXT_CONNECTING = "正在连接……"
//	private const val TXT_CONN_DROP = "掉线了……"
//	private const val TXT_CONNECTED = "已跟对方建立连接。"

	private var onReceiveFunc: (String) -> Unit = {}

	private var onErrorFunc: (Exception) -> Unit = {}

	init {
		op()
		if (initiative) {
		}
	}

	fun onReceive(func: (data: String) -> Unit) {
		onReceiveFunc = func
	}

	fun onError(func: (e: Exception) -> Unit) {
		onErrorFunc = func
	}

	override fun close() {
	}

}
