package com.acromere.data;

import lombok.Getter;

@Getter
public class DataNodeLink<T extends DataNode> extends IdDataNode {

	private final T node;

	public DataNodeLink( T node ) {
		this.node = node;
	}

	@Override
	public String toString() {
		return "NodeLink@" + node;
	}

	public static <T extends DataNode> DataNodeLink<T> of( T node ) {
		return new DataNodeLink<>( node );
	}

}
