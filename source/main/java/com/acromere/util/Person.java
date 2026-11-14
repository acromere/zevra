package com.acromere.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * A POJO that is a simple representation of a person that is intended to
 * be marshaled and unmarshalled.
 */
@Setter
@Getter
@JsonIgnoreProperties( ignoreUnknown = true )
public class Person implements Serializable {

	/** The person's name. */
	private String name;

	/** The person's email address. */
	private String email;

	/** The person's time zone. */
	private String timezone;

	/**
	 * Returns the person's name, which serves as a compact textual
	 * representation of this person.
	 *
	 * @return The person's name, or {@code null} if not set
	 */
	@Override
	public String toString() {
		return name;
	}

}
