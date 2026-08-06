fun main() {
    val summary = "Aviso de temperatura máxima de nivel amarillo de 11:00 08-08-2026 WEST (UTC+1) a 19:59 08-08-2026 WEST (UTC+1)."
    val regex = "de (\\d{2}:\\d{2} \\d{2}-\\d{2}-\\d{4}) .*? a (\\d{2}:\\d{2} \\d{2}-\\d{2}-\\d{4})".toRegex()
    val match = regex.find(summary)
    if (match != null) {
        println("Start: ${match.groupValues[1]}")
        println("End: ${match.groupValues[2]}")
    } else {
        println("No match")
    }
}
