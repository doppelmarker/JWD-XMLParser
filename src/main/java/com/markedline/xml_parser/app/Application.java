package com.markedline.xml_parser.app;

import com.markedline.xml_parser.node.Element;
import com.markedline.xml_parser.util.parser.Parser;
import com.markedline.xml_parser.util.parser.ParserFactory;
import com.markedline.xml_parser.util.printer.Printer;
import com.markedline.xml_parser.util.printer.PrinterImpl;

import java.io.File;
import java.io.FileInputStream;

public class Application {

    public static void main(String[] args) throws Exception {
        Parser parser = ParserFactory.createParser();

        long startTime = System.currentTimeMillis();

        ClassLoader classLoader = Application.class.getClassLoader();

        Element root = parser.parse(new FileInputStream(new File(classLoader.getResource(args[0]).getFile())));

        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("it took: " + (float) endTime / 1000 + "sec");
        Printer printer = new PrinterImpl();
        printer.print(root);
    }
}
