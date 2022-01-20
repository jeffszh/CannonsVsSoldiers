package cn.jeff.game.c3s15

import cn.jeff.game.c3s15.net.MqttDaemon
import javafx.stage.Stage
import tornadofx.*

class Cannons3VsSoldiers15 : App(MainWnd::class) {

	init {
		GlobalVars.loadConf()
		GlobalVars.saveConf()
	}

	override fun start(stage: Stage) {
		super.start(stage)
		MqttDaemon.start()
		// NetworkGameProcessor.start()
	}

	override fun stop() {
		super.stop()
		MqttDaemon.stop()
		// NetworkGameProcessor.stop()
		GlobalVars.netLink?.close()
		GlobalVars.netLink = null
	}

}
