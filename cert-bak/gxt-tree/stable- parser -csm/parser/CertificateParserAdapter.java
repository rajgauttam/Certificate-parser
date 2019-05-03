package com.cisco.nm.vms.api.certificate.parser;

/**
 * @author rgauttam
 *
 */
public interface CertificateParserAdapter {

	CertificateParser getCertificateParser(String certificateParserName);
}
