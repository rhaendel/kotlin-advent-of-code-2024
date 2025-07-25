package de.ronny_h.aoc.extensions.graphs

import java.util.*
import kotlin.Int.Companion.MAX_VALUE

// NOTE: This modified A* implementation is based on the pseudocode on the Wikipedia page
//       https://en.wikipedia.org/wiki/A*_search_algorithm


private fun <N> reconstructPaths(cameFrom: Map<N, Collection<N>>, currentNode: N): List<List<N>> {
    if (!cameFrom.contains(currentNode)) {
        return listOf(listOf(currentNode))
    }
    return cameFrom.getValue(currentNode)
        .flatMap { pred -> reconstructPaths(cameFrom, pred) }
        .map { path -> path + currentNode }
}

private const val LARGE_VALUE = MAX_VALUE / 2

/**
 * A modified A* algorithm that finds all shortest paths from `start` to `goal`.
 * @param start the start node
 * @param isGoal predicate deciding if a node is the goal
 * @param neighbors is a function that returns the list of neighbours for a given node.
 * @param d is the distance/cost function. d(m,n) provides the distance (or cost) to reach node n from node m.
 * @param h is the heuristic function. h(n) estimates the cost to reach goal from node n.
 */
fun <N> aStarAllPaths(
    start: N, isGoal: N.() -> Boolean, neighbors: (N) -> List<N>, d: (N, N) -> Int, h: (N) -> Int,
    printIt: (visited: Set<N>, current: N, additionalInfo: () -> String) -> Unit = { _, _, _ -> }
): List<ShortestPath<N>> {
    // For node n, fScore[n] := gScore[n] + h(n). fScore[n] represents our current best guess as to
    // how cheap a path could be from start to finish if it goes through n.
    val fScore = mutableMapOf<N, Int>().withDefault { _ -> LARGE_VALUE } // map with default value of "Infinity"

    // The set of discovered nodes that may need to be (re-)expanded.
    // Initially, only the start node is known.
    // This is usually implemented as a min-heap or priority queue rather than a hash-set.
    val openSet = PriorityQueue<N> { a, b -> fScore.getValue(a).compareTo(fScore.getValue(b)) }
    openSet.add(start)

    // For node n, cameFrom[n] is the set of nodes immediately preceding it on the cheapest paths from the start
    // to n currently known.
    val cameFrom = mutableMapOf<N, MutableSet<N>>()

    // For node n, gScore[n] is the currently known cost of the cheapest path from start to n.
    val gScore = mutableMapOf<N, Int>().withDefault { _ -> LARGE_VALUE } // map with default value of "Infinity"
    gScore[start] = 0
    fScore[start] = h(start)

    while (openSet.isNotEmpty()) {
        // This operation can occur in O(Log(N)) time if openSet is a min-heap or a priority queue
        val current = openSet.remove()
        if (current.isGoal()) {
            // Search for nodes in openSet with fScore[node] <= gScore[current]
            // If the heuristic function is admissible (it never overestimates the actual cost to get to the goal)
            // we can be sure to expand all possible paths.
            if (openSet.all { n -> fScore.getValue(n) > gScore.getValue(current) }) {
                return reconstructPaths(cameFrom, current).map { x -> ShortestPath(x, gScore.getValue(current)) }
            }
        }

        for (neighbor in neighbors(current)) {
            // d(current,neighbor) is the weight of the edge from current to neighbor
            // tentativeGScore is the distance from start to the neighbor through current
            val tentativeGScore = gScore.getValue(current) + d(current, neighbor)
            if (tentativeGScore <= gScore.getValue(neighbor)) {
                if (tentativeGScore < gScore.getValue(neighbor)) {
                    // This path to neighbor is better than any previous one. Record it!
                    cameFrom[neighbor] = mutableSetOf(current)
                } else {
                    // This path to neighbor is equal to the best one. Record it, too!
                    cameFrom.getOrPut(neighbor) { mutableSetOf() } += current
                }
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
    error("No path found from $start to goal")
}
