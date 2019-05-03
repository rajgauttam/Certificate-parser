package com.cisco.nm.vms.api.certificate.parser;

import com.cisco.nm.vms.api.certificate.node.AbstractCertificateNode;

public abstract class CertificateParser {

    private static CertificateParser instance;

    private static CertificateParserAdapter adapter = null;

    /**
     * @param certificateName
     * @return CertificateParser
     */
    public static CertificateParser getInstance(String certificateName) {

        switch (certificateName) {
        case "identity":
            instance = new KeyValuePairCertificateParser();
            break;

        case "ca":
            instance = new KeyValuePairCertificateParser();
            break;

        default:
            if (adapter != null)
                instance = adapter.getCertificateParser(certificateName);

            break;
        }

        return instance;
    }

    /**
     * @param adapter
     */
    public static void setCertificateParserAdapter(CertificateParserAdapter adapter) {
        CertificateParser.adapter = adapter;
    }

    /**
     * @param certStr
     * @return
     */
    public abstract <T extends AbstractCertificateNode> T parse(String certStr);
}