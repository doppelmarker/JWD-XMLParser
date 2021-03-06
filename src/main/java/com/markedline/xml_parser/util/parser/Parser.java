package com.markedline.xml_parser.util.parser;

import com.markedline.xml_parser.node.Element;
import com.markedline.xml_parser.util.tokenizer.XMLException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface Parser {

    Element parse(FileInputStream input) throws IOException, XMLException;
}
