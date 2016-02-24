package com.eli.oneos.model.oneos.transfer;

public interface TransferControlListener {
	void onPause(TransferElement element);

	void onContinue(TransferElement element);

	void onRestart(TransferElement element);

	void onCancel(TransferElement element);
}
