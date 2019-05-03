package com.cisco.nm.vms.api.certificate.node;

import java.util.ArrayList;
import java.util.List;

import com.cisco.nm.vms.api.certificate.parser.exception.InvalidCertificateNodeException;

/**
 * @author rgauttam
 *
 */
public class KeyValuePairCertificateNode extends AbstractCertificateNode {

    private String key;
    private String value;

    private KeyValuePairCertificateNode parent;
    private List<KeyValuePairCertificateNode> children = new ArrayList<>();

    public KeyValuePairCertificateNode() {

    }

    public KeyValuePairCertificateNode(String certNode) throws InvalidCertificateNodeException {
        loadCertNode(certNode);
    }

    public KeyValuePairCertificateNode(String certNode, KeyValuePairCertificateNode parent)
            throws InvalidCertificateNodeException {
        loadCertNode(certNode);
        this.parent = parent;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
        return children;
    }

    public void setChildren(List<KeyValuePairCertificateNode> children) {
        this.children = children;
    }

    public void addChild(KeyValuePairCertificateNode child) {
        this.children.add(child);
    }

    public void addChildAtIndex(KeyValuePairCertificateNode child, int index) {
        if (index > this.children.size())
            this.children.add(child);
        else
            this.children.add(index, child);
    }

    /**
     * @param index
     */
    // TODO:rgauttam need to be compacted after deletion
    public void removeChildAtIndex(int index) {
        this.children.remove(index);
    }

    /**
     * @param index
     */
    // TODO:rgauttam need to be compacted after deletion
    public void removeChild(KeyValuePairCertificateNode child) {
        this.children.remove(child);
    }

    public Boolean isLeaf() {
        return this.children.isEmpty();
    }

    public Boolean isLabel() {
        return this.value == null || this.value.isEmpty();
    }

    @Override
    void loadCertNode(String certNode) {
        String[] nodeArray = certNode.split(delegate.getKeyValueSplitRegx());
        if (certNode != null && certNode.isEmpty())
            throw new InvalidCertificateNodeException(
                    "Certificate token " + certNode + " is not valid to create " + this.getClass() + "type node");
        if (nodeArray.length == 0)
            this.key = certNode;
        else {
            this.key = (nodeArray[0] != null && !nodeArray[0].trim().isEmpty()) ? nodeArray[0].trim() : null;
            if (this.key == null)
                throw new InvalidCertificateNodeException(
                        "Certificate token " + certNode + " is not valid to create " + this.getClass() + "type node");
            if (nodeArray.length > 2) {
                boolean isValueSplitIgnored = false;
                for (String keyRegx : delegate.getKeysIgnoresSplit()) {
                    if (this.key.matches(keyRegx)) {
                        isValueSplitIgnored = true;
                    }
                }
                if (isValueSplitIgnored) {
                    this.value = (certNode.substring(certNode.indexOf(key) + key.length()))
                            .replaceFirst(delegate.getKeyValueSplitRegx(), "").trim();

                } else
                    throw new InvalidCertificateNodeException("Certificate token " + certNode
                            + " is not valid to create " + this.getClass() + "type node");
            } else
                this.value = (nodeArray.length == 2 && nodeArray[1] != null && !nodeArray[1].trim().isEmpty())
                        ? nodeArray[1].trim() : null;
        }
        this.value = delegate.getTransformedValue(this.key, this.value);
        this.key = delegate.getTransformedKey(this.key);
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