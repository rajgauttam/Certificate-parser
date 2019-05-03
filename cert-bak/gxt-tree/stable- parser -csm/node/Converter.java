package com.cisco.nm.vms.api.certificate.node;;

/**
 * @author rgauttam
 *
 */
public interface Converter {

	/**
	 * @param value
	 * @return
	 */
	public <T extends Object> T convert(String value);

}
