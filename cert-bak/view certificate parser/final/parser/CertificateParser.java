package com.cisco.nm.vms.api.certificate.parser;

import com.cisco.nm.cmf.security.javax.security.UnsupportedOperationException;
import com.cisco.nm.vms.api.certificate.node.KeyValuePairCertificateNode;
import com.cisco.nm.vms.api.certificate.parser.builder.CertificateParserBuilder;
import com.cisco.nm.vms.nbi.base.NBIException;

/**
 * @author rgauttam
 *
 */

public abstract class CertificateParser<T> {

    private static CertificateParser instance;

    protected CertificateParserBuilder certificateParserBuilder;

    public CertificateParser(CertificateParserBuilder certificateParserBuilder) {
        this.certificateParserBuilder = certificateParserBuilder;
    }

    public enum CertificateType{
        IDENTITY, CA;
    }

    public static <U extends CertificateParser> U getInstance(CertificateType type) {

        switch (type) {
        case IDENTITY:
            instance = new KeyValuePairCertificateParser<KeyValuePairCertificateNode>();
            break;

        case CA:
            instance = new KeyValuePairCertificateParser<KeyValuePairCertificateNode>();
            break;

        default:
            throw new UnsupportedOperationException("The type " + type + " is not supported by this certificate");
        }

        return (U) instance;
    }

    public CertificateParserBuilder getCertificateParserBuilder() {
        return certificateParserBuilder;
    }

    @SuppressWarnings("hiding")
    public abstract <T> T parse(String certStr) throws NBIException;

}