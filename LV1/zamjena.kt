fun calculateSumOfDigits(value: Int): Int {
    var sumOfDigits = 0
    var valueToString = value.toString()
    for(i in valueToString) {
        sumOfDigits += i.toString().toInt()
    }
    return sumOfDigits
}

fun minElement(input: Array<Int>): Int {
    var min = input[0]
    var min1 = 0
    for(num in input) {
        min1 = calculateSumOfDigits(num)
        if(min1 < min && min1 != 0) {
            min = min1
        }
    }

    return min
}


fun main() {
    val nums = arrayOf(999,19,199)
    val rez= minElement(nums)
    println("Najmanja vrijednost u vektoru koji je nastao sabiranjem cifara: $rez")
}