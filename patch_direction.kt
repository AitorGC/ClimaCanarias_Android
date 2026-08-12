fun getWindDirectionCode(degrees: Double?): String {
    if (degrees == null) return "-"
    val normalized = ((degrees % 360) + 360) % 360
    return when (normalized) {
        in 337.5..360.0, in 0.0..22.5 -> "N"
        in 22.5..67.5 -> "NE"
        in 67.5..112.5 -> "E"
        in 112.5..157.5 -> "SE"
        in 157.5..202.5 -> "S"
        in 202.5..247.5 -> "SO"
        in 247.5..292.5 -> "O"
        in 292.5..337.5 -> "NO"
        else -> "-"
    }
}
