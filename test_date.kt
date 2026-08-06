import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun main() {
    val inicio = "11:00 08-08-2026"
    val fin = "19:59 08-08-2026"
    val sdf = SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault())
    val startDate = sdf.parse(inicio)
    val endDate = sdf.parse(fin)
    val now = Date()
    
    val isActive = (startDate != null && endDate != null && !now.before(startDate) && !now.after(endDate))
    println(isActive)
}
