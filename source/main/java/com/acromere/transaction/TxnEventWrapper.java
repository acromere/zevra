package com.acromere.transaction;

record TxnEventWrapper(TxnEventTarget target, TxnEvent event) {

	TxnEventWrapper {
		if( target == null ) throw new NullPointerException( "Target cannot be null" );
		if( event == null ) throw new NullPointerException( "Event cannot be null" );
	}

}
