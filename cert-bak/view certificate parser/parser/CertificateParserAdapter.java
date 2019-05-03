package com.cisco.nm.vms.api.certificate.parser;

import com.cisco.nm.vms.api.certificate.node.AbstractCertificateNode;

/**
 * @author rgauttam
 *
 */
public interface CertificateParserAdapter<T extends AbstractCertificateNode> {

	CertificateParser<T> getCertificateFactory(String certificateFactoryName);
}
