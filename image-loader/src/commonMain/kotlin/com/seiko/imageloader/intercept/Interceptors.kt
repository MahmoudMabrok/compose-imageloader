package com.seiko.imageloader.intercept

import com.seiko.imageloader.Bitmap
import com.seiko.imageloader.cache.disk.DiskCache
import com.seiko.imageloader.cache.disk.DiskCacheBuilder
import com.seiko.imageloader.cache.memory.MemoryCache
import com.seiko.imageloader.cache.memory.MemoryCacheBuilder
import com.seiko.imageloader.cache.memory.MemoryKey
import com.seiko.imageloader.identityHashCode
import com.seiko.imageloader.model.ImageResult
import com.seiko.imageloader.size
import com.seiko.imageloader.util.defaultFileSystem
import com.seiko.imageloader.util.forEachIndices
import okio.FileSystem

internal typealias Interceptors = List<Interceptor>

class InterceptorsBuilder internal constructor() {

    private val interceptors = mutableListOf<Interceptor>()
    private val memoryCaches = mutableListOf<MemoryCacheWrapper<*>>()
    private var diskCache: (() -> DiskCache)? = null

    var useDefaultInterceptors = true

    fun addInterceptor(block: suspend (chain: Interceptor.Chain) -> ImageResult) {
        interceptors.add(Interceptor(block))
    }

    fun addInterceptor(interceptor: Interceptor) {
        interceptors.add(interceptor)
    }

    fun addInterceptors(interceptors: Collection<Interceptor>) {
        this.interceptors.addAll(interceptors)
    }

    fun memoryCacheConfig(
        valueHashProvider: (Bitmap) -> Int = { it.identityHashCode },
        valueSizeProvider: (Bitmap) -> Int = { it.size },
        block: MemoryCacheBuilder<MemoryKey, Bitmap>.() -> Unit,
    ) {
        memoryCache(
            block = {
                MemoryCache(
                    valueHashProvider = valueHashProvider,
                    valueSizeProvider = valueSizeProvider,
                    block = block,
                )
            },
        )
    }

    fun memoryCache(
        mapToMemoryValue: (ImageResult) -> Bitmap? = {
            if (it is ImageResult.Bitmap) {
                it.bitmap
            } else {
                null
            }
        },
        mapToImageResult: (Bitmap) -> ImageResult? = {
            ImageResult.Bitmap(it)
        },
        block: () -> MemoryCache<MemoryKey, Bitmap>,
    ) {
        memoryCaches.add(
            MemoryCacheWrapper(
                memoryCache = block,
                mapToMemoryValue = mapToMemoryValue,
                mapToImageResult = mapToImageResult,
            ),
        )
    }

    fun <T : Any> anyMemoryCacheConfig(
        valueHashProvider: (T) -> Int,
        valueSizeProvider: (T) -> Int,
        mapToMemoryValue: (ImageResult) -> T?,
        mapToImageResult: (T) -> ImageResult?,
        block: MemoryCacheBuilder<MemoryKey, T>.() -> Unit,
    ) {
        anyMemoryCache(
            mapToMemoryValue = mapToMemoryValue,
            mapToImageResult = mapToImageResult,
            block = {
                MemoryCache(
                    valueHashProvider = valueHashProvider,
                    valueSizeProvider = valueSizeProvider,
                    block = block,
                )
            },
        )
    }

    fun <T : Any> anyMemoryCache(
        mapToMemoryValue: (ImageResult) -> T?,
        mapToImageResult: (T) -> ImageResult?,
        block: () -> MemoryCache<MemoryKey, T>,
    ) {
        memoryCaches.add(
            MemoryCacheWrapper(
                memoryCache = block,
                mapToMemoryValue = mapToMemoryValue,
                mapToImageResult = mapToImageResult,
            ),
        )
    }

    fun diskCacheConfig(
        fileSystem: FileSystem? = defaultFileSystem,
        block: DiskCacheBuilder.() -> Unit,
    ) {
        if (fileSystem != null) {
            diskCache = { DiskCache(fileSystem, block) }
        }
    }

    fun diskCache(block: () -> DiskCache) {
        diskCache = block
    }

    internal fun build(): Interceptors {
        return if (useDefaultInterceptors) {
            interceptors + buildList {
                add(MappedInterceptor())
                memoryCaches.forEachIndices { wrapper ->
                    add(wrapper.toInterceptor())
                }
                add(DecodeInterceptor())
                diskCache?.let {
                    add(DiskCacheInterceptor(it))
                }
                add(FetchInterceptor())
            }
        } else {
            interceptors
        }
    }

    private class MemoryCacheWrapper<T>(
        val memoryCache: () -> MemoryCache<MemoryKey, T>,
        val mapToMemoryValue: (ImageResult) -> T?,
        val mapToImageResult: (T) -> ImageResult?,
    ) {
        fun toInterceptor() = MemoryCacheInterceptor(
            memoryCache = memoryCache,
            mapToMemoryValue = mapToMemoryValue,
            mapToImageResult = mapToImageResult,
        )
    }
}
