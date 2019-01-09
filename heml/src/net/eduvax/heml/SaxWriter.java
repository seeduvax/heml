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
import java.util.Date;
import java.util.Stack;
import java.text.SimpleDateFormat;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * XML writing sax helper, manage xml output encoding, xml complience...
 * @author Sebastien Devaux	
 */
public class SaxWriter {
	/**
	 * Create new writer
	 * @param out destination output stream.
	 */ 
	public SaxWriter(OutputStream out) {
		init();
/*
 TODO: This may be a way to avoid delayed writings but
 it requires org.apache.xml.serialize that is not part of the standard JDK.
		OutputFormat of=new OutputFormat("XML","UTF-8",true);
		of.setIndent(1);
		of.setIndenting(true);
//		of.setDoctype(null,"users.dtd");
		XMLSerializer serializer=new XMLSerializer(out,of);
		_handler=serialiser.asContnentHandler();
*/		
		TransformerHandler tHandler=null;
		_sr=new StreamResult(out);
		SAXTransformerFactory tf=(SAXTransformerFactory)SAXTransformerFactory.newInstance();
		try {
			tHandler=tf.newTransformerHandler();
			Transformer serializer=tHandler.getTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT,"yes");
			tHandler.setResult(_sr);
		}
		catch (TransformerConfigurationException ex) {
            // just log error, since transformer is programatically set
            // just above, exception should never be raised
            System.err.println(
                "Error in SaxWriterConstruction. Can't create XML Writer "
                +ex
                +". Please check JRE and XML libs installation and configuration.");
		}
		_handler=tHandler;
	}
	/**
	 * Create new writer
	 * @param out destination content handler.
	 */ 
	public SaxWriter(ContentHandler ch) {
		init();
		_handler=ch;
	}
	/**
	 * Start new element.
	 * Previously added attributes will be set to this new elements
	 * then current attribute list is cleared.
	 * @param name new element name.
	 */ 
	public void startElement(String name) throws SAXException {
		if (_elementStack.empty()) {
			_handler.startDocument();
		}
		_elementStack.push(_elementName);
		_elementName=name;
		_handler.startElement("","",name,_attrs);
		_attrs.clear();
	}
	/**
	 * Add attribute to the next start element.
	 * @param name attribute name
	 * @param value attribute value
	 */ 
	public void addAttribute(String name,String value) {
		_attrs.addAttribute("","",name,"CDATA",value);
	}
	/**
	 * Add date attribute to the next start element.
	 * set date format to be complient with exslt date extensions.
	 * cf. http://www.exslt.org/date/
	 * @param name attribute name
	 * @param date date value
	 */
	public void addAttribute(String name,Date date) {
		addAttribute(name,_xmlDateFormat.format(date));
	}
	/** 
	 * Add text to current element
	 * @param text text to add.
	 */
	public void addText(String text) throws SAXException {
		_handler.characters(text.toCharArray(),0,text.length());
	}
	/**
	 * Close current element
	 */ 
	public void endElement() throws SAXException {
		_handler.endElement("","",_elementName);
		_elementName=_elementStack.pop();
		if (_elementStack.empty()) {
			_handler.endDocument();
		}
	}
    /**
     * Helper for element containing only a string.
     * Chains startElement, addText and endElement.
     * @param name element's name.
     * @param value element's text value
     */
    public void addElement(String name, String value) throws SAXException {
        startElement(name);
        addText(value);
        endElement();
    }
     

	public void flush() throws IOException {
		if (_sr!=null) {
			if (_sr.getWriter()!=null) {
                _sr.getWriter().flush();
            }
            else if (_sr.getOutputStream()!=null) {
                _sr.getOutputStream().flush();
            }
		}	
	}
	/**
	 * Initialize writer.
	 */ 
	private void init() {
		_elementStack=new Stack<String>();
		_attrs=new AttributesImpl();
		_sr=null;
	}
	/** current element name */
	private String _elementName;
	/** "open" elements name stack */
	private Stack<String> _elementStack;
	/** xml document sax handler */
	private ContentHandler _handler;
	/** next element attributes */
	private AttributesImpl _attrs;
	/** output result stream (may be null) */
	private StreamResult _sr;
	/** format for date attribute */
	private static SimpleDateFormat _xmlDateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
}
