import de.ronny_h.extensions.Coordinates
import de.ronny_h.extensions.Direction
import de.ronny_h.extensions.Grid
import de.ronny_h.extensions.aStar

fun main() {
    val day = "Day16"

    println("$day part 1")

    fun part1(input: List<String>): Int {
        val maze = ReindeerMaze(input)
        maze.printGrid()
        return maze.calculateLowestScore()
    }

    printAndCheck(
        """
            ###############
            #.......#....E#
            #.#.###.#.###.#
            #.....#.#...#.#
            #.###.#####.#.#
            #.#.#.......#.#
            #.#.#####.###.#
            #...........#.#
            ###.#.#####.#.#
            #...#.....#.#.#
            #.#.#.###.#.#.#
            #.....#...#.#.#
            #.###.#.#.#.#.#
            #S..#.....#...#
            ###############
        """.trimIndent().lines(),
        ::part1, 7036
    )

    printAndCheck(
        """
            #################
            #...#...#...#..E#
            #.#.#.#.#.#.#.#.#
            #.#.#.#...#...#.#
            #.#.#.#.###.#.#.#
            #...#.#.#.....#.#
            #.#.#.#.#.#####.#
            #.#...#.#.#.....#
            #.#.#####.#.###.#
            #.#.#.......#...#
            #.#.###.#####.###
            #.#.#...#.....#.#
            #.#.#.#####.###.#
            #.#.#.........#.#
            #.#.#.#########.#
            #S#.............#
            #################
        """.trimIndent().lines(),
        ::part1, 11048
    )

    val input = readInput(day)
    printAndCheck(input, ::part1, 89471)
    // too high:  89472
    // too high:  89471
    // not right: 88971
    // too low:   88472


    println("$day part 2")

    fun part2(input: List<String>) = input.size

    printAndCheck(input, ::part2, 6512)
}

private class ReindeerMaze(input: List<String>) : Grid<Char>(input) {
    private val wall = '#'
    override val nullElement = wall
    override fun Char.toElementType() = this

    data class Node(val direction: Direction, val position: Coordinates) {
        // hashCode and equals are used to determine if a Node was already visited.
        // -> don't distinguish directions
        override fun hashCode() = position.hashCode()
        override fun equals(other: Any?): Boolean {
            if (other !is Node) {
                return false
            }
            return position == other.position
        }
        override fun toString() = "$position$direction"
    }

    fun calculateLowestScore(): Int {
        // A* algorithm
        // - heuristic function: manhattan distance
        // - weight function:
        //   * 1 for going forward
        //   * 1000 for each 90° turn
        val start = Node(Direction.EAST, Coordinates(height - 2, 1))
        val goal = Node(Direction.EAST, Coordinates(1, width - 2))

        val neighbours: (Node) -> List<Node> = { n ->
            n.position.directedNeighbours()
                .filter { !it.first.isOpposite(n.direction) } // don't go back
                .filter { getAt(it.second) != wall }
                .map { Node(it.first, it.second) }
        }

        val d: (Node, Node) -> Int = { a, b ->
            require(a.position in b.position.neighbours())
            if (a.position == goal.position && b.position == goal.position) {
                // direction at goal doesn't care
                0
            } else if (a.direction - b.direction == 0) {
                // straight ahead
                1
            } else {
                // turn left, right or u-turn -> turn cost + move cost
                (a.direction - b.direction) * 1000 + 1
            }
        }

        val h: (Node) -> Int = { n -> n.position taxiDistanceTo goal.position }

        val printIt: (Set<Node>, Node, () -> String) -> Unit = { visited, current, info ->
            printGrid(visited.map { it.position }.toSet(), 'o', current.position, current.direction)
            println(info.invoke())
        }

        val shortestPath = aStar(start, goal, neighbours, d, h) //, printIt)
        printGrid(path = shortestPath.path.associate { it.position to it.direction.asChar() })
        return shortestPath.distance
    }

}
