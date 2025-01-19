package de.ronny_h.extensions

import de.ronny_h.extensions.Direction.*
import io.kotlintest.data.forall
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row


class CoordinatesTest : StringSpec({

    "Coordinates are added" {
        forall(
            row(Coordinates(1, 1), Coordinates(0, 0), Coordinates(1, 1)),
            row(Coordinates(0, 0), Coordinates(1, 1), Coordinates(1, 1)),
            row(Coordinates(1, 2), Coordinates(3, 4), Coordinates(4, 6)),
        ) { first, second, result ->
            first + second shouldBe result
        }
    }

    "Coordinates are subtracted" {
        forall(
            row(Coordinates(1, 1), Coordinates(0, 0), Coordinates(1, 1)),
            row(Coordinates(0, 0), Coordinates(1, 1), Coordinates(-1, -1)),
            row(Coordinates(3, 5), Coordinates(2, 1), Coordinates(1, 4)),
        ) { first, second, result ->
            first - second shouldBe result
        }
    }

    "Multiplication with a scalar" {
        forall(
            row(0, Coordinates(5, 7), Coordinates(0, 0)),
            row(7, Coordinates(0, 0), Coordinates(0, 0)),
            row(3, Coordinates(5, 7), Coordinates(15, 21)),
            row(-3, Coordinates(5, 7), Coordinates(-15, -21)),
        ) { scalar, coordinates, result ->
            scalar * coordinates shouldBe result
            coordinates * scalar shouldBe result
        }
    }

    "Add a direction" {
        forall(
            row(Coordinates(5, 5), NORTH, Coordinates(4, 5)),
            row(Coordinates(5, 5), SOUTH, Coordinates(6, 5)),
            row(Coordinates(5, 5), EAST, Coordinates(5, 6)),
            row(Coordinates(5, 5), WEST, Coordinates(5, 4)),
        ) { coordinates, direction, result ->
            coordinates + direction shouldBe result
        }
    }

    "Neighbours" {
        Coordinates(5, 5).neighbours() shouldContainAll listOf(
            Coordinates(4, 5),
            Coordinates(6, 5),
            Coordinates(5, 4),
            Coordinates(5, 6),
        )
    }

    "Directed neighbours" {
        Coordinates(5, 5).directedNeighbours() shouldContainAll listOf(
            NORTH to Coordinates(4, 5),
            SOUTH to Coordinates(6, 5),
            EAST to Coordinates(5, 6),
            WEST to Coordinates(5, 4),
        )
    }

    "Direction turnRight() turns right" {
        NORTH.turnRight() shouldBe EAST
        EAST.turnRight() shouldBe SOUTH
        SOUTH.turnRight() shouldBe WEST
        WEST.turnRight() shouldBe NORTH
    }

    "Direction turnLeft() turns left" {
        NORTH.turnLeft() shouldBe WEST
        EAST.turnLeft() shouldBe NORTH
        SOUTH.turnLeft() shouldBe EAST
        WEST.turnLeft() shouldBe SOUTH
    }

    "asChar gives a graphical representation" {
        NORTH.asChar() shouldBe '↑'
        EAST.asChar() shouldBe '→'
        SOUTH.asChar() shouldBe '↓'
        WEST.asChar() shouldBe '←'
    }

    "A Direction's orientation is checked right" {
        NORTH.isVertical() shouldBe true
        NORTH.isHorizontal() shouldBe false
        SOUTH.isVertical() shouldBe true
        SOUTH.isHorizontal() shouldBe false

        EAST.isVertical() shouldBe false
        EAST.isHorizontal() shouldBe true
        WEST.isVertical() shouldBe false
        WEST.isHorizontal() shouldBe true
    }

    "Opposite directions" {
        NORTH.isOpposite(SOUTH) shouldBe true
        SOUTH.isOpposite(NORTH) shouldBe true
        EAST.isOpposite(WEST) shouldBe true
        WEST.isOpposite(EAST) shouldBe true
    }

    "Difference between directions" {
        NORTH - NORTH shouldBe 0
        NORTH - EAST shouldBe 1
        NORTH - SOUTH shouldBe 2
        NORTH - WEST shouldBe 1
    }
})
