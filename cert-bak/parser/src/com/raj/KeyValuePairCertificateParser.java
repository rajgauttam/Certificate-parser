package com.raj;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * @author rgauttam
 *
 */
public class KeyValuePairCertificateParser extends CertificateParser {

	private static final String ROOT = "ROOT";

	private static final String PATH_SEPARATOR = "/";

	protected final CertificateNodeFacade certificateNodeFacade;

	public KeyValuePairCertificateParser() {
		super(new KeyValueCertificateParserBuilder());
		certificateNodeFacade = new CertificateNodeFacade(certificateParserBuilder);
		certificateParserBuilder.addValueConverter(".*[Dd]ate.*", new Converter() {

			@SuppressWarnings("unchecked")
			@Override
			public String convert(String value) {
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss z MMMMM dd yyyy");
				// sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				Date existingDateValue = null;
				try {
					existingDateValue = sdf.parse(value.trim());
				} catch (ParseException e) {
					throw new InvalidCertificateNodeException("Date is not proper formated", e);
				}
				sdf.setTimeZone(TimeZone.getDefault());
				return sdf.format(existingDateValue);
			}
		});

	}

	@SuppressWarnings("unchecked")
	@Override
	public KeyValuePairCertificateNode parse(String certStr) {
		KeyValuePairCertificateNode rootNode = certificateNodeFacade.createCertNode(ROOT);
		Map<String, KeyValuePairCertificateNode> pendingNodesForMove = new HashMap<>();
		String[] certStrNodesArr = certStr.split(certificateParserBuilder.getSplitNodesRegx());
		int parentLevel = -1;
		int level = -1;
		Map<Integer, KeyValuePairCertificateNode> recentParentLevelMap = new HashMap<>();
		recentParentLevelMap.put(level, rootNode);
		for (int i = 0; i < certStrNodesArr.length; i++) {
			level = getAlignedSpace(certStrNodesArr[i]);
			KeyValuePairCertificateNode node = null;

			if (level > parentLevel) {
				KeyValuePairCertificateNode parentNode = recentParentLevelMap.get(parentLevel);
				node = createNode(certStrNodesArr[i], parentNode, pendingNodesForMove);
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
				node = createNode(certStrNodesArr[i], parentNode, pendingNodesForMove);
			} else {
				KeyValuePairCertificateNode parentNode = recentParentLevelMap.get(parentLevel).getParent();
				node = createNode(certStrNodesArr[i], parentNode, pendingNodesForMove);
			}
			if (node != null) {
				parentLevel = level;
				recentParentLevelMap.put(level, node);
			}
		}

		adjustMovedNodesInHeirarchy(rootNode, pendingNodesForMove);
		return rootNode;
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
			Map<String, KeyValuePairCertificateNode> pendingNodeForMove) {
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