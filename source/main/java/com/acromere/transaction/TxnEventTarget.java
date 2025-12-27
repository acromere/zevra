package com.acromere.transaction;

public interface TxnEventTarget {

	void dispatch( TxnEvent event );

}
