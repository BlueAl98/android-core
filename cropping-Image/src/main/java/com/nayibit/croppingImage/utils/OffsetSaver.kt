package com.nayibit.croppingImage.utils

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.geometry.Offset

// Offset has no built-in Saver, so rememberSaveable can't persist it without this.
internal val OffsetSaver: Saver<Offset, Any> = listSaver(
    save = { offset -> listOf(offset.x, offset.y) },
    restore = { saved -> Offset(x = saved[0], y = saved[1]) }
)
