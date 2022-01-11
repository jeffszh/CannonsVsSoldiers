package cn.jeff.game.c3s15.net

import cn.jeff.game.c3s15.board.ChessBoardContent

class GameMessage(
	var state: NetGameState,
	var localId: String,
	var remoteId: String,
	var packedChessCells: Long = 0,
	var lastMove: ChessBoardContent.Move? = null,
)
