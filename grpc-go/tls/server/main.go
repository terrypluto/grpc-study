package main

import (
	"context"
	"fmt"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"net"
	pb "terryliu/grpc/proto"
)

type server struct {
	pb.UnimplementedHelloServiceServer
}

func (s *server) SayHello(ctx context.Context, req *pb.HelloRequest) (*pb.HelloResponse, error) {
	return &pb.HelloResponse{Greeting: "hello " + req.FirstName + " " + req.LastName}, nil
}

func main() {
	var key, pem = "D:\\workspace\\mine\\grpc-study\\openssl\\test.key", "D:\\workspace\\mine\\grpc-study\\openssl\\test.pem"
	crews, _ := credentials.NewServerTLSFromFile(pem, key)
	//开启端口
	listen, _ := net.Listen("tcp", ":9090")
	//创建服务
	newServer := grpc.NewServer(grpc.Creds(crews))

	pb.RegisterHelloServiceServer(newServer, &server{})

	err := newServer.Serve(listen)

	if err != nil {
		fmt.Printf("failed to server : %v\n", err)
		return
	}

}
