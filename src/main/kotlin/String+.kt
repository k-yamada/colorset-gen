import java.util.*

val String.firstLowerCased: String
    get() {
        return when (this.length) {
            0 -> ""
            1 -> this.lowercase(Locale.getDefault())
            else -> this[0].lowercaseChar() + this.substring(1)
        }
    }
