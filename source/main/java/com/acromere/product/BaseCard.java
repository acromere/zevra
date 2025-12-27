package com.acromere.product;

import com.acromere.util.IdGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors( chain = true )
public abstract class BaseCard {

	private String internalId;

	protected BaseCard() {
		this.internalId = IdGenerator.getId();
	}

}
