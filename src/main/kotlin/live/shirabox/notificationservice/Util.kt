package live.shirabox.notificationservice

class Util {
    companion object {
        fun encodeString(str: String): String {
            return str.replace(Regex("[^a-zA-Z_0-9]"), "-")
                .replace(Regex("(^[A-Z][A-Z]^[A-Z])"), "-$1")
                .replace(Regex("^-"), "").replace(Regex("-$"), "")
                .replace(Regex("([a-zA-Z])([0-9])"), "$1-$2")
                .replace(Regex("([0-9])([a-zA-Z]^[nd])"), "$1-$2")
                .replace(Regex("-{2,}"), "-")
                .lowercase()
        }
    }
}