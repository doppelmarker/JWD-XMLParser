package com.markedline.xml_parser.util.printer;

import com.markedline.xml_parser.node.Element;

public final class PrinterImpl implements Printer {

    private static final String REG_EXP = "(?s).*[a-zA-Z0-9].*";
    private static final String DELIMITER = "-";

    // TODO: 28.12.2020 : delete redundant delimiter after last element
    @Override
    public void print(Element element) {
        if (element.hasChildren()) {
            int i = 0;
            for (Element elem : element.getChildren()) {
                if (elem.getText() == null || elem.getText().matches(REG_EXP)) {
                    System.out.print(++i + ". ");
                    printChildTree(elem);
                    System.out.println();
                }
            }
        }
    }

    private void printChildTree(Element child) {
        if (child.hasChildren()) {
            for (Element elem : child.getChildren()) {
                printChildTree(elem);
            }
        } else if (child.getText() != null && child.getText().matches(REG_EXP)) {
            System.out.print(child.getText());
            System.out.print(DELIMITER);
        }
    }
}
