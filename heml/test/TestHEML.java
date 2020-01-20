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
    private String _outputDir=System.getenv("TTARGETDIR");
    private Parser getParser(ParserCallback pc) {
        try {
            return new Parser("test/test.heml",pc);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    private Parser getParser(String outFile) {
        try {
            return new Parser("test/test.heml",
                new PrintStream(new FileOutputStream(_outputDir+"/"+outFile)));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    private Exception checkParse(Parser p) {
        Exception ex=null;
        try {
            reset();
            p.run();
        }
        catch (Exception e) {
            ex=e;
        }
        return ex;
    }

    private class EH implements Parser.ErrHandler {
        public int _count=0;
        public void handle(String streamName, int line, int col, String msg) {
            _count++;
        }
    }
    @Test
    public void testXsl() {
System.out.println("-------------------------------");
        Parser p=getParser("testHEML-Xsl.txt");
        EH eh=new EH();
        p.setXslPath("test/test.xsl");
        p.setErrHandler(eh);
        assertNull(checkParse(p));
        assertEquals(2,eh._count);
System.out.println("-------------------------------");
        p=getParser("testHEML-Xsl.txt");
        eh=new EH();
        p.setXslPath("test/test.xsl");
        p.setErrHandler(eh);
        p.addSearchPath("test/plop");
        assertNull(checkParse(p));
System.out.println("-------------------------------");
        assertEquals(1,eh._count);
    }

    @Test 
    public void testParseToXML() {
        try {
            _out=new PrintStream(new FileOutputStream(_outputDir+"/testHEML-XmlWriter.xml"));
            XmlWriter writer=new XmlWriter(_out);
            Parser p=getParser(writer);
            assertNull(checkParse(p));
            for (Exception wex : writer.getErrors()) {
                System.err.println("Err: "+wex);
            }
            assertFalse(writer.hasError());
            _out.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            assertNotNull(null);
        }
    }

    @Test 
    public void testParseToBasicXML() {
        try {
            _out=new PrintStream(new FileOutputStream(_outputDir+"/testHEML-BasicXmlWriter.xml"));
            BasicXmlWriter writer=new BasicXmlWriter(_out);
            Parser p=getParser(writer);
            assertNull(checkParse(p));
            for (Exception wex : writer.getErrors()) {
                System.err.println("Err: "+wex);
            }
            assertFalse(writer.hasError());
            _out.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            assertNotNull(null);
        }
    }

    @Test 
    public void testParse() {
        try {
            _out=new PrintStream(new FileOutputStream(_outputDir+"/testHEML-debugWriter.txt"));
            Parser p=getParser(this);
            assertNull(checkParse(p));
            _out.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            assertNotNull(null);
        }
    }

    @Test 
    public void testWriter() {
        try {
            _out=new PrintStream(new FileOutputStream(_outputDir+"/testHEML-HEMLWriter.txt"));
            Parser p=getParser(new HemlWriter(_out));
            assertNull(checkParse(p));
            _out.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            assertNotNull(null);
        }
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
