package org.terryliu.spring.grpc.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.terryliu.spring.grpc.client.service.HelloService;

import javax.annotation.Resource;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2023/4/12
 * @description todo
 */
@SpringBootTest(classes = GrpcClientSpringApplication.class)
public class GrpcSpringApplicationTest {
    @Resource
    private HelloService helloService;

    @Test
    public void test1(){
        String greeting = helloService.receiveGreeting("terry", "gRPC");
        Assertions.assertEquals("hello gRPC terry",greeting);
    }
}
