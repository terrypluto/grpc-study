package main

import (
	"context"
	"fmt"
	"google.golang.org/grpc"
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
	//开启端口
	listen, _ := net.Listen("tcp", ":9090")
	//创建服务
	newServer := grpc.NewServer()

	pb.RegisterHelloServiceServer(newServer, &server{})

	err := newServer.Serve(listen)

	if err != nil {
		fmt.Printf("failed to server : %v\n", err)
		return
	}

}
