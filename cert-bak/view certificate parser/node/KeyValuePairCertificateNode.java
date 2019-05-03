package com.cisco.nm.vms.api.certificate.node;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.cisco.nm.vms.api.certificate.parser.exception.InvalidCertificateNodeException;

/**
 * @author rgauttam
 *
 */
public class KeyValuePairCertificateNode extends AbstractCertificateNode {

	private static final String GROUPED_VALUE_SEPARATOR = ", ";
	private String key;
	private String dispKey;
	private String value;

	private KeyValuePairCertificateNode parent;
	private List<KeyValuePairCertificateNode> children = new ArrayList<>();
	private TreeMap<Integer, List<KeyValuePairCertificateNode>> childrenOrderMap = new TreeMap<>();

	KeyValuePairCertificateNode() {

	}
	
	KeyValuePairCertificateNode(KeyValuePairCertificateNode parent){
		this.parent = parent;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDispKey() {
		return dispKey;
	}

	public void setDispKey(String dispKey) {
		this.dispKey = dispKey;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public KeyValuePairCertificateNode getParent() {
		return parent;
	}

	public void setParent(KeyValuePairCertificateNode parent) {
		this.parent = parent;
	}

	public List<KeyValuePairCertificateNode> getChildren() {
		if(children.isEmpty())
			childrenOrderMap.forEach((index, nodesList) -> children.add(joinedNodes(nodesList)));
		return children;
	}

	public void setChildren(List<KeyValuePairCertificateNode> children) {
		this.children = children;
	}

	public void addChild(KeyValuePairCertificateNode child) {
		if(childrenOrderMap.isEmpty())
			addChildAtIndex(child, 0);
		else
			addChildAtIndex(child, childrenOrderMap.lastKey()+1);
	}

	public void addChildAtIndex(KeyValuePairCertificateNode child, int index) {
		// if (index > this.children.size())
		// this.children.add(child);
		// else
		// this.children.add(index, child);
		List<KeyValuePairCertificateNode> list = this.childrenOrderMap.get(index);
		if (list == null) {
			list = new ArrayList<>();
			this.childrenOrderMap.put(index, list);
		}

		list.add(child);
	}

	/**
	 * @param index
	 * 
	 */
	public void removeChildAtIndex(int index) {
		this.childrenOrderMap.remove(index);
	}
	/**
	 * @param index
	 * 
	 */
	@Deprecated
	public void removeChild(KeyValuePairCertificateNode child) {
		this.children.remove(child);
	}

	public Boolean isLeaf() {
		return this.children.isEmpty();
	}

	public Boolean isLabel() {
		return this.value == null || this.value.isEmpty();
	}
	
	public static KeyValuePairCertificateNode joinedNodes(List<KeyValuePairCertificateNode> list) {
		KeyValuePairCertificateNode node = null;
		for (KeyValuePairCertificateNode anotherNode : list) {
			if(node != null){
				if(node.getKey().equals(anotherNode.getKey())){
					if(node.getValue() != null && anotherNode.getValue() != null){
						node.setValue(node.getValue() + GROUPED_VALUE_SEPARATOR + anotherNode.getValue());
					}else if(node.getValue() == null && anotherNode.getValue() == null){
						node.getChildren().addAll(anotherNode.getChildren());
					}else
						throw new InvalidCertificateNodeException("Two or more than two diffent types of nodes with "
								+ "having same key should not exist in same parent");
				}
			}else 
					node = anotherNode;
		}
		return node;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyValuePairCertificateNode other = (KeyValuePairCertificateNode) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
