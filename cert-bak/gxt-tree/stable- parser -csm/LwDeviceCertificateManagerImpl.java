package com.cisco.nm.vms.api.certificate;

import static com.cisco.nm.vms.api.converters.ConverterRegistrar.getConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cisco.nm.vms.api.buildingblock.BBUtils;
import com.cisco.nm.vms.api.certificate.cli.CliUtility;
import com.cisco.nm.vms.api.certificate.node.KeyValuePairCertificateNode;
import com.cisco.nm.vms.api.certificate.parser.CertificateParser;
import com.cisco.nm.vms.api.common.LwBaseServiceImpl;
import com.cisco.nm.vms.api.converters.EntryConverter;
import com.cisco.nm.vms.api.converters.device.DeviceEntryConverter;
import com.cisco.nm.vms.api.device.LwDeviceManager;
import com.cisco.nm.vms.api.util.CommonUtils;
import com.cisco.nm.vms.api.xsd.CertificateNodeEntry;
import com.cisco.nm.vms.api.xsd.DeviceEntry;
import com.cisco.nm.vms.api.xsd.ObjectEntry;
import com.cisco.nm.vms.api.xsd.PkiCertificateEntry;
import com.cisco.nm.vms.api.xsd.PkiCertificateEntry.CaCertificateStatus;
import com.cisco.nm.vms.api.xsd.PkiCertificateEntry.EnrollmentType;
import com.cisco.nm.vms.api.xsd.PkiCertificateEntry.IdentityCertficateStatus;
import com.cisco.nm.vms.api.xsd.PkiPolicyEntry;
import com.cisco.nm.vms.api.xsd.VPNPkiEnrollment.UpdateType;
import com.cisco.nm.vms.buildingblock.BuildingBlockManager;
import com.cisco.nm.vms.buildingblock.caserver.CAServerBuildingBlock;
import com.cisco.nm.vms.buildingblock.caserver.CAServerData;
import com.cisco.nm.vms.buildingblock.util.BuildingBlocksConstants;
import com.cisco.nm.vms.ccm.ShowCommandResponse;
import com.cisco.nm.vms.common.LwContext;
import com.cisco.nm.vms.common.VmsContext;
import com.cisco.nm.vms.common.commandparser.lina.DirectDeploymentLinaCommandParser;
import com.cisco.nm.vms.common.idmapping.IdMappingUtil;
import com.cisco.nm.vms.common.util.LogUtil;
import com.cisco.nm.vms.deployment.DeploymentJob;
import com.cisco.nm.vms.deployment.DeploymentService;
import com.cisco.nm.vms.deployment.JobDeviceInfo;
import com.cisco.nm.vms.deployment.JobDeviceStatus;
import com.cisco.nm.vms.deployment.constants.DeploymentConstants;
import com.cisco.nm.vms.deployment.constants.DeploymentMap;
import com.cisco.nm.vms.deployment.constants.DeploymentOOBState;
import com.cisco.nm.vms.deployment.constants.DeploymentType;
import com.cisco.nm.vms.device.BaseDevice;
import com.cisco.nm.vms.device.DeviceConstants;
import com.cisco.nm.vms.device.DeviceManager;
import com.cisco.nm.vms.device.OsConstants;
import com.cisco.nm.vms.device.container.AbstractDeviceContainer;
import com.cisco.nm.vms.nbi.base.NBIException;
import com.cisco.nm.vms.policy.PolicyCacheManager;
import com.cisco.nm.vms.policy.PolicyContainer;
import com.cisco.nm.vms.policy.PolicyManager;
import com.cisco.nm.vms.policy.group.PolicyGroup;
import com.cisco.nm.vms.provision.common.Status;
import com.cisco.nm.vms.ratool.policy.PkiBBRefEntry;
import com.cisco.nm.vms.ratool.policy.PkiPolicy;
import com.cisco.nm.vms.sftunnel.SFTunnelService;
import com.google.common.base.Strings;

public class LwDeviceCertificateManagerImpl extends LwBaseServiceImpl implements LwDeviceCertificateManager {

    protected PolicyManager policyApi;
    protected DeviceManager deviceApi;
    protected PolicyCacheManager policyCacheApi;
    private BuildingBlockManager buildingBlockApi;
    private DeploymentService deploymentService;
    private SFTunnelService sfTunnelService;
    private LwDeviceManager lwDeviceManager;
    private final String PKI_PG = "PG.RA." + PkiPolicy.POLICY_TYPE;
    private String PKI_PG_NAME = "PKI.PG.Group";
    private String PKI_PG_DESC = "PKI.PG.Group.Desc";
    private boolean isServiceStarted;
    private static final String ENROLL_TYPE_MANUAL = "manual";
    private static final String ENROLL_TYPE_SELF = "self";
    private static final String ENROLL_TYPE_SCEP = "url";

    @Override
    public void initialize() {
        super.initialize();
        policyApi = getInterface(PolicyManager.class);
        deviceApi = getInterface(DeviceManager.class);
        policyCacheApi = getInterface(PolicyCacheManager.class);
        buildingBlockApi= getInterface(BuildingBlockManager.class);
        deploymentService = getInterface(DeploymentService.class);
        sfTunnelService=getInterface(SFTunnelService.class);
        lwDeviceManager = getInterface(LwDeviceManager.class);
        isServiceStarted = true;
    }
    
    

    private HashMap<String, String> deployConfigurationForTrustpoint(VmsContext ctx, List<String> cliList,
            List<Long> deviceids) throws NBIException {

        try {
            List<JobDeviceInfo> jobDeviceInfoList = new ArrayList<JobDeviceInfo>();
            for (long deviceId : deviceids) {
                JobDeviceInfo deviceInfo = new JobDeviceInfo();
                deviceInfo.setDeviceID(deviceId);
                BaseDevice device = deviceApi.getBasicDevice(ctx, deviceId);
                deviceInfo.setDeploymentType(DeploymentType.CSM_ONLY);
                deviceInfo.setDomainId(device.getDomainId());
                deviceInfo.setHostName(device.getHostName());
                deviceInfo.setDeviceName(device.getHostName());
                List<String> pgArray = new ArrayList<String>();
                pgArray.add(PKI_PG);
                deviceInfo.setPGTypes(pgArray);
                deviceInfo.setIpAddress(device.getMgmtIpAddress());
                deviceInfo.setMDFType(device.getMdfType());
                deviceInfo.setTargetType(device.getTargetType());
                jobDeviceInfoList.add(deviceInfo);
            }
            DeploymentMap otherAttributes = new DeploymentMap();
            otherAttributes.put(DeploymentMap.DeploymentKeys.REQUIRE_UNIVERSAL_MESSAGING, false);
            otherAttributes.put(DeploymentMap.DeploymentKeys.CUSTOM_DEPLOYMENT_TYPE,
                    DeploymentConstants.LINA_ONLY_DEPLOYMENT);
            otherAttributes.put(DeploymentMap.DeploymentKeys.CUSTOM_LINA_CONFIG, cliList);

            DeploymentJob deploymentJob =
                    deploymentService.createDeploymentJobInNoWFwithDevicesAndForceDeploy(ctx,
                            CommonUtils.getUserName(ctx), DeploymentOOBState.SKIP, jobDeviceInfoList,
                            System.currentTimeMillis(), false, otherAttributes);
            JobDeviceStatus jobDeviceStatus =
                    deploymentService.getDeviceStatus(ctx, deploymentJob.getId(), deviceids.get(0));
            while (jobDeviceStatus.getStatus() != null
                    && !(jobDeviceStatus.getStatus().equalsIgnoreCase(Status.SUCCEEDED)
                            || jobDeviceStatus.getStatus().equalsIgnoreCase(Status.FAILED) || jobDeviceStatus
                            .getStatus().equalsIgnoreCase(Status.CANCELLED))) {
                Thread.sleep(5000);
                jobDeviceStatus = deploymentService.getDeviceStatus(ctx, deploymentJob.getId(), deviceids.get(0));
            }
            return new DirectDeploymentLinaCommandParser().getErrorMap(jobDeviceStatus.getTempTranscriptURL());

        } catch (Throwable t) {
            LogUtil.error(getClass(), t);
            throw toNBIException("createLinaOnlyDeploymentForFTDDevices", t);
        }

    }

    @Override
    public String generateCSRForManualEnrolment(VmsContext ctx, LwContext lwCtx, long activityId, long deviceId,
            String caName) throws NBIException {
        String certificateString = null;
        try {
            List<String> enrollCommandList = new ArrayList<String>();
            enrollCommandList.add(CliUtility.CA_ENROLL_CLI + caName + CliUtility.NOCONFIRM_CLI);
            String deviceUUID = lwDeviceManager.getDeviceEntry(ctx, lwCtx, deviceId).getUuid();
            ShowCommandResponse response =
                    sfTunnelService.sendShowCommandsForDeviceUUID(ctx, deviceUUID, enrollCommandList);
            if (response.getCLIResponse(CliUtility.CA_ENROLL_CLI + caName + CliUtility.NOCONFIRM_CLI) != null) {
                certificateString =
                        response.getCLIResponse(CliUtility.CA_ENROLL_CLI + caName + CliUtility.NOCONFIRM_CLI)
                                .getResponse();
                if (!Strings.isNullOrEmpty(certificateString)) {
                    certificateString = certificateString.substring(certificateString.indexOf("----"));
                    if (Strings.isNullOrEmpty(certificateString)) {
                        throw new NBIException("Invalid CSR");
                    }
                } else {
                    throw new NBIException("Empty CSR");
                }
            }
            return certificateString;
        } catch (Throwable te) {
            throw toNBIException("Unable to generate CSR for PKI " + caName, te);
        }
    }

    @Override
    public boolean importCertificateForCAServer(VmsContext ctx, LwContext lwCtx, long activityId, long deviceId,
            String caName, String caCertificate) throws NBIException {
        boolean isCertificateImported = false;
        List<String> certificateList = new ArrayList<String>();
        try {
            ArrayList<Long> deviceIds = new ArrayList<>();
            deviceIds.add(deviceId);
            certificateList.add(CliUtility.CA_IMPORT_CLI + caName + CliUtility.CERT_NOINTERACT_CLI);
            certificateList.addAll(Arrays.asList(caCertificate.split("\n")));
            certificateList.add(CliUtility.QUIT_CLI);
            HashMap<String, String> linaErrorMap = deployConfigurationForTrustpoint(ctx, certificateList, deviceIds);
            if (linaErrorMap == null || linaErrorMap.size() == 0) {
                isCertificateImported = true;
            }
            return isCertificateImported;
        } catch (Throwable te) {
            throw toNBIException("Unable to Import the certificate" + caName, te);
        }
    }
    
    private final ShowCommandResponse sendCommandToDevice(VmsContext ctx, LwContext lwCtx, List<String> commandList,
            long deviceId) throws NBIException {
        try {
            return sfTunnelService.sendShowCommandsForDeviceUUID(ctx,
                    lwDeviceManager.getDeviceEntry(ctx, lwCtx, deviceId).getUuid(), commandList);
        } catch (Throwable te) {
            throw toNBIException("Unable to run the command " + commandList, te);
        }
    }

    @Override
    public PkiCertificateEntry createPkiPolicyEntry(VmsContext ctx, LwContext lwCtx, long activityId,
            String deviceUuid, String pkiBbUuid) throws NBIException {
        try {
            if (!isServiceStarted) {
                initialize();
            }
            Long deviceId = IdMappingUtil.convertUUIDtoID(deviceUuid, DeviceConstants.DEVICE_TYPE_STR);
            Long pkiBbId = BBUtils.convertUUIDtoID(pkiBbUuid, BuildingBlocksConstants.CA_SERVER);

            CAServerBuildingBlock caBuildingBlock =
                    (CAServerBuildingBlock) buildingBlockApi.getByObjectId(ctx, activityId, -1L,
                            BuildingBlocksConstants.CA_SERVER, pkiBbId);
            CAServerData caServerData = (CAServerData) caBuildingBlock.getData();
            List<String> keyCliList = new ArrayList<String>();
            keyCliList.add(CliUtility.generateKeyCLICommandForType(caServerData.getKeyName(), caServerData.getKeySize(), caServerData.getKeyType()));
            sendCommandToDevice(ctx, lwCtx, keyCliList, deviceId);
            List<String> cliList = CliUtility.constructCliListForSCEPInstall(caServerData);
            PkiCertificateEntry entry = new PkiCertificateEntry();
            entry.setStatus(Boolean.FALSE);
            List<Long> deviceids = new ArrayList<Long>();
            deviceids.add(deviceId);
            HashMap<String, String> linaErrorMap = deployConfigurationForTrustpoint(ctx, cliList, deviceids);
            if (linaErrorMap == null || linaErrorMap.size() == 0) {
                Set<String> pgTypes = new HashSet<String>();
                pgTypes.add(PKI_PG);
                Map<BaseDevice, List<PolicyGroup>> deviceGroupMap =
                        policyApi.getPolicyGroupsForTypesOnDevices(ctx, activityId, deviceids, pgTypes);

                if (deviceGroupMap == null || deviceGroupMap.size() == 0) {
                    PKI_PG_NAME += deviceId;
                    PolicyGroup pkiPolicyGroup =
                            policyApi.createGroup(ctx, activityId, PKI_PG_NAME, PKI_PG, PKI_PG_DESC);

                    deviceApi.assign(ctx, activityId, pkiPolicyGroup, deviceApi.getBasicDevice(ctx, deviceId), false);
                    PkiPolicy pkiPolicy = new PkiPolicy();
                    Set<PkiBBRefEntry> pkiBBRefSet = new HashSet<PkiBBRefEntry>();
                    updatePolicyWithCertStatusData(pkiBbId, caServerData.getEnrollType(), entry, pkiBBRefSet);
                    pkiPolicy.setPkiBBRefSet(pkiBBRefSet);
                    pkiPolicy.setType(PkiPolicy.POLICY_TYPE);
                    pkiPolicy = (PkiPolicy) policyApi.insert(ctx, activityId, pkiPolicyGroup.getId(), pkiPolicy);
                } else {
                    PolicyGroup pkiPolicyGroup = deviceGroupMap.values().iterator().next().get(0);
                    long[] pgIdsArray = new long[1];
                    pgIdsArray[0] = pkiPolicyGroup.getId();
                    Map[] policies = policyApi.getAllDefinedOnNodes(ctx, activityId, pgIdsArray);
                    if (policies != null && policies.length > 0) {
                        PolicyContainer policyContainer = (PolicyContainer) policies[0].get(PkiPolicy.POLICY_TYPE);
                        if (policyContainer != null && policyContainer.getType().equals(PkiPolicy.POLICY_TYPE)) {
                            PkiPolicy policy = (PkiPolicy) policyContainer.getPolicies()[0];
                            Set<PkiBBRefEntry> pkibbRefSet = policy.getPkiBBRefSet();
                            if (PkiBBRefEntry.getEntryById(pkiBbId, pkibbRefSet) == null) {
                                updatePolicyWithCertStatusData(pkiBbId, caServerData.getEnrollType(), entry,
                                        pkibbRefSet);

                                policy.setPkiBBRefSet(pkibbRefSet);
                                policyApi.update(ctx, activityId, policy);

                            } else {
                                throw new NBIException(
                                        "PKI policy already have selected trustpoint, Please select another one");
                            }

                        }
                    }

                }
                entry.setStatus(Boolean.TRUE);
                if (ENROLL_TYPE_MANUAL.equalsIgnoreCase(caServerData.getEnrollType())) {
                    entry.setEnrollmentType(EnrollmentType.MANUAL);
                } else if (ENROLL_TYPE_SCEP.equalsIgnoreCase(caServerData.getEnrollType())) {
                    entry.setEnrollmentType(EnrollmentType.SCEP);
                } else if (ENROLL_TYPE_SELF.equalsIgnoreCase(caServerData.getEnrollType())) {
                    entry.setEnrollmentType(EnrollmentType.SELF_SIGNED_CERTFICATE);
                }
                return entry;
            } else {
                StringBuffer errorBuffer = new StringBuffer("Unable To install Certificate\n");
                for (Map.Entry<String, String> linaEntry : linaErrorMap.entrySet()) {
                    errorBuffer.append(linaEntry.getKey()).append(" : ").append(linaEntry.getValue()).append("\n");
                }
                throw new NBIException(errorBuffer.toString());
            }
        } catch (Throwable t) {
            if (t instanceof NBIException) {
                throw toNBIException(t.getMessage(), t);
            } else
                throw toNBIException("Create PKI Policy", t);
        }

    }

    private void updatePolicyWithCertStatusData(long pkiBbId, String enrollType, PkiCertificateEntry entry,
            Set<PkiBBRefEntry> pkibbRefSet) {
        pkibbRefSet.add(new PkiBBRefEntry(pkiBbId,
                !(ENROLL_TYPE_SELF.equalsIgnoreCase(enrollType)) ? CaCertificateStatus.IN_PROGRESS.xmlValue()
                        : CaCertificateStatus.NOT_APPLICABLE.xmlValue(), IdentityCertficateStatus.IN_PROGRESS
                        .xmlValue(), Boolean.FALSE));
        entry.setIdentityCertficateStatus(IdentityCertficateStatus.IN_PROGRESS);
        entry.setIsOutdated(Boolean.FALSE);
        if (ENROLL_TYPE_SELF.equalsIgnoreCase(enrollType)) {
            entry.setCaCertificateStatus(CaCertificateStatus.NOT_APPLICABLE);
            entry.setEnrollmentType(EnrollmentType.SELF_SIGNED_CERTFICATE);
        } else if (ENROLL_TYPE_SCEP.equalsIgnoreCase(enrollType)) {
            entry.setCaCertificateStatus(CaCertificateStatus.IN_PROGRESS);
            entry.setEnrollmentType(EnrollmentType.convert(EnrollmentType.SCEP.xmlValue()));
        } else {
            entry.setCaCertificateStatus(CaCertificateStatus.IN_PROGRESS);
            entry.setEnrollmentType(EnrollmentType.convert(EnrollmentType.MANUAL.xmlValue()));
        }
    }

    @Override
    public PkiCertificateEntry deletePkiPolicyEntry(VmsContext ctx, LwContext lwCtx, long activityId, long deviceId,
            long pkiBbId) throws NBIException {
        try {
            if (!isServiceStarted) {
                initialize();
            }
            CAServerBuildingBlock caBuildingBlock =
                    (CAServerBuildingBlock) buildingBlockApi.getByObjectId(ctx, activityId, -1L,
                            BuildingBlocksConstants.CA_SERVER, pkiBbId);
            CAServerData caServerData = (CAServerData) caBuildingBlock.getData();
            if (caServerData == null) {
                throw new NBIException("Unable to fetch the PKI trustpoint data");
            }
            List<String> cliList = CliUtility.constructCliListForTrustpointDelete(caServerData.getCaName());
            PkiCertificateEntry entry = new PkiCertificateEntry();
            entry.setStatus(Boolean.FALSE);
            List<Long> deviceids = new ArrayList<Long>();
            deviceids.add(deviceId);
            HashMap<String, String> linaErrorMap = deployConfigurationForTrustpoint(ctx, cliList, deviceids);
            if (linaErrorMap == null || linaErrorMap.size() == 0) {
                Set<String> pgTypes = new HashSet<String>();
                pgTypes.add(PKI_PG);
                Map<BaseDevice, List<PolicyGroup>> deviceGroupMap =
                        policyApi.getPolicyGroupsForTypesOnDevices(ctx, activityId, deviceids, pgTypes);
                if (deviceGroupMap != null && deviceGroupMap.size() > 0) {
                    long[] pgIdsArray = new long[1];
                    pgIdsArray[0] = deviceGroupMap.values().iterator().next().get(0).getId();
                    Map[] policies = policyApi.getAllDefinedOnNodes(ctx, activityId, pgIdsArray);
                    if (policies != null && policies.length > 0) {
                        PolicyContainer policyContainer = (PolicyContainer) policies[0].get(PkiPolicy.POLICY_TYPE);
                        if (policyContainer != null && policyContainer.getType().equals(PkiPolicy.POLICY_TYPE)) {
                            PkiPolicy policy = (PkiPolicy) policyContainer.getPolicies()[0];
                            Set<PkiBBRefEntry> pkiBbRefSet = policy.getPkiBBRefSet();
                            if (PkiBBRefEntry.getEntryById(pkiBbId, pkiBbRefSet) != null ){
                                PkiBBRefEntry pkiEntry = PkiBBRefEntry.getEntryById(pkiBbId, pkiBbRefSet);
                                pkiBbRefSet.remove(pkiEntry);
                                policy.setPkiBBRefSet(pkiBbRefSet);
                                policyApi.update(ctx, activityId, policy);
                                entry.setStatus(Boolean.TRUE);
                            } else {
                                throw new NBIException("PKI policy does not have selected trustpoint, unable to delete");
                            }
                        }
                    }
                } else {
                    throw new NBIException("PKI.PG.Group is empty");
                }
            } else {
                StringBuffer errorBuffer = new StringBuffer("Unable To delete Trustpoint\n");
                for (Map.Entry<String, String> linaEntry : linaErrorMap.entrySet()) {
                    errorBuffer.append(linaEntry.getKey()).append(" : ").append(linaEntry.getValue()).append("\n");
                }
                throw new NBIException(errorBuffer.toString());
            }
            return entry;

        } catch (Throwable t) {
            if (t instanceof NBIException) {
                throw toNBIException(t.getMessage(), t);
            } else
                throw toNBIException("delete pki bb entry", t);
        }

    }

    @Override
    public Map<ObjectEntry, PkiPolicyEntry> getAllPkiPolicyList(VmsContext ctx, LwContext lwCtx, long activityId)
            throws NBIException {
        try {
            if (!isServiceStarted){
                initialize();
            }
            Map<ObjectEntry, PkiPolicyEntry> devicePolicyMap =
                    new HashMap<ObjectEntry, PkiPolicyEntry>();
            final Map<Integer, com.cisco.nm.vms.device.BaseDevice[]> deviceMap =
                    deviceManager.getDevicesByOsType(ctx, 0, new int[] { OsConstants.OS_TYPE_NGFW }, null);
            final com.cisco.nm.vms.device.BaseDevice[] hwDeviceArray = deviceMap.get(OsConstants.OS_TYPE_NGFW);
            List<Long> deviceIds = new ArrayList<Long>();
            Set<String> pgTypes = new HashSet<String>();
            pgTypes.add(PKI_PG);
            for (BaseDevice deviceEntry : hwDeviceArray) {
                deviceIds.add(deviceEntry.getId());
            }
            Map<BaseDevice, List<PolicyGroup>> deviceGroupMap =
                    policyApi.getPolicyGroupsForTypesOnDevices(ctx, activityId, deviceIds, pgTypes);
            if ( deviceGroupMap != null && deviceGroupMap.size() > 0) {

                for (Map.Entry<BaseDevice, List<PolicyGroup>> entry : deviceGroupMap.entrySet()) {
                    Map<String, Object> converterContext = createContext(ctx, lwCtx, activityId);
                    Map<Long, AbstractDeviceContainer> deviceContainerMap =
                            deviceManager.getDeviceContainerMap(ctx, 0L, new long[] { entry.getKey().getId() });
                    converterContext.put(DeviceEntryConverter.CONTAINER_MAP, deviceContainerMap);
                    if (deviceContainerMap != null) {
                        long nodeId = entry.getKey().getId();
                        AbstractDeviceContainer container = deviceContainerMap.get(nodeId);
                        if (container != null && container.isSecondaryDevice(nodeId)) {
                            // If the device is secondary device, continue
                            continue;
                        }
                    }
                    ObjectEntry deviceEntry =
                            getConverter(com.cisco.nm.vms.device.BaseDevice.class, DeviceEntry.class).toEntry(
                                    entry.getKey(), converterContext);
                    for (PolicyGroup pg : entry.getValue()) {
                        long[] pgIdsArray = new long[1];
                        pgIdsArray[0] = pg.getId();
                        Map[] policies = policyApi.getAllDefinedOnNodes(ctx, activityId, pgIdsArray);
                        if (policies != null && policies.length > 0) {
                            PolicyContainer policyContainer = (PolicyContainer) policies[0].get(PkiPolicy.POLICY_TYPE);
                            if (policyContainer.getType().equals(PkiPolicy.POLICY_TYPE)) {
                                PkiPolicy policy = (PkiPolicy) policyContainer.getPolicies()[0];
                                EntryConverter converter =
                                         getConverter(
                                                PkiPolicyEntry.class);
                                if (converter != null && policy != null) {
                                    PkiPolicyEntry pkiPolicyEntry =
                                            (PkiPolicyEntry) converter.toEntry(policy, converterContext);
                                    devicePolicyMap.put(deviceEntry, pkiPolicyEntry);
                                }

                            }
                        }
                    }
                }
            }
            return devicePolicyMap;
        } catch (Throwable t) {
            throw toNBIException("getAllPkiPolicyList", t);
        }
    }

    @Override
    public PkiCertificateEntry updatePkiEntry(VmsContext ctx, LwContext lwCtx, long activityId, String deviceUUID,
            String  pkibbUUID) throws NBIException {

        PkiCertificateEntry updatedEntry = new PkiCertificateEntry();
        PkiBBRefEntry pkiEntry = null;
        CAServerData caServerData = null;
        PkiPolicy policy = null;
        boolean isConfiguartionDeployed = Boolean.TRUE;
        try {
            long deviceId = IdMappingUtil.convertUUIDtoID(deviceUUID, DeviceConstants.DEVICE_TYPE_STR);
            long pkiBbId = BBUtils.convertUUIDtoID(pkibbUUID, BuildingBlocksConstants.CA_SERVER);
            CAServerBuildingBlock caBuildingBlock =
                    (CAServerBuildingBlock) buildingBlockApi.getByObjectId(ctx, activityId, -1L,
                            BuildingBlocksConstants.CA_SERVER, pkiBbId);
            if (caBuildingBlock != null) {
                caServerData = (CAServerData) caBuildingBlock.getData();
                if (caServerData == null) {
                    throw new NBIException("No PKI Buildingblock found");
                }

                List<Long> deviceIds = new ArrayList<Long>();
                Set<String> pgTypes = new HashSet<String>();
                pgTypes.add(PKI_PG);
                deviceIds.add(deviceId);
                Map<BaseDevice, List<PolicyGroup>> deviceGroupMap =
                        policyApi.getPolicyGroupsForTypesOnDevices(ctx, activityId, deviceIds, pgTypes);
                if (deviceGroupMap != null && deviceGroupMap.size() > 0) {
                    long[] pgIdsArray = new long[1];
                    pgIdsArray[0] = deviceGroupMap.values().iterator().next().get(0).getId();
                    Map[] policies = policyApi.getAllDefinedOnNodes(ctx, activityId, pgIdsArray);
                    if (policies != null && policies.length > 0) {
                        PolicyContainer policyContainer = (PolicyContainer) policies[0].get(PkiPolicy.POLICY_TYPE);
                        if (policyContainer != null && policyContainer.getType().equals(PkiPolicy.POLICY_TYPE)) {
                            policy = (PkiPolicy) policyContainer.getPolicies()[0];
                            Set<PkiBBRefEntry> pkiBbRefSet = policy.getPkiBBRefSet();
                            if (PkiBBRefEntry.getEntryById(pkiBbId, pkiBbRefSet) != null) {
                                pkiEntry = PkiBBRefEntry.getEntryById(pkiBbId, pkiBbRefSet);
                                updatedEntry.setCaCertificateStatus(CaCertificateStatus.convert(pkiEntry.getCaCertState()));
                                updatedEntry.setId(caBuildingBlock.getId());
                                updatedEntry.setUuid(pkibbUUID);
                                updatedEntry.setIdentityCertficateStatus(IdentityCertficateStatus.convert(pkiEntry.getIdentityCertState()));
                                updatedEntry.setName(caServerData.getCaName());
                                updatedEntry.setDescription(caBuildingBlock.getComment());
                                if (pkiEntry == null) {
                                    throw new NBIException("Unbale to get trustpoint data from DB");
                                }
                                if (pkiEntry.isOutdated()) {
                                    List<String> cliList = CliUtility.constructCliListForSCEPInstall(caServerData);
                                    HashMap<String, String> linaErrorMap =
                                            deployConfigurationForTrustpoint(ctx, cliList, deviceIds);
                                    if (linaErrorMap != null && !linaErrorMap.isEmpty()) {
                                        isConfiguartionDeployed = Boolean.FALSE;
                                    }
                                }
                            } else {
                                throw new NBIException("PKI policy does not have selected trustpoint, unable to delete");
                            }
                        }
                    }
                } else {
                    throw new NBIException("PKI.PG.Group is empty");
                }
                if (ENROLL_TYPE_SELF.equalsIgnoreCase(caServerData.getEnrollType())) {
                    performAuthenticateEnroll(ctx, deviceIds, caServerData, updatedEntry, pkiEntry, Boolean.FALSE,
                            isConfiguartionDeployed);
                    updatedEntry.setEnrollmentType(EnrollmentType.SELF_SIGNED_CERTFICATE);
                } else if (ENROLL_TYPE_MANUAL.equalsIgnoreCase(caServerData.getEnrollType())) {
                    performAuthenticateEnroll(ctx, deviceIds, caServerData, updatedEntry, pkiEntry, Boolean.TRUE,
                            isConfiguartionDeployed);
                    updatedEntry.setEnrollmentType(EnrollmentType.MANUAL);
                } else if (ENROLL_TYPE_SCEP.equalsIgnoreCase(caServerData.getEnrollType())) {
                    performAuthenticateEnroll(ctx, deviceIds, caServerData, updatedEntry, pkiEntry, Boolean.TRUE,
                            isConfiguartionDeployed);
                    updatedEntry.setEnrollmentType(EnrollmentType.SCEP);
                }
                policyApi.update(ctx, activityId, policy);

            } else {
                throw new NBIException("No PKI Buildingblock found");
            }
        } catch (Throwable t) {
            LogUtil.error(getClass(), t);
            if (pkiEntry != null) {
                setStatusForCertificateForLinaFailure(updatedEntry);
                return updatedEntry;
            }
            if (t instanceof NBIException) {
                throw toNBIException(t.getMessage(), t);
            } else
                throw toNBIException("update PKIEntry Failed", t);
        }
        return updatedEntry;
    }
    
    private void setStatusForCertificateForLinaFailure(PkiCertificateEntry updatedEntry) {
        if (CaCertificateStatus.IN_PROGRESS.xmlValue().equals(updatedEntry.getCaCertificateStatus().xmlValue())) {
            updatedEntry.setCaCertificateStatus(CaCertificateStatus.NOT_AVAILABLE);
        }
        if (IdentityCertficateStatus.IN_PROGRESS.xmlValue().equalsIgnoreCase(
                updatedEntry.getIdentityCertficateStatus().xmlValue())) {
            updatedEntry.setIdentityCertficateStatus(IdentityCertficateStatus.NOT_AVAILABLE);
        }
    }

    private void performAuthenticateEnroll(VmsContext ctx, List<Long> deviceIds, CAServerData caServerData,
            PkiCertificateEntry updatedEntry, PkiBBRefEntry pkiEntry, boolean isAuthenticationSupported,
            boolean isConfigurationDeployed) throws NBIException {
        if (!isConfigurationDeployed) {
            updatedEntry.setIsOutdated(Boolean.TRUE);
            if (!ENROLL_TYPE_SELF.equalsIgnoreCase(caServerData.getEnrollType())) {
                updatedEntry.setCaCertificateStatus(CaCertificateStatus.NOT_AVAILABLE);
                pkiEntry.setCaCertState(CaCertificateStatus.NOT_AVAILABLE.xmlValue());
            } else {
                updatedEntry.setCaCertificateStatus(CaCertificateStatus.NOT_APPLICABLE);
                pkiEntry.setCaCertState(CaCertificateStatus.NOT_APPLICABLE.xmlValue());
            }
            updatedEntry.setIdentityCertficateStatus(IdentityCertficateStatus.NOT_AVAILABLE);
            pkiEntry.setIdentityCertState(IdentityCertficateStatus.NOT_AVAILABLE.xmlValue());
            return;
        }
        boolean isAuthenticationDone = true;
        updatedEntry.setIsOutdated(Boolean.FALSE);
        if (isAuthenticationSupported
                && CaCertificateStatus.IN_PROGRESS.xmlValue().equalsIgnoreCase(pkiEntry.getCaCertState())
                || CaCertificateStatus.NOT_AVAILABLE.xmlValue().equalsIgnoreCase(pkiEntry.getCaCertState())) {
            Map<String, String> linaErrorMap =
                    deployConfigurationForTrustpoint(ctx, generateAuthenticateCLIList(caServerData), deviceIds);
            if (linaErrorMap == null || linaErrorMap.isEmpty()) {
                updatedEntry.setCaCertificateStatus(CaCertificateStatus.AVAILABLE);
                pkiEntry.setCaCertState(CaCertificateStatus.AVAILABLE.xmlValue());
            } else {
                isAuthenticationDone = false;
                updatedEntry.setCaCertificateStatus(CaCertificateStatus.NOT_AVAILABLE);
                updatedEntry.setIdentityCertficateStatus(IdentityCertficateStatus.NOT_AVAILABLE);
                pkiEntry.setCaCertState(CaCertificateStatus.NOT_AVAILABLE.xmlValue());
                pkiEntry.setIdentityCertState(IdentityCertficateStatus.NOT_AVAILABLE.xmlValue());
            }
        }
        if (isAuthenticationDone
                && IdentityCertficateStatus.NOT_AVAILABLE.xmlValue().equalsIgnoreCase(pkiEntry.getIdentityCertState())
                || IdentityCertficateStatus.IN_PROGRESS.xmlValue().equalsIgnoreCase(pkiEntry.getIdentityCertState())) {
            Map<String, String> linaErrorMap =
                    deployConfigurationForTrustpoint(ctx, generateEnrollCLIList(caServerData.getCaName()), deviceIds);
            if (linaErrorMap == null || linaErrorMap.isEmpty()) {
                if (!ENROLL_TYPE_MANUAL.equalsIgnoreCase(caServerData.getEnrollType())) {
                    updatedEntry.setIdentityCertficateStatus(IdentityCertficateStatus.AVAILABLE);
                    pkiEntry.setIdentityCertState(IdentityCertficateStatus.AVAILABLE.xmlValue());
                } else {
                    updatedEntry.setIdentityCertficateStatus(IdentityCertficateStatus.PENDING);
                    pkiEntry.setIdentityCertState(IdentityCertficateStatus.PENDING.xmlValue());
                }

            } else {
                updatedEntry.setIdentityCertficateStatus(IdentityCertficateStatus.NOT_AVAILABLE);
                pkiEntry.setIdentityCertState(IdentityCertficateStatus.NOT_AVAILABLE.xmlValue());
            }
        }
    }

    private List<String> generateAuthenticateCLIList(CAServerData caServerData) {
        List<String> authenticateCLIList = new ArrayList<String>();
        if (ENROLL_TYPE_MANUAL.equalsIgnoreCase(caServerData.getEnrollType())) {
            authenticateCLIList.add(CliUtility.AUTHENTICATEL_CLI + caServerData.getCaName()
                    + CliUtility.NOINTERACTIVE_CLI);
            authenticateCLIList.addAll(Arrays.asList(caServerData.getCaCertificate().split("\n")));
            authenticateCLIList.add(CliUtility.QUIT_CLI);
        } else if (ENROLL_TYPE_SCEP.equalsIgnoreCase(caServerData.getEnrollType())) {
            if ((caServerData.getCaCertificate() == null || caServerData.getCaCertificate().trim().length() == 0)
                    && caServerData.getFingerprint() != null && caServerData.getFingerprint().trim().length() > 0) {
                authenticateCLIList.add(CliUtility.AUTHENTICATEL_CLI + caServerData.getCaName()
                        + CliUtility.FINGERPRINT_CLI + caServerData.getFingerprint() + CliUtility.NOINTERACTIVE_CLI);
            } else if (caServerData.getCaCertificate() != null && caServerData.getCaCertificate().trim().length() > 0
                    && caServerData.getFingerprint() != null && caServerData.getFingerprint().trim().length() > 0) {
                authenticateCLIList.add(CliUtility.SCEP_MANUAL_CLI + caServerData.getCaName());
                authenticateCLIList.add(CliUtility.SCEP_CERTIFICATE_CLI + caServerData.getFingerprint());
                authenticateCLIList.addAll(Arrays.asList(caServerData.getCaCertificate().split("\n")));
                authenticateCLIList.add(CliUtility.QUIT_CLI);
            }
        }
        return authenticateCLIList;
    }

    private List<String> generateEnrollCLIList(String caName) {
        List<String> enrollCliList = new ArrayList<String>();
        enrollCliList.add(CliUtility.ENROLL_CLI + caName + CliUtility.NOCONFIRM_CLI);
        return enrollCliList;
    }

    @Override
    public UpdateType checkBuildingBlockStatus(VmsContext ctx, LwContext lwCtx, long activityId, long pkibbId)
            throws NBIException {
        try {

            UpdateType updateType =
                    getBuildingBlockUpdateType(ctx, lwCtx, activityId, pkibbId,
                            CaCertificateStatus.IN_PROGRESS.xmlValue());
            if (updateType.xmlValue().equalsIgnoreCase(UpdateType.BLOCK_UPDATE.xmlValue())) {
                return UpdateType.BLOCK_UPDATE;
            }
            updateType =
                    getBuildingBlockUpdateType(ctx, lwCtx, activityId, pkibbId,
                            CaCertificateStatus.AVAILABLE.xmlValue());
            if (updateType.xmlValue().equalsIgnoreCase(UpdateType.BLOCK_UPDATE.xmlValue())) {
                return UpdateType.SELECTIVE_UPDATE;
            }
            return UpdateType.ALLOWED;
        } catch (Throwable t) {
            throw toNBIException("Building Block status UPdate check failed", t);
        }

    }

    @Override
    public void setPkiBBDirty(VmsContext ctx, LwContext lwCtx, long activityId, long pkiBbId) throws NBIException {
        try {
            PkiBBRefEntry pkiEntry = null;
            final Map<Integer, com.cisco.nm.vms.device.BaseDevice[]> deviceMap =
                    deviceManager.getDevicesByOsType(ctx, 0, new int[] { OsConstants.OS_TYPE_NGFW }, null);
            final com.cisco.nm.vms.device.BaseDevice[] hwDeviceArray = deviceMap.get(OsConstants.OS_TYPE_NGFW);
            List<Long> deviceIds = new ArrayList<Long>();
            Set<String> pgTypes = new HashSet<String>();
            pgTypes.add(PKI_PG);
            for (BaseDevice deviceEntry : hwDeviceArray) {
                deviceIds.add(deviceEntry.getId());
            }
            Map<BaseDevice, List<PolicyGroup>> deviceGroupMap =
                    policyApi.getPolicyGroupsForTypesOnDevices(ctx, activityId, deviceIds, pgTypes);
            if (deviceGroupMap != null && deviceGroupMap.size() > 0) {

                for (Map.Entry<BaseDevice, List<PolicyGroup>> entry : deviceGroupMap.entrySet()) {
                    Map<String, Object> converterContext = createContext(ctx, lwCtx, activityId);
                    ObjectEntry deviceEntry =
                            getConverter(com.cisco.nm.vms.device.BaseDevice.class, DeviceEntry.class).toEntry(
                                    entry.getKey(), createContext(ctx, lwCtx, activityId));
                    for (PolicyGroup pg : entry.getValue()) {
                        long[] pgIdsArray = new long[1];
                        pgIdsArray[0] = pg.getId();
                        Map[] policies = policyApi.getAllDefinedOnNodes(ctx, activityId, pgIdsArray);
                        if (policies != null && policies.length > 0) {
                            PolicyContainer policyContainer = (PolicyContainer) policies[0].get(PkiPolicy.POLICY_TYPE);
                            if (policyContainer.getType().equals(PkiPolicy.POLICY_TYPE)) {
                                PkiPolicy policy = (PkiPolicy) policyContainer.getPolicies()[0];
                                Set<PkiBBRefEntry> pkibbRefSet = policy.getPkiBBRefSet();
                                pkiEntry = PkiBBRefEntry.getEntryById(pkiBbId, pkibbRefSet);
                                if (pkiEntry != null) {
                                    pkiEntry.setOutdated(Boolean.TRUE);
                                    policyApi.update(ctx, activityId, policy);
                                }

                            }
                        }
                    }
                }
            }

        } catch (Throwable t) {
            throw toNBIException("update of pki bb ref entry failed ", t);
        }
    }

    @Override
    public boolean isUpdatedSupported(VmsContext ctx, LwContext lwCtx, long activityId, long pkiBbId)
            throws NBIException {
        try {

            UpdateType updateType =
                    getBuildingBlockUpdateType(ctx, lwCtx, activityId, pkiBbId,
                            CaCertificateStatus.IN_PROGRESS.xmlValue());
            return UpdateType.ALLOWED.xmlValue().equalsIgnoreCase(updateType.xmlValue()) ? Boolean.TRUE : Boolean.FALSE;
        } catch (Throwable t) {
            throw toNBIException("", t);
        }
    }

    private UpdateType getBuildingBlockUpdateType(VmsContext ctx, LwContext lwCtx, long activityId, long pkiBbId,
            String checkForStatus) throws NBIException {
        try {
            PkiBBRefEntry pkiEntry = null;
            final Map<Integer, com.cisco.nm.vms.device.BaseDevice[]> deviceMap =
                    deviceManager.getDevicesByOsType(ctx, 0, new int[] { OsConstants.OS_TYPE_NGFW }, null);
            final com.cisco.nm.vms.device.BaseDevice[] hwDeviceArray = deviceMap.get(OsConstants.OS_TYPE_NGFW);
            List<Long> deviceIds = new ArrayList<Long>();
            Set<String> pgTypes = new HashSet<String>();
            pgTypes.add(PKI_PG);
            for (BaseDevice deviceEntry : hwDeviceArray) {
                deviceIds.add(deviceEntry.getId());
            }
            Map<BaseDevice, List<PolicyGroup>> deviceGroupMap =
                    policyApi.getPolicyGroupsForTypesOnDevices(ctx, activityId, deviceIds, pgTypes);
            if (deviceGroupMap != null && deviceGroupMap.size() > 0) {

                for (Map.Entry<BaseDevice, List<PolicyGroup>> entry : deviceGroupMap.entrySet()) {
                    Map<String, Object> converterContext = createContext(ctx, lwCtx, activityId);
                    ObjectEntry deviceEntry =
                            getConverter(com.cisco.nm.vms.device.BaseDevice.class, DeviceEntry.class).toEntry(
                                    entry.getKey(), createContext(ctx, lwCtx, activityId));
                    for (PolicyGroup pg : entry.getValue()) {
                        long[] pgIdsArray = new long[1];
                        pgIdsArray[0] = pg.getId();
                        Map[] policies = policyApi.getAllDefinedOnNodes(ctx, activityId, pgIdsArray);
                        if (policies != null && policies.length > 0) {
                            PolicyContainer policyContainer = (PolicyContainer) policies[0].get(PkiPolicy.POLICY_TYPE);
                            if (policyContainer.getType().equals(PkiPolicy.POLICY_TYPE)) {
                                PkiPolicy policy = (PkiPolicy) policyContainer.getPolicies()[0];
                                Set<PkiBBRefEntry> pkibbRefSet = policy.getPkiBBRefSet();
                                pkiEntry = PkiBBRefEntry.getEntryById(pkiBbId, pkibbRefSet);
                                if (pkiEntry != null && checkForStatus.equalsIgnoreCase(pkiEntry.getCaCertState())
                                        || checkForStatus.equalsIgnoreCase(pkiEntry.getIdentityCertState())) {
                                    return UpdateType.BLOCK_UPDATE;
                                }

                            }
                        }
                    }
                }

            }
            return UpdateType.ALLOWED;
        } catch (Throwable t) {
            throw toNBIException("Retrieve Building Block Update Type failed", t);
        }
    }

    @Override
    public List<CertificateNodeEntry> getCertificateForTrustpoint(VmsContext ctx, LwContext lwCtx, long activityId, String deviceUUID,
            String caName, boolean isCACert) throws NBIException {
        try {
            long deviceId = IdMappingUtil.convertUUIDtoID(deviceUUID, DeviceConstants.DEVICE_TYPE_STR);
            ArrayList<String> showCertCommandList = new ArrayList<String>();
            showCertCommandList.add(CliUtility.SHOW_CERTIFICATE_CLI + caName);
            ShowCommandResponse certResponse = sendCommandToDevice(ctx, lwCtx, showCertCommandList, deviceId);
            String certificateString = null;
            String caCertificateString = null;
            String identityCertString = null;
            int caCertIndex = -1;
            if (certResponse.getCLIResponse(CliUtility.SHOW_CERTIFICATE_CLI + caName) != null) {
                certificateString = certResponse.getCLIResponse(CliUtility.SHOW_CERTIFICATE_CLI + caName).getResponse();
                if (!Strings.isNullOrEmpty(certificateString)) {
                    caCertIndex = certificateString.indexOf("CA Certificate");
                    if (caCertIndex < 0) {
                        identityCertString = certificateString;
                    } else {
                        int certificateIndex = certificateString.indexOf("Certificate");
                        if (certificateIndex >= 0 && caCertIndex == certificateIndex) {

                        }
                    }
                }
            }
            KeyValuePairCertificateNode certificateNode = null;
            if (isCACert) {
                certificateNode = CertificateParser.getInstance("ca").parse(caCertificateString);
                return convertCertificateNodeToEntry(certificateNode.getChildren());
            } else {
                certificateNode = CertificateParser.getInstance("identity").parse(identityCertString);
                return convertCertificateNodeToEntry(certificateNode.getChildren());
            }
        } catch (Throwable t) {
            throw toNBIException("Unable to get certificate", t);
        }
    }
    
    List<CertificateNodeEntry> convertCertificateNodeToEntry(List<KeyValuePairCertificateNode> certificateNodesList){
        List<CertificateNodeEntry> certificateNodeEntries = new ArrayList<>();
        for (KeyValuePairCertificateNode certNode : certificateNodesList) {
            CertificateNodeEntry certificateNodeEntry = new CertificateNodeEntry();
            certificateNodeEntry.setKey(certNode.getKey());
            certificateNodeEntry.setValue(certNode.getValue());
            certificateNodeEntry.setChildrenList(convertCertificateNodeToEntry(certNode.getChildren()));
            certificateNodeEntries.add(certificateNodeEntry);
        }
        return certificateNodeEntries;
    }
}
