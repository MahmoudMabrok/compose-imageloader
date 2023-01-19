package com.seiko.imageloader.intercept

import com.seiko.imageloader.model.ImageResult

class MappedInterceptor : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = chain.request
        val options = chain.options

        val mappedData = chain.components.map(request.data, options)
        val newRequest = request.newBuilder().data(mappedData).build()
        return chain.proceed(newRequest)
    }
}
