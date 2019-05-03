package com.cisco.nm.vms.api.certificate;

import java.util.List;
import java.util.Map;

import com.cisco.nm.vms.api.xsd.CertificateNodeEntry;
import com.cisco.nm.vms.api.xsd.ObjectEntry;
import com.cisco.nm.vms.api.xsd.PkiCertificateEntry;
import com.cisco.nm.vms.api.xsd.PkiPolicyEntry;
import com.cisco.nm.vms.api.xsd.VPNPkiEnrollment.UpdateType;
import com.cisco.nm.vms.common.LwContext;
import com.cisco.nm.vms.common.VmsBaseManager;
import com.cisco.nm.vms.common.VmsContext;
import com.cisco.nm.vms.nbi.base.NBIException;

public interface LwDeviceCertificateManager extends VmsBaseManager {

    /**
     * This method create PKI policy PG and pki policy & assign to device
     * 
     * @param ctx
     * @param lwCtx
     * @param activityId
     * @param deviceUuid
     * @param pkiBbId
     * @throws NBIException
     * 
     * @vms-external
     * @vms-domain-aware
     */
    public PkiCertificateEntry createPkiPolicyEntry(VmsContext ctx, LwContext lwCtx, long activityId, String deviceUuid,
            String pkiBbUuid) throws NBIException;

    /**
     * This method delete PKI policy PG and pki policy & assign to device
     * 
     * @param ctx
     * @param lwCtx
     * @param activityId
     * @param deviceUuid
     * @param pkiBbId
     * @throws NBIException
     * 
     * @vms-external
     * @vms-domain-aware
     */
    public PkiCertificateEntry deletePkiPolicyEntry(VmsContext ctx, LwContext lwCtx, long activityId, long deviceId,
            long pkiBbId) throws NBIException;

    /**
     * This method get PKI policy PG and pki policy & assign to device
     * 
     * @param ctx
     * @param lwCtx
     * @param activityId
     * @throws NBIException
     * 
     * @vms-external
     * @vms-domain-aware
     */
    public Map<ObjectEntry, PkiPolicyEntry> getAllPkiPolicyList(VmsContext ctx, LwContext lwCtx, long activityId)
            throws NBIException;

    /**
     * This method to generate CSR on the device
     * 
     * @param ctx
     * @param lwCtx
     * @param activityId
     * @param deviceId
     * @param pkiBBName
     * @throws NBIException
     * 
     * @vms-external
     * @vms-domain-aware
     */
    public String generateCSRForManualEnrolment(VmsContext ctx, LwContext lwCtx, long activityId, long deviceId,
            String pkiBBName) throws NBIException;

    /**
     * This method to import certificate manually on the device
     * 
     * @param ctx
     * @param lwCtx
     * @param activityId
     * @param deviceId
     * @param caName
     * @param caCertificate
     * @throws NBIException
     * 
     * @vms-external
     * @vms-domain-aware
     */
    public boolean importCertificateForCAServer(VmsContext ctx, LwContext lwCtx, long activityId, long deviceId,
            String caName, String caCertificate) throws NBIException;

    /**
     * update PKI entry
     * 
     * @return PkiCertificateEntry
     * @param deviceId
     * @param pkiBbId
     * @throws NBIException
     *         *
     * @vms-external
     * @vms-domain-aware
     */
    public PkiCertificateEntry updatePkiEntry(VmsContext ctx, LwContext lwCtx, long activityId, String deviceUUID,
            String pkiBbUUID) throws NBIException;

    /**
     * Checking Building Block Status
     * 
     * @return UpdateType
     * @param pkiBbId
     * @throws NBIException
     *         *
     * @vms-external
     * @vms-domain-aware
     */
    public UpdateType checkBuildingBlockStatus(VmsContext ctx, LwContext lwCtx, long activityId, long pkiBbId)
            throws NBIException;

    /**
     * checking is update Supported
     * 
     * @return boolean
     * @param pkiBbId
     * @throws NBIException
     *         *
     * @vms-external
     * @vms-domain-aware
     */
    public boolean isUpdatedSupported(VmsContext ctx, LwContext lwCtx, long activityId, long pkiBbId)
            throws NBIException;

    /**
     * Setting building Block dirty in PkiPolicy
     * 
     * @return boolean
     * @param pkiBbId
     * @throws NBIException
     *         *
     * @vms-external
     * @vms-domain-aware
     */
    public void setPkiBBDirty(VmsContext ctx, LwContext lwCtx, long activityId, long pkiBbId) throws NBIException;
    
    /**
     * get Certificate for Trustpoint
     * 
     * @return List<CertificateNodeEntry> 
     * @param deviceUUID
     * @param caName
     * boolean isCACert
     * @throws NBIException
     *
     * @vms-external
     * @vms-domain-aware
     */
    public List<CertificateNodeEntry> getCertificateForTrustpoint(VmsContext ctx, LwContext lwCtx, long activityId, String deviceUUID,
            String caName, boolean isCACert) throws NBIException;

}
