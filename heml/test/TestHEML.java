package test;

import org.junit.*;
import static org.junit.Assert.*;
import net.eduvax.heml.HemlWriter;
import net.eduvax.heml.Parser;
import net.eduvax.heml.ParserCallback;
import net.eduvax.heml.XmlWriter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


public class TestHEML implements ParserCallback {
    private String _outputDir=System.getenv("TTARGETDIR");
    private Parser getParser(ParserCallback pc) {
        try {
            Parser p=new Parser("test/test.heml",pc);
            p.addSearchPath("test");
            return p;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    private Parser getParser(String outFile) {
        try {
            Parser p=new Parser("test/test.heml",
                new PrintStream(new FileOutputStream(_outputDir+"/"+outFile)));
            p.addSearchPath("test");
            return p;
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
System.out.println("ERROR: !!!!!! "+streamName+":"+line+":"+col+": "+msg);
            _count++;
        }
        @Override public int getErrCount() {
            return _count;
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
        assertEquals(3,eh._count);
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
    public void testParseToXML() throws IOException {
        try {
            String expected = "test/resources/expectedTestHEML-XmlWriter.xml";
            String expectedV8 = "test/resources/expectedTestHEML-XmlWriter_8.xml";
            String outPath = _outputDir+"/testHEML-XmlWriter.xml";
            
            _out=new PrintStream(new FileOutputStream(outPath));
            XmlWriter writer=new XmlWriter(_out);
            Parser p=getParser(writer);
            assertNull(checkParse(p));
            for (Exception wex : writer.getErrors()) {
                System.err.println("Err: "+wex);
            }
            assertFalse(writer.hasError());
            _out.close();
            
            if (getJavaVersion() > 9) {
                assertTrue(areFilesEquals(expected, outPath));            	
            }
            else {
                assertTrue(areFilesEquals(expectedV8, outPath));
            }
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
    public void testWriter() throws IOException {
        try {
            String expected = "test/resources/expectedTestHEML-HEMLWriter.txt";
            String outPath = _outputDir+"/testHEML-HEMLWriter.txt";
            
            _out=new PrintStream(new FileOutputStream(outPath));
            Parser p=getParser(new HemlWriter(_out));
            assertNull(checkParse(p));
            _out.close();
            
            assertTrue(areFilesEquals(expected, outPath));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            assertNotNull(null);
        }
    }
    
    private boolean areFilesEquals(String file1, String file2) throws IOException {
    	byte[] data1 = Files.readAllBytes(Paths.get(file1));
    	byte[] data2 = Files.readAllBytes(Paths.get(file2));
    	return data1 != null && data2 != null && Arrays.equals(data1, data2);
    }

    private int getJavaVersion() {
    	String version = System.getProperty("java.version");
    	if (version.startsWith("1.")) {
    		version = version.substring(2);
    	}
    	int dotPos = version.indexOf('.');
    	int dashPos = version.indexOf('-');
    	return Integer.parseInt(version.substring(0, dotPos > -1 ? dotPos : (dashPos > -1 ? dashPos : 1)));   			
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
