package com.seiko.imageloader.component.keyer

import com.seiko.imageloader.option.Options
import com.seiko.imageloader.option.Scale
import com.seiko.imageloader.util.DEFAULT_MAX_IMAGE_SIZE
import io.ktor.http.Url

class KtorUrlKeyer : Keyer {
    override fun key(data: Any, options: Options, type: Keyer.Type): String? {
        if (data !is Url) return null
        val playAnimeSuffix = when (type) {
            Keyer.Type.Memory -> memorySuffix(options)
            Keyer.Type.Disk -> ""
        }
        return data.toString() + playAnimeSuffix
    }

    private fun memorySuffix(options: Options): String {
        return buildString {
            if (options.allowInexactSize) {
                append("-allowInexactSize")
            }
            if (!options.premultipliedAlpha) {
                append("-premultipliedAlpha")
            }
            if (options.imageConfig != Options.ImageConfig.ARGB_8888) {
                append("-imageConfig=${options.imageConfig}")
            }
            if (options.scale != Scale.FILL) {
                append("-scale=fit")
            }
            if (!options.playAnimate) {
                append("-noPlay")
            }
            if (options.maxImageSize != DEFAULT_MAX_IMAGE_SIZE) {
                append("-maxSize${options.maxImageSize}")
            }
        }
    }
}
