package com.acromere.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * A POJO that represents a person that maintains a product.
 *
 * <p>A maintainer is a {@link Contributor} with no additional state. The
 * people maintaining a product are listed in its {@link ProductCard}.</p>
 *
 * @see Contributor
 * @see ProductCard
 */
@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )
public class Maintainer extends Contributor implements Serializable {}
