package org.terryliu.grpc.tls.client;

import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import org.terryliu.grpc.sample.hello.HelloRequest;
import org.terryliu.grpc.sample.hello.HelloResponse;
import org.terryliu.grpc.sample.hello.HelloServiceGrpc;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2023/4/13
 * @description todo
 */
public class GrpcClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        String pem = "D:\\workspace\\mine\\grpc-study\\openssl\\test.pem";
//        String pem = "D:\\workspace\\mine\\grpc-study\\grpc-authentication\\ca\\server.pem";
        SslContextBuilder builder = GrpcSslContexts.forClient();
        SslContext sslContext = builder
                .trustManager(new File(pem))
                .build();
        ManagedChannel channel = NettyChannelBuilder.forTarget("localhost:9090")
                .negotiationType(NegotiationType.TLS)
                .overrideAuthority("r.terryliu.com")
//                .overrideAuthority("java-grpc-tls")
                .sslContext(sslContext)
                .build();

        HelloServiceGrpc.HelloServiceBlockingStub stub
                = HelloServiceGrpc.newBlockingStub(channel);

        HelloResponse helloResponse = stub.sayHello(HelloRequest.newBuilder()
                .setFirstName("Terry")
                .setLastName("gRPC")
                .build());
        System.out.println(helloResponse.getGreeting());
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
}
