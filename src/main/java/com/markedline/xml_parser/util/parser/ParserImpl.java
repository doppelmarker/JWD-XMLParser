package com.markedline.xml_parser.util.parser;

import com.markedline.xml_parser.node.Element;
import com.markedline.xml_parser.util.tokenizer.TokenType;
import com.markedline.xml_parser.util.tokenizer.Tokenizer;
import com.markedline.xml_parser.util.tokenizer.TokenizerImpl;
import com.markedline.xml_parser.util.tokenizer.XMLException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Stack;

public final class ParserImpl implements Parser {
    @Override
    public Element parse(FileInputStream input) throws IOException, XMLException {

        Tokenizer tokenizer = new TokenizerImpl(input);

        Stack<Element> elementStack = new Stack<>();

        while (tokenizer.nextToken() != TokenType.EOF) {
            Element element = tokenizer.getCurrentToken();
            switch (tokenizer.getTokenType()) {
                case START_TAG -> {
                    if (!elementStack.isEmpty()) {
                        elementStack.peek().addChild(element);
                        element.setParent(elementStack.peek());
                    }
                    elementStack.push(element);
                }
                case END_TAG -> {
                    if (elementStack.size() > 1)
                        elementStack.pop();
                }
                case TEXT -> {
                    elementStack.peek().addChild(element);
                    element.setParent(elementStack.peek());
                }
            }
        }

        if (!elementStack.isEmpty())
            return elementStack.pop();
        else
            throw new XMLException("empty xml document");
    }
}
