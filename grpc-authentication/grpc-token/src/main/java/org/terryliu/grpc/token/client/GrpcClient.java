package org.terryliu.grpc.token.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.jsonwebtoken.Jwts;
import org.terryliu.grpc.sample.hello.HelloRequest;
import org.terryliu.grpc.sample.hello.HelloResponse;
import org.terryliu.grpc.sample.hello.HelloServiceGrpc;
import org.terryliu.grpc.token.Constants;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2023/4/12
 * @description todo
 */
public class GrpcClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8090)
                .usePlaintext()
                .build();
        BearerToken token = new BearerToken(getJwt());
        HelloServiceGrpc.HelloServiceBlockingStub stub
                = HelloServiceGrpc.newBlockingStub(channel)
                .withCallCredentials(token);

        HelloResponse helloResponse = stub.sayHello(HelloRequest.newBuilder()
                .setFirstName("Terry")
                .setLastName("gRPC")
                .build());
        System.out.println(helloResponse.getGreeting());
        channel.shutdown();
    }

    private static String getJwt() {
        return Jwts.builder()
                .setSubject("GreetingClient") // client's identifier
                .signWith(Constants.signKey())
                .compact();
    }
}
