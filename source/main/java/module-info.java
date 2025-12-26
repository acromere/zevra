import com.acromere.log.java.JavaLoggingProvider;
import com.acromere.log.provider.LoggingProvider;

module com.acromere.zevra {

	requires static java.logging;
	requires static java.management;
	requires static java.xml;
	requires static jdk.management;
	requires static jsr305;
	requires static lombok;
	requires static org.jspecify;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.core;
	requires me.xdrop.fuzzywuzzy;
	requires org.jsoup;

	opens com.acromere.util to com.fasterxml.jackson.databind;

	exports com.acromere.annotation;
	exports com.acromere.data;
	exports com.acromere.event;
	exports com.acromere.index;
	exports com.acromere.log;
	exports com.acromere.log.provider;
	exports com.acromere.product;
	exports com.acromere.result;
	exports com.acromere.settings;
	exports com.acromere.skill;
	exports com.acromere.test;
	exports com.acromere.transaction;
	exports com.acromere.util;

	uses LoggingProvider;

	provides LoggingProvider with JavaLoggingProvider;
}
