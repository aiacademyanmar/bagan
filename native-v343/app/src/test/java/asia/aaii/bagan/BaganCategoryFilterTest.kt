package asia.aaii.bagan

import org.junit.Assert.assertEquals
import org.junit.Test

class BaganCategoryFilterTest {
 private val sites = listOf(
  Temple("a","အာနန္ဒာဘုရား","Ananda Temple","","a.glb"),
  Temple("b","ရွှေစည်းခုံစေတီ","Shwezigon Pagoda","","b.glb"),
  Temple("c","မြစေတီကျောက်စာ","Myazedi Stone Inscription","","c.glb"),
  Temple("d","ဗူးဘုရား","Bupaya Pagoda","","d.glb")
 )
 @Test fun classificationsAreConsistent() {
  assertEquals(4,BaganCategoryFilter.count(sites,"All"))
  assertEquals(1,BaganCategoryFilter.count(sites,"Temples"))
  assertEquals(2,BaganCategoryFilter.count(sites,"Stupas"))
  assertEquals(4,BaganCategoryFilter.count(sites,"Famous"))
 }
 @Test fun unicodeAndEnglishSearch() {
  assertEquals("b",BaganCategoryFilter.matching(sites,"All","ရွှေစည်း").single().id)
  assertEquals("a",BaganCategoryFilter.matching(sites,"Temples","ANANDA").single().id)
  assertEquals(0,BaganCategoryFilter.matching(sites,"Stupas","Ananda").size)
 }
}
