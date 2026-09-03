package com.acromere.data;

class NamedDataNode extends MockDataNode {

	public NamedDataNode() {
		defineNaturalKey( "name" );
	}

	public String getName(){
		return getValue( "name" );
	}

	public void setName( String name ) {
		setValue( "name", name );
	}

}
