package com.raj;

/**
 * @author rgauttam
 *
 */
public interface Converter {
	
	public <T extends Object> T convert(String value);

}
