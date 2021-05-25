package org.xinc.http.client;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Admin
 */
@Slf4j
public class HttpClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    Channel downStreamChanel = null;
    HttpClientProperty property = null;

    public HttpClientHandler(Channel downStreamChanel, HttpClientProperty property) {
        super(false);
        this.downStreamChanel = downStreamChanel;
        this.property = property;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
        if(downStreamChanel!=null ){
            System.out.println(httpObject);
            downStreamChanel.write(httpObject);
            if(httpObject instanceof LastHttpContent){
                downStreamChanel.flush().closeFuture().sync();
            }
        }
    }
}
