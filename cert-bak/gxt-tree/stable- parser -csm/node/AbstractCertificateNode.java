package com.cisco.nm.vms.api.certificate.node;

/**
 * @author rgauttam
 *
 */
public abstract class AbstractCertificateNode {

    protected static CertificateNodeDelegate delegate;

    static {
        delegate = new CertificateNodeDelegate();
    }

    abstract void loadCertNode(String certNode);

    public static CertificateNodeDelegate getDelegate() {
        return delegate;
    }

    public static void setDelegate(CertificateNodeDelegate delegate) {
        AbstractCertificateNode.delegate = delegate;
    }

}