package com.markedline.xml_parser.util.tokenizer;

import com.markedline.xml_parser.node.Attribute;
import com.markedline.xml_parser.node.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class TokenizerImpl implements Tokenizer {
    private final Reader input;
    private final List<Attribute> attributes;
    private int tokenType = BOF;
    private String tagName;
    private String text;
    private boolean hasEndTag;

    public TokenizerImpl(InputStream input) {
        this.input = new BufferedReader(new InputStreamReader(input));
        this.attributes = new ArrayList<>();
    }

    private void resetState(boolean resetTagName) {
        if (resetTagName) {
            this.tagName = null;
        }

        this.attributes.clear();
        this.text = null;
    }

    @Override
    public int nextToken() throws IOException, XMLException {
        if (this.tokenType == BOF) {
            this.parseText();
        }

        switch (this.tokenType) {
            case START_TAG:
            case END_TAG:
                if (this.hasEndTag) {
                    this.resetState(false);
                    this.tokenType = END_TAG;
                    this.hasEndTag = false;
                } else {
                    this.resetState(true);
                    this.parseText();
                    if (this.tokenType == TEXT && this.text.equals("")) {
                        this.resetState(true);
                        this.parseTag();
                    }
                }
                break;
            case TEXT:
                this.resetState(true);
                this.parseTag();
                break;
            case EOF:
                break;
            default:
                throw new IllegalStateException();
        }

        return this.tokenType;
    }

    @Override
    public Element getCurrentToken() {
        return switch (this.tokenType) {
            case START_TAG -> new Element.Builder().
                    withTagName(this.tagName).
                    withAttributes(new ArrayList<>(this.attributes)).
                    withText(this.text).build();
            case END_TAG -> new Element.Builder().withTagName(this.tagName).build();
            case TEXT -> new Element.Builder().withText(this.text).build();
            default -> null;
        };
    }

    public int getTokenType() {
        return this.tokenType;
    }

    private int readOptionalChar(boolean skipWS) throws IOException {
        while (true) {
            int n = this.input.read();
            if (n >= 0) {
                char c = (char) n;
                if (skipWS && Character.isWhitespace(c)) {
                    continue;
                }
            }

            return n;
        }
    }

    private int readOptionalChar() throws IOException {
        return this.readOptionalChar(false);
    }

    private char readChar(boolean skipWS) throws IOException, XMLException {
        int n = this.readOptionalChar(skipWS);
        if (n < 0) {
            throw new XMLException("unexpected end of document");
        } else {
            return (char) n;
        }
    }

    private char readChar() throws IOException, XMLException {
        return this.readChar(false);
    }

    private void match(char expected, boolean skipWS) throws IOException, XMLException {
        char c = this.readChar(skipWS);
        if (c != expected) {
            throw new XMLException("unexpected character: expected [" + expected + "], got [" + c + "]");
        }
    }

    private void match(char expected) throws IOException, XMLException {
        this.match(expected, false);
    }

    private void parseTag() throws IOException, XMLException {
        boolean isStartTag = true;
        char c = this.readChar();
        if (c == '/') {
            isStartTag = false;
            c = this.readChar();
        }

        StringBuilder tagName = new StringBuilder();
        for (this.tagName = ""; Character.isLetterOrDigit(c) || c == '-'; c = this.readChar()) {
            tagName.append(c);
        }
        this.tagName = tagName.toString();

        String INVALID_TAG = "invalid tag: <" + (isStartTag ? "" : "/") + this.tagName + ">";
        if (this.tagName.length() == 0) {
            throw new XMLException(INVALID_TAG);
        } else {
            if (isStartTag) {
                this.tokenType = START_TAG;
                switch (c) {
                    case '/':
                        this.match('>', false);
                        this.hasEndTag = true;
                    case '>':
                        break;
                    default:
                        if (!Character.isWhitespace(c)) {
                            throw new XMLException(INVALID_TAG);
                        }

                        this.hasEndTag = this.parseAttrs();
                }
            } else {
                this.tokenType = END_TAG;
                switch (c) {
                    case '>':
                        break;
                    default:
                        if (!Character.isWhitespace(c)) {
                            throw new XMLException(INVALID_TAG);
                        }

                        this.match('>', true);
                }
            }

        }
    }

    private boolean parseAttrs() throws IOException, XMLException {
        String INVALID_TAG = "invalid tag: <" + this.tagName + ">";
        char c = this.readChar(true);

        while (c != '>') {
            if (c == '/') {
                this.match('>', false);
                return true;
            }

            StringBuilder attrName;
            for (attrName = new StringBuilder(); Character.isLetterOrDigit(c); c = this.readChar(false)) {
                attrName.append(c);
            }

            if (attrName.length() == 0) {
                throw new XMLException(INVALID_TAG);
            }

            String INVALID_ATTR = "invalid attribute: <" + this.tagName + ">, " + attrName;
            if (Character.isWhitespace(c)) {
                c = this.readChar(true);
            }

            if (c != '=') {
                throw new XMLException(INVALID_ATTR);
            }

            StringBuilder attrValue = new StringBuilder();
            c = this.readChar(true);
            if (c != '\'' && c != '"') {
                throw new XMLException(INVALID_ATTR);
            }

            char delimiter = c;

            for (c = this.readChar(false); c != delimiter; c = this.readChar(false)) {
                attrValue.append(c);
            }

            c = this.readChar(true);
            this.attributes.add(new Attribute(attrName.toString(), attrValue.toString()));
        }

        return false;
    }

    private void parseText() throws IOException {
        StringBuilder data = new StringBuilder();

        int n;
        for (n = this.readOptionalChar(); n != -1 && (char) n != '<'; n = this.readOptionalChar()) {
            data.append((char) n);
        }

        if (n != -1) {
            this.tokenType = 3;
            this.text = data.toString();
        } else {
            this.tokenType = 4;
        }

    }
}