package org.xinc.http;


import lombok.extern.slf4j.Slf4j;
import org.xinc.function.Inception;
import org.xinc.function.InceptionException;


@Slf4j
public class HttpInception implements Inception {
    @Override
    public void checkRule(Object source) throws InceptionException {
        System.out.println("http 请求审核 Inception");
    }
}
