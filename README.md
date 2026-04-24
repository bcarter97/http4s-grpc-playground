# http4s-grpc-playground

## Intent

Testing differences between fs2-grpc and http4s-grpc.

### Running

```text
grpcurl -plaintext \
  -import-path src/main/protobuf \
  -proto greeter.proto \
  -d '{"name": "world"}' \
  localhost:4770 \
  io.github.bcarter97.Greeter/SayHello
```

### TODO

- [ ] Reflection
- [ ] sbt protoc settings (e.g. flatpackage)
- [ ] Interceptors
