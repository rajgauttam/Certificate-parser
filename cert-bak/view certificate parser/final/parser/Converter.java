package com.cisco.nm.vms.api.certificate.parser;

import com.cisco.nm.vms.nbi.base.NBIException;

/**
 * @author rgauttam
 *
 */
public interface Converter<T> {

	public T convert(String value) throws NBIException;

}
