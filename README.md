# http4s-grpc-playground

## Intent

Testing differences between fs2-grpc and http4s-grpc.

### Running

#### http4s-grpc

```shell
sbt "http4s-grpc/run"
```

#### fs2-grpc

```shell
sbt "fs2-grpc/run"
```

```text
grpcurl -plaintext \
  -proto protobuf/greeter.proto \
  -d '{"name": "world"}' \
  localhost:4770 \
  io.github.bcarter97.Greeter/SayHello
```

### TODO

- [ ] Reflection
- [ ] sbt protoc settings (e.g. flatpackage)
- [ ] Interceptors
