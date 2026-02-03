package fr.geoffreyCoulaud.pinryReborn.api.usecases.search

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TrigramSimilarityTest {

    @Test
    fun `Given empty string, Then generates empty trigram set`() {
        // Given
        val text = ""

        // When
        val trigrams = TrigramSimilarity.generateTrigrams(text)

        // Then
        assertTrue(trigrams.isEmpty())
    }

    @Test
    fun `Given short string, Then generates padded trigrams`() {
        // Given
        val text = "ab"

        // When
        val trigrams = TrigramSimilarity.generateTrigrams(text)

        // Then
        // With padding: "  ab  " -> "  a", " ab", "ab ", "b  "
        assertTrue(trigrams.isNotEmpty())
    }

    @Test
    fun `Given normal string, Then generates correct trigrams`() {
        // Given
        val text = "cat"

        // When
        val trigrams = TrigramSimilarity.generateTrigrams(text)

        // Then
        // With padding: "  cat  " -> "  c", " ca", "cat", "at ", "t  "
        assertTrue(trigrams.contains("cat"))
    }

    @Test
    fun `Given identical strings, Then trigram similarity is 1`() {
        // Given
        val query = "landscape"
        val target = "landscape"

        // When
        val score = TrigramSimilarity.trigramSimilarity(query, target)

        // Then
        assertEquals(1.0, score, 0.001)
    }

    @Test
    fun `Given completely different strings, Then trigram similarity is low`() {
        // Given
        val query = "abc"
        val target = "xyz"

        // When
        val score = TrigramSimilarity.trigramSimilarity(query, target)

        // Then
        assertTrue(score < 0.3)
    }

    @Test
    fun `Given similar strings with typo, Then trigram similarity is high`() {
        // Given
        val query = "landscpe"  // missing 'a'
        val target = "landscape"

        // When
        val score = TrigramSimilarity.trigramSimilarity(query, target)

        // Then
        assertTrue(score > 0.5)
    }

    @Test
    fun `Given identical strings, Then Jaro-Winkler similarity is 1`() {
        // Given
        val query = "landscape"
        val target = "landscape"

        // When
        val score = TrigramSimilarity.jaroWinklerSimilarity(query, target)

        // Then
        assertEquals(1.0, score, 0.001)
    }

    @Test
    fun `Given similar strings with typo, Then Jaro-Winkler similarity is high`() {
        // Given
        val query = "landscpe"  // missing 'a'
        val target = "landscape"

        // When
        val score = TrigramSimilarity.jaroWinklerSimilarity(query, target)

        // Then
        assertTrue(score > 0.8)
    }

    @Test
    fun `Given completely different strings, Then Jaro-Winkler similarity is low`() {
        // Given
        val query = "abc"
        val target = "xyz"

        // When
        val score = TrigramSimilarity.jaroWinklerSimilarity(query, target)

        // Then
        assertTrue(score < 0.5)
    }

    @Test
    fun `Given strings with different case, Then similarity is case-insensitive`() {
        // Given
        val query = "LANDSCAPE"
        val target = "landscape"

        // When
        val trigramScore = TrigramSimilarity.trigramSimilarity(query, target)
        val jaroWinklerScore = TrigramSimilarity.jaroWinklerSimilarity(query, target)

        // Then
        assertEquals(1.0, trigramScore, 0.001)
        assertEquals(1.0, jaroWinklerScore, 0.001)
    }

    @Test
    fun `Given query as substring of target, Then combined similarity is reasonable`() {
        // Given
        val query = "mountain"
        val target = "Beautiful mountain landscape"

        // When
        val score = TrigramSimilarity.combinedSimilarity(query, target)

        // Then
        assertTrue(score > 0.3)
    }

    @Test
    fun `Given empty query, Then similarity is 0`() {
        // Given
        val query = ""
        val target = "landscape"

        // When
        val trigramScore = TrigramSimilarity.trigramSimilarity(query, target)

        // Then
        assertEquals(0.0, trigramScore, 0.001)
    }

    @Test
    fun `Given empty target, Then similarity is 0`() {
        // Given
        val query = "landscape"
        val target = ""

        // When
        val trigramScore = TrigramSimilarity.trigramSimilarity(query, target)

        // Then
        assertEquals(0.0, trigramScore, 0.001)
    }
}
