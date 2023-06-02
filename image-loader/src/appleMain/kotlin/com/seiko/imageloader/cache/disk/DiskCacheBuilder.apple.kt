package com.seiko.imageloader.cache.disk

import okio.Path
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSystemFreeSize
import platform.Foundation.NSNumber

internal actual fun directorySize(directory: Path): Long {
    val fileAttributes = NSFileManager.defaultManager.attributesOfFileSystemForPath(directory.toString(), null)
    val number = fileAttributes?.get(NSFileSystemFreeSize) as? NSNumber
    if (number != null) {
        return number.integerValue
    }
    return 512L * 1024 * 1024 // 512MB
}
