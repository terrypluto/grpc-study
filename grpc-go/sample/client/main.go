package main

import (
	"context"
	"fmt"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"log"
	pb "terryliu/grpc/proto"
)

func main() {
	conn, err := grpc.Dial("127.0.0.1:9090", grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Fatalf("did not connect: %v", err)
	}

	defer func(conn *grpc.ClientConn) {
		err := conn.Close()
		if err != nil {
			log.Fatalf("did not connect: %v", err)
		}
	}(conn)

	client := pb.NewHelloServiceClient(conn)

	resp, _ := client.SayHello(context.Background(), &pb.HelloRequest{FirstName: "gRPC", LastName: "terryliu"})

	fmt.Println(resp.GetGreeting())
}
