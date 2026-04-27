# http4s-grpc-playground

## Intent

Testing differences between fs2-grpc and http4s-grpc.

### Running

#### fs2-grpc

```shell
sbt "fs2-grpc/run"
```

```text
grpcurl -plaintext \
  -d '{"name": "world"}' \
  localhost:4770 \
  io.github.bcarter97.Greeter/SayHello
```

#### http4s-grpc

```shell
sbt "http4s-grpc/run"
```

> [!NOTE]
>
> No reflection, needs proto path

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
