package za.co.wethinkcode.fixme.core.messages;

public class Message {
	private int		_messageTypeLength;
	private String	_messageType;
	private int		_marketId;
	private int		_checksumLength;
	private String	_checksum;

	Message(String MessageType, int MarketId) {
		this._messageType = MessageType;
		this._messageTypeLength = MessageType.length();
		this._marketId = MarketId;
	}

	public Message() {}

	public String getMessageType() {
		return _messageType;
	}

	public void setMessageType(String MessageType) {
		this._messageType = MessageType;
		_messageTypeLength = MessageType.length();
	}

	public int getMarketId() {
		return _marketId;
	}

	public void setMarketId(int MarketId) {
		this._marketId = MarketId;
	}

	public String getChecksum() {
		return _checksum;
	}

	public void setChecksum(String Checksum) {
		this._checksum = Checksum;
		_checksumLength = Checksum.length();
	}

	public int getTypeLength() {
		return _messageTypeLength;
	}

	public int getChecksumLength() {
		return _checksumLength;
	}
}
