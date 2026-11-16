package com.acromere.product;

import com.acromere.util.Person;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A POJO that represents a person that contributes to a cause.
 */
@Setter
@Getter
@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )
public class Contributor extends Person implements Serializable {

	/**
	 * The name of the organization the contributor belongs to.
	 */
	private String organization;

	/**
	 * The url of the organization the contributor belongs to.
	 */
	private String organizationUrl;

	/**
	 * The roles the contributor has.
	 */
	private List<String> roles = new ArrayList<>();

	/**
	 * Return the contributor name and roles.
	 *
	 * @return A string of the form "Contributor{name=name roles=roles}"
	 */
	@Override
	public String toString() {
		return "Contributor{" + "name='" + getName() + " roles=" + roles + '}';
	}

}
