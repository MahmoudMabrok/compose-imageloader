package com.seiko.imageloader.component

import com.seiko.imageloader.component.decoder.Decoder
import com.seiko.imageloader.component.fetcher.Fetcher
import com.seiko.imageloader.component.keyer.Keyer
import com.seiko.imageloader.component.mapper.Mapper

class ComponentRegistryBuilder(
    private val mappers: MutableList<Mapper<out Any>> = mutableListOf(),
    private val keyers: MutableList<Keyer> = mutableListOf(),
    private val fetcherFactories: MutableList<Fetcher.Factory> = mutableListOf(),
    private val decoderFactories: MutableList<Decoder.Factory> = mutableListOf(),
) {

    fun takeFrom(
        componentRegistry: ComponentRegistry,
        clearComponents: Boolean = false,
    ) {
        componentRegistry.newBuilder().let {
            if (clearComponents) {
                mappers.clear()
                keyers.clear()
                fetcherFactories.clear()
                decoderFactories.clear()
            }
            mappers.addAll(it.mappers)
            keyers.addAll(it.keyers)
            fetcherFactories.addAll(it.fetcherFactories)
            decoderFactories.addAll(it.decoderFactories)
        }
    }

    fun add(mapper: Mapper<out Any>) {
        mappers.add(mapper)
    }

    fun add(keyer: Keyer) {
        keyers.add(keyer)
    }

    fun add(fetcherFactory: Fetcher.Factory) {
        fetcherFactories.add(fetcherFactory)
    }

    fun add(decoderFactory: Decoder.Factory) {
        decoderFactories.add(decoderFactory)
    }

    internal fun build(): ComponentRegistry = ComponentRegistry(
        mappers = mappers,
        keyers = keyers,
        fetcherFactories = fetcherFactories,
        decoderFactories = decoderFactories,
    )
}
