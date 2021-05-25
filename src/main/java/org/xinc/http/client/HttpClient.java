package org.xinc.http.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.Queue;


@Slf4j
public class HttpClient implements Closeable {

    private EventLoopGroup eventLoopGroup;

    private Bootstrap bootstrap;

    HttpClientProperty property = null;

    Channel downstream;

    Channel upstreamChannel;

    Queue<Object> msgs = new LinkedList<>();

    public HttpClient(HttpClientProperty mysqlClientProperty, Channel downStream) {

        downstream = downStream;

        property = mysqlClientProperty;

        start(mysqlClientProperty, downStream);
    }

    public void start(HttpClientProperty httpClientProperty, Channel downStream) {
        downstream = downStream;

        property = httpClientProperty;

        eventLoopGroup = new NioEventLoopGroup();

        bootstrap = new Bootstrap();

        bootstrap.group(eventLoopGroup);

        bootstrap.channel(NioSocketChannel.class);

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(
                        new LoggingHandler(),
                        new HttpClientCodec(),
                        new HttpClientHandler(downStream, httpClientProperty)
                );
            }
        });

        var cf = bootstrap.connect(property.server, property.port);

        cf.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                System.out.println("http 服务器连接成功");
                Thread t= new Thread(this::consume);
                t.start();
            }
            System.out.println(future);
        });
        try {
            cf.sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!cf.isSuccess()) {
            throw new RuntimeException(cf.cause());
        }
        upstreamChannel = cf.channel();
        log.info("服务器基本信息:" + upstreamChannel.remoteAddress().toString());
    }


    public void consume() {
        log.info("http连接建立完成");
        while (true) {
            //写入后端
            MsgQueue msgQueue = (MsgQueue) msgs.poll();
            if (msgQueue == null) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if ("write".equals(msgQueue.op)) {
//                System.out.println("------>" + msgQueue.op);
//                System.out.println("------>" + msgQueue.payload);
                upstreamChannel.write(msgQueue.payload);
            } else {
                System.out.println("发送消息2"+System.currentTimeMillis());
                upstreamChannel.flush();
            }
        }
    }

    public void write(Object msg) {
        //写入队列
        if (msg instanceof HttpRequest) {
            //改写http host
            log.info("重写 host地址");
            ((DefaultHttpRequest) msg).headers().set(HttpHeaderNames.HOST, "39.156.69.79");
        }
        msgs.add(new MsgQueue("write", msg));
    }

    public void flush() {
        System.out.println("发送消息1"+System.currentTimeMillis());
        msgs.add(new MsgQueue("flush"));
    }

    public void get(String s) {
        System.out.println("发送请求");
        DefaultFullHttpRequest httpRequest=new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,new HttpMethod("GET"),s,Unpooled.EMPTY_BUFFER);
        httpRequest.headers().set("HOST", "39.156.69.79");
        httpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        msgs.add(new MsgQueue("write",httpRequest));
        msgs.add(new MsgQueue("flush"));
    }

    @Override
    public void close() {
        log.info("断开与后端服务器连接");
        try {
            upstreamChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();

            eventLoopGroup.shutdownGracefully();
        }
    }

    static class MsgQueue {
        String op;
        Object payload;

        public MsgQueue(String op) {
            this.op = op;
        }

        public MsgQueue(String op, Object payload) {
            this.op = op;
            this.payload = payload;
        }
    }
}
