package com.cisco.nm.vms.api.certificate.parser.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cisco.nm.vms.api.certificate.parser.Converter;
import com.cisco.nm.vms.api.certificate.parser.exception.InvalidCertificateNodeException;

/**
 * @author rgauttam
 *
 */
public class KeyValueCertificateParserBuilder implements CertificateParserBuilder {

	private String splitNodesRegx = "\n";

	private String keyValueSplitRegx = "[:=]";

	/**
	 * If key matches with set of regx then it will ignore rest sub string from
	 * splitting and considered as value.
	 */
    private final Set<String> keysIgnoresSplit = new HashSet<>();

    private final Set<String> excludedKey = new HashSet<>();
    
    private final Map<String, String> multiNodeSpitter = new HashMap<>();

    private final Map<String, String> keyTransformations = new HashMap<>();

    private final Map<String, Converter> keyConverters = new HashMap<>();

    private final Map<String, Converter> valueConverters = new HashMap<>();

    /**
     * Mapping for keys and their order among its parent's children.
     * 
     * 
     * Keys will be like: ROOT/Certificate/Subject Name
     * 
     */
    private final Map<String, Integer> displayEntriesInOrderMap = new HashMap<>();

    private final Map<String, String> reArrangeNodesMap = new HashMap<>();

	@Override
	public String getSplitNodesRegx() {
		return splitNodesRegx;
	}

	@Override
	public CertificateParserBuilder addSplitNodesRegx(String splitNodesRegx) {
		this.splitNodesRegx = splitNodesRegx;
		return this;
	}

	@Override
	public CertificateParserBuilder addKeyValueSplitRegx(String keyValueSplitRegx) {
		this.keyValueSplitRegx = keyValueSplitRegx;
		return this;
	}

	@Override
	public String getKeyValueSplitRegx() {
		return keyValueSplitRegx;
	}

	@Override
	public CertificateParserBuilder addKeysIgnoresSplit(String regx) {
		keysIgnoresSplit.add(regx);
		return this;
	}

	@Override
	public CertificateParserBuilder removeKeysIgnoresSplit(String regx) {
		keysIgnoresSplit.add(regx);
		return this;
	}

	@Override
	public CertificateParserBuilder removeExcludedKey(String excludedKey) {
		this.excludedKey.remove(excludedKey);
		return this;
	}

	@Override
	public CertificateParserBuilder addExcludedKey(String excludedKey) {
		this.excludedKey.add(excludedKey);
		return this;
	}
	
	@Override
	public Boolean isExcludedKey(String excludedKey) {
		return this.excludedKey.contains(excludedKey);
	}

	@Override
	public Set<String> getKeysIgnoresSplit() {
		return Collections.unmodifiableSet(keysIgnoresSplit);
	}

	@Override
	public CertificateParserBuilder addValueConverter(String keyRegx, Converter valueConverter) {
		valueConverters.put(keyRegx, valueConverter);
		return this;
	}

	@Override
	public CertificateParserBuilder addKeyConverter(String keyRegx, Converter keyConverter) {
		keyConverters.put(keyRegx, keyConverter);
		return this;
	}

	@Override
	public Map<String, Converter> getKeyConverters() {
		return Collections.unmodifiableMap(keyConverters);
	}

	@Override
	public Map<String, Converter> getValueConverters() {
		return Collections.unmodifiableMap(valueConverters);
	}

	@Override
	public CertificateParserBuilder addKeyTransformation(String fromKey, String toKey) {
		keyTransformations.put(fromKey, toKey);
		return this;
	}

	@Override
	public String getTransformedKey(String key) {
		return keyTransformations.get(key);
	}

	@Override
	public CertificateParserBuilder removeKeyTransformation(String key) {
		keyTransformations.remove(key);
		return this;
	}

	@Override
	public CertificateParserBuilder addDisplayOrderEntry(String nodePath, Integer order) {
		displayEntriesInOrderMap.put(nodePath, order);
		return this;
	}

	@Override
	public CertificateParserBuilder removeDisplayOrderEntry(String nodePath) {
		displayEntriesInOrderMap.remove(nodePath);
		return this;
	}

	@Override
	public Integer getDisplayOrder(String nodePath) {
		return displayEntriesInOrderMap.get(nodePath);
	}

	@Override
	public CertificateParserBuilder addNodeMovementEntry(String fromNodePath, String toNodePath) {
		reArrangeNodesMap.put(fromNodePath, toNodePath);
		return this;
	}

	@Override
	public CertificateParserBuilder removeNodeMovementEntry(String fromNodePath) {
		reArrangeNodesMap.remove(fromNodePath);
		return this;
	}

	@Override
	public String getNewReArrangedPath(String existingPath) {
		return reArrangeNodesMap.get(existingPath);
	}
    
    @Override
    public String getPrimaryKeyByCertStr(String certStr) {
        String[] arr = certStr.split(keyValueSplitRegx, 2);
        if (arr != null && arr.length > 0 && !arr[0].isEmpty())
            return arr[0].trim();
        throw new InvalidCertificateNodeException("Certificate Str :" + certStr + "Not valid for parse");
    }
    
    @Override
    public CertificateParserBuilder addMultiNodeSpitterEntry(String key, String regx) {
        this.multiNodeSpitter.put(key, regx);
        return this;
    }
    
    @Override
    public CertificateParserBuilder removeMultiNodeSpitterEntry(String key) {
        this.multiNodeSpitter.remove(key);
        return this;
    }

    @Override
    public String getMultiNodeSpitterRegx(String key) {
        return this.multiNodeSpitter.get(key);
    }
}