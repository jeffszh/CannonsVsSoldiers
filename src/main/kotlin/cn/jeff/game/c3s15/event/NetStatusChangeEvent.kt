package cn.jeff.game.c3s15.event

import tornadofx.*

class NetStatusChangeEvent :
	FXEvent(runOn = EventBus.RunOn.ApplicationThread)
