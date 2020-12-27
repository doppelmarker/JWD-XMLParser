package com.markedline.xml_parser.node;

import java.util.*;

public final class Element {

    private String tagName;
    private List<Attribute> attributes;
    private Element parent;
    private List<Element> children;
    private String text;


    public Element(Builder builder) {
        this.tagName = builder.tagName;
        this.attributes = builder.attributes;
        this.parent = builder.parent;
        this.children = builder.children;
        this.text = builder.text;
    }

    public Element() {
    }

    public String getTagName() {
        return this.tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAttributeValue(String name) {
        if (this.attributes == null) {
            throw new IllegalStateException();
        } else if (name == null) {
            throw new IllegalArgumentException();
        } else {
            int idx = this.getAttrIdx(name);
            return idx >= 0 ? this.attributes.get(idx).getValue() : null;
        }
    }

    private int getAttrIdx(String name) {
        if (this.attributes != null) {
            for (int i = 0; i < this.attributes.size(); ++i) {
                if (this.attributes.get(i).getName().equals(name)) {
                    return i;
                }
            }
        }

        return -1;
    }

    public int getAttributeCount() {
        return this.attributes.size();
    }

    public String getAttributeName(int i) {
        if (this.attributes == null) {
            throw new IllegalStateException();
        } else if (i >= 0 && i < this.getAttributeCount()) {
            return this.attributes.get(i).getName();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public String getAttributeValue(int i) {
        if (this.attributes == null) {
            throw new IllegalStateException();
        } else if (i >= 0 && i < this.getAttributeCount()) {
            return this.attributes.get(i).getValue();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void setAttribute(String name, String value) {
        if (this.attributes == null) {
            throw new IllegalStateException();
        } else if (name == null || value == null) {
            throw new IllegalArgumentException();
        } else {
            for (Attribute attribute : attributes) {
                if (attribute.getName().equals(name))
                    attribute.setValue(value);
            }
        }
    }


    public Element getParent() {
        return parent;
    }

    public void setParent(Element parent) {
        this.parent = parent;
    }

    public Element getChild(int idx) {
        if (idx >= 0 && idx < children.size())
            return children.get(idx);
        else throw new NoSuchElementException();
    }

    public Element getNextSibling() {
        if (this.parent != null) {
            Element parent = this.getParent();
            int currentIdx = parent.getChildren().indexOf(this);
            if (++currentIdx - 1 < parent.getChildren().size())
                return parent.getChild(currentIdx);
            else
                return null;
        } else throw new NoSuchElementException();
    }
//
//    public Node getPreviousSibling() {
//        return null;
//    }
//
//    public void insertChildBefore(Node newChild, Node child) {
//
//    }
//
    public void addChild(Element newChild) {
        if (this.children == null)
            children = new ArrayList<>();
        children.add(newChild);
    }

    public void removeChild(Element child) {
        this.children.remove(child);
    }

    public void replaceChild(Element newChild, Element oldChild) {
        Collections.replaceAll(this.children, oldChild, newChild);
    }

    public boolean hasChildren() {
        return children != null;
    }

    public List<Element> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Element element = (Element) o;
        return Objects.equals(tagName, element.tagName) &&
                Objects.equals(attributes, element.attributes) &&
                Objects.equals(parent, element.parent) &&
                Objects.equals(children, element.children) &&
                Objects.equals(text, element.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagName, attributes, children, text);
    }

    @Override
    public String toString() {
        return "Element{" +
                "tagName='" + tagName + '\'' +
                ", attributes=" + attributes +
                ", children=" + children +
                ", text='" + text + '\'' +
                '}';
    }

    public static class Builder {
        private String tagName;
        private List<Attribute> attributes;
        private Element parent;
        private List<Element> children;
        private String text;

        public Builder(String tagName, List<Attribute> attributes, Element parent, List<Element> children, String text) {
            this.tagName = tagName;
            this.attributes = attributes;
            this.parent = parent;
            this.children = children;
            this.text = text;
        }

        public Builder() {
        }

        public Builder withTagName(String tagName) {
            this.tagName = tagName;
            return this;
        }

        public Builder withAttributes(List<Attribute> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder withParent(Element parent) {
            this.parent = parent;
            return this;
        }

        public Builder withChildren(List<Element> children) {
            this.children = children;
            return this;
        }

        public Builder withText(String text) {
            this.text = text;
            return this;
        }

        public Element build() {
            return new Element(this);
        }
    }
}
