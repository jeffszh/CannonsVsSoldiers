package cn.jeff.game.c3s15;

import cn.jeff.game.c3s15.board.ChessBoard;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;

public class MainWndJ {

	MainWnd k;
	public ChessBoard chessBoard;
	public Label label01;
	public Label label02;
	public Label statusLabel;
	public Label netStatusLabel;

	public void btnRestartClick() {
		k.btnRestartClick();
	}

	public void btnSetupClick() {
		k.btnSetupClick();
	}

	public void btnSetChannelClick() {
		k.btnSetChannelClick();
	}

	public void btnSetAiDepth() {
		k.btnSetAiDepth();
	}

}
