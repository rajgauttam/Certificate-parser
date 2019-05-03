package com.cisco.nm.vms.api.certificate.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.cisco.nm.vms.api.certificate.node.AbstractCertificateNode;
import com.cisco.nm.vms.api.certificate.node.Converter;
import com.cisco.nm.vms.api.certificate.node.KeyValuePairCertificateNode;
import com.cisco.nm.vms.api.certificate.parser.exception.InvalidCertificateNodeException;

public class KeyValuePairCertificateParser extends CertificateParser{

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
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date existingDateValue = null;
                try {
                    existingDateValue = sdf.parse(value.trim());
                    System.err.println(existingDateValue);
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
	public  KeyValuePairCertificateNode parse(String certStr){
		KeyValuePairCertificateNode rootNode = new KeyValuePairCertificateNode();
		rootNode.setKey("ROOT");
		String[] certStrNodesArr = certStr.split(SPLIT_NODES_REGX);
		int parentLevel = -1;
		int level = -1;
		Map<Integer, KeyValuePairCertificateNode> recentParentLevelMap = new HashMap<>();
		recentParentLevelMap.put(level, rootNode);
		for (int i = 0; i < certStrNodesArr.length; i++) {
			level = getAlignedSpace(certStrNodesArr[i]);
			System.out.println(i +" |||| "+ level +" ---- >"+certStrNodesArr[i]);
		    KeyValuePairCertificateNode node = null;
		    if(level > parentLevel){
		    	KeyValuePairCertificateNode parentNode = recentParentLevelMap.get(parentLevel);
		    	node = createNode(certStrNodesArr, i, parentNode);
			}else if(level < parentLevel){
				KeyValuePairCertificateNode parentNode = null;
				for(int j= level-1 ; i >=-1; j-- ){
					parentNode = recentParentLevelMap.get(j);
					if (parentNode == null && j != -1)
							continue;
					if(j == -1)
						break;
					KeyValuePairCertificateNode tmpNode = recentParentLevelMap.get(parentLevel).getParent();
					boolean isFound= false;
					while(tmpNode != null){
						if(parentNode.getKey().equals(tmpNode.getKey()) ){
							isFound = true;
							break;
						}
						tmpNode = tmpNode.getParent();
					}
					if(isFound){
						break;
					}
					 
				}
				node = createNode(certStrNodesArr, i, parentNode);
			}else{
				KeyValuePairCertificateNode parentNode = recentParentLevelMap.get(parentLevel).getParent();
				node = createNode(certStrNodesArr, i, parentNode);
			}
			if (node != null) {
				parentLevel = level;
				recentParentLevelMap.put(level, node);
			}
		    System.out.println(node.getKey());
		}
		
		System.out.println(recentParentLevelMap.get(0).getKey());
		System.out.println(recentParentLevelMap.get(2).getKey());
		System.out.println(recentParentLevelMap.get(4).getKey());
		return rootNode;
	}

	private KeyValuePairCertificateNode createNode(String[] arr, int i, KeyValuePairCertificateNode parentNode)
			throws InvalidCertificateNodeException {
		KeyValuePairCertificateNode node = new KeyValuePairCertificateNode(arr[i], parentNode);
		if(!isExcludedNode(node))
		   addChild(parentNode, node);
		return node;
	}
	
	private Boolean isExcludedNode(KeyValuePairCertificateNode node) {
		StringBuilder sb = new StringBuilder("");
		while(node.getParent() != null){
			sb.insert(0, node.getKey());
			sb.insert(0, "/");
			node = node.getParent();
		}	
		if (node != null && excludedKey.contains(sb)) {
			return true;
		};
		return false;
	}
	
	private void addChild(KeyValuePairCertificateNode parent, KeyValuePairCertificateNode node) {
		StringBuilder sb = new StringBuilder("");
		sb.insert(0, node.getKey());
		sb.insert(0, "/");
		while (parent != null && parent.getParent() != null) {
			sb.insert(0, parent.getKey());
			sb.insert(0, "/");
			parent = parent.getParent();
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