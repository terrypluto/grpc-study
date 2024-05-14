package org.terryliu.grpc.tls.server;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2023/4/13
 * @description todo
 */
public class GrpcServer {


    private final String host;
    private final int port;
    private final String certChainFilePath;
    private final String privateKeyFilePath;

    public GrpcServer(String host, int port, String certChainFilePath, String privateKeyFilePath) {
        this.host = host;
        this.port = port;
        this.certChainFilePath = certChainFilePath;
        this.privateKeyFilePath = privateKeyFilePath;
    }

    private SslContextBuilder getSslContextBuilder() {
        SslContextBuilder sslClientContextBuilder = SslContextBuilder.forServer(new File(certChainFilePath),
                new File(privateKeyFilePath));
        return GrpcSslContexts.configure(sslClientContextBuilder,
                SslProvider.OPENSSL);
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        String key = "D:\\workspace\\mine\\grpc-study\\openssl\\test.key";
//        String key = "D:\\workspace\\mine\\grpc-study\\grpc-authentication\\ca\\server.key";
        String pem = "D:\\workspace\\mine\\grpc-study\\openssl\\test.pem";
//        String pem = "D:\\workspace\\mine\\grpc-study\\grpc-authentication\\ca\\server.pem";

        GrpcServer grpcServer = new GrpcServer("localhost", 9090, pem,key);
        Server server = NettyServerBuilder.forAddress(new InetSocketAddress(grpcServer.host,grpcServer.port))
                .addService(new HelloServiceImpl())
                .sslContext(GrpcSslContexts.configure(grpcServer.getSslContextBuilder()).build())
                .build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){

                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                server.shutdown();
                System.err.println("*** server shut down");
            }
        });
        server.awaitTermination();
    }
}
