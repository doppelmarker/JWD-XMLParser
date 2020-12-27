package com.markedline.xml_parser.util.parser;

public class ParserFactory {

    public static Parser createParser() {
        return new ParserImpl();
    }
}
