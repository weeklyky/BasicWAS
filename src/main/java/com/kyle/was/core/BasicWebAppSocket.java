package com.kyle.was.core;

import com.kyle.was.router.rule.ErrorPageRoutingRule;
import com.kyle.was.router.rule.IndexPageRoutingRule;
import com.kyle.was.router.rule.ServletByClassNameRoutingRule;
import com.kyle.was.units.HttpRequest;
import com.kyle.was.units.HttpResponse;
import com.kyle.was.router.StaticWebRouter;
import com.kyle.was.router.rule.RoutingRule;
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
    public static String newline = System.getProperty("line.separator");

    private final AsynchronousSocketChannel socketChannel;

    private RoutingRule router;
    private static final int socketReadBufferSize = 1000;

    public BasicWebAppSocket(AsynchronousSocketChannel socketChannel) {

        this.socketChannel = socketChannel;

        router = StaticWebRouter.getInstance();
        /*Caution! Order of the rules matter!*/
        router = new ErrorPageRoutingRule(router);
        router = new ServletByClassNameRoutingRule(router);
        router = new IndexPageRoutingRule(router);

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

        byte[] body = null;

        StringTokenizer tokenizer = new StringTokenizer(requestString, "\n");
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
            String[] keyValue = line.split("\\s*:\\s*");
            if(line.length()<2){
                break;
            }
            //System.out.println("?");
            header.put(keyValue[0].toUpperCase(), keyValue[1]);
        }

        //for POST Body
        StringBuilder stringBuilder = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            stringBuilder.append(tokenizer.nextToken());
            stringBuilder.append(newline);
        }
        if(stringBuilder.length()>0){
            body = stringBuilder.toString().getBytes();
        }

        logger.debug("Header(keys): {}", header.keySet());
        logger.debug("Header-Host: {}", header.get("HOST"));
        logger.debug("Body : {}", body);


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
