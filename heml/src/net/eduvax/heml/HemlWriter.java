package net.eduvax.heml;

import java.io.OutputStream;
import java.io.PrintStream;

public class HemlWriter implements ParserCallback {
    private PrintStream _out;
    private final static String INDENT="    ";    
    private int _indent;
    private int _attrIndent=0;

    public HemlWriter(OutputStream out) {
        _out=new PrintStream(out);
    }
    public HemlWriter(PrintStream out) {
        _out=out;
    }

    private void indent() {
        for (int i=0;i<_indent;i++) {
            _out.print(INDENT);
        }
    }

    public void openElement(String name) {
        indent();
        _out.print("{"+name);
        _indent++;
    }
    public void closeElement() {
        _indent--;
        indent();
        _out.println("}");
    }
    public void addText(String text) {
        _out.print(text);
    }
    public void addAttribute(String name,String value) {
        _out.print(" %"+name+"="+value);
    }
    public void endAttributes() {
        _out.println();
    }
    public void openPara() {
        indent();
    }
    public void closePara() {
        _out.println();
    }
    public void openIndent() {
        _out.println();
        _indent++;
    }
    public void closeIndent() {
        _indent--;
    }
    public void openEnum() {
        indent();
        _out.print("- ");
    }
    public void closeEnum() {
        _out.println();
    }
    public void addComment(String comment) {
        _out.println("{#"+comment+"#}");
    }
    public void addCData(String data) {
        _out.println("{#"+data+"#}");
    }
    public void openDocument() {
    }
    public void closeDocument() {
    }
    public void stateChanged(Parser.State s) {
    }
    public void openTable(int rowStyle,String rowName, Iterable<String> fieldsName) {
        indent();
        _out.print("{%"+rowName);
        for (String field : fieldsName) {
            _out.print("%"+field);
        }
        _out.println();
    }
    public void addRow(Iterable<String> fieldsValue) {
        boolean sep=false;
        for (String val : fieldsValue) {
            if (sep){
                _out.print("\t%");
            }
            _out.print(val);
            sep=true;
        }
        _out.println();
    }
    public void closeTable() {
        indent();
        _out.println("%}");
    }
}
