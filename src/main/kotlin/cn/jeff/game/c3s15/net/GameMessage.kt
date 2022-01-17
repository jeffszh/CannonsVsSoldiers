package cn.jeff.game.c3s15.net

import cn.jeff.game.c3s15.board.ChessBoardContent

class GameMessage(
//	val state: NetGameState,
//	val localId: String,
//	val remoteId: String,
//	val packedChessCells: Long = 0,
//	val lastMove: ChessBoardContent.Move? = null,
	val packedChessCells: Long,
	val lastMove: ChessBoardContent.Move,
)
