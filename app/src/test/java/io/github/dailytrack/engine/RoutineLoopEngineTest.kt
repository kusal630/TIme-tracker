package io.github.dailytrack.engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RoutineLoopEngineTest {
    private lateinit var engine: RoutineLoopEngine

    @Before
    fun setup() {
        engine = RoutineLoopEngine()
    }

    @Test
    fun `cosine similarity of identical vectors is 1`() {
        val vec1 = mapOf("a" to 0.5, "b" to 0.3, "c" to 0.2)
        val vec2 = mapOf("a" to 0.5, "b" to 0.3, "c" to 0.2)
        assertEquals(1.0, engine.cosineSimilarity(vec1, vec2), 0.001)
    }

    @Test
    fun `cosine similarity of orthogonal vectors is 0`() {
        val vec1 = mapOf("a" to 1.0, "b" to 0.0)
        val vec2 = mapOf("a" to 0.0, "b" to 1.0)
        assertEquals(0.0, engine.cosineSimilarity(vec1, vec2), 0.001)
    }

    @Test
    fun `cosine similarity of opposite vectors is -1`() {
        val vec1 = mapOf("a" to 1.0, "b" to 0.0)
        val vec2 = mapOf("a" to -1.0, "b" to 0.0)
        assertEquals(-1.0, engine.cosineSimilarity(vec1, vec2), 0.001)
    }

    @Test
    fun `empty vectors return similarity 1`() {
        assertEquals(1.0, engine.cosineSimilarity(emptyMap(), emptyMap()), 0.001)
    }

    @Test
    fun `loop detection with similar vectors`() {
        val vectors = listOf(
            mapOf("a" to 0.5, "b" to 0.3, "c" to 0.2),
            mapOf("a" to 0.5, "b" to 0.3, "c" to 0.2),
            mapOf("a" to 0.5, "b" to 0.3, "c" to 0.2),
            mapOf("a" to 0.5, "b" to 0.3, "c" to 0.2)
        )
        val growthScores = listOf(30.0, 25.0, 20.0, 15.0)
        val noveltyScores = listOf(20.0, 15.0, 10.0, 5.0)

        val result = engine.detectLoop(vectors, growthScores, noveltyScores, daysThreshold = 3)
        assertTrue(result.isLoopDetected)
        assertTrue(result.similarity > 0.85)
    }

    @Test
    fun `no loop with diverse vectors`() {
        val vectors = listOf(
            mapOf("a" to 0.7, "b" to 0.1, "c" to 0.2),
            mapOf("a" to 0.1, "b" to 0.7, "c" to 0.2),
            mapOf("a" to 0.2, "b" to 0.1, "c" to 0.7),
            mapOf("a" to 0.3, "b" to 0.4, "c" to 0.3)
        )
        val growthScores = listOf(60.0, 70.0, 65.0, 75.0)
        val noveltyScores = listOf(50.0, 60.0, 55.0, 70.0)

        val result = engine.detectLoop(vectors, growthScores, noveltyScores, daysThreshold = 3)
        assertFalse(result.isLoopDetected)
    }

    @Test
    fun `insufficient data returns no loop`() {
        val vectors = listOf(
            mapOf("a" to 0.5, "b" to 0.5),
            mapOf("a" to 0.5, "b" to 0.5)
        )
        val result = engine.detectLoop(vectors, listOf(50.0, 50.0), listOf(30.0, 30.0), daysThreshold = 3)
        assertFalse(result.isLoopDetected)
    }
}
