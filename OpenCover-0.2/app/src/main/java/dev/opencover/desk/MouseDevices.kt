package dev.opencover.desk

/** getevent queries device capabilities directly; Samsung blocks the equivalent sysfs files. */
internal object MouseDevices {
    fun find(listing: String): String? = all(listing).firstOrNull()

    fun all(listing: String): List<String> {
        val paths = mutableListOf<String>()
        val devices = listing.split(Regex("(?m)(?=^add device )"))
        for (device in devices) {
            val path = Regex("^add device [0-9]+: (/dev/input/event[0-9]+)\r?\n").find(device)?.groupValues?.get(1) ?: continue
            val capabilities = device.lineSequence().filterNot { it.trimStart().startsWith("name:") }.joinToString("\n")
            if (listOf("REL_X", "REL_Y", "BTN_MOUSE").all { Regex("\\b$it\\b").containsMatchIn(capabilities) }) paths.add(path)
        }
        return paths.distinct()
    }
}
