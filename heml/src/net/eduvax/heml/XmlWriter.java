/*  
 * Tabs = 4 chars
 * copyright (C) 2002 Sebastien Devaux
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA
 *
 *              Sebastien Devaux <sebastien.devaux@laposte.net>
 */
package net.eduvax.heml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import org.xml.sax.SAXException;

public class XmlWriter implements ParserCallback {
	private String _paraElem="p";
	private String _enumElem="li";
	private String _indentElem="ul";
	private String _cDataElem="pre";
	private SaxWriter _writer;
    private Vector<Exception> _exceptions;
	private String _elemName;
	private String _rowName;
	private int _rowStyle;
	private Iterable<String> _fieldsName;
	public XmlWriter(OutputStream out) {
		_writer=new SaxWriter(out);
        _exceptions=new Vector<Exception>();
	}
    public Collection<Exception> getErrors() {
        return _exceptions;
    }
    public boolean hasError() {
        return _exceptions.size()>0;
    }
	public void openElement(String name) {
		_elemName=name;
	}
	public void closeElement() {
        try {
		    _writer.endElement();
        }
        catch (SAXException ex) {
            _exceptions.add(ex);
        }
	}
	public void addAttribute(String name,String value) {
		_writer.addAttribute(name,value);
	}
	public void endAttributes() {
        try {
		    _writer.startElement(_elemName);
        }
        catch (SAXException ex) {
            _exceptions.add(ex);
        }
	}
	public void addText(String text) {
        try {
		    _writer.addText(text);
        }
        catch (SAXException ex) {
            _exceptions.add(ex);
        }
	}
	public void openPara() {
        try {
		    _writer.startElement(_paraElem);
        }
        catch (SAXException ex) {
            _exceptions.add(ex);
        }
	}
	public void closePara() {
        try {
		    _writer.endElement();
        }
        catch (SAXException ex) {
            _exceptions.add(ex);
        }
	}
	public void openEnum() {
        try {
		    _writer.startElement(_enumElem);
        }
        catch (SAXException ex) {
            _exceptions.add(ex);
        }
	}
	public void closeEnum() {
        try {
		    _writer.endElement();
        }
        catch (SAXException ex) {
            _exceptions.add(ex);
        }
	}
	public void openIndent() {
        try {
		    _writer.startElement(_indentElem);
        }
        catch (SAXException ex) {
            _exceptions.add(ex);
        }
	}
	public void closeIndent() {
        try {
		    _writer.endElement();
        }
        catch (SAXException ex) {
            _exceptions.add(ex);
        }
	}
	public void addComment(String comment) {
	}
	public void addCData(String cData) {
        try {
			_writer.startElement(_cDataElem);
			_writer.addText(cData);
		    _writer.endElement();
        }
        catch (SAXException ex) {
            _exceptions.add(ex);
        }
	}
    public void openDocument() {
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
				_writer.startElement(_rowName); 
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
					_writer.startElement(fName);
					_writer.addText(value);
					_writer.endElement();
				}
				_writer.endElement();
			}
			else {
				StringBuffer text=new StringBuffer();
				Iterator<String> it=_fieldsName.iterator();
				int i=0;
				for (String value : fieldsValue) {
					if (it.hasNext()) {
						_writer.addAttribute(it.next(),value);
					}
					else {
						text.append(value);
					}
				}
				_writer.startElement(_rowName);
				_writer.addText(text.toString());
				_writer.endElement();
			}
		}
        catch (SAXException ex) {
            _exceptions.add(ex);
        }
		
	}
}
