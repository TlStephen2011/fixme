package za.co.wethinkcode.fixme.core;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import za.co.wethinkcode.fixme.core.decoders.MessageDecoder;
import za.co.wethinkcode.fixme.core.encoders.ActionEncoder;
import za.co.wethinkcode.fixme.core.encoders.ConnectionEncoder;
import za.co.wethinkcode.fixme.core.exceptions.ChecksumNotEqualException;
import za.co.wethinkcode.fixme.core.exceptions.InputEmptyException;
import za.co.wethinkcode.fixme.core.exceptions.InputErrorException;
import za.co.wethinkcode.fixme.core.messages.Message;
import za.co.wethinkcode.fixme.core.messages.MessageType;
import za.co.wethinkcode.fixme.core.messages.MessageAction;
import za.co.wethinkcode.fixme.core.messages.MessageConnectionAccepted;

/**
 * Client
 */
public class Client implements Runnable {
	private String _name;
	private EventLoopGroup _eventLoopGroup;
	private int _id;

	public Client(String Name) {
		this._name = Name;
	}

	public static void inputHandler(Client client) {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		String command;
		while (true) {
			command = null;
			try {
				command = bufferedReader.readLine();
			} catch (IOException ex) {
				System.err.println(ex.getMessage());
			}
			if (command != null && command.toLowerCase().equals("exit")) {
				client.shutDown();
				break;
			}
		}
	}

	@Override
	public void run() {
		String host = "localhost";
		int port = 5000;
		if (_name.equals("Market"))
			port = 5001;
		_eventLoopGroup = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(_eventLoopGroup).channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) {
							ch.pipeline().addLast(new MessageDecoder(), new ConnectionEncoder(), new ActionEncoder(),
									new ClientHandler());
						}
					}).option(ChannelOption.SO_KEEPALIVE, true);
			ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
			channelFuture.channel().closeFuture().sync();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} finally {
			shutDown();
		}
	}

	private void shutDown() {
		_eventLoopGroup.shutdownGracefully();
	}

	class ClientHandler extends ChannelInboundHandlerAdapter {
		@Override
		public void channelActive(ChannelHandlerContext channelHandlerContext) {
			System.out.println(_name + " is connecting to router..");
			MessageConnectionAccepted messageConnectionAccepted = new MessageConnectionAccepted(
					MessageType.CONNECTION_ACCEPTED.toString(), 0, 0);
			channelHandlerContext.writeAndFlush(messageConnectionAccepted);
		}

		@Override
		public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) {
			Message message = (Message) msg;
			if (message.getMessageType().equals(MessageType.CONNECTION_ACCEPTED.toString())) {
				MessageConnectionAccepted messageConnectionAccepted = (MessageConnectionAccepted) msg;
				_id = messageConnectionAccepted.getMarketId();
				System.out.println("Connection with router established. ID: " + _id);
			} else if (message.getMessageType().equals(MessageType.BUY.toString())
					|| message.getMessageType().equals(MessageType.SELL.toString())) {
				MessageAction messageAction = (MessageAction) msg;
				try {
					if (!messageAction.getMessageMD5().equals(messageAction.getChecksum()))
						throw new ChecksumNotEqualException();
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
					return;
				}
				if (checkForBrokerAnswerFromMarket(messageAction))
					return;
				if (message.getMessageType().equals(MessageType.SELL.toString()))
					marketForSaleRequest(channelHandlerContext, messageAction);
				else
					marketForPurchaseRequest(channelHandlerContext, messageAction);
			}
		}

		private boolean checkForBrokerAnswerFromMarket(MessageAction messageAction) {
			if (messageAction.getMessageAction().equals(MessageType.EXECUTE.toString())
					|| messageAction.getMessageAction().equals(MessageType.REJECT.toString())) {
				System.out.println("Request result: " + messageAction.getMessageAction());
				return true;
			}
			return false;
		}

		private void marketForSaleRequest(ChannelHandlerContext channelHandlerContext, MessageAction messageAction) {
			Random rand = new Random();
			if (rand.nextBoolean()) {
				System.out.println("EXECUTE. Thank you for this commodity!");
				messageAction.setMessageAction(MessageType.EXECUTE.toString());
			} else {
				System.out.println("REJECT. We don't want this commodity.");
				messageAction.setMessageAction(MessageType.REJECT.toString());
			}
			messageAction.setNewChecksum();
			channelHandlerContext.writeAndFlush(messageAction);
		}

		private void marketForPurchaseRequest(ChannelHandlerContext channelHandlerContext,
				MessageAction messageAction) {
			Random rand = new Random();
			int randomInt = rand.nextInt(100);
			if (randomInt >= 0 && randomInt < 20) {
				System.out.println("REJECT. No such commodity in the market!");
				messageAction.setMessageAction(MessageType.REJECT.toString());
			} else if (randomInt >= 20 && randomInt < 40) {
				System.out.println("REJECT. Not enough of this commodity in the market!");
				messageAction.setMessageAction(MessageType.REJECT.toString());
			} else {
				System.out.println("EXECUTE. Thank you for your purchase!");
				messageAction.setMessageAction(MessageType.EXECUTE.toString());
			}
			messageAction.setNewChecksum();
			channelHandlerContext.writeAndFlush(messageAction);
		}

		private void writeToChannel(ChannelHandlerContext channelHandlerContext) {
			try {
				String input = getTextFromUser();
				if (input.length() == 0)
					throw new InputEmptyException();
				else if (_name.equals("Broker"))
					brokerOutput(channelHandlerContext, input);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				writeToChannel(channelHandlerContext);
			}
		}

		private void brokerOutput(ChannelHandlerContext channelHandlerContext, String string) throws Exception {
			String[] split = string.split("\\s+");
			if (split.length != 5)
				throw new InputErrorException();
			MessageAction messageAction;
			int marketID = checkID(split[1]);
			String commodity = split[2];
			int quantity = Integer.parseInt(split[3]);
			int price = Integer.parseInt(split[4]);
			if (split[0].toLowerCase().equals("sell")) {
				messageAction = new MessageAction(MessageType.SELL.toString(), "-", marketID, _id, commodity,
						quantity, price);
			} else if (split[0].toLowerCase().equals("buy")) {
				messageAction = new MessageAction(MessageType.BUY.toString(), "-", marketID, _id, commodity,
						quantity, price);
			} else
				throw new InputErrorException();
			messageAction.setNewChecksum();
			channelHandlerContext.writeAndFlush(messageAction);
			System.out.println("Sending a request to the router...");
		}

		private int checkID(String Id) throws Exception {
			int idToInt = Integer.parseInt(Id);
			if (Id.length() != 6)
				throw new InputErrorException();
			return idToInt;
		}

		@Override
		public void channelReadComplete(ChannelHandlerContext channelHandlerContext) {
			if (_name.equals("Broker"))
				writeToChannel(channelHandlerContext);
		}

		private String getTextFromUser() throws Exception {
			System.out.println(
					"Enter request message of any of the following types: [sell || buy] [market id] [commodity] [quantity] [price]");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			return bufferedReader.readLine();
		}
	}
}
