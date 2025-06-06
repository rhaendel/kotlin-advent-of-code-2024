import de.ronny_h.extensions.PrefixTree

fun main() {
    val day = "Day19"

    fun List<String>.parseTowels(): List<String> = this.first().split(", ")
    fun List<String>.parseDesigns(): List<String> = drop(2)

    fun part1(input: List<String>): Int {
        val towels = input.parseTowels()
        val designs = input.parseDesigns()
        println("${towels.size} towels, ${designs.size} designs")

        return designs.count { it.isPossibleWith(towels) }
    }

    fun part2(input: List<String>): Long {
        val towels = input.parseTowels()
        val designs = input.parseDesigns()
        println("${towels.size} towels, ${designs.size} designs")

        return designs.sumOf { it.countPossibilitiesWith(towels) }
    }

    println("$day part 1")

    val testInput = """
        r, wr, b, g, bwu, rb, gb, br

        brwrr
        bggr
        gbbr
        rrbgbr
        ubwu
        bwurrg
        brgr
        bbrgwb
    """.trimIndent().split('\n')
    printAndCheck(testInput, ::part1, 6)

    val input = readInput(day)
    printAndCheck(input, ::part1, 251)


    println("$day part 2")

    printAndCheck(testInput, ::part2, 16)
    printAndCheck(input, ::part2, 616957151871345)
}

private fun String.isPossibleWith(towels: List<String>): Boolean = (this.countPossibilitiesWith(towels) > 0)
private fun String.countPossibilitiesWith(towels: List<String>): Long = PrefixTree().insert(this, towels)
