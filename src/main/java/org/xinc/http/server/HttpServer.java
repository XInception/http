package org.xinc.http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServer {

    EventLoopGroup bossGroup = new NioEventLoopGroup(1);

    EventLoopGroup workerGroup = new NioEventLoopGroup();

    ChannelFuture f = null;

    HttpServerProperty property = null;


    public HttpServer() {
    }

    public HttpServer(HttpServerProperty httpServerProperty) {
        property = httpServerProperty;
        start(property);
    }

    public void start(HttpServerProperty httpServerProperty) {
        property = httpServerProperty;
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(
                                    new LoggingHandler(),
                                    new HttpRequestDecoder(),
                                    new HttpResponseEncoder(),
                                    new HttpServerHandler()
                            );
                        }
                    });
            f = b.bind(property.server, property.port);
            log.info("http proxy 启动完成 {} {} ", property.server, property.port);
            f.channel().closeFuture().sync();
            f.addListener(f->{
                log.info("启动完成消息");
                System.out.println(f);
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
