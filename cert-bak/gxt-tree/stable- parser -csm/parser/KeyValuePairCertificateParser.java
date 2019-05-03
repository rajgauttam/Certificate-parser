package com.cisco.nm.vms.api.certificate.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cisco.nm.vms.api.certificate.node.AbstractCertificateNode;
import com.cisco.nm.vms.api.certificate.node.Converter;
import com.cisco.nm.vms.api.certificate.node.KeyValuePairCertificateNode;
import com.cisco.nm.vms.api.certificate.parser.exception.InvalidCertificateNodeException;

/**
 * @author rgauttam
 *
 */
public class KeyValuePairCertificateParser extends CertificateParser {

    private static final String SPLIT_NODES_REGX = "\n";

    /**
     *
     * Excluded keys will be like: ROOT/Certificate/Subject Name
     *
     */
    private Set<String> excludedKey = new HashSet<>();

    /**
     * Mapping for keys and their order among its parent's children.
     * 
     * 
     * Keys will be like: ROOT/Certificate/Subject Name
     * 
     */
    private Map<String, Integer> displayEntriesInOrderMap = new HashMap<>();

    public KeyValuePairCertificateParser() {
        AbstractCertificateNode.getDelegate().addValueConverter(".*[Dd]ate.*", new Converter() {

            @SuppressWarnings("unchecked")
            @Override
            public String convert(String value) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss z MMMMM dd yyyyy");
                // sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date existingDateValue = null;
                try {
                    existingDateValue = sdf.parse(value.trim());
                } catch (ParseException e) {
                    throw new InvalidCertificateNodeException("Date is not proper formated", e);
                }
                // sdf.setTimeZone(TimeZone.getDefault());
                return sdf.format(existingDateValue);
            }
        });

    }

    @SuppressWarnings("unchecked")
    @Override
    public KeyValuePairCertificateNode parse(String certStr) {
        KeyValuePairCertificateNode rootNode = new KeyValuePairCertificateNode();
        rootNode.setKey("ROOT");
        String[] certStrNodesArr = certStr.split(SPLIT_NODES_REGX);
        int parentLevel = -1;
        int level = -1;
        Map<Integer, KeyValuePairCertificateNode> recentParentLevelMap = new HashMap<>();
        recentParentLevelMap.put(level, rootNode);
        for (int i = 0; i < certStrNodesArr.length; i++) {
            level = getAlignedSpace(certStrNodesArr[i]);
            KeyValuePairCertificateNode node = null;
            if (level > parentLevel) {
                KeyValuePairCertificateNode parentNode = recentParentLevelMap.get(parentLevel);
                node = createNode(certStrNodesArr[i], parentNode);
            } else if (level < parentLevel) {
                KeyValuePairCertificateNode parentNode = null;
                for (int j = level - 1; i >= -1; j--) {
                    parentNode = recentParentLevelMap.get(j);
                    if (parentNode == null && j != -1)
                        continue;
                    if (j == -1)
                        break;
                    KeyValuePairCertificateNode tmpNode = recentParentLevelMap.get(parentLevel).getParent();
                    boolean isFound = false;
                    while (tmpNode != null) {
                        if (parentNode.getKey().equals(tmpNode.getKey())) {
                            isFound = true;
                            break;
                        }
                        tmpNode = tmpNode.getParent();
                    }
                    if (isFound) {
                        break;
                    }

                }
                node = createNode(certStrNodesArr[i], parentNode);
            } else {
                KeyValuePairCertificateNode parentNode = recentParentLevelMap.get(parentLevel).getParent();
                node = createNode(certStrNodesArr[i], parentNode);
            }
            if (node != null) {
                parentLevel = level;
                recentParentLevelMap.put(level, node);
            }
        }

        return rootNode;
    }

    private KeyValuePairCertificateNode createNode(String certStrNode, KeyValuePairCertificateNode parentNode)
            throws InvalidCertificateNodeException {
        KeyValuePairCertificateNode node = new KeyValuePairCertificateNode(certStrNode, parentNode);
        if (!isExcludedNode(node))
            addChild(parentNode, node);
        return node;
    }

    // TODO: rgauttam need to be added logic if parent is already excluded as excluded map then child will be excluded.
    private Boolean isExcludedNode(KeyValuePairCertificateNode node) {
        KeyValuePairCertificateNode tmpNode = node;
        StringBuilder sb = new StringBuilder("");
        while (tmpNode.getParent() != null) {
            sb.insert(0, tmpNode.getKey());
            sb.insert(0, "/");
            tmpNode = tmpNode.getParent();
        }
        if (tmpNode != null && excludedKey.contains(sb)) {
            return true;
        }
        ;
        return false;
    }

    private void addChild(KeyValuePairCertificateNode parent, KeyValuePairCertificateNode node) {
        KeyValuePairCertificateNode tmpParent = parent;
        StringBuilder sb = new StringBuilder("");
        sb.insert(0, node.getKey());
        sb.insert(0, "/");
        while (tmpParent != null && tmpParent.getParent() != null) {
            sb.insert(0, tmpParent.getKey());
            sb.insert(0, "/");
            tmpParent = tmpParent.getParent();
        }
        if (parent != null) {
            Integer index = displayEntriesInOrderMap.get(sb);
            if (index != null)
                parent.addChildAtIndex(node, displayEntriesInOrderMap.get(sb));
            else
                parent.addChild(node);
        }
    }

    private int getAlignedSpace(String str) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (Character.isWhitespace(str.charAt(i)))
                count++;
            else
                break;
        }
        return count;
    }
}