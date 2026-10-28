package asia.aaii.bagan

/**
 * A single source of truth for the Categories page, Pagodas list and count badges.
 * "Famous" is currently the first six editorial picks in catalog order.
 * Categories based on the English name are a temporary classification pending
 * verified per-site category metadata.
 */
object BaganCategoryFilter {
    fun matching(temples: List<Temple>, category: String, search: String = ""): List<Temple> {
        val chosen = when (category) {
            "Famous" -> temples.take(6)
            "Temples" -> temples.filter { it.nameEn.contains("temple", ignoreCase = true) }
            "Stupas" -> temples.filter {
                it.nameEn.contains("pagoda", ignoreCase = true) ||
                    it.nameEn.contains("stupa", ignoreCase = true)
            }
            else -> temples
        }
        val needle = search.trim()
        return if (needle.isEmpty()) chosen else chosen.filter {
            it.nameMm.contains(needle, ignoreCase = true) ||
                it.nameEn.contains(needle, ignoreCase = true)
        }
    }

    fun count(temples: List<Temple>, category: String): Int =
        matching(temples, category).size
}
