package com.markedline.xml_parser.util.tokenizer;

import com.markedline.xml_parser.node.Element;

import java.io.IOException;

public interface Tokenizer {

    TokenType nextToken() throws IOException, XMLException;

    Element getCurrentToken();

    TokenType getTokenType();
}
