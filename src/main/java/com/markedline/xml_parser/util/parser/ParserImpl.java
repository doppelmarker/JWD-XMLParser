package com.markedline.xml_parser.util.parser;

import com.markedline.xml_parser.node.Element;
import com.markedline.xml_parser.util.tokenizer.Tokenizer;
import com.markedline.xml_parser.util.tokenizer.TokenizerImpl;
import com.markedline.xml_parser.util.tokenizer.XMLException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import static com.markedline.xml_parser.util.tokenizer.Tokenizer.*;

public class ParserImpl implements Parser {
    @Override
    public Element parse(InputStream input) throws IOException, XMLException {

        Tokenizer tokenizer = new TokenizerImpl(input);

        Stack<Element> elementStack = new Stack<>();

        while (tokenizer.nextToken() != Tokenizer.EOF) {
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

        return elementStack.pop();
    }
}
