package za.co.wethinkcode.fixme.core.messages;

import za.co.wethinkcode.fixme.core.MD5Creator;

/**
 * MessageAction
 */
public class MessageAction extends Message {

	private int		_messageActionLength;
	private String	_messageAction;
	private int		_id;
	private int		_commodityLength;
	private String	_commodity;
	private int		_quantity;
	private int		_price;

public MessageAction(String MessageType, String MessageAction, int MarketId, int Id, String Commodity, int Quantity, int Price) {
super(MessageType, MarketId);
this._messageAction = MessageAction;
this._messageActionLength = MessageAction.length();
this._id = Id;
this._commodity = Commodity;
this._commodityLength = Commodity.length();
this._quantity = Quantity;
this._price = Price;
setChecksum(getMessageMD5());
}

public MessageAction() {

}

public int getId() {
	return this._id;
}

public void setId(int Id) {
	this._id = Id;
}

public String getCommodity() {
	return _commodity;
}

public void setCommodity(String Commodity) {
	this._commodity = Commodity;
	this._commodityLength = Commodity.length();
}

public int getQuantity() {
	return _quantity;
}

public void setQuantity(int Quantity) {
	this._quantity = Quantity;
}

public int getPrice() {
	return _price;
}

public void setPrice(int Price) {
	this._price = Price;
}

public int getCommodityLength() {
	return _commodityLength;
}

public String getMessageMD5() {
	return MD5Creator.createMD5FromObject(String.valueOf(_id).concat(_commodity).concat(String.valueOf(_quantity)).concat(_messageAction));
}

public void setNewChecksum() {
	setChecksum(getMessageMD5());
}

public String getMessageAction() {
	return _messageAction;
}

public void setMessageAction(String MessageAction) {
	this._messageAction = MessageAction;
	this._messageActionLength = MessageAction.length();
}

public int getMessageActionLength() {
	return _messageActionLength;
}

@Override
public String toString() {
	return "Message Action {" +
			"MESSAGE_ID = " + _id +
			" - MESSAGE_TYPE = '" + getMessageType() + "'" +
			" - MESSAGE_ACTION = '" + _messageAction + "'" +
			" - COMMODITY = '" + _commodity + "'" +
			" - MARKET_ID = " + getMarketId() +
			" - QUANTITY = " + _quantity +
			" - PRICE = " + _price +
			" - CHECKSUM = '" + getChecksum() + "'" +
			'}';
}
}
