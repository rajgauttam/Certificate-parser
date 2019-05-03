package com.raj;

public class Mapper {
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

	static CertificateParser caCertificateParser = CertificateParser.getInstance("ca");
	static CertificateParser identityCertificateParser = CertificateParser.getInstance("identity");
	
	public static KeyValuePairCertificateNode caCertMapper(String certStr){
		buildParsingRules(caCertificateParser, "ROOT/CaCertificate/");
		return caCertificateParser.parse(certStr);
	}


	public static KeyValuePairCertificateNode identityCertMapper(String certStr){
		buildParsingRules(identityCertificateParser, "ROOT/Certificate/" );
		return identityCertificateParser.parse(certStr);
	}
	
	
	private static void buildParsingRules(CertificateParser parser, String basePath) {
		
		parser.getCertificateParserBuilder()
		        .addSplitNodesRegx("\n")
		        .addKeyValueSplitRegx("[:=]")
		        .addNodeMovementEntry(basePath + "Validity Date/start date", "ROOT/Certificate/start date")
		        .addNodeMovementEntry(basePath + "Validity Date/end date", "ROOT/Certificate/end date")
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
		          // .addDisplayOrderEntry(basePath +"Issuer Name/ip", 2)
		        .addDisplayOrderEntry(basePath +"Issuer Name/cn", 3)
		        .addDisplayOrderEntry(basePath +"Issuer Name/ou", 4)
		        .addDisplayOrderEntry(basePath +"Issuer Name/o", 5)
		        .addDisplayOrderEntry(basePath +"Issuer Name/l", 6)
		        .addDisplayOrderEntry(basePath +"Issuer Name/st", 7)
		        .addDisplayOrderEntry(basePath +"Issuer Name/c", 8)
		        .addDisplayOrderEntry(basePath +"Issuer Name/e", 9)
		        
		        .addDisplayOrderEntry(basePath +"Subject Name/serialNumber", 0)
		        .addDisplayOrderEntry(basePath +"Subject Name/hostname", 1)
		       // .addDisplayOrderEntry(basePath +"Subject Name/ip", 2)
		        .addDisplayOrderEntry(basePath +"Subject Name/cn", 3)
		        .addDisplayOrderEntry(basePath +"Subject Name/ou", 4)
		        .addDisplayOrderEntry(basePath +"Subject Name/o", 5)
		        .addDisplayOrderEntry(basePath +"Subject Name/l", 6)
		        .addDisplayOrderEntry(basePath +"Subject Name/st", 7)
		        .addDisplayOrderEntry(basePath +"Subject Name/c", 8)
		        .addDisplayOrderEntry(basePath +"Subject Name/e", 9)
		        
//		        .addKeyTransformation(basePath +"Certificate Serial Number", basePath +"Serial Number")
//		        .addKeyTransformation(basePath +"Issuer Name", basePath +"Issued By")
//		        .addKeyTransformation(basePath +"Subject Name",  basePath +"Issuer")
//		        .addKeyTransformation(basePath +"Public Key Type", basePath +"Key Type")
//		        .addKeyTransformation(basePath +"Signature Algorithm", basePath +"Algorithm")
//		        .addKeyTransformation(basePath +"Associated Trustpoints", basePath +"Trust Points")
//		        
//		        .addKeyTransformation(basePath +"Issuer Name/serialNumber", basePath +"Issuer Name/SNO")
//		        .addKeyTransformation(basePath +"Issuer Name/hostname", basePath +"Issuer Name/Host Name")
//		        .addKeyTransformation(basePath +"Issuer Name/ou", basePath +"Issuer Name/Organisation Unit")
//		        .addKeyTransformation(basePath +"Issuer Name/o", basePath +"Issuer Name/Organisation")
//		        .addKeyTransformation(basePath +"Issuer Name/cn", basePath +"CN")
//		        .addKeyTransformation(basePath +"Issuer Name/l", basePath +"L")
//		        .addKeyTransformation(basePath +"Issuer Name/st", basePath +"ST")
//		        .addKeyTransformation(basePath +"Issuer Name/c", basePath +"Country")
//		        .addKeyTransformation(basePath +"Issuer Name/e", basePath +"Email")
//		        
//		        .addKeyTransformation(basePath +"Subject Name/serialNumber", basePath +"SNO")
//		        .addKeyTransformation(basePath +"Subject Name/hostname", basePath +"Host Name")
//		        .addKeyTransformation(basePath +"Subject Name/ou", basePath +"Organisation Unit")
//		        .addKeyTransformation(basePath +"Subject Name/o", basePath +"Organisation")
//		        .addKeyTransformation(basePath +"Subject Name/cn", basePath +"CN")
//		        .addKeyTransformation(basePath +"Subject Name/l", basePath +"L")
//		        .addKeyTransformation(basePath +"Subject Name/st", basePath +"ST")
//		        .addKeyTransformation(basePath +"Subject Name/c", basePath +"Country")
//		        .addKeyTransformation(basePath +"Subject Name/e", basePath +"Email")
		        
		        
		        .addKeyTransformation("Certificate Serial Number", "Serial Number")
		        .addKeyTransformation("Issuer Name", "Issued By")
		        .addKeyTransformation("Subject Name",  "Issued From")
		        .addKeyTransformation("Public Key Type", "Public Key Type")
		        .addKeyTransformation("Signature Algorithm", "Signature Algorithm")
		        .addKeyTransformation("Associated Trustpoints", "Associated Trustpoints")
		        
		        .addKeyTransformation("serialNumber", "Serial Number")
		        .addKeyTransformation("hostname", "Host Name")
		        .addKeyTransformation("ou", "Organization Unit")
		        .addKeyTransformation("o", "Organization")
		        .addKeyTransformation("cn", "Common Name")
		        .addKeyTransformation("l", "Locality")
		        .addKeyTransformation("st", "State")
		        .addKeyTransformation("c", "Country Code")
		        .addKeyTransformation("e", "Email")
		        
		        .addKeyTransformation("start date", "Valid From")
		        .addKeyTransformation("end date", "Valid To");
		
	}
}

