fun getSneakyNumbers(nums: IntArray): IntArray {
    var seen = mutableListOf<Int>()
    var result = mutableListOf<Int>()

    for(num in nums) {
        if(seen.contains(num)) {
            result.add(num)
        }else {
            seen.add(num)
        }
    }
    return result.toIntArray()
}

fun main() {

    val nums1 = intArrayOf(0,1,1,0)
    val nums2 = intArrayOf(0,3,2,1,3,2)
    println(getSneakyNumbers(nums1).joinToString())
    println(getSneakyNumbers(nums2).joinToString())
}
