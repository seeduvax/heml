package net.eduvax.heml;

public interface ParserCallback {
	void openElement(String name);
	void closeElement();
	void addAttribute(String name,String value);
	void endAttributes();
	void addText(String text);
	void openPara();
	void closePara();
	void openEnum();
	void closeEnum();
	void openIndent();
	void closeIndent();
	void addComment(String comment);
	void addCData(String cData);
    void openDocument();
	void closeDocument();
    void stateChanged(Parser.State s);
	void openTable(int rowStyle,String rowName,Iterable<String> fieldsName);
	void addRow(Iterable<String> fieldsValue);
	void closeTable();
    int ROW_ELEM=0;
    int ROW_ATTR=1;
}
