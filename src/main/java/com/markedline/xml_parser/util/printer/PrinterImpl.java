package com.markedline.xml_parser.util.printer;

import com.markedline.xml_parser.node.Element;

public class PrinterImpl implements Printer {
    // TODO: 27.12.2020
    @Override
    public void print(Element element, int i) {
        int level = i;
        if (element.hasChildren()) {
            for (Element elem : element.getChildren()) {
                print(elem, ++level);
            }
        } else if (element.getText().matches("[a-zA-Z0-9]+")) {
            System.out.print(element.getText());
            if (level == i)
                System.out.print("-");
            else
                System.out.println();
        }
    }
}
