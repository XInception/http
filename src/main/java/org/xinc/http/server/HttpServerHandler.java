package org.xinc.http.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.xinc.function.InceptionException;
import org.xinc.http.HttpInception;
import org.xinc.http.client.HttpClient;
import org.xinc.http.client.HttpClientProperty;

import java.util.HashMap;

@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {


    HttpInception httpInception = new HttpInception();

    private HttpRequest request;

    StringBuilder responseData = new StringBuilder();

    HashMap<String, Object> config = new HashMap<>();
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端已经离线 返还 http 句柄");
        HttpClient upstreamClient = (HttpClient) ctx.channel().attr(AttributeKey.valueOf("http_connect")).get();
        upstreamClient.close();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        log.info("客户端已经上线 获取http 句柄");
        config.put("downStream",ctx.channel());
        HttpClient upstreamClient = new HttpClient(new HttpClientProperty("/application-client.properties"),ctx.channel());
        ctx.channel().attr(AttributeKey.valueOf("http_connect")).set(upstreamClient);
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("异常断开连接");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        HttpClient upstreamClient = (HttpClient) ctx.channel().attr(AttributeKey.valueOf("http_connect")).get();
        try {
            httpInception.checkRule(msg);
        } catch (InceptionException e) {
            e.printStackTrace();
            return;
        }

        upstreamClient.write(msg);

        if (msg instanceof LastHttpContent) {
            upstreamClient.flush();
        }
    }
}