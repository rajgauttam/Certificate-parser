package com.cisco.nm.vms.api.certificate.parser;

import com.cisco.nm.vms.api.certificate.node.AbstractCertificateNode;
import com.cisco.nm.vms.api.certificate.node.KeyValuePairCertificateNode;
import com.cisco.nm.vms.api.certificate.parser.builder.CertificateParserBuilder;

/**
 * @author rgauttam
 *
 */

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class CertificateParser<T extends AbstractCertificateNode> {

    private static CertificateParser instance;

    private static CertificateParserAdapter adapter = null;

    protected CertificateParserBuilder certificateParserBuilder;

    public CertificateParser(CertificateParserBuilder certificateParserBuilder) {
        this.certificateParserBuilder = certificateParserBuilder;
    }

    public static <U extends CertificateParser> U getInstance(String certificateName) {
        
        switch (certificateName) {
        case "indentity":
            instance = new KeyValuePairCertificateParser<KeyValuePairCertificateNode>();
            break;

        case "ca":
            instance = new KeyValuePairCertificateParser<KeyValuePairCertificateNode>();
            break;

        default:
            if (adapter != null)
                instance = adapter.getCertificateFactory(certificateName);

            break;
        }

        return (U) instance;
    }

    public static void setCertificateParserAdapter(CertificateParserAdapter adapter) {
        CertificateParser.adapter = adapter;
    }

    public CertificateParserBuilder getCertificateParserBuilder() {
        return certificateParserBuilder;
    }

    public static <T extends AbstractCertificateNode> T parse(String type, String certStr,
            CertificateParserAdapter adapter) {
        CertificateParser.adapter = adapter;
        return (T) getInstance(type).parse(certStr);
    }

    public static <T extends AbstractCertificateNode> T parse(String type, String certStr) {
        return (T) getInstance(type).parse(certStr);
    }

    public static <T extends AbstractCertificateNode> T parse(String type, String certStr,
            CertificateParserBuilder builder) {
        CertificateParser parser = getInstance(type);
        parser.certificateParserBuilder = builder;
        return (T) parser.parse(certStr);
    }

    public static <T extends AbstractCertificateNode> T parse(String type, String certStr,
            CertificateParserAdapter adapter, CertificateParserBuilder builder) {
        CertificateParser.adapter = adapter;
        CertificateParser parser = getInstance(type);
        parser.certificateParserBuilder = builder;
        return (T) parser.parse(certStr);
    }

    @SuppressWarnings("hiding")
    public abstract <T extends AbstractCertificateNode> T parse(String certStr);

}
