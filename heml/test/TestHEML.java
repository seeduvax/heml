package test;

import org.junit.*;
import static org.junit.Assert.*;
import net.eduvax.heml.HemlWriter;
import net.eduvax.heml.Parser;
import net.eduvax.heml.ParserCallback;
import net.eduvax.heml.XmlWriter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;


public class TestHEML implements ParserCallback {
	private Parser _heml;
    private String _outputDir=System.getenv("TTARGETDIR");
    private void doParse(ParserCallback pc) throws Exception {
		reset();
		_heml=new Parser("test/test.heml", pc);
		_heml.run();
    }

    @Test 
    public void testParseToXML() {
        Exception ex=null;
        try {
            _out=new PrintStream(new FileOutputStream(_outputDir+"/testHEML-XmlWriter.xml"));
            XmlWriter writer=new XmlWriter(_out);
            doParse(writer);
            for (Exception wex : writer.getErrors()) {
                System.err.println("Err: "+wex);
            }
            assertFalse(writer.hasError());
            _out.close();
        }
        catch(Exception e) {
            ex=e;
            ex.printStackTrace();
        }
        assertNull(ex);
    }

    @Test 
    public void testParseToBasicXML() {
        Exception ex=null;
        try {
            _out=new PrintStream(new FileOutputStream(_outputDir+"/testHEML-BasicXmlWriter.xml"));
            BasicXmlWriter writer=new BasicXmlWriter(_out);
            doParse(writer);
            for (Exception wex : writer.getErrors()) {
                System.err.println("Err: "+wex);
            }
            assertFalse(writer.hasError());
            _out.close();
        }
        catch(Exception e) {
            ex=e;
            ex.printStackTrace();
        }
        assertNull(ex);
    }

    @Test 
    public void testParse() {
        Exception ex=null;
        try {
            _out=new PrintStream(new FileOutputStream(_outputDir+"/testHEML-debugWriter.txt"));
            doParse(this);
            _out.close();
        }
        catch(Exception e) {
            ex=e;
            ex.printStackTrace();
        }
        assertNull(ex);
    }

    @Test 
    public void testWriter() {
        Exception ex=null;
        try {
            _out=new PrintStream(new FileOutputStream(_outputDir+"/testHEML-HEMLWriter.txt"));
            doParse(new HemlWriter(_out));
            _out.close();
        }
        catch(Exception e) {
            ex=e;
            ex.printStackTrace();
        }
        assertNull(ex);
    }

    private int _indent=0;
    private PrintStream _out;

	private void reset() {
		_indent=0;
		_openElement=0;
		_closeElement=0;
		_addText=0;
		_endAttributes=0;
		_openPara=0;
		_closePara=0;
		_openEnum=0;
		_closeEnum=0;
		_openIndent=0;
		_closeIndent=0;
	}

    private void indent() {
        for (int i=0;i<_indent;i++) {
            _out.print("    ");
        }
    }

	private int _openElement=0;
    public void openElement(String name) {
		_openElement++;
        indent();
        _out.print("{openElement:"+name+":"+_openElement+":");
    }

	private int _closeElement=0;
    public void closeElement() {
		_closeElement++;
        _indent--;
        indent();
        _out.println(":closeElement:"+_closeElement+"}");
    }
    public void addAttribute(String name,String value) {
        _out.print(" "+name+"=\""+value+"\"");
    }

	private int _endAttributes=0;
    public void endAttributes() {
		_endAttributes++;
        _out.println("{endAttribute:"+_endAttributes+"}");
        _indent++;
    }

	private int _addText=0;
    public void addText(String text) {
		_addText++;
        indent();
        _out.println("{addText:"+_addText+":"+text+"}");
    }

	private int _openPara=0;
    public void openPara() {
		_openPara++;
        _indent++;
        indent();
        _out.println("{openPara:"+_openPara+":");
    }

	private int _closePara=0;
    public void closePara() {
		_closePara++;
        indent();
        _out.println(":closePara:"+_closePara+"}");
        _indent--;
    }
	private int _openEnum=0;
    public void openEnum() {
		_openEnum++;
        _indent++;
        indent();
        _out.println("{openEnum:"+_openEnum+":");
    }

	private int _closeEnum=0;
    public void closeEnum() {
		_closeEnum++;
        indent();
        _out.println(":closeEnum:"+_closeEnum+"}");
        _indent--;
    }

	private int _openIndent=0;
    public void openIndent() {
        _indent++;
		_openIndent++;
        indent();
        _out.println("{openIndent:"+_openIndent+":");
    }

	private int _closeIndent=0;
    public void closeIndent() {
		_closeIndent++;
        indent();
        _out.println(":closeIndent:"+_closeIndent+"}");
        _indent--;
    }
    
	public void addComment(String comment) {
        _out.println("{addComment:"+comment+"}");
    }
	public void addCData(String cData) {
        _out.println("{addCData:"+cData+"}");
    }
    public void openDocument() {
        _out.println(".........................");
    }
    public void closeDocument() {
        _out.println(".........................");
    }
    public void stateChanged(Parser.State s) {
        _out.print("["+s.getClass().getName()+"]");
    }
	public void openTable(int rowStyle,String rowName,Iterable<String> fieldsName) {
        _out.print("{openTable:"+rowStyle+":"+rowName);
		for (String name : fieldsName) {
			_out.print(":"+name);
		}
		_out.println("}");
	}
	public void addRow(Iterable<String> fieldsValue) {
		_out.print("[:");
		for (String value : fieldsValue) {
			_out.print(value+":");
		}
		_out.println("]");
	}
	public void closeTable() {
		_out.println(":closeTable}");
	}
}        
