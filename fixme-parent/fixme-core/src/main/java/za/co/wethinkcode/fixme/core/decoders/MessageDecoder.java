package za.co.wethinkcode.fixme.core.decoders;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import za.co.wethinkcode.fixme.core.messages.Message;
import za.co.wethinkcode.fixme.core.messages.MessageType;
import za.co.wethinkcode.fixme.core.messages.MessageAction;
import za.co.wethinkcode.fixme.core.messages.MessageConnectionAccepted;

/**
 * MessageDecoder
 */
public class MessageDecoder extends ReplayingDecoder<Object>{
	private final Charset charset = StandardCharsets.UTF_8;

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf buffer, List<Object> objList) {
		Message message = new Message();
		message.setMessageType(buffer.readCharSequence(buffer.readInt(), charset).toString());
		if (message.getMessageType().equals(MessageType.CONNECTION_ACCEPTED.toString())) {
			MessageConnectionAccepted messageConnectionAccepted = new MessageConnectionAccepted();
			messageConnectionAccepted.setMessageType(message.getMessageType());
			messageConnectionAccepted.setId(buffer.readInt());
			messageConnectionAccepted.setChecksum(buffer.readCharSequence(buffer.readInt(), charset).toString());
			objList.add(messageConnectionAccepted);
		} else if (	message.getMessageType().equals(MessageType.BUY.toString()) ||
					message.getMessageType().equals(MessageType.SELL.toString())) {
			MessageAction messageAction = new MessageAction();
			messageAction.setMessageType(message.getMessageType());
			messageAction.setMessageAction(buffer.readCharSequence(buffer.readInt(), charset).toString());
			messageAction.setId(buffer.readInt());
			messageAction.setCommodity(buffer.readCharSequence(buffer.readInt(), charset).toString());
			messageAction.setMarketId(buffer.readInt());
			messageAction.setQuantity(buffer.readInt());
			messageAction.setPrice(buffer.readInt());
			messageAction.setNewChecksum();
			objList.add(messageAction);
		}
	}
}
