package main

import (
	"context"
	"fmt"
	"google.golang.org/grpc"
	"net"
	pb "terryliu/grpc/proto"
	td "terryliu/grpc/token/token"
)

type server struct {
	pb.UnimplementedHelloServiceServer
}

func (s *server) SayHello(ctx context.Context, req *pb.HelloRequest) (*pb.HelloResponse, error) {
	return &pb.HelloResponse{Greeting: "hello " + req.FirstName + " " + req.LastName}, nil
}

func AuthorizationServerInterceptor(ctx context.Context, req interface{}, info *grpc.UnaryServerInfo,
	handler grpc.UnaryHandler) (interface{}, error) {
	fmt.Printf("gRPC method: %s, %v", info.FullMethod, req)
	username := td.CheckAuth(ctx)
	if len(username) == 0 {
		panic("username is empty")
	}
	resp, err := handler(ctx, req)
	fmt.Printf("gRPC method: %s, %v", info.FullMethod, resp)
	return resp, err
}

func main() {
	//开启端口
	listen, _ := net.Listen("tcp", ":9090")
	//创建服务
	newServer := grpc.NewServer(grpc.UnaryInterceptor(AuthorizationServerInterceptor))

	pb.RegisterHelloServiceServer(newServer, &server{})

	err := newServer.Serve(listen)

	if err != nil {
		fmt.Printf("failed to server : %v\n", err)
		return
	}

}
