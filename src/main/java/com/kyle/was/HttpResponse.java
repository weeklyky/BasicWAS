package com.kyle.was;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

public class HttpResponse {
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    public static final int OK = 200;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int INTERNAL_SERVER_ERROR = 500;

    private final ByteArrayOutputStream outputStream;
    private AsynchronousSocketChannel socketChannel;
    private PrintWriter writer;
    private int responseCode;
    private String contentType = "text/html; charset=UTF-8";

    public HttpResponse(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        outputStream = new ByteArrayOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream));
    }


    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Writer getWriter() {
        return writer;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "outputStream=" + outputStream +
                ", socketChannel=" + socketChannel +
                ", writer=" + writer +
                ", responseCode=" + responseCode +
                ", contentType='" + contentType + '\'' +
                '}';
    }

    public void flush()  {
        writer.flush();
        byte[] body = outputStream.toByteArray();
        writer.close();
        byte[] header = setHeader(responseCode, body.length);

        int size = header.length + body.length;

        ByteBuffer write = ByteBuffer.allocate(size);

        write.clear();
        write.put(header);
        write.put(body);
        write.flip();

        socketChannel.write(write, socketChannel, new CompletionHandler<Integer, AsynchronousSocketChannel>() {
            @Override
            public void completed(Integer result, AsynchronousSocketChannel socketChannel) {

                logger.debug("Write Successfully (size: {})", result);
                try {
                    socketChannel.close();
                    logger.debug("Response has been written. Connection close.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel socketChannel) {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });



    }

    //some values should be calculated based on htmlbody
    private byte[] setHeader(int responseCode, int contentLength) {
        StringBuilder builder = new StringBuilder();
        builder.append("HTTP/1.1 ");
        builder.append(responseCode + "\r\n");
        builder.append("Date: " + new Date() + "\r\n");
        builder.append("Server: com.kyle.was.BasicServer\r\n");
        builder.append("Content-length: " + contentLength + "\r\n");
        builder.append("Content-type: " + contentType + "\r\n\r\n");
        return builder.toString().getBytes();
    }

    public void setStaticFileOutput(Path path)  {
        logger.debug("Show static file (path : {})", path);
        writer.close();
        outputStream.reset();
        try {
            outputStream.write(Files.readAllBytes(path));
        } catch (IOException e) {
            logger.error("{}",e);
        }
        flush();
    }
}
