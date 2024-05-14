package org.terryliu.grpc.sample.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.terryliu.grpc.sample.hello.HelloRequest;
import org.terryliu.grpc.sample.hello.HelloResponse;
import org.terryliu.grpc.sample.hello.HelloServiceGrpc;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2023/4/12
 * @description todo
 */
public class GrpcClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        HelloServiceGrpc.HelloServiceBlockingStub stub
                = HelloServiceGrpc.newBlockingStub(channel);

        HelloResponse helloResponse = stub.sayHello(HelloRequest.newBuilder()
                .setFirstName("Terry")
                .setLastName("gRPC")
                .build());
        System.out.println(helloResponse.getGreeting());
        channel.shutdown();
    }
}
