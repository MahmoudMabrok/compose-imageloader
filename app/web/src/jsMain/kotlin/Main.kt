import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.cache.memory.maxSizePercent
import com.seiko.imageloader.component.setupDefaultComponents
import com.seiko.imageloader.demo.App
import com.seiko.imageloader.demo.util.LocalResLoader
import com.seiko.imageloader.demo.util.ResLoader
import com.seiko.imageloader.demo.util.commonConfig
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        CanvasBasedWindow("ComposeImageLoader") {
            CompositionLocalProvider(
                LocalImageLoader provides generateImageLoader(),
                LocalResLoader provides ResLoader(),
            ) {
                App()
            }
        }
    }
}

private fun generateImageLoader(): ImageLoader {
    return ImageLoader {
        commonConfig()
        components {
            setupDefaultComponents(imageScope)
        }
        interceptor {
            memoryCacheConfig {
                // Set the max size to 25% of the app's available memory.
                maxSizePercent(0.25)
            }
            // diskCacheConfig {
            //     directory(getCacheDir().toPath().resolve("image_cache"))
            //     maxSizeBytes(512L * 1024 * 1024) // 512MB
            // }
        }
    }
}
