package com.cisco.nm.vms.api.certificate.parser;

/**
 * @author rgauttam
 *
 */
public interface Converter {
	
	public <T extends Object> T convert(String value);

}
