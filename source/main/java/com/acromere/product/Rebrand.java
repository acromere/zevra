package com.acromere.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )
public class Rebrand implements Serializable {

	private Class<?> splashScreenBackgroundClass;

	private Double splashScreenTitleFontSize;

	private Class<?> productIconClass;

	private List<Class<?>> moduleClasses;

}
