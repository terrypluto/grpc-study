package org.terryliu.grpc.sample.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2023/4/12
 * @description todo
 */
public class GrpcServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8080)
                .addService(new HelloServiceImpl())
                .build();
        server.start();
        System.out.println("Server started, listening on 8080");
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
