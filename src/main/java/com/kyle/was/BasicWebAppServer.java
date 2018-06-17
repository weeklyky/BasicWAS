package com.kyle.was;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;

public class BasicWebAppServer {
    private static final Logger logger = LoggerFactory.getLogger(BasicWebAppServer.class);

    public static final int threadCount = 1;

    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverChannel;

    public void start() throws IOException {
        Config config = Config.getInstance();
        channelGroup = AsynchronousChannelGroup.withFixedThreadPool(threadCount, Executors.defaultThreadFactory());
        serverChannel = AsynchronousServerSocketChannel.open(channelGroup);
        serverChannel.bind(new InetSocketAddress(config.getPort()));
        serverChannel.accept(serverChannel, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>(){
            @Override
            public void completed(AsynchronousSocketChannel socketChannel, AsynchronousServerSocketChannel attachment) {
                logger.debug("serverChannel {}",serverChannel);
                if (serverChannel == null) { return; }
                serverChannel.accept(serverChannel, this);

                BasicWebAppSocket socket = new BasicWebAppSocket(socketChannel);
                socket.readSocketChannel();
            }

            @Override
            public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
                if (serverChannel.isOpen()) { try { serverChannel.close(); } catch (Exception e) { e.printStackTrace(); } }
                exc.printStackTrace();
            }

        });
        logger.info("Server is ready.");

    }

    public void shutdown(){
        if (serverChannel != null && serverChannel.isOpen()) { try { serverChannel.close(); } catch (Exception e) { e.printStackTrace(); } }
        if (channelGroup != null && (!channelGroup.isShutdown())) { channelGroup.shutdown(); }

        logger.info("Server shutdown.");

    }

    public static void main(String[] args) {
        BasicWebAppServer server = new BasicWebAppServer();
        try {
            server.start();
        } catch (IOException e) {
            server.shutdown();
        }
    }
}
