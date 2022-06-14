package com.seiko.imageloader

import androidx.compose.runtime.Immutable
import com.seiko.imageloader.component.ComponentRegistry
import com.seiko.imageloader.intercept.CacheInterceptor
import com.seiko.imageloader.intercept.EngineInterceptor
import com.seiko.imageloader.intercept.Interceptor
import com.seiko.imageloader.intercept.MapDataInterceptor
import com.seiko.imageloader.intercept.RealInterceptorChain
import com.seiko.imageloader.request.ErrorResult
import com.seiko.imageloader.request.ImageRequest
import com.seiko.imageloader.request.ImageResult
import com.seiko.imageloader.request.Options
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Immutable
interface ImageLoader {
    suspend fun execute(request: ImageRequest): ImageResult
}

@Immutable
class RealImageLoader(
    private val components: ComponentRegistry,
    private val options: Options,
    private val requestDispatcher: CoroutineDispatcher,
    interceptors: List<Interceptor>,
) : ImageLoader {

    private val interceptors = listOf(
        MapDataInterceptor(),
        CacheInterceptor(),
        EngineInterceptor(this),
    ) + interceptors

    override suspend fun execute(request: ImageRequest): ImageResult {
        return withContext(requestDispatcher) {
            executeMain(request)
        }
    }

    private suspend fun executeMain(initialRequest: ImageRequest): ImageResult {
        val request = initialRequest.newBuilder().build()
        return try {
            RealInterceptorChain(
                initialRequest = initialRequest,
                interceptors = interceptors,
                index = 0,
                components = components,
                options = options,
                request = request,
            ).proceed(request)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            } else {
                ErrorResult(
                    request = initialRequest,
                    error = throwable,
                )
            }
        }
    }
}
