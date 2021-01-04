package com.markedline.xml_parser.app;

import com.markedline.xml_parser.node.Element;
import com.markedline.xml_parser.util.parser.Parser;
import com.markedline.xml_parser.util.parser.ParserFactory;
import com.markedline.xml_parser.util.printer.Printer;
import com.markedline.xml_parser.util.printer.PrinterImpl;

import java.io.FileInputStream;

public class Application {

    public static void main(String[] args) throws Exception {
        Parser parser = ParserFactory.createParser();

        Element root = parser.parse(new FileInputStream("document.xml"));

        Printer printer = new PrinterImpl();
        printer.print(root);
    }
}
