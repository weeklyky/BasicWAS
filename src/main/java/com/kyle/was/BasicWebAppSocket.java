package com.kyle.was;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BasicWebAppSocket {
    private static final Logger logger = LoggerFactory.getLogger(BasicWebAppServer.class);

    private static final Pattern RequestHeaderFirstLine = Pattern.compile("^(?<method>[A-Za-z]+) (?<location>[^ \\?]+)(?<query>[^ ]*) (?<protocol>.+)");

    private final AsynchronousSocketChannel socketChannel;

    private RequestRouter router = BasicRequestRouter.getInstance();
    private static final int socketReadBufferSize = 1000;

    public BasicWebAppSocket(AsynchronousSocketChannel socketChannel) {

        this.socketChannel = socketChannel;
    }
    

    public void readSocketChannel() {

        ByteBuffer readBuffer = ByteBuffer.allocate(socketReadBufferSize);

        logger.debug("Browser Connected. Try to read request...");
        readBuffer.clear();

        socketChannel.read(readBuffer, BasicWebAppSocket.this, new CompletionHandler<Integer, BasicWebAppSocket>() {
            @Override
            public void completed(Integer size, BasicWebAppSocket socket) {
                if (size > 0) {
                    logger.debug("Received something.");
                    readBuffer.flip();
                    String requestString = Charset.forName("utf-8").decode(readBuffer).toString();
                    readBuffer.compact();
                    socket.parseRequest(requestString);
                }
            }

            @Override
            public void failed(Throwable exc, BasicWebAppSocket attachment) {
                closeSocketChannel();
            }

        });
    }

    private void parseRequest(String requestString) {
        logger.debug("request Raw: {}", requestString);
        String[] headerAndBody = requestString.split("\\s{2}");
        Pattern extract = Pattern.compile("^(?<method>[^\\s{2}]*) (?<location>[^ \\?]+)(?<query>[^ ]*) (?<protocol>.+)");

        String body = null;

        if(headerAndBody.length>1){
            body = headerAndBody[1];
        }

        StringTokenizer tokenizer = new StringTokenizer(headerAndBody[0], "\n");
        String firstLine = tokenizer.nextToken();
        logger.debug("Header (First Line) {}", firstLine);
        Matcher matcher = RequestHeaderFirstLine.matcher(firstLine);

        if (!matcher.find()) {
            logger.error("NO HEADER. Close socket");
            closeSocketChannel();
            return;
        }

        String method = matcher.group("method");
        String location = matcher.group("location").replaceFirst("^/","");
        String queryString = matcher.group("query");
        String protocol = matcher.group("protocol");

        Map<String, String> header = new HashMap<String, String>();
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            System.out.println(line);
            String[] keyValue = line.split("\\s*:\\s*");
            header.put(keyValue[0].toUpperCase(), keyValue[1]);
        }

        logger.debug("Header {}", header);


        HttpRequest req = HttpRequest.builder()
                .method(method)
                .location(location)
                .queryString(queryString)
                .protocol(protocol)
                .header(header)
                .body(body)
                .build();


        HttpResponse res = new HttpResponse(socketChannel);

        router.route(req, res);
        res.flush();
    }


    public void closeSocketChannel(){
        logger.error("Something went wrong. Close socket");
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
