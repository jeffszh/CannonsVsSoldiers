package cn.jeff.game.c3s15.event

import cn.jeff.game.c3s15.board.ChessBoardContent
import tornadofx.*

class MoveChessEvent(val move: ChessBoardContent.Move, val byRemote: Boolean = false) :
	FXEvent(runOn = EventBus.RunOn.ApplicationThread)
