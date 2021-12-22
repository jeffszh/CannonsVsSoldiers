package cn.jeff.game.c3s15.brain

import tornadofx.*
import java.util.*

class Brain : Component() {

	fun startRunning() {
		runAsync(true) {
			while (true) {
				Thread.sleep(3000)
				println("时间：${Date()}")
			}
		}
	}

}
