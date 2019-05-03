package com.raj;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.ParseConversionEvent;

public class Main {
	
	static String cert = "Certificate:\n  Status: Available\n  Certificate Serial Number: b6d89557\n  Certificate Usage: General Purpose\n  Public Key Type: RSA (512 bits)\n  Signature Algorithm: SHA256 with RSA Encryption\n  Issuer Name: \n    serialNumber=9AQV9NG9RNM\n    hostname=firepower\n    ou=TESTOU\n    ou=TESTOU1\n    ou=TESTOU2\n    o=TESTO\n    cn=TESTCN\n    l=TESTL\n    st=TESTST\n    c=IN\n    e=ssss@cisco.com\n  Subject Name:\n    serialNumber=9AQV9NG9RNM\n    hostname=firepower\n    ou=TESTOU\n    o=TESTO\n    cn=TESTCN\n    l=TESTL\n    st=TESTST\n    c=IN\n    e=ssss@cisco.com\n  Validity Date: \n    start date: 13:56:20 UTC Jul 28 2016\n    end   date: 13:56:20 UTC Jul 26 2026\n  Storage: config\n  Associated Trustpoints: TestSS \n\n";

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
  "Associated Trustpoints: Manual2";
	
	public static void main(String[] args)  {
		
		int index =cert.indexOf("\n");
		//System.out.println(index);
		//parse(cert);
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++");
		
		//CertificateParser caCertificateParser = CertificateParser.getInstance("ca");
		//caCertificateParser.getCertificateParserBuilder().
		KeyValuePairCertificateNode node = Mapper.identityCertMapper(cert);
		
		System.out.println("=================================================");
		System.out.println("=================================================");
		System.out.println("=================================================");
		
	//	System.out.println(node.getKey() +" : "+node.getDispKey() +" - "+node.getValue());
		System.out.println(node.getDispKey() +" : "+node.getValue());
	//	System.out.println(" ---> " + node.getChildren().size());
		for(KeyValuePairCertificateNode node1 : node.getChildren()){
		//	System.out.println("  "+ node1.getKey() +" : "+node1.getDispKey() +" - "+node1.getValue());
			System.out.println("  " + node1.getDispKey() +" : "+node1.getValue());
		//	System.out.println(" ---> " + node1.getChildren().size());
			for(KeyValuePairCertificateNode node2 : node1.getChildren()){
			//	System.out.println("    "+ node2.getKey() +" : "+node2.getDispKey() +" - "+node2.getValue());
				System.out.println("    "+ node2.getDispKey() +" : "+node2.getValue());
			//	System.out.println(" ---> " + node2.getChildren().size());
				for(KeyValuePairCertificateNode node3 : node2.getChildren()){
				//	System.out.println("      "+ node3.getKey() +" : "+node3.getDispKey() +" - "+node3.getValue());
					System.out.println("      "+node3.getDispKey() +" : "+node3.getValue());
    			//	System.out.println(" ---> " + node3.getChildren().size());
				}
			}
		}
	
	}
	

}
