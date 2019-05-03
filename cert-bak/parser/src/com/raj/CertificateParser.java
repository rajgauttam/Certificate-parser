package com.raj;

/**
 * @author rgauttam
 *
 */
@SuppressWarnings("rawtypes")
public abstract class CertificateParser {

	private static  CertificateParser instance;
	
	private static CertificateParserAdapter adapter = null;

	protected CertificateParserBuilder certificateParserBuilder;

	public CertificateParser(CertificateParserBuilder certificateParserBuilder) {
		this.certificateParserBuilder = certificateParserBuilder;
	}

	public static CertificateParser getInstance(String certificateName){
		
		switch (certificateName) {
		case "indentity":
			instance = new KeyValuePairCertificateParser();
			break;
			
		case "ca":
			instance = new KeyValuePairCertificateParser();
			break;

		default:
			if(adapter != null)
				instance = adapter.getCertificateFactory(certificateName);
			
			break;
		}
		
		return instance;
	}

	public static void setCertificateParserAdapter(CertificateParserAdapter adapter) {
		CertificateParser.adapter = adapter;
	}

	public CertificateParserBuilder getCertificateParserBuilder() {
		return certificateParserBuilder;
	}

	public static <T extends AbstractCertificateNode> T parse(String type, String certStr, CertificateParserAdapter adapter){
		CertificateParser.adapter = adapter;
		return getInstance(type).parse(certStr);
	}

	public static <T extends AbstractCertificateNode> T parse(String type, String certStr){
		return getInstance(type).parse(certStr);
	}

	public static <T extends AbstractCertificateNode> T parse(String type, String certStr, CertificateParserBuilder builder){
		CertificateParser parser = getInstance(type);
		parser.certificateParserBuilder = builder;
		return parser.parse(certStr);
	}
	
	public static <T extends AbstractCertificateNode> T parse(String type, String certStr, CertificateParserAdapter adapter, CertificateParserBuilder builder){
		CertificateParser.adapter = adapter;
		CertificateParser parser = getInstance(type);
		parser.certificateParserBuilder = builder;
		return parser.parse(certStr);
	}

	public  abstract <T extends AbstractCertificateNode> T parse(String certStr);
	
}
