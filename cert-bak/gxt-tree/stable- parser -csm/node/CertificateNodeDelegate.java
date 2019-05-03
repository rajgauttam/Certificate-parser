package com.cisco.nm.vms.api.certificate.node;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author rgauttam
 *
 */
public class CertificateNodeDelegate {

	private String KeyValueSplitRegx = "[:=]";

	private final Map<String, String> keyTransformations = new HashMap<>();

	/**
	 * If key matches with set of regx then it will ignore rest sub string from
	 * splitting and considered as value.
	 */
	private Set<String> keysIgnoresSplit = new HashSet<>();
	{
		keysIgnoresSplit.add(".*[Dd]ate.*");
	}

	private Map<String, Converter> keyConverters = new HashMap<>();

	private Map<String, Converter> valueConverters = new HashMap<>();

	public CertificateNodeDelegate transformation(String fromKey, String toKey) {
		keyTransformations.put(fromKey, toKey);
		return this;
	}

	public String getTransformedKey(String key) {
		if(key == null)
			return key;
		String transformedKey = keyTransformations.get(key);
		if (transformedKey != null)
			return transformedKey;
		for (String keyRegx : keyConverters.keySet()) {
			if (key.matches(keyRegx)) {
				Converter vc = keyConverters.get(keyRegx);
				transformedKey = vc != null ? vc.convert(key) : key;
				keyTransformations.put(key, transformedKey);
				return transformedKey;
			}
		}
		return key;
	}

	public String getTransformedValue(String key, String value) {
		if(key == null || value == null)
			return value;
		for (String keyRegx : valueConverters.keySet()) {
			if(key.matches(keyRegx))
				return valueConverters.get(keyRegx).convert(value);
		}
		return value;
	}

	public void addKeysIgnoresSplit(String regx) {
		keysIgnoresSplit.add(regx);
	}

	public void removeKeysIgnoresSplit(String regx) {
		keysIgnoresSplit.add(regx);
	}

	public Set<String> getKeysIgnoresSplit() {
		return Collections.unmodifiableSet(keysIgnoresSplit);
	}

	public CertificateNodeDelegate addValueConverter(String keyRegx, Converter valueConverter) {
		valueConverters.put(keyRegx, valueConverter);
		return this;
	}

	public void setKeyValueSplitRegx(String keyValueSplitRegx) {
		KeyValueSplitRegx = keyValueSplitRegx;
	}

	public String getKeyValueSplitRegx() {
		return KeyValueSplitRegx;
	}

}