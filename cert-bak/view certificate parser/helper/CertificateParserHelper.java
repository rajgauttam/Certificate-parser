package com.cisco.nm.vms.api.certificate.helper;

import java.util.ArrayList;
import java.util.List;

import com.cisco.nm.vms.api.certificate.node.KeyValuePairCertificateNode;
import com.cisco.nm.vms.api.certificate.parser.CertificateParser;
import com.cisco.nm.vms.api.xsd.CertificateNodeEntry;

public class CertificateParserHelper {

    /**
    String cert1 = "CA Certificate"+
              "Status: Available"+
              "Certificate Serial Number: 01"+
              "Certificate Usage: Signature"+
              "Public Key Type: RSA (1024 bits)"+
              "Signature Algorithm: MD5 with RSA Encryption"+
              "Issuer Name:"+
              "  ou=ISR30"+
              "Subject Name:"+
              "  ou=ISR30"+
              "Validity Date:"+
               " start date: 06:17:16 UTC Apr 19 2016"+
               " end   date: 06:17:16 UTC Apr 19 2019"+
              "Storage: config"+
              "Associated Trustpoints: SCEP Manual4 Manual3 Manual2"+

            "Certificate"+
              "Status: Available"+
              "Certificate Serial Number: 45"+
             " Certificate Usage: General Purpose"+
              "Public Key Type: RSA (512 bits)"+
             " Signature Algorithm: MD5 with RSA Encryption"+
              "Issuer Name:"+
              "  ou=ISR30"+
              "Subject Name:"+
              "  hostname=firepower"+
              "Validity Date:"+
              "  start date: 11:44:16 UTC Jul 21 2016"+
              "  end   date: 11:44:16 UTC Jul 21 2017"+
              "Storage: config"+
              "Associated Trustpoints: Manual2";*/

    private static CertificateParser<KeyValuePairCertificateNode> caCertificateParser = CertificateParser.getInstance("ca");
    private static CertificateParser<KeyValuePairCertificateNode> identityCertificateParser = CertificateParser.getInstance("identity");
    
    public static List<CertificateNodeEntry> getCaCertificateNodeEntry(String certStr){
        buildParsingRules(caCertificateParser);
        buildParsingRules(caCertificateParser, "ROOT/CA Certificate/");
        KeyValuePairCertificateNode rootNode = caCertificateParser.parse(certStr);
        return convertCertificateNodeToEntry(rootNode.getChildren());
    }

    public static List<CertificateNodeEntry> getIdentityCertificateNodeEntry(String certStr){
        buildParsingRules(identityCertificateParser);
        buildParsingRules(identityCertificateParser, "ROOT/Certificate/" );
        KeyValuePairCertificateNode rootNode = identityCertificateParser.parse(certStr);
        return convertCertificateNodeToEntry(rootNode.getChildren());
    }
    
    
    private static List<CertificateNodeEntry> convertCertificateNodeToEntry(List<KeyValuePairCertificateNode> certificateNodesList){
        List<CertificateNodeEntry> certificateNodeEntries = new ArrayList<>();
        for (KeyValuePairCertificateNode certNode : certificateNodesList) {
            CertificateNodeEntry certificateNodeEntry = new CertificateNodeEntry();
            certificateNodeEntry.setKey(certNode.getDispKey());
            certificateNodeEntry.setValue(certNode.getValue());
            certificateNodeEntry.setChildrenList(convertCertificateNodeToEntry(certNode.getChildren()));
            certificateNodeEntries.add(certificateNodeEntry);
        }
        return certificateNodeEntries;
    }

    private static void buildParsingRules(CertificateParser<KeyValuePairCertificateNode> parser, String basePath) {
        
        parser.getCertificateParserBuilder()
                .addNodeMovementEntry(basePath + "Validity Date/start date", basePath + "start date")
                .addNodeMovementEntry(basePath + "Validity Date/end date", basePath + "end date")
                .addExcludedKey(basePath +"Certificate Usage")
                .addExcludedKey(basePath +"Storage")
                .addExcludedKey(basePath +"Validity Date")
            
                .addDisplayOrderEntry(basePath +"Status", 0)
                .addDisplayOrderEntry(basePath +"Certificate Serial Number", 1)
                .addDisplayOrderEntry(basePath +"Issuer Name", 2)
                .addDisplayOrderEntry(basePath +"Subject Name", 3)
                .addDisplayOrderEntry(basePath +"Public Key Type", 4)
                .addDisplayOrderEntry(basePath +"Signature Algorithm", 5)
                .addDisplayOrderEntry(basePath +"Associated Trustpoints", 6)
                .addDisplayOrderEntry(basePath +"start date", 7)
                .addDisplayOrderEntry(basePath +"end date", 8)
                
                .addDisplayOrderEntry(basePath +"Issuer Name/serialNumber", 0)
                .addDisplayOrderEntry(basePath +"Issuer Name/hostname", 1)
                .addDisplayOrderEntry(basePath +"Issuer Name/ipaddress", 2)
                .addDisplayOrderEntry(basePath +"Issuer Name/cn", 3)
                .addDisplayOrderEntry(basePath +"Issuer Name/ou", 4)
                .addDisplayOrderEntry(basePath +"Issuer Name/o", 5)
                .addDisplayOrderEntry(basePath +"Issuer Name/l", 6)
                .addDisplayOrderEntry(basePath +"Issuer Name/st", 7)
                .addDisplayOrderEntry(basePath +"Issuer Name/c", 8)
                .addDisplayOrderEntry(basePath +"Issuer Name/e", 9)
                
                .addDisplayOrderEntry(basePath +"Subject Name/serialNumber", 0)
                .addDisplayOrderEntry(basePath +"Subject Name/hostname", 1)
                .addDisplayOrderEntry(basePath +"Subject Name/ipaddress", 2)
                .addDisplayOrderEntry(basePath +"Subject Name/cn", 3)
                .addDisplayOrderEntry(basePath +"Subject Name/ou", 4)
                .addDisplayOrderEntry(basePath +"Subject Name/o", 5)
                .addDisplayOrderEntry(basePath +"Subject Name/l", 6)
                .addDisplayOrderEntry(basePath +"Subject Name/st", 7)
                .addDisplayOrderEntry(basePath +"Subject Name/c", 8)
                .addDisplayOrderEntry(basePath +"Subject Name/e", 9)
                ;
    }
    
    private static void buildParsingRules(CertificateParser<KeyValuePairCertificateNode> parser) {

        parser.getCertificateParserBuilder()
               .addSplitNodesRegx("\n")
               .addKeyValueSplitRegx("[:=]")
               .addKeyTransformation("Certificate Serial Number", "Serial Number")
               .addKeyTransformation("Issuer Name", "Issued By")
               .addKeyTransformation("Subject Name",  "Issued From")
               .addKeyTransformation("Public Key Type", "Public Key Type")
               .addKeyTransformation("Signature Algorithm", "Signature Algorithm")
               .addKeyTransformation("Associated Trustpoints", "Associated Trustpoints")
            
               .addKeyTransformation("serialNumber", "Serial Number")
               .addKeyTransformation("ipaddress", "Ip Address")
               .addKeyTransformation("hostname", "Host Name")
               .addKeyTransformation("ou", "Organization Unit")
               .addKeyTransformation("o", "Organization")
               .addKeyTransformation("cn", "Common Name")
               .addKeyTransformation("l", "Locality")
               .addKeyTransformation("st", "State")
               .addKeyTransformation("c", "Country Code")
               .addKeyTransformation("e", "Email")
            
               .addKeyTransformation("start date", "Valid From")
               .addKeyTransformation("end date", "Valid To")
               .addMultiNodeSpitterEntry("ipaddress", "[+]")
               .addKeysIgnoresSplit("ipaddress")
               ;
    }

}