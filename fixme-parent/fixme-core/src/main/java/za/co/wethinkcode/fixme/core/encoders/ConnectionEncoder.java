package za.co.wethinkcode.fixme.core.encoders;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import za.co.wethinkcode.fixme.core.messages.MessageConnectionAccepted;
import za.co.wethinkcode.fixme.core.messages.MessageType;

public class ConnectionEncoder extends MessageToByteEncoder<MessageConnectionAccepted> {
	private final Charset charset = StandardCharsets.UTF_8;

	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext,
			MessageConnectionAccepted messageConnectionAccepted, ByteBuf buffer) {
		buffer.writeInt(messageConnectionAccepted.getTypeLength());
		buffer.writeCharSequence(messageConnectionAccepted.getMessageType(), charset);
		if (messageConnectionAccepted.getMessageType().equals(MessageType.CONNECTION_ACCEPTED.toString())) {
			buffer.writeInt(messageConnectionAccepted.getId());
			buffer.writeCharSequence(messageConnectionAccepted.getChecksum(), charset);
		}
	}
}
