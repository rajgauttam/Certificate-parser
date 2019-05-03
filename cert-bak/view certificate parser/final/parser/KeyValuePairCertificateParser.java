package com.cisco.nm.vms.api.certificate.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.cisco.nm.vms.api.certificate.node.CertificateNodeFacade;
import com.cisco.nm.vms.api.certificate.node.KeyValuePairCertificateNode;
import com.cisco.nm.vms.api.certificate.parser.builder.CertificateParserBuilder;
import com.cisco.nm.vms.nbi.base.NBIException;

/**
 * @author rgauttam
 *
 */
public class KeyValuePairCertificateParser<T> extends CertificateParser<KeyValuePairCertificateNode> {

	private static final String ROOT = "ROOT";

	private static final String PATH_SEPARATOR = "/";

	protected final CertificateNodeFacade certificateNodeFacade;

	public KeyValuePairCertificateParser() {
		super(new CertificateParserBuilder());
		certificateNodeFacade = new CertificateNodeFacade(certificateParserBuilder);
		certificateParserBuilder.addValueConverter(".*[Dd]ate.*", new Converter<String>() {

			@Override
			public String convert(String value) throws NBIException {
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss z MMMMM dd yyyy");
				//sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				Date existingDateValue = null;
				try {
					existingDateValue = sdf.parse(value.trim());
				} catch (ParseException e) {
					throw new NBIException("Date is not proper formated" + e.getMessage());
				}
				sdf.setTimeZone(TimeZone.getDefault());
				return sdf.format(existingDateValue);
			}
		});

	}

    @Override
    public KeyValuePairCertificateNode parse(String certStr) throws NBIException {
        KeyValuePairCertificateNode rootNode = certificateNodeFacade.createCertNode(ROOT);
        Map<String, KeyValuePairCertificateNode> pendingNodesForMove = new HashMap<>();
        String[] certStrNodesArr = certStr.split(certificateParserBuilder.getSplitNodesRegx());
        int parentLevel = -1;
        int level = -1;
        Map<Integer, KeyValuePairCertificateNode> recentParentLevelMap = new HashMap<>();
        recentParentLevelMap.put(level, rootNode);
        for (int i = 0; i < certStrNodesArr.length; i++) {
            String certNodeStr = certStrNodesArr[i];
            level = getAlignedSpace(certNodeStr);
            String multiNodeSplitRegx = certificateParserBuilder.getMultiNodeSpitterRegx(certificateParserBuilder.getPrimaryKeyByCertStr(certNodeStr));
            String[] certStrBrokeNodesArr = null;

            if (multiNodeSplitRegx != null && (certStrBrokeNodesArr =   certNodeStr.split(multiNodeSplitRegx)).length >=2) {
                    for (String certStrBrokeNode : certStrBrokeNodesArr) {
                        parentLevel = processNode(certStrBrokeNode, i, parentLevel, level, recentParentLevelMap,
                                pendingNodesForMove);
                    }
            } else
                parentLevel = processNode(certNodeStr, i, parentLevel, level, recentParentLevelMap,
                        pendingNodesForMove);
        }

        adjustMovedNodesInHeirarchy(rootNode, pendingNodesForMove);
        return rootNode;
    }

    private int processNode(String certNodeStr, int i, int parentLevel, int level,
            Map<Integer, KeyValuePairCertificateNode> recentParentLevelMap,
            Map<String, KeyValuePairCertificateNode> pendingNodesForMove) throws NBIException {

        KeyValuePairCertificateNode node = null;
        if (level > parentLevel) {
            KeyValuePairCertificateNode parentNode = recentParentLevelMap.get(parentLevel);
            node = createNode(certNodeStr, parentNode, pendingNodesForMove);
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
            node = createNode(certNodeStr, parentNode, pendingNodesForMove);
        } else {
            KeyValuePairCertificateNode parentNode = recentParentLevelMap.get(parentLevel).getParent();
            node = createNode(certNodeStr, parentNode, pendingNodesForMove);
        }
        if (node != null) {
            parentLevel = level;
            recentParentLevelMap.put(level, node);
        }
        return parentLevel;
    }

	private void adjustMovedNodesInHeirarchy(KeyValuePairCertificateNode rootNode,
			Map<String, KeyValuePairCertificateNode> pendingNodesForMove) {
		Set<String> pendingNodesForMoveKeys = Collections.unmodifiableSet(pendingNodesForMove.keySet());
		for (String path : pendingNodesForMoveKeys) {
			while (!path.equals(ROOT)) {
				String parentPath = path.substring(0, path.lastIndexOf(PATH_SEPARATOR));
				KeyValuePairCertificateNode parentNode = pendingNodesForMove.get(parentPath);
				KeyValuePairCertificateNode node = pendingNodesForMove.get(path);
				// pendingNodesForMove.remove(path);
				if (parentNode == null) {
					parentNode = getNode(rootNode, parentPath);
					node.setParent(parentNode);
					addChild(parentNode, node, path);
					break;
				} else {
					node.setParent(parentNode);
					addChild(parentNode, node, path);
				}
				path = parentPath;
			}
		}
	}

	private KeyValuePairCertificateNode getNode(KeyValuePairCertificateNode rootNode, String restPath) {
		if (restPath == null || rootNode == null)
			return null;
		if (restPath.indexOf(PATH_SEPARATOR) == -1 && restPath.equals(rootNode.getKey()))
			return rootNode;
		String pathKey = restPath.substring(0, restPath.indexOf(PATH_SEPARATOR));
		restPath = restPath.substring(pathKey.length() + 1);
		KeyValuePairCertificateNode result = null;
		if (rootNode.getKey().equals(pathKey)) {

			String childPathKey = restPath.indexOf(PATH_SEPARATOR) == -1 ? restPath
					: restPath.substring(0, restPath.indexOf(PATH_SEPARATOR));
			Set<KeyValuePairCertificateNode> matchedNodesSet = new HashSet<>();
			for (KeyValuePairCertificateNode childNode : rootNode.getChildren()) {
				if (childNode.getKey().equals(childPathKey))
					matchedNodesSet.add(childNode);

			}
			for (KeyValuePairCertificateNode matchedNode : matchedNodesSet) {
				result = getNode(matchedNode, restPath);
				if (result != null)
					return result;
			}
		}
		return null;
	}

	private KeyValuePairCertificateNode createNode(String certStrNode, KeyValuePairCertificateNode parentNode,
			Map<String, KeyValuePairCertificateNode> pendingNodeForMove) throws NBIException {
		KeyValuePairCertificateNode node = certificateNodeFacade.createCertNode(certStrNode, parentNode);
		String path = getNodePath(node);
		String newPath = certificateParserBuilder.getNewReArrangedPath(path);
		if (newPath != null) {
			pendingNodeForMove.put(newPath, node);
			return node;
		}
		if (node != null && !certificateParserBuilder.isExcludedKey(path))
			addChild(parentNode, node, path);
		return node;
	}

	private void addChild(KeyValuePairCertificateNode parent, KeyValuePairCertificateNode node, String path) {
		if (parent != null) {
			Integer index = certificateParserBuilder.getDisplayOrder(path);
			if (index != null)
				parent.addChildAtIndex(node, index);
			else
				parent.addChild(node);
		}
	}

	private String getNodePath(KeyValuePairCertificateNode node) {
		KeyValuePairCertificateNode tmp = node;
		StringBuilder sb = new StringBuilder("");
		while (tmp != null) {
			sb.insert(0, tmp.getKey());
			sb.insert(0, PATH_SEPARATOR);
			tmp = tmp.getParent();
		}
		sb.deleteCharAt(0);
		return sb.toString();
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