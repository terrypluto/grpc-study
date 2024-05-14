# gRPC

- [x] 安装protobuf
- [x] protobuf
- [x] java简单例子
- [x] java import，外部包 内部包
- [x] springboot gRPC
- [x] 安全性 Authentication
- [x] golang grpc sample

## 安装protobuf

下载地址： [protocolbuffers/protobuf: Protocol Buffers - Google's data interchange format (github.com)](https://github.com/protocolbuffers/protobuf)

- 根据自己所在系统下载对应最新版本的zip包
- 解压
- 将解压后`bin`目录配置到环境变量中

## protobuf

protobuf官网：[Language Guide (proto 3) | Protocol Buffers Documentation (protobuf.dev)](https://protobuf.dev/programming-guides/proto3/)

### 定义message type

```protobuf
syntax = "proto3";

message SearchRequest {
  string query = 1;
  int32 page_number = 2;
  int32 result_per_page = 3;
}
```

> 消息定义中的每个字段都有一个惟一的编号。这些字段号用于以消息二进制格式标识字段，一旦使用了消息类型，就不应更改它们。
>
> 编号范围1~2^19-1;19000到19999不能够使用，因为它们是为Protocol Buffers实现保留的

### 指定字段规则

- `singular`:格式良好的消息可以有零或一个此字段(但不能超过一个)。当使用proto3语法时，当没有为给定字段指定其他字段规则时，这是默认的字段规则。您无法确定它是否是从线路中解析的。除非它是默认值，否则它将被序列化到连接
- `optional`:与此相同，除了可以检查是否显式设置了该值。字段处于两种可能的状态之一
  - 字段已设置，并包含从连接中显式设置或解析的值。它将被序列化到网络。
  - 该字段未设置，将返回默认值。它不会被序列化到网络。
- `repeated`:在格式良好的消息中，此字段类型可以重复0次或多次。重复值的顺序将被保留。
- `map`:这是一个配对的键/值字段类型。有关此字段类型的更多信息。

### 添加更多消息类型

可以在单个文件中定义多种消息类型。如果您定义了多个相关的消息，这将非常有用

```protobuf
message SearchRequest {
  string query = 1;
  int32 page_number = 2;
  int32 result_per_page = 3;
}

message SearchResponse {
 ...
}
```

### 添加备注

要在文件中添加注释，请使用C/ c++风格和语法..原型/ / / *……* /

```protobuf
/* SearchRequest represents a search query, with pagination options to
 * indicate which results to include in the response. */

message SearchRequest {
  string query = 1;
  int32 page_number = 2;  // Which page number do we want?
  int32 result_per_page = 3;  // Number of results to return per page.
}
```

### 保留字段

如果您通过完全删除字段或将其注释掉来更新消息类型，那么将来的用户在对类型进行更新时可以重用字段号。如果他们后来加载相同的旧版本，这可能会导致严重的问题，包括数据损坏、隐私错误等等。确保这种情况不会发生的一种方法是指定已删除字段的字段号(和/或名称，这也可能导致JSON序列化问题)。如果将来任何用户尝试使用这些字段标识符，协议缓冲区编译器将报错

```protobuf
message Foo {
  reserved 2, 15, 9 to 11;
  reserved "foo", "bar";
}
```

> 注意，不能在同一个语句中混合字段名和字段号

### 枚举

在定义消息类型时，可能希望其中一个字段仅具有预定义值列表中的一个

```protobuf
enum Corpus {
  CORPUS_UNSPECIFIED = 0;
  CORPUS_UNIVERSAL = 1;
  CORPUS_WEB = 2;
  CORPUS_IMAGES = 3;
  CORPUS_LOCAL = 4;
  CORPUS_NEWS = 5;
  CORPUS_PRODUCTS = 6;
  CORPUS_VIDEO = 7;
}

message SearchRequest {
  string query = 1;
  int32 page_number = 2;
  int32 result_per_page = 3;
  Corpus corpus = 4;
}
```

正如您所看到的，Corpus枚举的第一个常量映射到0:每个枚举定义必须包含一个映射到0的常量作为其第一个元素。这是因为:

- 必须有一个0值，这样我们才能使用0作为数值默认值。
- 0值需要是第一个元素，以兼容proto2语义，其中第一个enum值总是默认值。

### 使用其他消息类型

您可以使用其他消息类型作为字段类型

```protobuf
message SearchResponse {
  repeated Result results = 1;
}

message Result {
  string url = 1;
  string title = 2;
  repeated string snippets = 3;
}
```

### 导入定义

在上面的示例中，Result消息类型定义在与SearchResponse相同的文件中——如果您想用作字段类型的消息类型已经定义在另一个.proto文件中，该怎么办?

可以通过导入其他.proto文件中的定义来使用它们。要导入另一个.proto的定义，你在文件的顶部添加一个import语句:

```protobuf
import "myproject/other_protos.proto";
```

默认情况下，只能使用直接导入的.proto文件中的定义。然而，有时您可能需要将.proto文件移动到新的位置。您可以在旧位置放置一个占位符的.proto文件，使用导入公共概念将所有导入转发到新位置，而不是直接移动.proto文件并在一次更改中更新所有调用站点。

**Note that the public import functionality is not available in Java.**

导入公共依赖项可以被任何导入包含导入公共语句的原型的代码传递依赖。例如:

```protobuf
// new.proto
// All definitions are moved here  所有定义都移到这里
```

```protobuf
// old.proto
// This is the proto that all clients are importing. 这是所有客户端正在导入的原型。
import public "new.proto";
import "other.proto";
```

```protobuf
// client.proto
import "old.proto";
// You use definitions from old.proto and new.proto, but not other.proto
```

### 嵌套类型

你可以在其他消息类型中定义和使用消息类型

```protobuf
message SearchResponse {
  message Result {
    string url = 1;
    string title = 2;
    repeated string snippets = 3;
  }
  repeated Result results = 1;
}
```

如果你想在其父消息类型之外重用这个消息类型，你可以将它引用为_`_Parent_._Type_`_:

```protobuf
message SomeOtherMessage {
  SearchResponse.Result result = 1;
}
```

### Any

Any消息类型允许您在没有.proto定义的情况下将消息作为嵌入式类型使用。Any包含以字节表示的任意序列化消息，以及作为该消息类型的全局唯一标识符并解析为该消息类型的URL。要使用Any类型，您需要`import` `google/protobuf/any.proto`.

```protobuf
import "google/protobuf/any.proto";

message ErrorStatus {
  string message = 1;
  repeated google.protobuf.Any details = 2;
}
```

### Maps

如果希望创建关联映射作为数据定义的一部分，协议缓冲区提供了一种方便的快捷语法

```protobuf
map<key_type, value_type> map_field = N;
```

- 映射字段不能重复。
- 线路格式排序和映射值的映射迭代排序是未定义的，因此您不能依赖于您的映射项处于特定的顺序。
- 当为.proto生成文本格式时，映射按键排序。数字键按数字排序。
- 在从连线进行解析或合并时，如果存在重复的映射键，则使用最后看到的键。当从文本格式解析映射时，如果有重复的键，解析可能会失败。
- 如果为map字段提供了键但没有值，则该字段序列化时的行为是依赖于语言的。在c++、Java、Kotlin和Python中，该类型的默认值是序列化的，而在其他语言中没有序列化。

### Packages

您可以向.proto文件添加可选的包说明符，以防止协议消息类型之间的名称冲突。

```protobuf
package foo.bar;
message Open { ... }
```

然后你可以在定义你的消息类型的字段时使用包说明符:

```protobuf
message Foo {
  ...
  foo.bar.Open open = 1;
  ...
}
```

包说明符影响生成代码的方式取决于你选择的语言:

### 定义service

如果您想在RPC(远程过程调用)系统中使用您的消息类型，您可以在.proto文件中定义RPC服务接口，协议缓冲区编译器将用您选择的语言生成服务接口代码和存根

```protobuf
service SearchService {
  rpc Search(SearchRequest) returns (SearchResponse);
}
```

与协议缓冲区一起使用的最直接的RPC系统是gRPC:一个与语言和平台无关的开源RPC系统，由谷歌开发。gRPC特别适合使用协议缓冲区，并允许您使用特殊的协议缓冲区编译器插件直接从.proto文件生成相关的RPC代码

### Options

.proto文件中的各个声明可以用许多选项进行注释。选项不会改变声明的整体含义，但可能会影响在特定上下文中处理它的方式。可用选项的完整列表定义在/谷歌/protobuf/descriptor.proto中。

有些选项是文件级选项，这意味着它们应该在顶级范围内编写，而不是在任何消息、枚举或服务定义中。有些选项是消息级选项，这意味着它们应该在消息定义中编写。有些选项是字段级选项，这意味着它们应该在字段定义中编写。选项也可以写在枚举类型、枚举值、字段之一、服务类型和服务方法上;然而，目前没有任何有用的选项。

以下是一些最常用的选项:

- `java_package`(file option):要用于生成的Java/Kotlin类的包。如果.proto文件中没有给出显式的java_package选项，那么默认情况下将使用proto包(在.proto文件中使用" package "关键字指定)。但是，原型包通常不是好的Java包，因为原型包不希望以反向域名开始。如果不生成Java或Kotlin代码，则此选项无效。

  ```protobuf
  option java_package = "com.example.foo";
  ```

- `java_outer_classname`(file option): 希望生成的包装器Java类的类名(以及文件名)。如果.proto文件中没有显式地指定java_outer_classname，则类名将通过将.proto文件名称转换为驼背格式来构造(因此foo_bar. classname将在.proto文件中使用。proto变成FooBar.java)。如果java_multiple_files选项被禁用，那么所有其他类/enum /etc。为.proto文件生成的.proto文件将在这个外部包装器Java类中生成嵌套类/enum /等。如果不生成Java代码，则此选项无效。

  ```protobuf
  option java_outer_classname = "Ponycopter";
  ```

- `optimize_for` (file option):可设置为SPEED、CODE_SIZE或LITE_RUNTIME。这将以以下方式影响c++和Java代码生成器(可能还有第三方生成器)

### 生成类

为了生成Java、Kotlin、Python、c++、Go、Ruby、Objective-C或c#代码，您需要使用.proto文件中定义的消息类型，您需要在.proto上运行协议缓冲编译器协议。如果您还没有安装编译器，请下载该包并按照README中的说明进行操作。对于Go，你还需要为编译器安装一个特殊的代码生成器插件:你可以在GitHub的golang/protobuf存储库中找到这个和安装说明。

协议编译器的调用如下:

```shell
protoc --proto_path=IMPORT_PATH --cpp_out=DST_DIR --java_out=DST_DIR --python_out=DST_DIR --go_out=DST_DIR --ruby_out=DST_DIR --objc_out=DST_DIR --csharp_out=DST_DIR path/to/file.proto
```

- `IMPORT_PATH`指定解析导入指令时查找`.proto`文件的目录。如果省略，则使用当前目录。通过多次传递`--proto_path`选项可以指定多个导入目录;他们将按顺序被搜查。`-I=_IMPORT_PATH_`可以作为`--proto_path`的缩写形式使用。
- 你可以提供一个或多个输出指令
  - `--cpp_out` generates C++ code in `DST_DIR`. See the [C++ generated code reference](https://protobuf.dev/reference/cpp/cpp-generated) for more.
  - `--java_out` generates Java code in `DST_DIR`. See the [Java generated code reference](https://protobuf.dev/reference/java/java-generated) for more.
  - `--kotlin_out` generates additional Kotlin code in `DST_DIR`. See the [Kotlin generated code reference](https://protobuf.dev/reference/kotlin/kotlin-generated) for more.
  - `--python_out` generates Python code in `DST_DIR`. See the [Python generated code reference](https://protobuf.dev/reference/python/python-generated) for more.
  - `--go_out` generates Go code in `DST_DIR`. See the [Go generated code reference](https://protobuf.dev/reference/go/go-generated) for more.
  - `--ruby_out` generates Ruby code in `DST_DIR`. See the [Ruby generated code reference](https://protobuf.dev/reference/ruby/ruby-generated) for more.
  - `--objc_out` generates Objective-C code in `DST_DIR`. See the [Objective-C generated code reference](https://protobuf.dev/reference/objective-c/objective-c-generated) for more.
  - `--csharp_out` generates C# code in `DST_DIR`. See the [C# generated code reference](https://protobuf.dev/reference/csharp/csharp-generated) for more.
  - `--php_out` generates PHP code in `DST_DIR`. See the [PHP generated code reference](https://protobuf.dev/reference/php/php-generated) for more.
- 您必须提供一个或多个`.proto`文件作为输入。可以同时指定多个`.proto`文件。尽管这些文件是相对于当前目录命名的，但每个文件必须位于`IMPORT_PATH`中的一个，以便编译器可以确定其规范名称。

## java gRPC

github: [grpc/grpc-java: The Java gRPC implementation. HTTP/2 based RPC (github.com)](https://github.com/grpc/grpc-java)

### maven dependencies

```xml
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
    <version>1.54.0</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
    <version>1.54.0</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-stub</artifactId>
    <version>1.54.0</version>
</dependency>
```

### create hello.proto

在java项目位于`src/main/proto`下创建相关`.proto`

```protobuf
syntax = "proto3";
package hello;

option java_multiple_files=true;
option java_package="org.terryliu.grpc.sample.hello";
option java_outer_classname="HelloProto";

service HelloService  {
  rpc SayHello (HelloRequest) returns (HelloResponse) {}
}

// The request message containing the user's name.
message HelloRequest {
  string firstName = 1;
  string lastName = 2;
}

// The response message containing the greetings
message HelloResponse {
  string greeting = 1;
}
```

### **Using Maven Plugin**

```xml
<build>
    <extensions>
        <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.7.1</version>
        </extension>
    </extensions>
    <plugins>
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.6.1</version>
            <configuration>
<!--                    <protoSourceRoot>src/main/proto</protoSourceRoot>-->
                <protocArtifact>
                    com.google.protobuf:protoc:3.21.7:exe:${os.detected.classifier}
                </protocArtifact>
                <pluginId>grpc-java</pluginId>
                <pluginArtifact>
                    io.grpc:protoc-gen-grpc-java:1.54.0:exe:${os.detected.classifier}
                </pluginArtifact>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 插件编译

maven -> Plugins -> protobuf

- 先运行`protobuf compile`
- 再运行`protobuf compile-custom`

编译成功后，会生成`target`文件，文件里包含`generated-sources`,`protoc-dependencies`,`protoc-plugins`

### server

在`target/generated-sources/protobuf/grpc-java`目录下，server需要继承相关grpc，并实现定义的service方法。下面是demo里定义的service

```protobuf
service HelloService  {
  rpc SayHello (HelloRequest) returns (HelloResponse) {}
}
```

java 业务类

```java
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
```

启动server

```java
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
```

### client

```java
public class GrpcClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
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
```

运行后客户端输出

```
hello gRPC Terry
```

## grpc java import

### overview

1. 创建目录`grpc`并配置grpc pom
2. 创建`module` `base-message-definition`公共`.proto`
3. 创建`module` `dependence-message-definition` import `base-message-definition`的 公共`.proto`
4. maven install

### `grpc pom`的配置

```xml
<artifactId>grpc</artifactId>
<packaging>pom</packaging>
<modules>
    <module>base-message-definition</module>
    <module>dependence-message-definition</module>
</modules>

<properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <base-path>${basedir}/../base-message-definition/src/main/proto</base-path>
</properties>

<dependencies>
    <dependency>
        <groupId>io.grpc</groupId>
        <artifactId>grpc-netty-shaded</artifactId>
        <version>1.54.0</version>
    </dependency>
    <dependency>
        <groupId>io.grpc</groupId>
        <artifactId>grpc-protobuf</artifactId>
        <version>1.54.0</version>
    </dependency>
    <dependency>
        <groupId>io.grpc</groupId>
        <artifactId>grpc-stub</artifactId>
        <version>1.54.0</version>
    </dependency>
</dependencies>

<build>
    <extensions>
        <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.7.1</version>
        </extension>
    </extensions>
    <plugins>
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.6.1</version>
            <configuration>
                <additionalProtoPathElements>
                    <additionalProtoPathElement>${base-path}</additionalProtoPathElement>
                </additionalProtoPathElements>
                <attachDescriptorSet>true</attachDescriptorSet>
                <protoSourceRoot>src/main/proto</protoSourceRoot>
                <protocArtifact>
                    com.google.protobuf:protoc:3.21.9:exe:${os.detected.classifier}
                </protocArtifact>
                <pluginId>grpc-java</pluginId>
                <pluginArtifact>
                    io.grpc:protoc-gen-grpc-java:1.54.0:exe:${os.detected.classifier}
                </pluginArtifact>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

在`protobuf-maven-plugin`里增加了一下几个配置

```xml
<additionalProtoPathElements>
    <additionalProtoPathElement>${base-path}</additionalProtoPathElement>
</additionalProtoPathElements>
<attachDescriptorSet>true</attachDescriptorSet>
<protoSourceRoot>src/main/proto</protoSourceRoot>
```

这个配置是为了此项目下`module`可以找到依赖`.proto`的位置和需要生成类的`.proto`的位置



### 配置base-message-definition

1. 在`base-message-definition`下创建`src/main/proto/terryliu/protobuf`目录

2. 将`proto`目录设置成`resource root`

3. 创建并编辑`commons.proto`文件

   ```protobuf
   syntax = "proto3";
   package terryliu.protobuf;
   
   option java_multiple_files = true;
   option java_package = "org.terryliu.grpc.commons";
   import "google/protobuf/any.proto";
   
   message GrpcCommonResult{
     int32 code = 1;
     string message = 2;
     optional google.protobuf.Any data = 3;
   }
   
   message CommonRequest{
     string params = 1;
   }
   
   message CommonResponse{
     string businessData = 1;
   }
   ```

**重要**

> 在ide创建目录最好一级一级的创建，不然其他`.porto`导入依赖，目录标识符`/`可能找不到依赖包位置，可能用到`.`来连接文件，比如其他`.proto`依赖上面创建的`commoms.proto`，一般使用`improt` `"terryliu/protobuf/commons.proto"`,如果用ide直接创建`src.main.proto.terryliu.protobuf`目录，导入依赖则需要`import` `"terryliu.protobuf/commons.proto"`

### 配置dependence-message-definition

#### maven dependence

将上面创建的jar配置到这个项目的依赖

```xml
<dependency>
    <groupId>org.terryliu</groupId>
    <artifactId>base-message-definition</artifactId>
    <version>${revision}</version>
</dependency>
```

#### 编辑.proto

1. 在`dependence-message-definition`下创建`src/main/proto/terryliu/protobuf/dependence`目录

2. 将`proto`目录设置成`resource root`

3. 创建并编辑`HelloDependence.proto`文件

   ```protobuf
   syntax = "proto3";
   package terryliu.protobuf.dependence;
   option java_multiple_files = true;
   option java_package = "org.terryliu.grpc.dependence";
   option java_outer_classname = "GrpcDependenceService";
   import "terryliu/protobuf/commons.proto";
   
   service DependenceService {
     rpc baseInfo(CommonRequest) returns (GrpcCommonResult) {
     }
   }
   ```

### maven install

准备工作准备好之后，开始`maven install`将jar打到本机仓库。这样就可以用其他项目里引入依赖，创建server或者client了

## springboot gRPC

官方文档：[gRPC-Spring-Boot-Starter 文档 | grpc-spring-boot-starter (yidongnan.github.io)](https://yidongnan.github.io/grpc-spring-boot-starter/zh-CN/)

github：[yidongnan/grpc-spring-boot-starter: Spring Boot starter module for gRPC framework. (github.com)](https://github.com/yidongnan/grpc-spring-boot-starter)

### server

#### maven dependencies

```xml
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-spring-boot-starter</artifactId>
</dependency>

<!--自己的proto项目-->
<dependency>
    <groupId>org.terryliu</groupId>
    <artifactId>grpc-sample</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 实现服务逻辑

这里用的是`java grpc`标题的例子，使用`@GrpcService`标识这是个`gRPC server`

```java
@GrpcService
public class MyServiceImpl extends HelloServiceGrpc.HelloServiceImplBase{
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        HelloResponse helloResponse = HelloResponse.newBuilder()
                .setGreeting("hello " + request.getLastName() + " "+request.getFirstName()).build();
        responseObserver.onNext(helloResponse);
        responseObserver.onCompleted();

        System.out.println("response:" + helloResponse.getGreeting());
    }
}
```

启动springboot,默认`grpc.server.port=9090`

#### 测试

使用java grpc`标题的client,将端口号改为9090。正常输出：

```
hello gRPC Terry
```

### client

#### maven dependencies

```xml
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-client-spring-boot-starter</artifactId>
</dependency>

<dependency>
    <groupId>org.terryliu</groupId>
    <artifactId>grpc-sample</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 实现调用逻辑

```java
@Service
public class HelloService {
    @GrpcClient("helloService")
    private HelloServiceGrpc.HelloServiceBlockingStub stub;

    public String receiveGreeting(String firstName,String lastName){
        HelloRequest request = HelloRequest.newBuilder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .build();

        return stub.sayHello(request).getGreeting();
    }
}
```

#### `application.yaml`

```yaml
grpc:
  client:
    hello-service:
      address: static://localhost:9090
      negotiation-type: PLAINTEXT
      enable-keep-alive: true
      keep-alive-without-calls: true
```

`hello-service`就是`@GrpcClient("helloService")`的内容

#### 测试

```java
@SpringBootTest(classes = GrpcClientSpringApplication.class)
public class GrpcSpringApplicationTest {
    @Resource
    private HelloService helloService;

    @Test
    public void test1(){
        String greeting = helloService.receiveGreeting("terry", "gRPC");
        Assertions.assertEquals("hello gRPC terry",greeting);
    }
}
```

## gRPC Authentication

官方文档：[Authentication | gRPC](https://grpc.io/docs/guides/auth/)

- [x] 支持的认证机制
- [x] openssl
  - [x] 安装
  - [x] 生成证书
- [x] 实现 **SSL/TLS**
- [x] 实现 **Token**

### 支持的认证机制

- **SSL/TLS**: gRPC集成了SSL/TLS，并促进使用SSL/TLS来验证服务器，并加密客户端和服务器之间交换的所有数据。客户端可以使用可选机制为相互身份验证提供证书。
- **ALTS**: 如果应用程序运行在谷歌云平台(GCP)上，gRPC支持ALTS作为传输安全机制。有关详细信息，请参阅以下特定于语言的页面之一:[ALTS in C++](https://grpc.io/docs/languages/cpp/alts/), [ALTS in Go](https://grpc.io/docs/languages/go/alts/), [ALTS in Java](https://grpc.io/docs/languages/java/alts/), [ALTS in Python](https://grpc.io/docs/languages/python/alts/).
- **Token**: gRPC提供了一种通用机制(如下所述)，将基于元数据的凭据附加到请求和响应。在通过gRPC访问谷歌api时，为某些认证流提供了获取访问令牌(通常是OAuth2令牌)的额外支持:您可以在下面的代码示例中看到这是如何工作的。一般来说，这种机制必须和通道上的SSL/TLS一样使用——谷歌不允许没有SSL/TLS的连接，大多数gRPC语言实现不允许在未加密的通道上发送凭据。

### openssl

官网：[openssl (openssl.org)](https://www.openssl.org/)

#### 安装

别人制作的安装包：[Win32/Win64 OpenSSL Installer for Windows - Shining Light Productions (slproweb.com)](https://slproweb.com/products/Win32OpenSSL.html)

我下载的版本`Win64 OpenSSL v3.1.0`，下载好进行以下步骤：

1. 安装`.exe`文件

2. 安装好后将安装目录下的`bin`目录配置到环境变量中，最好将环境变量放到第一行，安装的`git`有`openssl`，可在`cmd`中输入命令验证。在编辑器中的`Terminal`使用`cmd`，用`git.exe`会出错

   ```
   openssl version  #OpenSSL 3.1.0 14 Mar 2023 (Library: OpenSSL 3.1.0 14 Mar 2023)
   ```

#### 生成证书

在项目中创建文件`openssl`，进入到这个目录下，进行以下步骤：

1. 生成CA证书

   ```shell
   #生成RSA私钥(无加密)
   openssl genrsa -out ca.key 2048
   #使用 已有RSA 私钥生成自签名证书
   openssl req -new -x509 -key ca.key -days 3650 -out ca.crt
   Country Name (2 letter code) [AU]:CN
   State or Province Name (full name) [Some-State]:anhui
   Locality Name (eg, city) []:hefei
   Organization Name (eg, company) [Internet Widgits Pty Ltd]:terry CO. Ltd
   Organizational Unit Name (eg, section) []:Dev
   Common Name (e.g. server FQDN or YOUR name) []:java-grpc-tls
   Email Address []:taiyueliu@126.com
   ```

2. 复制刚安装的openssl bin目录下的openssl.cfg到项目中

3. 编辑openssl.cfg

   ```
   #开启
   copy_extensions = copy
   req_extensions = v3_req
   #找到[ v3_req ] 添加
   subjectAltName = @alt_names
   #添加alt_names
   [ alt_names ]
   DNS.1 = *.terryliu.com
   ```

4. 生成私钥和公钥

   ```shell
   openssl genpkey -algorithm RSA -out test.key
   openssl req -new -nodes -key test.key -out test.csr -subj "/C=cn/OU=java/O=terry/CN=terryliu" -config ./openssl.cfg -extensions v3_req
   openssl x509 -req -days 365 -in test.csr -out test.pem -CA ca.crt -CAkey ca.key -CAcreateserial -extfile ./openssl.cfg -extensions v3_req
   ```

以上生成的证书，需要用到`test.key`,`test.pem`

#### SSL/TLS

##### maven dependencies

```xml
<dependency>
    <groupId>org.terryliu</groupId>
    <artifactId>grpc-sample</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-stub</artifactId>
</dependency>
```

##### server

```java
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
```

##### client

```java
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
```

#### token

##### maven dependencies

在上面例子的基础上增加jwt依赖

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId> <!-- or jjwt-gson if Gson is preferred -->
</dependency>
```

##### Constants

```java
public class Constants {
    public static final String BEARER_TYPE = "Bearer";

    public static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER);
    public static final Context.Key<String> CLIENT_ID_CONTEXT_KEY = Context.key("clientId");

    private Constants() {
        throw new AssertionError();
    }

    public static Key signKey(){
        String encode = "dC8ziH16pGnQu7Sl6HwgFnQpBtE5YnnBSEBkGIs/uN8AYi87Mphnob2kvlQte5WenkFjzNJGaZMYdisBe74OrQ==";
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(encode));
    }
}
```

##### server

- 继承`ServerInterceptor`，并实现自己的token验证逻辑

  ```java
  public class AuthorizationServerInterceptor implements ServerInterceptor {
      private final JwtParser parser = Jwts.parserBuilder().setSigningKey(Constants.signKey()).build();
      @Override
      public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
          String value = metadata.get(Constants.AUTHORIZATION_METADATA_KEY);
          Status status;
          if (value == null) {
              status = Status.UNAUTHENTICATED.withDescription("Authorization token is missing");
          } else if (!value.startsWith(Constants.BEARER_TYPE)) {
              status = Status.UNAUTHENTICATED.withDescription("Unknown authorization type");
          } else {
              try {
                  String token = value.substring(Constants.BEARER_TYPE.length()).trim();
                  Jws<Claims> claims = parser.parseClaimsJws(token);
                  Context ctx = Context.current().withValue(Constants.CLIENT_ID_CONTEXT_KEY, claims.getBody().getSubject());
                  return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);
              } catch (Exception e) {
                  status = Status.UNAUTHENTICATED.withDescription(e.getMessage()).withCause(e);
              }
          }
  
          serverCall.close(status, metadata);
          return new ServerCall.Listener<>() {
              // noop
          };
      }
  }
  ```

- 启动类,增加intercept

  ```java
  public class GrpcServer {
      public static void main(String[] args) throws IOException, InterruptedException {
          Server server = ServerBuilder.forPort(8090)
                  .intercept(new AuthorizationServerInterceptor())
                  .addService(new HelloServiceImpl())
                  .build().start();
          System.out.println("Server started, listening on 8090");
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
  ```

#### client

- 继承`CallCredentials`,实现token的传输

  ```java
  public class BearerToken extends CallCredentials {
      private String value;
  
      public BearerToken(String value) {
          this.value = value;
      }
      @Override
      public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
          executor.execute(() -> {
              try {
                  Metadata headers = new Metadata();
                  headers.put(Constants.AUTHORIZATION_METADATA_KEY, String.format("%s %s", Constants.BEARER_TYPE, value));
                  metadataApplier.apply(headers);
              } catch (Throwable e) {
                  metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e));
              }
          });
      }
  
      @Override
      public void thisUsesUnstableApi() {
  
      }
  }
  ```

- 启动类，添加withCallCredentials

  ```java
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
  ```

## golang gRPC

- [x] 简单例子
- [x] ssl/tls
- [x] token

### 安装插件

```go
$ go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
$ go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
```

### 创建go项目

```go
go mod init terryliu/grpc
```

为项目添加依赖

```go
go get google.golang.org/grpc
```

### proto

1. 创建proto目录

2. 创建hello.proto

   ```protobuf
   syntax = "proto3";
   package proto;
   
   option go_package = ".;proto";
   
   service HelloService  {
     rpc SayHello (HelloRequest) returns (HelloResponse) {}
   }
   
   // The request message containing the user's name.
   message HelloRequest {
     string firstName = 1;
     string lastName = 2;
   }
   
   // The response message containing the greetings
   message HelloResponse {
     string greeting = 1;
   }
   ```

3. 进入proto目录，运行protoc

   ```shell
   protoc --go_out=. --go_opt=paths=. --go-grpc_out=. hello.proto
   ```

在proto目录下就会创建`hello.pb.go`与 `hello_grpc.pb.go`

### Simple example

#### server

```go
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
```

#### client

```go
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
```

### ssl/tls

#### server

```go
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
```

#### client

```go
package main

import (
	"context"
	"fmt"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"log"
	pb "terryliu/grpc/proto"
)

func main() {
	var pem = "D:\\workspace\\mine\\grpc-study\\openssl\\test.pem"
	crews, _ := credentials.NewClientTLSFromFile(pem, "r.terryliu.com")
	conn, err := grpc.Dial("127.0.0.1:9090", grpc.WithTransportCredentials(crews))
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
```

### token

#### interface与jwt token

创建token.go，需要实现grpc的接口

```go
type PerRPCCredentials interface {
	GetRequestMetadata(ctx context.Context, uri ...string) (map[string]string, error)
	RequireTransportSecurity() bool
}
```

具体代码

```go
package token

import (
	"context"
	"fmt"
	"github.com/dgrijalva/jwt-go"
	"google.golang.org/grpc/metadata"
	"time"
)

func CreateToken(userName string) (tokenString string) {
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.MapClaims{
		"iss":      "lora-app-server",
		"aud":      "lora-app-server",
		"nbf":      time.Now().Unix(),
		"exp":      time.Now().Add(time.Hour).Unix(),
		"sub":      "user",
		"username": userName,
	})
	tokenString, err := token.SignedString([]byte("verysecret"))
	if err != nil {
		panic(err)
	}
	return tokenString
}

// AuthToken 自定义认证
type AuthToken struct {
	Token string
}

func (c AuthToken) GetRequestMetadata(ctx context.Context, uri ...string) (map[string]string, error) {
	return map[string]string{
		"authorization": c.Token,
	}, nil
}

func (c AuthToken) RequireTransportSecurity() bool {
	return false
}

// Claims defines the struct containing the token claims.
type Claims struct {
	jwt.StandardClaims

	// Username defines the identity of the user.
	Username string `json:"username"`
}

// Step1. 从 context 的 metadata 中，取出 token

func getTokenFromContext(ctx context.Context) (string, error) {
	md, ok := metadata.FromIncomingContext(ctx)
	if !ok {
		return "", fmt.Errorf("ErrNoMetadataInContext")
	}
	// md 的类型是 type MD map[string][]string
	token, ok := md["authorization"]
	if !ok || len(token) == 0 {
		return "", fmt.Errorf("ErrNoAuthorizationInMetadata")
	}
	// 因此，token 是一个字符串数组，我们只用了 token[0]
	return token[0], nil
}

func CheckAuth(ctx context.Context) (username string) {
	tokenStr, err := getTokenFromContext(ctx)
	if err != nil {
		panic("get token from context error")
	}
	var clientClaims Claims
	token, err := jwt.ParseWithClaims(tokenStr, &clientClaims, func(token *jwt.Token) (interface{}, error) {
		if token.Header["alg"] != "HS256" {
			panic("ErrInvalidAlgorithm")
		}
		return []byte("verysecret"), nil
	})
	if err != nil {
		panic("jwt parse error")
	}

	if !token.Valid {
		panic("ErrInvalidToken")
	}

	return clientClaims.Username
}
```

#### server

```go
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
```

#### client

```go
package main

import (
	"context"
	"fmt"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"log"
	pb "terryliu/grpc/proto"
	"terryliu/grpc/token/token"
)

func main() {

	t := new(token.AuthToken)
	tokenString := token.CreateToken("test")
	t.Token = tokenString

	conn, err := grpc.Dial("127.0.0.1:9090", grpc.WithTransportCredentials(insecure.NewCredentials()),
		grpc.WithPerRPCCredentials(t))
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
```

