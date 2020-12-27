package com.markedline.xml_parser.util.tokenizer;

import com.markedline.xml_parser.node.Element;

import java.io.IOException;

public interface Tokenizer {
    int BOF = 0;
    int START_TAG = 1;
    int END_TAG = 2;
    int TEXT = 3;
    int EOF = 4;

    int nextToken() throws IOException, XMLException;

    Element getCurrentToken();

    int getTokenType();
}
