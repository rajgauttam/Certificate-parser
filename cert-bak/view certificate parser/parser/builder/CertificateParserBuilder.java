package com.cisco.nm.vms.api.certificate.parser.builder;

import java.util.Map;
import java.util.Set;

import com.cisco.nm.vms.api.certificate.parser.Converter;

/**
 * @author rgauttam
 *
 */
public interface CertificateParserBuilder {

	String getSplitNodesRegx();

	CertificateParserBuilder addSplitNodesRegx(String splitNodesRegx);

	CertificateParserBuilder addKeyValueSplitRegx(String keyValueSplitRegx);

	String getKeyValueSplitRegx();

	CertificateParserBuilder addKeysIgnoresSplit(String regx);

	CertificateParserBuilder removeKeysIgnoresSplit(String regx);

	CertificateParserBuilder removeExcludedKey(String excludedKey);

	CertificateParserBuilder addExcludedKey(String excludedKey);

	Set<String> getKeysIgnoresSplit();

	CertificateParserBuilder addValueConverter(String keyRegx, Converter valueConverter);

	CertificateParserBuilder addKeyConverter(String keyRegx, Converter keyConverter);

	CertificateParserBuilder addKeyTransformation(String fromKey, String toKey);

	String getTransformedKey(String key);

	CertificateParserBuilder removeKeyTransformation(String key);

	CertificateParserBuilder addDisplayOrderEntry(String nodePath, Integer order);

	CertificateParserBuilder removeDisplayOrderEntry(String nodePath);

	Integer getDisplayOrder(String nodePath);

	CertificateParserBuilder addNodeMovementEntry(String fromNodePath, String toNodePath);

	CertificateParserBuilder removeNodeMovementEntry(String fromNodePath);

    CertificateParserBuilder addMultiNodeSpitterEntry(String key, String regx);

    CertificateParserBuilder removeMultiNodeSpitterEntry(String key);

	String getNewReArrangedPath(String existingPath);

	Map<String, Converter> getKeyConverters();

	Map<String, Converter> getValueConverters();

	Boolean isExcludedKey(String excludedKey);

    String getPrimaryKeyByCertStr(String certStr);

    String getMultiNodeSpitterRegx(String key);

}