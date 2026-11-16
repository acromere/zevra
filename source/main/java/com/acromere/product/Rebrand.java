package com.acromere.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Settings used to rebrand a packaged program.
 *
 * <p>A rebrand allows a product to be distributed under a different identity
 * by overriding the splash screen background and title, the product icon, and
 * the classes of the modules that are loaded. This is useful when a program
 * is packaged for a different customer or brand while sharing the same
 * underlying code.</p>
 *
 * @see ProductCard
 */
@Setter
@Getter
@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )
public class Rebrand implements Serializable {

	/**
	 * The class of the splash screen background image.
	 */
	private String splashScreenBackgroundClass;

	/**
	 * The font size of the splash screen title.
	 */
	private Double splashScreenTitleFontSize;

	/**
	 * The class of the product icon.
	 */
	private String productIconClass;

	/**
	 * The classes of the modules to load.
	 */
	private List<String> moduleClasses;

}
