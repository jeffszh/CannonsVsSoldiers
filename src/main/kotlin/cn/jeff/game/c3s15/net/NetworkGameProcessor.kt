package cn.jeff.game.c3s15.net

import cn.jeff.game.c3s15.board.ChessBoardContent
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.SynchronousQueue
import kotlin.concurrent.thread

object NetworkGameProcessor {

	private val gameMsgQueue = LinkedBlockingQueue<GameMessage>()
	private val moveChessQueue = SynchronousQueue<Pair<Long, ChessBoardContent.Move>>()
	// private val moveChessQueue = Exchanger<Pair<Long, ChessBoardContent.Move>>()

	fun start() {
		workThread.start()
	}

	fun stop() {
		workThread.interrupt()
	}

	private val workThread = thread(start = false, name = this.javaClass.simpleName) {
		try {
			process()
		} catch (e: InterruptedException) {
			// e.printStackTrace()
		}
	}

	private fun process() {
		println("thread name is ${Thread.currentThread().name}")
	}

}
