package com.book.manager.greeter

//@GRpcService
class GreeterService : GreeterGrpcKt.GreeterCoroutineImplBase() {
    override suspend fun hello(request: HelloRequest): HelloResponse {
        return HelloResponse.newBuilder().setText("Hello ${request.name}").build()
    }
}
