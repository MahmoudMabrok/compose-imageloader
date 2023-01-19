package com.seiko.imageloader.component.mapper

import com.eygraber.uri.Uri
import com.seiko.imageloader.option.Options
import com.seiko.imageloader.util.firstPathSegment
import com.seiko.imageloader.util.logv
import java.io.File

class FileUriMapper : Mapper<File> {
    override fun map(data: Any, options: Options): File? {
        if (data !is Uri) return null
        if (!isApplicable(data)) return null
        return File(data.path).also {
            logv(
                tag = "FileUriMapper",
                data = data,
            ) { "mapper to File" }
        }
    }

    private fun isApplicable(data: Uri): Boolean {
        return data.scheme.let { it == null || it == "file" } &&
            data.path.orEmpty().startsWith('/') && data.firstPathSegment != null
    }
}
