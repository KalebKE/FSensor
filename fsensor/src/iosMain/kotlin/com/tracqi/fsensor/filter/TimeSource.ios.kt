package com.tracqi.fsensor.filter

import platform.Foundation.NSProcessInfo

actual fun currentTimeNanos(): Long =
    (NSProcessInfo.processInfo.systemUptime * 1_000_000_000.0).toLong()
