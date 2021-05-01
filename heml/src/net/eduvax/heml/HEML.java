/*  $Id: HEML.java 1550 2017-05-08 10:59:21Z sdevaux $
 *
 * Tabs = 4 chars
 * copyright (C) 2008 Sebastien Devaux
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

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import org.xml.sax.SAXException;

/**
 * xtxt file parser.
 * convert ascii file with simplified markup to xml.
 */ 
public class HEML {
	static public void help() {
		System.out.println("java -jar net.eduvax.heml.HEML [-in <input file>] [-out <ouput file>] [-xsl <stylesheet>]");
	}
	/**
	 * Reads xdoc from stdin and send xml to stdout.
	 */
	static public void main(String[] args) {
        int rc=0;
        try {
			// parse commandline arguments
			int i=0;
			String inputPath="-";
			String outputPath="-";
			String xslPath=null;
            String depFile=null;
            List<String> searchPaths = new ArrayList<>();
            Hashtable<String,String> xslParams=new Hashtable<String,String>();
            Hashtable<String,Hashtable<String,String>> depAttrMap=
                new Hashtable<String,Hashtable<String,String>>();
			while (i<args.length) {
				if ("-in".equals(args[i])) {
					i++;
					inputPath=args[i];
				}
				else if ("-out".equals(args[i])) {
					i++;
					outputPath=args[i];
				}
				else if ("-xsl".equals(args[i])) {
					i++;
					xslPath=args[i];
				}
				else if ("-dep".equals(args[i])) {
					i++;
					depFile=args[i];
				}
				else if ("-depattr".equals(args[i])) {
					i++;
                    StringTokenizer st=new StringTokenizer(args[i],":");
                    String eName=st.nextToken();
                    if (st.hasMoreTokens()) {
                        String aName=st.nextToken();
                        Hashtable<String,String> map=depAttrMap.get(eName);
                        if (map==null) {
                            map=new Hashtable<String,String>();
                            depAttrMap.put(eName,map);
                            if (st.hasMoreTokens()) {
                                map.put(aName,st.nextToken()+"/");
                            }
                            else {
                                map.put(aName,"");
                            }
                        }
                    }
				}
				else if ("-param".equals(args[i])) {
					i++;
					String name=args[i];
                    i++;
                    String value=args[i];
                    xslParams.put(name,value);
				}
                else if ("-path".equals(args[i])) {
					i++;
					searchPaths.add(args[i]);
                }
				else {
					System.out.println("Unexpected argument: "+args[i]);
				}
				i++;
			}
			// Define output
			OutputStream output;
			if ("-".equals(outputPath)) {
				output=System.out;
			}
			else {
				output=new FileOutputStream(outputPath);
			}
			// HEML parsing.	
            Parser parser=new Parser(inputPath,output);
			if (xslPath!=null) {
                parser.setXslPath(xslPath);
                for (String name : xslParams.keySet()) {
                    parser.setXslParam(name,xslParams.get(name));
                }
                for (String searchPath : searchPaths) {
                    parser.addSearchPath(searchPath);                	
                }
			}
            PrintStream depOut=null;
            if (depFile!=null && !("-".equals(outputPath))) {
                try {
                    depOut=new PrintStream(new FileOutputStream(depFile));
                    depOut.print(outputPath);
                    depOut.print(":");
                    parser.setDepOut(depOut,depAttrMap);
                }
                catch (IOException ex) {
                    System.err.println("Can't create dependency file: "+depFile);
                }
            }
            parser.run();
            if (depOut!=null) {
                depOut.println();
                depOut.println();
            }
        }
        catch (Exception ex) {
ex.printStackTrace();		
			rc=1;
			System.out.println("Exception: "+ex);
			help();
		}
        System.exit(rc);
	}
}
