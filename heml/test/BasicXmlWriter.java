package test;

import net.eduvax.heml.Parser;
import net.eduvax.heml.ParserCallback;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.Stack;
import java.io.PrintStream;

public class BasicXmlWriter implements ParserCallback {
	private String _paraElem="p";
	private String _enumElem="li";
	private String _indentElem="ul";
	private String _cDataElem="pre";
	private PrintStream _writer;
    private Vector<Exception> _exceptions;
	private String _rowName;
	private int _rowStyle;
	private Iterable<String> _fieldsName;
    private Stack<String> _eStack;
	public BasicXmlWriter(OutputStream out) {
		_writer=new PrintStream(out);
        _eStack=new Stack<String>();
        _exceptions=new Vector<Exception>();
	}
    public Collection<Exception> getErrors() {
        return _exceptions;
    }
    public boolean hasError() {
        return _exceptions.size()>0;
    }
    private void printIndent() {
        for (int i=0;i<_eStack.size();i++) {
            _writer.print(' ');
        }
    }
	public void openElement(String name) {
        printIndent();
	    _writer.print("<"+name);
        _eStack.push(name); 	
	}
	public void closeElement() {
        try {
		    _writer.println("</"+_eStack.pop()+">");
        }
        catch (Exception ex) {
            _exceptions.add(ex);
        }
	}
	public void addAttribute(String name,String value) {
		_writer.print(" "+name+"=\""+value+"\"");
	}
	public void endAttributes() {
		_writer.println(">");
	}
	public void addText(String text) {
        printIndent();
        _writer.println(text);
	}
	public void openPara() {
        printIndent();
	    _writer.println("<"+_paraElem+">");
	}
	public void closePara() {
        printIndent();
        _writer.println("</"+_paraElem+">");
	}
	public void openEnum() {
            printIndent();
		    _writer.println("<"+_enumElem+">");
	}
	public void closeEnum() {
            printIndent();
		    _writer.println("</"+_enumElem+">");
	}
	public void openIndent() {
        printIndent();
        _writer.println("<"+_indentElem+">");
	}
	public void closeIndent() {
        printIndent();
        _writer.println("</"+_indentElem+">");
	}
	public void addComment(String comment) {
        printIndent();
        _writer.println("<!--"+comment+"-->");
	}
	public void addCData(String cData) {
        printIndent();
		_writer.print("<"+_cDataElem+">");
		_writer.print(cData);
		_writer.println("</"+_cDataElem+">");
	}
    public void openDocument() {
        _writer.println("<?xml version=\"1.0\"?>");
    }
    public void closeDocument() {
    }
    public void stateChanged(Parser.State s) {
    }

	public void openTable(int rowStyle,String rowName,Iterable<String> fieldsName) {
		_rowStyle=rowStyle;
		_rowName=rowName;
		_fieldsName=fieldsName;
	}
	public void closeTable() {
	}
	public void addRow(Iterable<String> fieldsValue) {
		try {
			if (_rowStyle==ParserCallback.ROW_ELEM) {
				_writer.println("<"+_rowName+">"); 
				Iterator<String> it=_fieldsName.iterator();
				int i=0;
                int qfn=0;
				String fName=null;
				for (String value : fieldsValue) {
					if (it.hasNext()) {
                        qfn++;
						fName=it.next();
					}
					else if (qfn!=1) {
						i++;
						fName="f"+i;
					}
					_writer.print("<"+fName+">");
					_writer.print(value);
					_writer.print("</"+fName+">");
				}
				_writer.println("</"+_rowName+">"); 
			}
			else {
				_writer.print("<"+_rowName); 
				StringBuffer text=new StringBuffer();
				Iterator<String> it=_fieldsName.iterator();
				int i=0;
				for (String value : fieldsValue) {
					if (it.hasNext()) {
                        _writer.print(" "+it.next()+"=\""+value+"\"");
					}
					else {
                        text.append(value);
					}
				}
                _writer.println(">");
				_writer.println(text.toString());
				_writer.println("</"+_rowName+">"); 
			}
		}
        catch (Exception ex) {
            _exceptions.add(ex);
        }
		
	}
}
