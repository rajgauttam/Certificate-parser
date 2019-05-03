package com.cisco.nm.vms.rpc.certificate;

import java.util.List;
import java.util.Map;

import com.cisco.nm.vms.api.xsd.CertificateNodeEntry;
import com.cisco.nm.vms.api.xsd.ObjectEntry;
import com.cisco.nm.vms.api.xsd.PkiCertificateEntry;
import com.cisco.nm.vms.api.xsd.PkiPolicyEntry;
import com.cisco.nm.vms.common.LwContext;
import com.cisco.nm.vms.common.VmsContext;
import com.cisco.nm.vms.nbi.base.NBIException;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.XsrfProtectedService;
import com.google.gwt.user.server.rpc.XsrfProtect;

/**
 * Expose LwPolicyManager as RPC API
 */
@XsrfProtect
@RemoteServiceRelativePath("../../csm/DeviceCertificateRPC")
public interface DeviceCertificateRPC extends XsrfProtectedService {
    
    /**
    * create pki policy & associate bb
    * 
    * @param deviceId
    * @param pkiBbId
    * @return boolean
    * @throws NBIException
    */
    public PkiCertificateEntry createPkiPolicyEntry(String deviceId, String pkiBbId) throws NBIException;

    /**
     * delete associated bb
     * 
     * @param deviceId
     * @param pkiBbId
     * @return boolean
     * @throws NBIException
     */
    public PkiCertificateEntry deletePkiPolicyEntry(long deviceId, long pkiBbId) throws NBIException;

    /**
     * get all pki bbs for devices
     * @return Map
     * @throws NBIException
     */
    public Map<ObjectEntry, PkiPolicyEntry> getAllPkiPolicyList() throws NBIException;

    /**
     * generate CSR on device
     * 
     * @return String
     * @param deviceId
     * @param pkiBBName
     * @throws NBIException
     */
    public String generateCSRForManualEnrolment(long deviceId, String pkiBBName) throws NBIException;

    /**
     * import certificate on device
     * 
     * @return boolean
     * @param deviceId
     * @param pkiBBName
     * @param caCertificate
     * @throws NBIException
     */
    public boolean importCertificateForCAServer(long deviceId, String caName, String caCertificate) throws NBIException;

    /**
     * update PKI entry
     * 
     * @return PkiCertificateEntry
     * @param deviceUUID
     * @param pkiBbUUID
     * @throws NBIException
     */
    public PkiCertificateEntry updatePkiEntry(String deviceUUID, String pkiBbUUID) throws NBIException;

    /**
     * get Certificate for Trustpoint
     * 
     * @return ist<CertificateNodeEntry>
     * @param deviceUUID
     * @param caName
     * @param isCACert
     * @throws NBIException
     *
     * @vms-external
     * @vms-domain-aware
     */
    public List<CertificateNodeEntry> getCertificateForTrustpoint(String deviceUUID, String caName, boolean isCACert) throws NBIException;

}
