package de.ronny_h.extensions

import java.util.*
import kotlin.Int.Companion.MAX_VALUE

// NOTE: This A* implementation is a 1:1 equivalent in Kotlin to the pseudo code on the Wikipedia page
//       https://en.wikipedia.org/wiki/A*_search_algorithm

private fun <N> reconstructPath(cameFrom: Map<N, N>, last: N): List<N> {
    var current = last
    val totalPath = mutableListOf(current)
    while (current in cameFrom.keys) {
        current = cameFrom.getValue(current)
        totalPath.add(0, current)
    }
    return totalPath
}

data class ShortestPath<N>(val path: List<N>, val distance: Int)

/**
 * A* finds a path from `start` to `goal`.
 * @param start the start node
 * @param goal the goal node
 * @param neighbors is a function that returns the list of neighbours for a given node.
 * @param d is the distance/cost function. d(m,n) provides the distance (or cost) to reach node n from node m.
 * @param h is the heuristic function. h(n) estimates the cost to reach goal from node n.
 */
fun <N> aStar(start: N, goal: N, neighbors: (N) -> List<N>, d: (N, N) -> Int, h: (N) -> Int,
              printIt: (visited: Set<N>, current: N, additionalInfo: () -> String) -> Unit = {_, _, _ -> }): ShortestPath<N> {
    // For node n, fScore[n] := gScore[n] + h(n). fScore[n] represents our current best guess as to
    // how cheap a path could be from start to finish if it goes through n.
    val fScore = mutableMapOf<N, Int>() // map with default value of Infinity

    // The set of discovered nodes that may need to be (re-)expanded.
    // Initially, only the start node is known.
    // This is usually implemented as a min-heap or priority queue rather than a hash-set.
    val openSet = PriorityQueue<N> { a, b -> fScore.getValue(a).compareTo(fScore.getValue(b)) }
    openSet.add(start)

    // For node n, cameFrom[n] is the node immediately preceding it on the cheapest path from the start
    // to n currently known.
    val cameFrom = mutableMapOf<N, N>()

    // For node n, gScore[n] is the currently known cost of the cheapest path from start to n.
    val gScore = mutableMapOf<N, Int>() // map with default value of Infinity
    gScore[start] = 0

    fScore[start] = h(start)

    while (openSet.isNotEmpty()) {
        // This operation can occur in O(Log(N)) time if openSet is a min-heap or a priority queue
        val current = openSet.peek()
        if (current == goal) {
            return ShortestPath(reconstructPath(cameFrom, current), gScore.getValue(current))
        }

        openSet.remove(current)
        for (neighbor in neighbors(current)) {
            // d(current,neighbor) is the weight of the edge from current to neighbor
            // tentative_gScore is the distance from start to the neighbor through current
            val tentativeGScore = gScore.getOrDefault(current, MAX_VALUE) + d(current, neighbor)
            if (tentativeGScore < gScore.getOrDefault(neighbor, MAX_VALUE)) {
                // This path to neighbor is better than any previous one. Record it!
                cameFrom[neighbor] = current
                gScore[neighbor] = tentativeGScore
                fScore[neighbor] = tentativeGScore + h(neighbor)
                if (neighbor !in openSet) {
                    openSet.add(neighbor)
                }
            }
            printIt(cameFrom.keys, neighbor) {
                "current: $current=${fScore[current]}, neighbor: $neighbor=${fScore[neighbor]}, open: " + openSet.joinToString { "$it=${fScore[it]}" }
            }
        }
    }

    // Open set is empty but goal was never reached
    error("No path found from $start to $goal")
}
