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
import com.google.gwt.user.client.rpc.AsyncCallback;


public interface DeviceCertificateRPCAsync {

    void createPkiPolicyEntry(String deviceUuid, String pkiBbId, AsyncCallback<PkiCertificateEntry> callback);

    void deletePkiPolicyEntry(long deviceUuid, long pkiBbId, AsyncCallback<PkiCertificateEntry> callback);

    void getAllPkiPolicyList(AsyncCallback<Map<ObjectEntry, PkiPolicyEntry>> callback);
    
    void generateCSRForManualEnrolment(long deviceId, String pkiBBName, AsyncCallback<String> callback);
    
    void importCertificateForCAServer(long deviceId, String caName, String caCertificate, AsyncCallback<Boolean> callback);

    void updatePkiEntry(String deviceUUID, String pkiBbUUID, AsyncCallback<PkiCertificateEntry> callback);

    void getCertificateForTrustpoint(String deviceUUID, String caName, boolean isCACert,
            AsyncCallback<List<CertificateNodeEntry>> callback);

}
