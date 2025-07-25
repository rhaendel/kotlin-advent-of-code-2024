package de.ronny_h.aoc.year2024.day24

import de.ronny_h.aoc.AdventOfCode
import de.ronny_h.aoc.extensions.numbers.toBoolean
import de.ronny_h.aoc.extensions.numbers.toDigit

fun main() = CrossedWires().run("66055249060558", "fcd,fhp,hmk,rvf,tpc,z16,z20,z33")

class CrossedWires : AdventOfCode<String>(2024, 24) {

    class And : (Boolean, Boolean) -> Boolean {
        override fun invoke(a: Boolean, b: Boolean) = a && b
        override fun toString() = "AND"
        override fun equals(other: Any?) = other is And
        override fun hashCode(): Int = javaClass.hashCode()
    }

    class Or : (Boolean, Boolean) -> Boolean {
        override fun invoke(a: Boolean, b: Boolean) = a || b
        override fun toString() = "OR"
        override fun equals(other: Any?) = other is Or
        override fun hashCode(): Int = javaClass.hashCode()
    }

    class Xor : (Boolean, Boolean) -> Boolean {
        override fun invoke(a: Boolean, b: Boolean) = a xor b
        override fun toString() = "XOR"
        override fun equals(other: Any?) = other is Xor
        override fun hashCode(): Int = javaClass.hashCode()
    }

    fun parseWires(input: List<String>) = input
        .takeWhile { it.isNotEmpty() }
        .map {
            val (name, value) = it.split(": ")
            Wire(name, value == "1")
        }

    fun parseGates(input: List<String>) = input
        .dropWhile { it.isNotEmpty() }
        .drop(1)
        .map {
            val (term, result) = it.split(" -> ")
            val (in1, op, in2) = term.split(" ")
            val operation = when (op) {
                "AND" -> And()
                "OR" -> Or()
                "XOR" -> Xor()
                else -> error("Operation not supported: $op")
            }
            Gate(in1.trim(), in2.trim(), operation, result.trim())
        }

    override fun part1(input: List<String>): String {
        val gates = parseGates(input)
        return parseWires(input)
            .associateBy(Wire::name)
            .simulateGates(gates)
            .withPrefixAsDecimal("z")
            .toString()
    }

    private fun Map<String, Wire>.withPrefixAsDecimal(prefix: String): Long = withPrefixAsBinary(prefix)
        .toLong(2)

    private fun Map<String, Wire>.withPrefixAsBinary(prefix: String): String = withPrefixSortedByLSBFirst(prefix)
        .joinToString("") { it.value.toDigit() }

    private fun Map<String, Wire>.withPrefixSortedByLSBFirst(prefix: String): List<Wire> = values
        .filter { it.name.startsWith(prefix) }
        .sortedByDescending(Wire::name)

    private fun Map<String, Wire>.simulateGates(gates: List<Gate>): Map<String, Wire> {
        val wires = toMutableMap()
        var simulatedGates: Int
        do {
            val simulatableGates = gates.filter { it.in1 in wires && it.in2 in wires }
            simulatedGates = simulatableGates.size
            val outWires = simulatableGates
                .map { it.simulateWith(wires) }
                .associateBy(Wire::name)
            wires.putAll(outWires)
        } while (simulatedGates != gates.size)
        return wires
    }

    override fun part2(input: List<String>): String {
        // inputs: x00..x44, y00..y44
        // outputs: z00..z45
        val wires = parseWires(input).associateBy(Wire::name)
        val gates = parseGates(input)
        simulateGates(wires, gates)

        val fixedGates = fixBySwappingTheRightOutputWires(gates)
        simulateGates(wires, fixedGates)

        val largest45BitNumber = "1".repeat(45).toLong(2)
        listOf(
            Pair(0L, 0L),
            Pair(1000L, 0L),
            Pair(0L, 1000L),
            Pair(largest45BitNumber, 0L),
            Pair(0L, largest45BitNumber),
            Pair(largest45BitNumber, largest45BitNumber),
        ).forEach { (x, y) ->
            check(add(x, y, wires, fixedGates) == x + y)
        }

        return listOf("z16", "hmk", "z20", "fhp", "rvf", "tpc", "z33", "fcd").sorted().joinToString(",")
    }

    /*
    Using only XOR, AND and OR gates, a simple adder works like this:

    The first digit:
    - the digit: z_0 = x_0 XOR y_0
    - carryover: c_1 = x_0 AND y_0

    For higher digits:
    - the digit: z_i   = x_i XOR y_i XOR c_i
    - carryover: c_i+1 = (x_i AND y_i) OR ((x_i XOR y_i) AND c_i)

    Looking at the graph generated by PlotCrossedWires.ipynb from top to bottom, the first z_i that is not a
    successor of an XOR node, is z_16.
    Its predecessor is AND, in the input: y16 AND x16 -> z16
    It should have been the XOR node on the same level as z16: vmr XOR bnc -> hmk.
    => swap `z16,hmk`

    The same with z_20
    => swap `z20,fhp`

    z_27 has only one XOR as predecessor
    => swap `rvf,tpc`

    z_33 has an OR as predecessor
    => swap `z33,fcd`
     */
    fun fixBySwappingTheRightOutputWires(gates: List<Gate>): List<Gate> = gates
        .swapOutWires("z16", "hmk")
        .swapOutWires("z20", "fhp")
        .swapOutWires("rvf", "tpc")
        .swapOutWires("z33", "fcd")

    private fun List<Gate>.swapOutWires(
        firstOut: String,
        secondOut: String
    ): List<Gate> {
        val first = first { it.out == firstOut }
        val second = first { it.out == secondOut }
        val oneSwapped = first.copy(out = secondOut)
        val twoSwapped = second.copy(out = firstOut)
        return filter { it.out !in listOf(firstOut, secondOut) } + oneSwapped + twoSwapped
    }

    fun add(
        x: Long,
        y: Long,
        wires: Map<String, Wire>,
        gates: List<Gate>
    ): Long {
        val modifiedWires = wires.toMutableMap()
        modifiedWires.putAll(x.toWires("x"))
        modifiedWires.putAll(y.toWires("y"))
        return simulateGates(modifiedWires, gates)
    }

    private fun Long.toWires(prefix: String): Map<String, Wire> = toString(2)
        .reversed()
        .padEnd(45, '0')
        .mapIndexed { i, digit ->
            Wire(prefix + i.toString().padStart(2, '0'), digit.toBoolean())
        }
        .associateBy(Wire::name)

    private fun simulateGates(
        wires: Map<String, Wire>,
        gates: List<Gate>
    ): Long {
        println("--- simulating gates ---")
        val x0 = wires.withPrefixAsDecimal("x")
        val y0 = wires.withPrefixAsDecimal("y")

        val simulatedWires = wires.simulateGates(gates)

        val x1 = simulatedWires.withPrefixAsDecimal("x")
        val y1 = simulatedWires.withPrefixAsDecimal("y")

        val sumSimulatedBinary = simulatedWires.withPrefixAsBinary("z")
        val zWires = simulatedWires.withPrefixSortedByLSBFirst("z")
        val sumSimulated = sumSimulatedBinary.toLong(2)
        val sumExpectedBinary = (x1 + y1).toString(2).padStart(46, '0')

        // check that the input wires aren't modified
        check(x0 == x1)
        check(y0 == y1)

        println("inputs:")
        println("x=$x0")
        println("y=$y0")
        println()
        println("expected : x+y=${x1 + y1}")
        println("simulated: z  =$sumSimulated")
        println()
        println("as binary:")
        println("expected : x+y=$sumExpectedBinary")
        println("simulated: z  =$sumSimulatedBinary")

        val differentIndices = mutableListOf<Int>()
        for (i in sumSimulatedBinary.indices) {
            if (sumSimulatedBinary[i] != sumExpectedBinary[i]) {
                differentIndices.add(i)
            }
        }
        println("different indices: $differentIndices (${differentIndices.size})")

        val wrongWires = differentIndices.map { zWires[it].name }
        println("zWires at these indices: $wrongWires")

        val gatesToChange = gates.filter { it.out in wrongWires }
        println("gates to change: $gatesToChange")

        return sumSimulated
    }
}

data class Wire(val name: String, val value: Boolean) {
    override fun toString() = "$name: $value"
}

data class Gate(val in1: String, val in2: String, val operation: (Boolean, Boolean) -> (Boolean), val out: String) {
    fun simulateWith(inWires: Map<String, Wire>): Wire = Wire(
        out,
        operation(inWires.valueOf(in1), inWires.valueOf(in2))
    )

    override fun toString() = "$in1 $operation $in2 = $out"
}

private fun Map<String, Wire>.valueOf(inWire: String): Boolean = getValue(inWire).value
