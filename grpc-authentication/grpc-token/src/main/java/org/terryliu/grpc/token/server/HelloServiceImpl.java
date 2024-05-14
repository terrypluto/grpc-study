package org.terryliu.grpc.token.server;

import io.grpc.stub.StreamObserver;
import org.terryliu.grpc.sample.hello.HelloRequest;
import org.terryliu.grpc.sample.hello.HelloResponse;
import org.terryliu.grpc.sample.hello.HelloServiceGrpc;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2023/4/12
 * @description todo
 */
public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase{
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        HelloResponse helloResponse = HelloResponse.newBuilder()
                .setGreeting("hello " + request.getLastName() + " "+request.getFirstName()).build();
        responseObserver.onNext(helloResponse);
        responseObserver.onCompleted();

        System.out.println("response:" + helloResponse.getGreeting());
    }
}
