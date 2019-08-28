package za.co.wethinkcode.fixme.core.messages;

import za.co.wethinkcode.fixme.core.MD5Creator;

/**
 * MessageConnectionAccepted
 */
public class MessageConnectionAccepted extends Message {

private int _id;

public MessageConnectionAccepted(String messageType, int marketId, int id) {
super(messageType, marketId);
this._id = id;
setChecksum(MD5Creator.createMD5FromObject(String.valueOf(_id)));
}

public MessageConnectionAccepted() {

}

public int getId() {
	return this._id;
}

public void setId(int id) {
	this._id = id;
}

public void setNewChecksum() {
setChecksum(MD5Creator.createMD5FromObject(String.valueOf(_id)));
}

@Override
public String toString() {
	return "Connection Accepted {"
	+ "ID = " + _id
	+ " - MESSAGE_TYPE = '" + getMessageType() + "' "
	+ " - CHECKSUM = '" + getChecksum() + "' "
	+ '}';
}
}
