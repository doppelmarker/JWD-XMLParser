package com.markedline.xml_parser.util.tokenizer;

import com.markedline.xml_parser.node.Attribute;
import com.markedline.xml_parser.node.Element;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class TokenizerImpl implements Tokenizer {
    private final BufferedReader reader;
    private final List<Attribute> attributes;
    private TokenType tokenType = TokenType.BOF;
    private String tagName;
    private String text;
    private boolean hasEndTag;

    public TokenizerImpl(FileInputStream reader) {
        this.reader = new BufferedReader(new InputStreamReader(reader));
        this.attributes = new ArrayList<>();
    }

    @Override
    public TokenType nextToken() throws IOException, XMLException {
        // skip leading whitespaces and newline characters
        if (this.tokenType == TokenType.BOF) {
            this.parseText();
        }

        switch (this.tokenType) {
            case START_TAG, END_TAG:
                // if symbol '/' is present in tag
                if (this.hasEndTag) {
                    this.resetState(false);
                    this.tokenType = TokenType.END_TAG;
                    this.hasEndTag = false;
                } else {
                    this.resetState(true);
                    this.parseText();
                    // if tags are going in a row, this will let us skip adding a child with empty text:
                    // <people><person /></people>
                    if (this.tokenType == TokenType.TEXT && this.text.equals("")) {
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

    @Override
    public TokenType getTokenType() {
        return this.tokenType;
    }

    private void resetState(boolean resetTagName) {
        if (resetTagName) {
            this.tagName = null;
        }
        this.attributes.clear();
        this.text = null;
    }

    private void parseText() throws IOException {
        StringBuilder data = new StringBuilder();

        int n;
        for (n = this.readIntegerChar(false); n != -1 && (char) n != '<'; n = this.readIntegerChar(false)) {
            data.append((char) n);
        }

        if (n != -1) {
            this.tokenType = TokenType.TEXT;
            this.text = data.toString();
        } else {
            this.tokenType = TokenType.EOF;
        }
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
                this.tokenType = TokenType.START_TAG;
                switch (c) {
                    // end parsing tag after reaching '>' symbol
                    case '>':
                        break;
                    // in a single opening tag the next symbol after '/' must be '>': <person />
                    case '/':
                        this.matchNextSymbol('>', false);
                        this.hasEndTag = true;
                    default:
                        // presence of any character except whitespace after tag name is incorrect: <person@id="1">
                        if (!Character.isWhitespace(c)) {
                            throw new XMLException(INVALID_TAG);
                        }
                        // parse attributes if tag is correct: <person id="1"> or <person    id="1">
                        this.hasEndTag = this.parseAttrs();
                }
            } else {
                this.tokenType = TokenType.END_TAG;
                switch (c) {
                    // end parsing tag after reaching '>' symbol
                    case '>':
                        break;
                    default:
                        // presence of any character except whitespace after tag name is incorrect: </person@>
                        if (!Character.isWhitespace(c)) {
                            throw new XMLException(INVALID_TAG);
                        }
                        // this checks if '>' is going after possible multiple whitespaces in a closing tag: </person       >
                        this.matchNextSymbol('>', true);
                }
            }

        }
    }

    private boolean parseAttrs() throws IOException, XMLException {
        // skip whitespaces and read the first symbol of the name of the first attribute: <person   id="1"> -> 'i'
        char c = this.readChar(true);

        while (c != '>') {
            if (c == '/') {
                // in a single opening tag the next symbol after '/' must be '>': <person id="1" />
                this.matchNextSymbol('>', false);
                return true;
            }

            StringBuilder attrName;

            // read attribute name
            for (attrName = new StringBuilder(); Character.isLetterOrDigit(c); c = this.readChar()) {
                attrName.append(c);
            }

            if (attrName.length() == 0) {
                throw new XMLException("empty attribute name in tag: <" + this.tagName + ">");
            }

            // there may be only whitespaces after tag name, we read and skip them:
            // <person id   ="1"> - ok, <person id@="1"> - not ok
            if (Character.isWhitespace(c)) {
                c = this.readChar(true);
            }

            if (c != '=') {
                throw new XMLException("invalid attribute");
            }

            // read the delimiter symbol: ' or "
            c = this.readChar(true);
            if (c != '\'' && c != '"') {
                throw new XMLException("invalid attribute");
            }

            char delimiter = c;

            StringBuilder attrValue = new StringBuilder();

            // read attribute value till the second delimiter
            for (c = this.readChar(); c != delimiter; c = this.readChar()) {
                attrValue.append(c);
            }

            // skip whitespaces and read the first symbol of the name of the next attribute or '/' if it is a single tag
            // or just '>' symbol and exit the loop
            c = this.readChar(true);
            this.attributes.add(new Attribute(attrName.toString(), attrValue.toString()));
        }

        return false;
    }

    private int readIntegerChar(boolean skipWS) throws IOException {
        while (true) {
            int n = this.reader.read();
            if (n >= 0) {
                char c = (char) n;
                if (skipWS && Character.isWhitespace(c)) {
                    continue;
                }
            }

            return n;
        }
    }

    private char readChar(boolean skipWS) throws IOException, XMLException {
        int n = this.readIntegerChar(skipWS);
        if (n < 0) {
            throw new XMLException("unexpected end of document");
        } else {
            return (char) n;
        }
    }

    private char readChar() throws IOException, XMLException {
        return this.readChar(false);
    }

    private void matchNextSymbol(char expected, boolean skipWS) throws IOException, XMLException {
        char c = this.readChar(skipWS);
        if (c != expected) {
            throw new XMLException("unexpected character: expected [" + expected + "], got [" + c + "]");
        }
    }
}