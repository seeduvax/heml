package net.eduvax.heml;

import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Hashtable;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class Parser implements Runnable {
    private int _line=1;
    private int _col=1;
    private int _tabSize=4;
	private String _streamName="";
	private InputStream _in;
    private InputStreamReader _reader=null;
	private ParserCallback _handler;
	private char[] _separators={'{','}','%','#','!','\\','?'};
	private final static int S_OPEN=0;
	private final static int S_CLOSE=1;
	private final static int S_SEP=2;
	private final static int S_COMMENT=3;
	private final static int S_CDATA=4;
	private final static int S_ESC=5;
	private final static int S_META=6;
	private boolean _includedParser=false;
    private boolean _meta=false;
    private String _metaName;
    private Hashtable<String,String> _metaArgs;
    private Hashtable<String,String> _parameters;
    private String _xslPath=null;
    private ByteArrayOutputStream _bufOutput=null;
    private OutputStream _out;
    private boolean _docOpen=false;
    private ErrHandler _errHandler;
    private String _searchPath=null;

    private void startMeta() {
        _meta=true;
        _metaName="";
        _metaArgs=new Hashtable<String,String>();
    }

    private void openDoc() {
        if (!_docOpen) {
            _docOpen=true;
            if (_handler==null) {
                if (_xslPath!=null && _bufOutput!=null) {
                    _handler=new XmlWriter(_bufOutput);
                }
				else {
					_handler=new XmlWriter(_out);
				}
            }
            _handler.openDocument();
        }
    }

    public void endMetaAttributes() {
        _meta=false;
        MetaCommand metaCmd=null;
        if ("include".equals(_metaName)) {
            metaCmd=new MetaInclude();
        }
        else if ("set".equals(_metaName)) {
            metaCmd=new MetaSet();
        }
		else if ("table".equals(_metaName)) {
			metaCmd=new MetaTable();
		}
        if (metaCmd!=null) {
            try {
                for (String param : _metaArgs.keySet()) {
                    metaCmd.setParameter(param,_metaArgs.get(param));
                }
                metaCmd.run();
            }
            catch (Exception ex) {
                printErr("Can't set and run command: "+_metaName+", "+ex.getMessage());
            }
        }
        else {
            printErr("Unsupported meta command: "+_metaName);
        }
    }

	public State getState() {
		return _state;
	}
    private void setState(State state) {
        _state=state;
		if (_handler!=null) {
	        _handler.stateChanged(state);
		}
    }
	public Parser(String input,OutputStream out) throws HemlException {
		_streamName=input;
        _parameters=new Hashtable<String,String>();
		if ("-".equals(_streamName)) {
			_in=System.in;
		}
		else {
			try {
			File f=new File(_streamName);
			if (f.exists()) {
				_in=new FileInputStream(f);
			}
			else {
				URL url=new URL(_streamName);
				_in=url.openStream();
			}
			}
			catch (Exception ex) {
				throw new HemlException("Can't setup parser for stream "+input+".",ex);
			}
		}
        _out=out;
        if (_out==null) {
            _out=System.out;
        }
        setDefErrHandler();
        setState(new Indent(null,0));
    }
	public Parser(InputStream in,OutputStream out) {
        _in=in;
        _out=out;
        _parameters=new Hashtable<String,String>();
        if (_in==null) {
            _in=System.in;
        }
        if (_out==null) {
            _out=System.out;
        }
		_streamName=""+_in;
        setDefErrHandler();
        setState(new Indent(null,0));
    }
	public Parser(InputStream in,ParserCallback handler) {
		_in=in;
        _parameters=new Hashtable<String,String>();
        if (_in==null) {
            _in=System.in;
        }
		_streamName=""+_in;
		_handler=handler;
        setDefErrHandler();
        setState(new Indent(null,0));
	}
	public Parser(String input,ParserCallback handler) throws HemlException {
		_streamName=input;
        _parameters=new Hashtable<String,String>();
		if ("-".equals(_streamName)) {
			_in=System.in;
		}
		else {
			try {
			File f=new File(_streamName);
			if (f.exists()) {
				_in=new FileInputStream(f);
			}
			else {
				URL url=new URL(_streamName);
				_in=url.openStream();
			}
			}
			catch (Exception ex) {
				throw new HemlException("Can't setup parser for stream "+input+".",ex);
			}
		}
		_handler=handler;
        setDefErrHandler();
        setState(new Indent(null,0));
	}
    private void setDefErrHandler() {
        _errHandler=new ErrHandler() {
            public void handle(String streamName, int line, int col,String msg) {
                System.err.println("Err:"+streamName+":"+line+":"+col+": "+msg);
            }
        };
    }
    public void setErrHandler(ErrHandler handler) {
        _errHandler=handler;
    }

	public void run() {
        try {
    		int ch=_in.read();
	    	while (ch>=0) {
                char c=(char)ch;
		    	_state.handle(c);
                _col++;
                if (c=='\n') {
                    _line++;
                    _col=1;
                }
                ch=_reader==null?_in.read():_reader.read();
		    }
			if (_reader!=null) {
				_reader.close();
			}
			else {
				_in.close();
			}
        }
        catch (IOException ex) {
            printErr(""+ex);        
        }
        if (_state.getBackState()!=null) {
            printErr("Unexpected end of file "+_state);
        }
        if (_includedParser) {
            _state.close();
        }
        else {
            _handler.closeDocument();
        }
        if (_xslPath!=null) {
            try {
				InputStream xslInput=
                        new ByteArrayInputStream(_bufOutput.toByteArray());
				TransformerFactory tf=TransformerFactory.newInstance();
                if (_searchPath!=null) {
                    URIResolver defResolver=tf.getURIResolver();
                    tf.setURIResolver(new URIResolver() {
                        public Source resolve(String href, String base) throws TransformerException {
                            try {
                                InputStream is=new FileInputStream(_searchPath+"/"+href);
                                return new StreamSource(is);
                            }
                            catch (IOException ex) {
                                return defResolver.resolve(href,base);
                            }
                        }
                    });
                }
				Transformer style=tf.newTransformer(new StreamSource(_xslPath));
				// TODO handle xsl parameters. (style.setParamter(key,value));
                for (String param : _parameters.keySet()) {
                    style.setParameter(param,_parameters.get(param));
                }
				style.transform(new StreamSource(xslInput), 
                                new StreamResult(_out));
            }
            catch (Exception ex) {
                printErr("XSL:"+_xslPath+" error: "+ex);
            }
        }    
	}
    public void setXslParam(String name,String value) {
        _parameters.put(name,value);
    }
    public void setSearchPath(String path) {
        _searchPath=path;
    }
    public void setXslPath(String path) {
        if (_xslPath==null&&_handler==null) {
            _xslPath=path;
            _bufOutput=new ByteArrayOutputStream();
        }
        else {
            printErr("XSL stylesheet or handler already set, ignoring new XSL settings.");
        }
    }            

    private void printErr(String msg) {
        _errHandler.handle(_streamName,_line,_col,msg);
    }

	private String popAcc() {
		String res=_acc.toString();
		_acc=new StringBuilder();
		return res;
	}

	private int _attrCount=0;
	public void openElement() {
		_attrCount=0;
        if (_meta) {
            _metaName=popAcc();
        }
        else {
            openDoc();
		    _handler.openElement(popAcc());
        }    
	}
	private String _attrName=null;
	public void addAttribute() {
		String aName=_attrName;
		if (aName==null) {
			_attrCount++;
			aName="a"+_attrCount;
		}
        else {
            aName=aName.trim();
        }
        if (_meta) {
            _metaArgs.put(aName,popAcc().trim());
        }
        else {
		    _handler.addAttribute(aName,popAcc().trim());
        }
		_attrName=null;
	}
	public void addText() {
		_handler.addText(popAcc());
	}
	
	/** State able to call back specified state */
	public class State {
		private State _retState;
		public void handle(char ch) {
		}
        public void close() {
        }
		public State(State retState) {
			_retState=retState;
		}
		public void goBackState() {
            if (_retState==null) {
                printErr("Too much closure !");
                setState(new Indent(null,0));
            }
            else {
			    setState(_retState);
            }
		}
		public State getBackState() {
			return _retState;
		}
	}
	/** current state */
	private State _state;
	/** accumulator */
	private StringBuilder _acc=new StringBuilder();

	/**
	 * Inside attribute name or value.
	 * Or value if anonymous attribute.
	 */
	private class Attr extends State {
		public Attr(State s) {
			super(s);
		}
		public void handle(char ch) {
			if (ch=='=') {
					_attrName=popAcc();
					setState(new AttrV(getBackState()));
			}					
			else if (ch==_separators[S_SEP]) {
					addAttribute();
					setState(new TorA1(getBackState()));
			}
			else if (ch=='\r') {
			}
			else if (ch=='\n') {
					addAttribute();
					setState(new TorA2(getBackState()));
			}
			else if (ch==_separators[S_ESC]) {
					setState(new EscChar(this));
			}
			else if (ch==_separators[S_CLOSE]) {
				addAttribute();
                if (_meta) {
                    endMetaAttributes();
                }
                else {
                    _handler.endAttributes();
                    _handler.closeElement();
                }
                goBackState();
            }
			else {
					_acc.append(ch);
			}
		}
	}
	/**
	 * Inside attribute value.
	 */
	private class AttrV extends State {
		public AttrV(State s) {
			super(s);
		}
		public void handle(char ch) {
			if (ch==_separators[S_SEP]) {
					addAttribute();
					setState(new TorA1(getBackState()));
			}
			else if (ch=='\r') {
			}
			else if (ch=='\n') {
					addAttribute();
					setState(new TorA2(getBackState()));
			}
			else if (ch==_separators[S_ESC]) {
					setState(new EscChar(this));
			}
			else if (ch==_separators[S_CLOSE]) {
				addAttribute();
                if (_meta) {
                    endMetaAttributes();
                }
                else {
                    _handler.endAttributes();
                    _handler.closeElement();
                }
                goBackState();
            }
			else {
					_acc.append(ch);
			}
		}
	}
	/** CData.
	 * Waiting for end CData, ignoring anything else.
	 */
	private class CData extends State {
		public CData(State s) {
			super(s);
		}
		public void handle(char ch) {
			if (ch==_separators[S_CDATA]) {
					setState(new ECData(getBackState()));
			}
			else {
					_acc.append(ch);
			}
		}
	}
	/** End CData.
	 * '}' confirms comment end and go back to before comment state, other leads
	 * back to Comment.
	 */
	private class ECData extends State {
		public ECData(State s) {
			super(s);
		}
		public void handle(char ch) {
			if (ch==_separators[S_CLOSE]) {
					_handler.addCData(popAcc());
					goBackState();
			}
			else {
					_acc.append(_separators[S_CDATA]);
					_acc.append(ch);
					setState(new CData(getBackState()));
			}
		}
	}

	/** Comment.
	 * Waiting for end comment, ignoring anything else.
	 */
	private class Comment extends State {
		public Comment(State s) {
			super(s);
		}
		public void handle(char ch) {
			if (ch==_separators[S_COMMENT]) {
					setState(new EComment(getBackState()));
			}
			else {
					_acc.append(ch);
			}
		}
	}
	/** End Comment.
	 * '}' confirms comment end and go back to before comment state, other leads
	 * back to Comment.
	 */
	private class EComment extends State {
		public EComment(State s) {
			super(s);
		}
		public void handle(char ch) {
			if (ch==_separators[S_CLOSE]) {
					_handler.addComment(popAcc());
					goBackState();
			}
			else {
					_acc.append(_separators[S_COMMENT]);
					_acc.append(ch);
					setState(new Comment(getBackState()));
			}
		}
	}
	/**
	 * Inside element name.
	 */ 
	private class ElemName extends State {
		public ElemName(State s) {
			super(s);
		}
		public void handle(char ch) { 
			if (ch==_separators[S_SEP]) {
						openElement();
						setState(new TorA1(getBackState()));
			}
			else if (ch=='\r') {
			}
			else if (ch=='\n') {
						openElement();
						setState(new TorA2(getBackState()));
			}						
			else if (ch==' ') {
						openElement();
						setState(new TorA3(getBackState()));
			}						
			else if (ch==_separators[S_CLOSE]) {
                openElement();
                if (_meta) {
                    endMetaAttributes();
                }
                else {
	                _handler.endAttributes();
	                _handler.closeElement();
                }
	            goBackState();
			}
			else {
						_acc.append(ch);
			}
		}
	}


	/** Escape char 
	 * anti slash is the escape char.
	 */
	private class EscChar extends State {
		public EscChar(State s) {
			super(s);
		}
		public void handle(char ch) {
            switch (ch) {
                case 't':
                    _acc.append('\t');
                    break;
                case 'r':
                    _acc.append('\r');
                    break;
                case 'n':
                    _acc.append('\n');
                    break;
                default:
                    _acc.append(ch);
                    break;
            }
            goBackState();
		}
	}
	
	/**
	 * count indent chars.
	 */
	private class Indent extends State {
		private int _indent=0;
        private Stack<Integer> _indentStack;
            
		public Indent(State back,int startIndent) {
			super(back);
            _indentStack=new Stack<Integer>();
            _indent=startIndent;
            _indentStack.push(_indent);
		}
        public void close() {
            for (int i=1;i<_indentStack.size();i++) {
                _handler.closeIndent();
            }
        }

        private void checkIndentClosure() {
            int prevIndent=0;
            if (_indentStack.size()>0) {
                prevIndent=_indentStack.peek();
            }
            while (prevIndent>_indent && _indentStack.size()>1) {
                _handler.closeIndent();
                _indentStack.pop();
                prevIndent=_indentStack.peek();
            }
            if (_indent>prevIndent) {
                openDoc();
                _handler.openIndent();
                _indentStack.push(_indent);
            }
            _indent=0;
        }
		public void handle(char ch) {
			if (ch=='\t') {
					_indent+=_tabSize;;
			}
			else if (ch==' ') {
					_indent++;
			}
			else if (ch==_separators[S_OPEN]) {
					setState(new SElem(this));
			}
			else if (ch=='-') {
                    checkIndentClosure();
					setState(new IndentText(this,true));
			}
			else if (ch=='\r') {
			}
			else if (ch=='\n') {
					_indent=0;
			}
			else if (ch==_separators[S_CLOSE]) {
                close();
                _handler.closeElement();
                goBackState();
			}
			else {
                checkIndentClosure();
				_acc.append(ch);
				setState(new IndentText(this,false));
			}
		}
	}
	/**
	 * Manage indentation
	 */
	private class IndentText extends State {
        private int _openIndent=0;
		private Indent _indent=null;
        private boolean _closed=false;
		private boolean _indentAdded=false;
		private boolean _bullet=false;
        public int getOpenIndent() {
            return _openIndent;
        }
		public IndentText(Indent back,boolean bullet) {
			super(back);
            _indent=back;
			_bullet=bullet;
            openDoc();
			if (_bullet) {
				_handler.openEnum();
			}
			else {
				_handler.openPara();
			}
		}
        public void close() {
            if (!_closed) {
				if (_bullet) {
	  				_handler.closeEnum();
				}
				else {
					_handler.closePara();
				}
                _closed=true;
            }
        }
		public void handle(char ch) {
			if (ch=='\r') {
			}
			else if (ch=='\n') {
					addText();
                    close();
					goBackState();
			}
			else if (ch==_separators[S_OPEN]) {
                    addText();
					setState(new SElem(this));
			}
			else if (ch==_separators[S_CLOSE]) {
                    addText();
                    close();
                    if (_meta) {
                        endMetaAttributes();
                    }
                    else {
					    _indent.close();
                        _handler.closeElement();
                        _indent.goBackState();
                    }
            }
			else if (ch==_separators[S_ESC]) {
					setState(new EscChar(this));
			}
			else {
					_acc.append(ch);
			}
		}
	}
	/**
	 * Inline element text
	 */
	private class InlineText extends State {
		public InlineText(State s) {
			super(s);
		}
        public void close() {
            goBackState();
        }
		public void handle(char ch) {
			if (ch==_separators[S_CLOSE]) {
                    addText();
					_handler.closeElement();
                    goBackState();
			}
			else if (ch==_separators[S_ESC]) {
					setState(new EscChar(this));
			}
			else if (ch==_separators[S_OPEN]) {
                    addText();
					setState(new SElem(this));
			}
			else {
					_acc.append(ch);
			}
		}
	}
	/** 
	 * Opening brace. 
	 * May be start of comment, table or standard element.
	 */ 
	private class SElem extends State {
		public SElem(State s) {
			super(s);
		}
		public void handle(char ch) {
			if (ch==_separators[S_META]) {
                    startMeta();
					setState(new ElemName(getBackState()));
			}
			else if (ch==_separators[S_CDATA]) {
					setState(new CData(getBackState()));
			}
			else if (ch==_separators[S_COMMENT]) {
					setState(new Comment(getBackState()));
			}
			else {
					_acc.append(ch);
					setState(new ElemName(getBackState()));
			}
		}
	}

    public interface MetaCommand extends Runnable {
        void setParameter(String id,String value);
    }
    /**
     * Set meta command
     */   
	public class MetaSet implements MetaCommand {
        public void run() {
        }
        public void setParameter(String id, String value) {
            if ("encoding".equals(id)) {
				if (_reader==null) {
					try {
						_reader=new InputStreamReader(_in,value);
					}
					catch (UnsupportedEncodingException ex) {
						printErr(""+ex);
					}
				}
				else {
					printErr("Encoding already set, ignoring new encoding settings.");
				}
            }
            else if ("tab".equals(id)) {
                _tabSize=Integer.parseInt(value);
                // TODO check if value is a valid integer
            }
            else if ("xsl".equals(id)) {
                setXslPath(value);
            }
            else {
                printErr("Unhandled parameter: "+id);
            }
        }
    }
	/**
	 * Include meta command 
	 */
	public class MetaInclude implements MetaCommand {
        private String _src;
        public void setParameter(String id,String value) {
            if ("src".equals(id)) {
                _src=value;
            }
        }
		public void run() {
            try {
                Parser incParser=new Parser(_src,_handler);
                incParser._docOpen=true;
                incParser._includedParser=true;
                incParser.run();
            }
            catch (Exception ex) {
                printErr("Can't include "+_src+": "+ex.getMessage());
            }
		}
	}
	/**
	 * Table meta command
	 */
	public class MetaTable extends State implements MetaCommand {
		private boolean _token=false;
		private String _recordName="tr";
		private Vector<String> _fieldNames;
		private String _src=null;
		private String _recordSep="\n";
		private String _fieldSep="%";
		private String _commentChar=null;
		private String _encoding=null;
		private boolean _trim=true;
		private State _backState=null;
		private int _style=ParserCallback.ROW_ELEM;
		public void setParameter(String id,String value) {
			if ("fields".equals(id)) {
                _fieldNames=new Vector<String>();
				for (String f: value.split(",")) {
					_fieldNames.add(f.trim());
				}
			} 
			else if ("record".equals(id)) {
				_recordName=value;	
			}
			else if ("fieldSep".equals(id)) {
				_fieldSep=value;
                if (_fieldSep.length()>2 &&
                        _fieldSep.charAt(0)==_fieldSep.charAt(_fieldSep.length()-1)) {
                    _fieldSep=_fieldSep.substring(1,_fieldSep.length()-1);
                }
			}
			else if ("recordSep".equals(id)) {
				_recordSep=value;
                if (_recordSep.length()>2 &&
                        _recordSep.charAt(0)==_recordSep.charAt(_recordSep.length()-1)) {
                    _recordSep=_recordSep.substring(1,_recordSep.length()-1);
                }
			}
			else if ("encoding".equals(id)) {
				_encoding=value;
			}
			else if ("token".equals(id)) {
				_token="true".equals(value);
			}
			else if ("trim".equals(id)) {
				_trim="true".equals(value);
			}
			else if ("src".equals(id)) {
				_src=value;
			}
            else if ("style".equals(id)) {
                _style="attr".equals(value)?ParserCallback.ROW_ATTR:ParserCallback.ROW_ELEM;
            }
			else {
				printErr("Unexpected table meta command argument: "+id);
			}
		}

		public MetaTable() {
			super(null);
			_fieldNames=new Vector<String>();
            _fieldNames.add("td");
		}

		public void run() {
            openDoc();
			_handler.openTable(_style,_recordName,_fieldNames);
			if (_src!=null) {
				try {
					String encoding=(_encoding==null&&_reader!=null)?_reader.getEncoding():_encoding;
					if (encoding==null) {
						encoding="utf-8";
					}
					InputStreamReader reader=new InputStreamReader(
									new FileInputStream(_src),encoding);
					int ch=reader.read();
					while(ch>=0) {
						handle((char)ch);
					    ch=reader.read();
					}
					reader.close();
                    handleRecord(popAcc());
				}
				catch (Exception ex) {
					printErr("Can't parse included table "+_src+": "+ex);
ex.printStackTrace();					
				}
			}
			else {
				_backState=_state;
				setState(this);
			}
		}

		private void handleRecord(String record) {
            if (_commentChar!=null 
                    && _commentChar.indexOf(record.trim().charAt(0))>=0
                    || record.trim().length()==0) {
                return;
            }
			Vector<String> cellValues=new Vector<String>();
			if (_token) {
				StringTokenizer st=new StringTokenizer(record,_fieldSep);
				while(st.hasMoreTokens()) {
					cellValues.add(_trim?st.nextToken().trim():st.nextToken());
				}
			}
			else {
				for (String field: record.split(_fieldSep)) {
					cellValues.add(_trim?field.trim():field);
				}
			}
			_handler.addRow(cellValues);
		}

		public void handle(char ch) {
			if (_recordSep.indexOf(ch)>=0) {
				handleRecord(popAcc());
			}
			else if (_src==null && ch==_separators[S_CLOSE]) {
                handleRecord(popAcc());
   				_handler.closeTable();
    			setState(_backState.getBackState());
			}
			else {
				_acc.append(ch);
			}
		}
	}
    /**
     * Table row content
     */
    private class RowContent extends State {
        private Vector<String> _cellValues=new Vector<String>();
        public RowContent(State s) {
            super(s);
        }
		public void close() {
			if (_cellValues.size()>0) {
				_handler.addRow(_cellValues);
			}	
			_cellValues=new Vector<String>();
		}
		public void addField() {
			_cellValues.add(popAcc());
		}
        public void handle(char ch) {
			if (ch=='\r') {
			}
            else if (ch=='\n') {
                    addField();
					close();
			}
            else if (ch==_separators[S_SEP]) {
                    addField();
			}
            else if (ch==_separators[S_CLOSE]) {
                    String f=popAcc();
                    if (f.length()>0 || _cellValues.size()>0) {
			            _cellValues.add(f);
                    }
                    close();
                    goBackState();
			}
            else {
                    _acc.append(ch);
            }
        }
    }

	/**
	 * Text or attribute case 1.
	 * % encountered, follwoing may be attribute, element end or inline text.
	 */
	private class TorA1 extends State {
		public TorA1(State s) {
			super(s);
		}
		public void handle(char ch) {
            if (ch==_separators[S_SEP]) {
                _handler.endAttributes();
                setState(new InlineText(getBackState()));
			}
            else if (ch=='\r') {
			}
            else if (ch=='\n') {
                    setState(new TorA2(getBackState()));
			}
            else if (ch==_separators[S_CLOSE]) {
                if (_meta) {
                    endMetaAttributes();
                }
                else {
					_handler.endAttributes();
					_handler.closeElement();
                }    
				goBackState();
			}
            else {
					_acc.append(ch);
					setState(new Attr(getBackState()));
			}
		}
	}
	/**
	 * Text or attribute case 2.
	 * % encountered, follwoing may be:
	 * - attribute indented in new line, 
	 * - structured text.  
	 */
	private class TorA2 extends State {
        private boolean _endAttribute=false;
		public TorA2(State s) {
			super(s);
		}
		private int _indent=0;
        private void endAttributes() {
            if (!_endAttribute) {
                _endAttribute=true;
                _handler.endAttributes();
            }
        }
		public void handle(char ch) {
            if (ch=='\r'||ch=='\n') {
			}
            else if (ch=='\t') {
					_indent+=_tabSize;
			}
            else if (ch==' ') {
					_indent++;
			}
            else if (ch==_separators[S_SEP]) {
					setState(new TorA1(getBackState()));
			}
            else if (ch==_separators[S_OPEN]) {
                    endAttributes();
                    setState(new SElem(new Indent(getBackState(),_indent))); 
			}
            else if (ch==_separators[S_CLOSE]) {
                if (_meta) {
                    endMetaAttributes();
                }
                else {
					_handler.endAttributes();
					_handler.closeElement();
                }    
				goBackState();
			}
            else {
                if (_meta) {
                    endMetaAttributes();
                }
                else {
					endAttributes();
                    State s=new Indent(getBackState(),_indent);
                    setState(s);
                }    
                _state.handle(ch);
			}
		}
	}
	/**
	 * Text or attribute case 3.
	 * space encountered, element name is known, following may be inline text 
	 * or attributes.
	 */
	private class TorA3 extends State {
		public TorA3(State s) {
			super(s);
		}
		public void handle(char ch) {
            if (ch==_separators[S_SEP]) {
					setState(new TorA1(getBackState()));
			}
            else if (ch=='\t'||ch==' ') {
			}
            else if (ch==_separators[S_CLOSE]) {
                if (_meta) {
                    endMetaAttributes();
                }
                else {
					_handler.endAttributes();
					_handler.closeElement();
                }    
				goBackState();
			}
            else {
                if (_meta) {
                    endMetaAttributes();
					_state.handle(ch);
                }
                else {
                    _handler.endAttributes();
					_acc.append(ch);
					setState(new InlineText(getBackState()));
                }
			}
		}
	}

    public interface ErrHandler {
        void handle(String streamName,int line,int col,String msg);
    }
}
