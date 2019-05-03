package com.cisco.nm.vms.api.certificate.node;

import java.util.Map;
import java.util.Set;

import com.cisco.nm.vms.api.certificate.parser.Converter;
import com.cisco.nm.vms.api.certificate.parser.builder.CertificateParserBuilder;
import com.cisco.nm.vms.api.certificate.parser.exception.InvalidCertificateNodeException;

/**
 * @author rgauttam
 *
 */
public class CertificateNodeFacade{
	

	private String keyValueSplitRegx = "[:=]";
	
	private final Map<String, Converter> keyConverters;

	private final Map<String, Converter> valueConverters;
	
	protected CertificateParserBuilder certificateParserBuilder;

	public CertificateNodeFacade(CertificateParserBuilder certificateParserBuilder) {
		this.certificateParserBuilder = certificateParserBuilder;
		this.keyConverters = certificateParserBuilder.getKeyConverters();
		this.valueConverters = certificateParserBuilder.getValueConverters();
		String keyValueSplitRegx = certificateParserBuilder.getKeyValueSplitRegx();
		if(keyValueSplitRegx != null && !keyValueSplitRegx.isEmpty())
			this.keyValueSplitRegx = keyValueSplitRegx;
		certificateParserBuilder.addKeysIgnoresSplit(".*[Dd]ate.*");
	}

	public CertificateNodeFacade transformation(String fromKey, String toKey) {
		certificateParserBuilder.addKeyTransformation(fromKey, toKey);
		return this;
	}

	/**
	 *
	 * TODO Currently key transformation is based on key which will transform any other node which have same key
	 * It doesn't matter that where does it exist? means who is it parent whether same or not
	 * 
	 * Improvement required : It should be based on key path 
	 *
	 * @param key
	 * @return
	 */
	public String getTransformedKey(String key) {
		if(key == null)
			return key;
		String transformedKey = certificateParserBuilder.getTransformedKey(key);
		if (transformedKey != null)
			return transformedKey;
		for (String keyRegx : keyConverters.keySet()) {
			if (key.matches(keyRegx)) {
				Converter vc = keyConverters.get(keyRegx);
				transformedKey = vc != null ? vc.convert(key) : key;
				certificateParserBuilder.addKeyTransformation(key, transformedKey);
				return transformedKey;
			}
		}
		return key;
	}
	
	/**
	 *
	 * TODO Currently key transformation is based on key which will transform all the value which have same key
	 * It doesn't matter that where does it exist? means who is it parent whether same or not
	 * 
	 * Improvement required : It should be based on key path 
	 *
	 * @param key
	 * @return
	 */public String getTransformedValue(String key, String value) {
		if(key == null || value == null)
			return value;
		for (String keyRegx : valueConverters.keySet()) {
			if(key.matches(keyRegx))
				return valueConverters.get(keyRegx).convert(value);
		}
		return value;
	}

	public void addKeysIgnoresSplit(String regx) {
		certificateParserBuilder.addKeysIgnoresSplit(regx);
	}

	public void removeKeysIgnoresSplit(String regx) {
		certificateParserBuilder.removeKeysIgnoresSplit(regx);
	}

	public Set<String> getKeysIgnoresSplit() {
		return certificateParserBuilder.getKeysIgnoresSplit();
	}

	public KeyValuePairCertificateNode createCertNode(String certNode, KeyValuePairCertificateNode parent)  {
		KeyValuePairCertificateNode node = createCertNode(certNode);
		node.setParent(parent);
		return node;
	}

	public KeyValuePairCertificateNode createCertNode(String certNode) {
		KeyValuePairCertificateNode node = new KeyValuePairCertificateNode(); 
		String[] nodeArray = certNode.split(this.keyValueSplitRegx);
		if (certNode != null && certNode.isEmpty())
			throw new InvalidCertificateNodeException(
					"Certificate token " + certNode + " is not valid to create " + this.getClass() + "type node");
		if (nodeArray.length == 0)
			node.setKey(certNode);
		else {
			node.setKey((nodeArray[0] != null && !nodeArray[0].trim().isEmpty()) ? nodeArray[0].trim() : null);
			if (node.getKey() == null)
				throw new InvalidCertificateNodeException(
						"Certificate token " + certNode + " is not valid to create " + this.getClass() + "type node");
			if (nodeArray.length > 2) {
				boolean isValueSplitIgnored = false;
				for (String keyRegx : getKeysIgnoresSplit()) {
					if (node.getKey().matches(keyRegx)) {
						isValueSplitIgnored = true;
					}
				}
				if (isValueSplitIgnored) {
					node.setValue((certNode.substring(certNode.indexOf(node.getKey()) + node.getKey().length()))
							.replaceFirst(this.keyValueSplitRegx, "").trim());

				} else
					throw new InvalidCertificateNodeException("Certificate token " + certNode
							+ " is not valid to create " + this.getClass() + "type node");
			} else
				node.setValue((nodeArray.length == 2 && nodeArray[1] != null && !nodeArray[1].trim().isEmpty())
						? nodeArray[1].trim() : null);
		}
		node.setKey(node.getKey().replaceAll(" +", " "));
		node.setValue(getTransformedValue(node.getKey(), node.getValue()));
		node.setDispKey(getTransformedKey(node.getKey()));
		return node;
	}

}