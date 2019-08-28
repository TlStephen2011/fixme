package za.co.wethinkcode.fixme.core.encoders;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import za.co.wethinkcode.fixme.core.messages.MessageAction;
import za.co.wethinkcode.fixme.core.messages.MessageType;

/**
 * ActionEncoder
 */
public class ActionEncoder extends MessageToByteEncoder<MessageAction> {
	private final Charset charset = StandardCharsets.UTF_8;

	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, MessageAction messageAction, ByteBuf buffer) {
		buffer.writeInt(messageAction.getTypeLength());
		buffer.writeCharSequence(messageAction.getMessageType(), charset);
		if (messageAction.getMessageType().equals(MessageType.BUY.toString())
				|| messageAction.getMessageType().equals(MessageType.SELL.toString())) {
			buffer.writeInt(messageAction.getMessageActionLength());
			buffer.writeCharSequence(messageAction.getMessageAction(), charset);
			buffer.writeInt(messageAction.getId());
			buffer.writeInt(messageAction.getCommodityLength());
			buffer.writeCharSequence(messageAction.getCommodity(), charset);
			buffer.writeInt(messageAction.getMarketId());
			buffer.writeInt(messageAction.getPrice());
			buffer.writeInt(messageAction.getQuantity());
			buffer.writeInt(messageAction.getChecksumLength());
			buffer.writeCharSequence(messageAction.getChecksum(), charset);
		}
	}
}
