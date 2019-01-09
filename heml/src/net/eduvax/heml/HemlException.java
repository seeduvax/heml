package net.eduvax.heml;


public class HemlException extends Exception {
	private Exception _source;
	public HemlException(String msg,Exception source) {
		super(msg);
		_source=source;
	}
	public Exception getSource() {
		return _source;
	}
}
