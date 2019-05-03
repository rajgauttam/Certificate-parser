package com.cisco.nm.vms.api.certificate.parser.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cisco.nm.vms.api.certificate.parser.Converter;
import com.cisco.nm.vms.nbi.base.NBIException;

/**
 * @author rgauttam
 *
 */
public class CertificateParserBuilder {

    private String splitNodesRegx = "\n";

    private String keyValueSplitRegx = "[:=]";

    /**
     * If key matches with set of regx then it will ignore rest sub string from splitting and considered as value.
     */
    private final Set<String> keysIgnoresSplit = new HashSet<>();

    private final Set<String> excludedKey = new HashSet<>();

    private final Map<String, String> multiNodeSpitter = new HashMap<>();

    private final Map<String, String> keyTransformations = new HashMap<>();

    private final Map<String, Converter<String>> keyConverters = new HashMap<>();

    private final Map<String, Converter<String>> valueConverters = new HashMap<>();

    /**
     * Mapping for keys and their order among its parent's children.
     * 
     * 
     * Keys will be like: ROOT/Certificate/Subject Name
     * 
     */
    private final Map<String, Integer> displayEntriesInOrderMap = new HashMap<>();

    private final Map<String, String> reArrangeNodesMap = new HashMap<>();

    public String getSplitNodesRegx() {
        return splitNodesRegx;
    }

    public CertificateParserBuilder addSplitNodesRegx(String splitNodesRegx) {
        this.splitNodesRegx = splitNodesRegx;
        return this;
    }

    public CertificateParserBuilder addKeyValueSplitRegx(String keyValueSplitRegx) {
        this.keyValueSplitRegx = keyValueSplitRegx;
        return this;
    }

    public String getKeyValueSplitRegx() {
        return keyValueSplitRegx;
    }

    public CertificateParserBuilder addKeysIgnoresSplit(String regx) {
        keysIgnoresSplit.add(regx);
        return this;
    }

    public CertificateParserBuilder removeKeysIgnoresSplit(String regx) {
        keysIgnoresSplit.add(regx);
        return this;
    }

    public CertificateParserBuilder removeExcludedKey(String excludedKey) {
        this.excludedKey.remove(excludedKey);
        return this;
    }

    public CertificateParserBuilder addExcludedKey(String excludedKey) {
        this.excludedKey.add(excludedKey);
        return this;
    }

    public Boolean isExcludedKey(String excludedKey) {
        return this.excludedKey.contains(excludedKey);
    }

    public Set<String> getKeysIgnoresSplit() {
        return Collections.unmodifiableSet(keysIgnoresSplit);
    }

    public CertificateParserBuilder addValueConverter(String keyRegx, Converter<String> valueConverter) {
        valueConverters.put(keyRegx, valueConverter);
        return this;
    }

    public CertificateParserBuilder addKeyConverter(String keyRegx, Converter<String> keyConverter) {
        keyConverters.put(keyRegx, keyConverter);
        return this;
    }

    public Map<String, Converter<String>> getKeyConverters() {
        return Collections.unmodifiableMap(keyConverters);
    }

    public Map<String, Converter<String>> getValueConverters() {
        return Collections.unmodifiableMap(valueConverters);
    }

    public CertificateParserBuilder addKeyTransformation(String fromKey, String toKey) {
        keyTransformations.put(fromKey, toKey);
        return this;
    }

    public String getTransformedKey(String key) {
        return keyTransformations.get(key);
    }

    public CertificateParserBuilder removeKeyTransformation(String key) {
        keyTransformations.remove(key);
        return this;
    }

    public CertificateParserBuilder addDisplayOrderEntry(String nodePath, Integer order) {
        displayEntriesInOrderMap.put(nodePath, order);
        return this;
    }

    public CertificateParserBuilder removeDisplayOrderEntry(String nodePath) {
        displayEntriesInOrderMap.remove(nodePath);
        return this;
    }

    public Integer getDisplayOrder(String nodePath) {
        return displayEntriesInOrderMap.get(nodePath);
    }

    public CertificateParserBuilder addNodeMovementEntry(String fromNodePath, String toNodePath) {
        reArrangeNodesMap.put(fromNodePath, toNodePath);
        return this;
    }

    public CertificateParserBuilder removeNodeMovementEntry(String fromNodePath) {
        reArrangeNodesMap.remove(fromNodePath);
        return this;
    }

    public String getNewReArrangedPath(String existingPath) {
        return reArrangeNodesMap.get(existingPath);
    }

    public String getPrimaryKeyByCertStr(String certStr) throws NBIException {
        String[] arr = certStr.split(keyValueSplitRegx, 2);
        if (arr != null && arr.length > 0 && !arr[0].isEmpty())
            return arr[0].trim();
        throw new NBIException("Certificate Str :" + certStr + "Not valid for parse");
    }

    public CertificateParserBuilder addMultiNodeSpitterEntry(String key, String regx) {
        this.multiNodeSpitter.put(key, regx);
        return this;
    }

    public CertificateParserBuilder removeMultiNodeSpitterEntry(String key) {
        this.multiNodeSpitter.remove(key);
        return this;
    }

    public String getMultiNodeSpitterRegx(String key) {
        return this.multiNodeSpitter.get(key);
    }
}