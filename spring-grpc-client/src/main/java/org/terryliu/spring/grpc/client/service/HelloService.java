package org.terryliu.spring.grpc.client.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.terryliu.grpc.sample.hello.HelloRequest;
import org.terryliu.grpc.sample.hello.HelloServiceGrpc;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2023/4/12
 * @description todo
 */
@Service
public class HelloService {
    @GrpcClient("helloService")
    private HelloServiceGrpc.HelloServiceBlockingStub stub;

    public String receiveGreeting(String firstName,String lastName){
        HelloRequest request = HelloRequest.newBuilder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .build();

        return stub.sayHello(request).getGreeting();
    }
}
