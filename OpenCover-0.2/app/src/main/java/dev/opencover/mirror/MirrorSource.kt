package dev.opencover.mirror

/** A snapshot only: display IDs are checked again when the session starts. */
data class MirrorSource(val id: Int, val name: String, val internal: Boolean)

fun mirrorSources(displays: List<MirrorSource>): List<MirrorSource> = displays.filter {
    it.id == 0 || (it.id > 1 && !it.internal && it.name != "OpenCover-mirror")
}

fun selectedMirrorSource(displays: List<MirrorSource>, id: Int): MirrorSource? =
    mirrorSources(displays).firstOrNull { it.id == id }
