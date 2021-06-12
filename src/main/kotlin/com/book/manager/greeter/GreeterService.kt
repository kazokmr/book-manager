package com.book.manager.greeter

import org.lognet.springboot.grpc.GRpcService

@GRpcService
class GreeterService : GreeterGrpcKt.GreeterCoroutineImplBase() {
    override suspend fun hello(request: HelloRequest): HelloResponse {
        return HelloResponse.newBuilder().setText("Hello ${request.name}").build()
    }
}