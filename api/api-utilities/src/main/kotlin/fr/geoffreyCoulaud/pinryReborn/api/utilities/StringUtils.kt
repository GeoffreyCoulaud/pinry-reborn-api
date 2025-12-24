package fr.geoffreyCoulaud.pinryReborn.api.utilities

fun createRandomString(
    length: Int = 32,
    alphabet: CharSequence = "azertyuiopqsdfghjklmwxcvbn1234567890",
): String = buildString(length) { append(alphabet.random()) }
