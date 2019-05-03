package com.cisco.nm.vms.api.certificate.parser;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cisco.nm.vms.api.certificate.helper.CertificateParserHelper;
import com.cisco.nm.vms.api.xsd.CertificateInfoNode;
import com.cisco.nm.vms.nbi.base.NBIException;

/**
 *
 *
 *  Expected certificates results by parser for given input
 *
 *
       CA Certificate : null
        Status : Available
        Serial Number : 01
        Issued By : null
          Host Name : firepower, firepower2
          Ip Address : 4.5.6.7
          Organization Unit : ISR30
        Issued From : null
          Organization Unit : ISR30
        Public Key Type : RSA (1024 bits)
        Signature Algorithm : MD5 with RSA Encryption
        Associated Trustpoints : SCEP Manual4 Manual3 Manual2
        Valid From : 11:47:16 IST April 19 2016
        Valid To : 11:47:16 IST April 19 2019
  *
  *     
      Certificate : null
        Status : Available
        Serial Number : 45
        Issued By : null
          Host Name : firepower
          Ip Address : 4.5.6.7
          Organization Unit : ISR30, ISR32
        Issued From : null
          Host Name : firepower, firepower2
          Ip Address : 4.5.6.7
        Public Key Type : RSA (512 bits)
        Signature Algorithm : MD5 with RSA Encryption
        Associated Trustpoints : Manual2
        Valid From : 17:14:16 IST July 21 2016
        Valid To : 17:14:16 IST July 21 2017
 *
 *
 *
 *
 *     @author rgauttam
 *
 */
@RunWith(PowerMockRunner.class)
public class CertificateParserTest {
 
    String identityCertificateStr;
   
    String caCertificateStr;
    
    @Before
    public void setup(){
        caCertificateStr = "CA Certificate:\n"+
                "  Status: Available\n"+
                "  Certificate Serial Number: 01\n"+
                "  Certificate Usage: Signature\n"+
                "  Public Key Type: RSA (1024 bits)\n"+
                "  Signature Algorithm: MD5 with RSA Encryption\n"+
                "  Issuer Name:\n"+
                "    ou=ISR30\n"+
                "    ipaddress=4.5.6.7+hostname=firepower\n"+
                "    hostname=firepower2\n"+
                "  Subject Name:\n"+
                "    ou=ISR30\n"+
                "  Validity Date:\n"+
                "    start date: 06:17:16 UTC Apr 19 2016\n"+
                "    end   date: 06:17:16 UTC Apr 19 2019\n"+
                "  Storage: config\n"+
                "  Associated Trustpoints: SCEP Manual4 Manual3 Manual2\n\n";

        identityCertificateStr = "Certificate:\n"+
                "  Status: Available\n"+
                "  Certificate Serial Number: 45\n"+
                "  Certificate Usage: General Purpose\n"+
                "  Public Key Type: RSA (512 bits)\n"+
                "  Signature Algorithm: MD5 with RSA Encryption\n"+
                "  Issuer Name:\n"+
                "    ou=ISR30\n"+
                "    ou=ISR32\n"+
                "    ipaddress=4.5.6.7+hostname=firepower\n"+
                "  Subject Name:\n"+
                "    hostname=firepower\n"+
                "    ipaddress=4.5.6.7+hostname=firepower2\n"+
                "  Validity Date:\n"+
                "    start date: 11:44:16 UTC Jul 21 2016\n"+
                "    end   date: 11:44:16 UTC Jul 21 2017\n"+
                "  Storage: config\n"+
                "  Associated Trustpoints: Manual2\n\n";
    }

    @Test
    public void identityCertificateTest() throws NBIException{
        List<CertificateInfoNode> certificateNodeEntries =
                CertificateParserHelper.getInstance().getIdentityCertificateInfoNode(identityCertificateStr);
        assertNotNull(certificateNodeEntries);
        assertEquals(certificateNodeEntries.size(), 1);

        /** Certificate */
        assertEquals(certificateNodeEntries.get(0).getKey(), "Certificate");
        assertNull(certificateNodeEntries.get(0).getValue());
        List<CertificateInfoNode> certificateNodeChildrenList = certificateNodeEntries.get(0).getChildrenList();
        assertEquals(certificateNodeChildrenList.size(), 9);

        /** Status */
        assertEquals(certificateNodeChildrenList.get(0).getKey(), "Status");
        assertEquals(certificateNodeChildrenList.get(0).getValue(), "Available");
        assertTrue(certificateNodeChildrenList.get(0).getChildrenList().isEmpty());

        /** Serial Number */
        assertEquals(certificateNodeChildrenList.get(1).getKey(), "Serial Number");
        assertEquals(certificateNodeChildrenList.get(1).getValue(), "45");
        assertTrue(certificateNodeChildrenList.get(1).getChildrenList().isEmpty());

        /** Issued By */
        assertEquals(certificateNodeChildrenList.get(2).getKey(), "Issued By");
        assertNull(certificateNodeChildrenList.get(2).getValue());
        List<CertificateInfoNode> issuedByChildrenList = certificateNodeChildrenList.get(2).getChildrenList();
        assertEquals(issuedByChildrenList.size(), 3);

        /** Issued By -child 1 */
        assertEquals(issuedByChildrenList.get(0).getKey(), "Host Name");
        assertEquals(issuedByChildrenList.get(0).getValue(), "firepower");
        assertTrue(issuedByChildrenList.get(0).getChildrenList().isEmpty());

        /** Issued By -child 2 */
        assertEquals(issuedByChildrenList.get(1).getKey(), "IP Address");
        assertEquals(issuedByChildrenList.get(1).getValue(), "4.5.6.7");
        assertTrue(issuedByChildrenList.get(1).getChildrenList().isEmpty());

        /** Issued By -child 3 */
        assertEquals(issuedByChildrenList.get(2).getKey(), "Organization Unit");
        assertEquals(issuedByChildrenList.get(2).getValue(), "ISR30, ISR32");
        assertTrue(issuedByChildrenList.get(2).getChildrenList().isEmpty());

        /** Issued From */
        assertEquals(certificateNodeChildrenList.get(3).getKey(), "Issued To");
        assertNull(certificateNodeChildrenList.get(3).getValue());
        List<CertificateInfoNode> issuedFromChildrenList = certificateNodeChildrenList.get(3).getChildrenList();
        assertEquals(issuedFromChildrenList.size(), 2);

        /** Issued From -child 1 */
        assertEquals(issuedFromChildrenList.get(0).getKey(), "Host Name");
        assertEquals(issuedFromChildrenList.get(0).getValue(), "firepower, firepower2");
        assertTrue(issuedFromChildrenList.get(0).getChildrenList().isEmpty());

        /** Issued From -child 2 */
        assertEquals(issuedFromChildrenList.get(1).getKey(), "IP Address");
        assertEquals(issuedFromChildrenList.get(1).getValue(), "4.5.6.7");
        assertTrue(issuedFromChildrenList.get(1).getChildrenList().isEmpty());

        /** Public Key Type */
        assertEquals(certificateNodeChildrenList.get(4).getKey(), "Public Key Type");
        assertEquals(certificateNodeChildrenList.get(4).getValue(), "RSA (512 bits)");
        assertTrue(certificateNodeChildrenList.get(4).getChildrenList().isEmpty());

        /** Signature Algorithm */
        assertEquals(certificateNodeChildrenList.get(5).getKey(), "Signature Algorithm");
        assertEquals(certificateNodeChildrenList.get(5).getValue(), "MD5 with RSA Encryption");
        assertTrue(certificateNodeChildrenList.get(5).getChildrenList().isEmpty());

        /** Associated Trustpoints */
        assertEquals(certificateNodeChildrenList.get(6).getKey(), "Associated Trustpoints");
        assertEquals(certificateNodeChildrenList.get(6).getValue(), "Manual2");
        assertTrue(certificateNodeChildrenList.get(6).getChildrenList().isEmpty());

        /** Valid From */
        assertEquals(certificateNodeChildrenList.get(7).getKey(), "Valid From");
        assertNotNull(certificateNodeChildrenList.get(7).getValue());
        assertEquals(convertDate(certificateNodeChildrenList.get(7).getValue(), "IST"), "17:14:16 IST July 21 2016");
        assertTrue(certificateNodeChildrenList.get(7).getChildrenList().isEmpty());

        /** Valid To */
        assertEquals(certificateNodeChildrenList.get(8).getKey(), "Valid To");
        assertNotNull(certificateNodeChildrenList.get(8).getValue());
        assertEquals(convertDate(certificateNodeChildrenList.get(8).getValue(), "IST"), "17:14:16 IST July 21 2017");
        assertTrue(certificateNodeChildrenList.get(8).getChildrenList().isEmpty());

    }

    @Test
    public void caCertificateTest() throws NBIException {
        List<CertificateInfoNode> certificateNodeEntries =
                CertificateParserHelper.getInstance().getCaCertificateInfoNode(caCertificateStr);
        assertNotNull(certificateNodeEntries);
        assertEquals(certificateNodeEntries.size(), 1);

        /** CA Certificate */
        assertEquals(certificateNodeEntries.get(0).getKey(), "CA Certificate");
        assertNull(certificateNodeEntries.get(0).getValue());
        List<CertificateInfoNode> certificateNodeChildrenList = certificateNodeEntries.get(0).getChildrenList();
        assertEquals(certificateNodeChildrenList.size(), 9);

        /** Status */
        assertEquals(certificateNodeChildrenList.get(0).getKey(), "Status");
        assertEquals(certificateNodeChildrenList.get(0).getValue(), "Available");
        assertTrue(certificateNodeChildrenList.get(0).getChildrenList().isEmpty());

        /** Serial Number */
        assertEquals(certificateNodeChildrenList.get(1).getKey(), "Serial Number");
        assertEquals(certificateNodeChildrenList.get(1).getValue(), "01");
        assertTrue(certificateNodeChildrenList.get(1).getChildrenList().isEmpty());

        /** Issued By */
        assertEquals(certificateNodeChildrenList.get(2).getKey(), "Issued By");
        assertNull(certificateNodeChildrenList.get(2).getValue());
        List<CertificateInfoNode> issuedByChildrenList = certificateNodeChildrenList.get(2).getChildrenList();
        assertEquals(issuedByChildrenList.size(), 3);

        /** Issued By -child 1 */
        assertEquals(issuedByChildrenList.get(0).getKey(), "Host Name");
        assertEquals(issuedByChildrenList.get(0).getValue(), "firepower, firepower2");
        assertTrue(issuedByChildrenList.get(0).getChildrenList().isEmpty());

        /** Issued By -child 2 */
        assertEquals(issuedByChildrenList.get(1).getKey(), "IP Address");
        assertEquals(issuedByChildrenList.get(1).getValue(), "4.5.6.7");
        assertTrue(issuedByChildrenList.get(1).getChildrenList().isEmpty());

        /** Issued By -child 3 */
        assertEquals(issuedByChildrenList.get(2).getKey(), "Organization Unit");
        assertEquals(issuedByChildrenList.get(2).getValue(), "ISR30");
        assertTrue(issuedByChildrenList.get(2).getChildrenList().isEmpty());

        /** Issued From */
        assertEquals(certificateNodeChildrenList.get(3).getKey(), "Issued To");
        assertNull(certificateNodeChildrenList.get(3).getValue());
        List<CertificateInfoNode> issuedFromChildrenList = certificateNodeChildrenList.get(3).getChildrenList();
        assertEquals(issuedFromChildrenList.size(), 1);

        /** Issued From -child 1 */
        assertEquals(issuedFromChildrenList.get(0).getKey(), "Organization Unit");
        assertEquals(issuedFromChildrenList.get(0).getValue(), "ISR30");
        assertTrue(issuedFromChildrenList.get(0).getChildrenList().isEmpty());

        /** Public Key Type */
        assertEquals(certificateNodeChildrenList.get(4).getKey(), "Public Key Type");
        assertEquals(certificateNodeChildrenList.get(4).getValue(), "RSA (1024 bits)");
        assertTrue(certificateNodeChildrenList.get(4).getChildrenList().isEmpty());

        /** Signature Algorithm */
        assertEquals(certificateNodeChildrenList.get(5).getKey(), "Signature Algorithm");
        assertEquals(certificateNodeChildrenList.get(5).getValue(), "MD5 with RSA Encryption");
        assertTrue(certificateNodeChildrenList.get(5).getChildrenList().isEmpty());

        /** Associated Trustpoints */
        assertEquals(certificateNodeChildrenList.get(6).getKey(), "Associated Trustpoints");
        assertEquals(certificateNodeChildrenList.get(6).getValue(), "SCEP Manual4 Manual3 Manual2");
        assertTrue(certificateNodeChildrenList.get(6).getChildrenList().isEmpty());

        /** Valid From */
        assertEquals(certificateNodeChildrenList.get(7).getKey(), "Valid From");
        assertNotNull(certificateNodeChildrenList.get(7).getValue());
        assertEquals(convertDate(certificateNodeChildrenList.get(7).getValue(), "IST"), "11:47:16 IST April 19 2016");
        assertTrue(certificateNodeChildrenList.get(7).getChildrenList().isEmpty());

        /** Valid To */
        assertEquals(certificateNodeChildrenList.get(8).getKey(), "Valid To");
        assertNotNull(certificateNodeChildrenList.get(8).getValue());
        assertEquals(convertDate(certificateNodeChildrenList.get(8).getValue(), "IST"), "11:47:16 IST April 19 2019");
        assertTrue(certificateNodeChildrenList.get(8).getChildrenList().isEmpty());

    }

    private String convertDate(String dateValue, String zone) throws NBIException {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss z MMMMM dd yyyy");
        // sdf.setTimeZone(TimeZone.getTimeZone(zone));
        Date existingDateValue = null;
        try {
            existingDateValue = sdf.parse(dateValue.trim());
        } catch (ParseException e) {
            throw new NBIException("Date is not proper formated");
        }

        sdf.setTimeZone(TimeZone.getTimeZone(zone));
        return sdf.format(existingDateValue);
    }
}