
fun main() {
    val day = "Day17"

    println("$day part 1")

    fun part1(input: List<String>): String {
        return ThreeBitComputer(input).runProgram().joinToString(",")
    }

    printAndCheck(
        """
            Register A: 729
            Register B: 0
            Register C: 0
            
            Program: 0,1,5,4,3,0
        """.trimIndent().lines(),
        ::part1, "4,6,3,5,6,3,5,2,1,0"
    )

    val input = readInput(day)
    printAndCheck(input, ::part1, "4,1,7,6,4,1,0,2,7")


    println("$day part 2")

    fun part2(input: List<String>): Long {
        val computer = ThreeBitComputer(input)
        val initState = computer.save()
        var registerA = 0L
        println("${computer.program} ${computer.program.size}")
        while (computer.runProgram() != computer.program) {
            registerA++
            if (registerA % 1000000 == 0L) {
                println("$registerA - ${computer.getOutputList()} ${computer.getOutputList().size}")
            }
            computer.restore(initState, registerA)
        }
        return registerA
    }

    printAndCheck(
        """
            Register A: 2024
            Register B: 0
            Register C: 0

            Program: 0,3,5,4,3,0
        """.trimIndent().lines(),
        ::part2, 117440
    )

    printAndCheck(input, ::part2, 0)
}

/**
 * - program = list of 3-bit numbers
 * - 3 registers: A, B, C of type Int
 * - 8 instructions: 3-bit opcode + 3-bit operand
 * - instruction pointer, increases by 2 after instruction processed if not jumped
 * - past the last opcode: program halts
 * - operands: literal or combo
 *
 * combo operands:
 * - 0-3: literal value 0-3
 * - 4, 5, 6: register A, B, C
 * - 7: reserved
 *
 * instructions:
 * opcode  instruction  function
 * 0       adv          division of A and 2^<combo operand>, truncated result in A
 * 1       bxl          bitwise XOR of B and literal operand, result in B
 * 2       bst          <combo operand> modulo 8, lowest 3 bits of result in B
 * 3       jnz          if A=0: nothing, else: jumps to <literal operand>
 * 4       bxc          bitwise XOR of B and C, result in B, reads but ignores operand
 * 5       out          <combo operand> modulo 8, then output result (multiple output values separated by ',')
 * 6       bdv          division of A and 2^<combo operand>, truncated result in B
 * 7       cdv          division of A and 2^<combo operand>, truncated result in C
 */
private class ThreeBitComputer(input: List<String>) {

    private val instructionStep = 2

    val program = input.readProgram()
    private var registerA = input.readRegister('A')
    private var registerB = input.readRegister('B')
    private var registerC = input.readRegister('C')
    private var instructionPointer = 0

    private val output = mutableListOf<Int>()

    fun getOutputList(): List<Int> = output

    private val instructions: List<(Int) -> Unit> = listOf(
        { op -> registerA = (registerA shr combo(op).toIntChecked()); next() }, // 0 adv; A/2^x = A shr x
        { op -> registerB = (registerB xor op.toLong())             ; next() }, // 1 bxl
        { op -> registerB = (combo(op) % 8) and 7                   ; next() }, // 2 bst; 7=111 -> take only lowest 3 bits
        { op -> if (registerA == 0L) next() else instructionPointer = op },     // 3 jnz
        { _  -> registerB = (registerB xor registerC)               ; next() }, // 4 bxc
        { op -> output.add(((combo(op) % 8) and 7).toInt())         ; next() }, // 5 out
        { op -> registerB = (registerA shr combo(op).toIntChecked()); next() }, // 6 bdv
        { op -> registerC = (registerA shr combo(op).toIntChecked()); next() }, // 7 cdv
    )

    fun runProgram(): List<Int> {
        while (instructionPointer < program.size) {
            val instruction = program[instructionPointer]
            val op = program[instructionPointer + 1]
            instructions[instruction].invoke(op)
        }
        return output
    }

    fun save() = State(registerA, registerB, registerC, instructionPointer)
    fun restore(state: State, registerAOverride: Long) {
        this.registerA = registerAOverride
        this.registerB = state.registerB
        this.registerC = state.registerC
        this.instructionPointer = state.instructionPointer
        this.output.clear()
    }

    private fun combo(op: Int) = when (op) {
        1 -> 1L
        2 -> 2L
        3 -> 3L
        4 -> registerA
        5 -> registerB
        6 -> registerC
        else -> error("unknown combo operand code '$op'")
    }

    private fun next() {
        instructionPointer += instructionStep
    }

    private fun List<String>.readRegister(register: Char) =
        first { it.startsWith("Register $register: ") }
            .substringAfter("Register $register: ")
            .toLong()

    private fun List<String>.readProgram() =
        first { it.startsWith("Program: ") }
            .substringAfter("Program: ")
            .split(",")
            .map(String::toInt)

    data class State(
        val registerA: Long,
        val registerB: Long,
        val registerC: Long,
        val instructionPointer: Int,
    )
}

private fun Long.toIntChecked(): Int {
    if (this > Int.MAX_VALUE) {
        throw IllegalArgumentException("$this exceeds Int range")
    }
    return this.toInt()
}
