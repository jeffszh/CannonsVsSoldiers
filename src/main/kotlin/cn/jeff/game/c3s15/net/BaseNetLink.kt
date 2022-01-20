package cn.jeff.game.c3s15.net

import cn.jeff.game.c3s15.GlobalVars
import com.google.gson.GsonBuilder

abstract class BaseNetLink(op: BaseNetLink.() -> Unit) : AutoCloseable {

	companion object {
		internal const val LINK_TIMEOUT = 8000L
		internal val gson = GsonBuilder().setPrettyPrinting().create()
	}

	private var onConnectFunc: () -> Unit = {}
	private var onErrorFunc: (Exception) -> Unit = {}

	@Volatile
	var connected = false
		private set

	init {
		this.op()
	}

	fun onConnect(func: () -> Unit) {
		onConnectFunc = func
	}

	fun onError(func: (e: Exception) -> Unit) {
		onErrorFunc = func
	}

	abstract fun sendData(data: String)

	protected fun doOnReceived(data: String) {
		NetworkGameProcessor.onDataReceived(data)
	}

	protected fun doOnConnect() {
		connected = true
		GlobalVars.netLink = this
		onConnectFunc()
	}

	protected fun doOnError(e: Exception) {
		GlobalVars.netLink?.close()
		GlobalVars.netLink = null
		onErrorFunc(e)
	}

}
