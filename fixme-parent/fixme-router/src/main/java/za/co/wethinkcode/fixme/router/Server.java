package za.co.wethinkcode.fixme.router;

import java.nio.channels.SocketChannel;
import java.util.HashMap;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import za.co.wethinkcode.fixme.core.decoders.MessageDecoder;
import za.co.wethinkcode.fixme.core.encoders.ActionEncoder;
import za.co.wethinkcode.fixme.core.encoders.ConnectionEncoder;
import za.co.wethinkcode.fixme.core.exceptions.ChecksumNotEqualException;
import za.co.wethinkcode.fixme.core.exceptions.ClientNotFoundException;
import za.co.wethinkcode.fixme.core.messages.Message;
import za.co.wethinkcode.fixme.core.messages.MessageAction;
import za.co.wethinkcode.fixme.core.messages.MessageConnectionAccepted;
import za.co.wethinkcode.fixme.core.messages.MessageType;

public class Server implements Runnable {
    private static HashMap<Integer, ChannelHandlerContext> routingTable = new HashMap<>();
	static final int SVR_MARKET = 5001;
	static final int SVR_BROKER = 5000;
	private EventLoopGroup _serverGroup;
	private EventLoopGroup _clientGroup;
	private int _serverType;

	Server (int ServerType) {
	    this._serverType = ServerType;
    }

    @Override
    public void run() {
	    createServer(_serverType);
    }

    private String setServerType() {
	    return _serverType == SVR_MARKET ? "market" : "broker";
    }

    private boolean isBroker() {
	    return _serverType != SVR_MARKET;
    }

    private void createServer(int serverPort) {
	    _serverGroup = new NioEventLoopGroup();
	    _clientGroup = new NioEventLoopGroup();
	    try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(_serverGroup, _clientGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        public void initChannel(Channel channel) {
                            channel.pipeline().addLast(
                                    new MessageDecoder(),
                                    new ConnectionEncoder(),
                                    new ActionEncoder(),
                                    new ProcessingHandler()
                            );
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = serverBootstrap.bind(serverPort).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException ex) {
	        ex.printStackTrace();
        } finally {
	        shutThingsDown();
        }
    }

    void shutThingsDown() {
	    _clientGroup.shutdownGracefully();
	    _serverGroup.shutdownGracefully();
    }

    class ProcessingHandler extends ChannelInboundHandlerAdapter {
	    @Override
        public void channelRead(ChannelHandlerContext channelHandlerContext, Object message) {
            Message fixmessage = (Message)message;
            if (fixmessage.getMessageType().equals(MessageType.CONNECTION_ACCEPTED.toString()))
                acceptNewConnection(channelHandlerContext, message);
            else if (fixmessage.getMessageType().equals(MessageType.BUY.toString()) || fixmessage.getMessageType().equals(MessageType.SELL.toString())) {
                MessageAction messageAction = (MessageAction)message;
                try {
                    checkMessageForErrors(messageAction);
                    if (isMessageRejectedOrExecuted(messageAction))
                        return;
                    System.out.println("Sending a request to market with ID " + messageAction.getMarketId());
                    getById(messageAction.getMarketId()).channel().writeAndFlush(messageAction);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    messageAction.setMessageAction(MessageType.REJECT.toString());
                    messageAction.setNewChecksum();
                    channelHandlerContext.writeAndFlush(messageAction);
                }
            }
        }
    }

    private void acceptNewConnection(ChannelHandlerContext channelHandlerContext, Object message) {
        MessageConnectionAccepted messageConnectionAccepted = (MessageConnectionAccepted)message;
        String newId = channelHandlerContext.channel().remoteAddress().toString().substring(11);
        newId = newId.concat(isBroker() ? "2" : "3");
        messageConnectionAccepted.setId(Integer.parseInt(newId));
        messageConnectionAccepted.setNewChecksum();
        channelHandlerContext.writeAndFlush(messageConnectionAccepted);
        routingTable.put(messageConnectionAccepted.getId(), channelHandlerContext);
        System.out.println("Connection accepted from " + setServerType() + ": " + newId);
    }

    private void checkMessageForErrors(MessageAction messageAction) throws Exception {
	    if (!messageAction.getMessageMD5().equals(messageAction.getChecksum()))
	        throw new ChecksumNotEqualException();
	    if (!isInTable(messageAction.getMarketId()))
	        throw new ClientNotFoundException();
    }

    private boolean isMessageRejectedOrExecuted(MessageAction messageAction) throws Exception {
	    if (messageAction.getMessageAction().equals(MessageType.EXECUTE.toString()) || messageAction.getMessageAction().equals(MessageType.REJECT.toString())) {
	        if (!messageAction.getMessageMD5().equals(messageAction.getChecksum()))
	            throw new ChecksumNotEqualException();
	        getById(messageAction.getId()).writeAndFlush(messageAction);
	        return true;
        }
	    return false;
    }

    private boolean isInTable(int id) {
	    return routingTable.containsKey(id);
    }

    private ChannelHandlerContext getById(int id) {
	    return routingTable.get(id);
    }
}
