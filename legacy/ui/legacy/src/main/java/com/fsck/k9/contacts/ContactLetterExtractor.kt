package com.fsck.k9.contacts

import com.fsck.k9.mail.Address

class ContactLetterExtractor {
    fun extractContactLetter(address: Address): String {
        val displayName = (address.personal ?: address.address).trim()
        if (displayName.isEmpty()) return FALLBACK_CONTACT_LETTER

        // Split into words by whitespace
        val words = displayName.split(Regex("\\s+")).filter { it.isNotBlank() }
        val initials = when {
            words.size >= 2 -> {
                val firstMatch = EXTRACT_LETTER_PATTERN.find(words[0])
                val secondTargetWord = if (words.size > 2) words.last() else words[1]
                val secondMatch = EXTRACT_LETTER_PATTERN.find(secondTargetWord)
                if (firstMatch != null && secondMatch != null) {
                    firstMatch.value.uppercase() + secondMatch.value.uppercase()
                } else {
                    firstMatch?.value?.uppercase() ?: secondMatch?.value?.uppercase()
                }
            }
            words.isNotEmpty() -> {
                val matchResult = EXTRACT_LETTER_PATTERN.find(words[0])
                matchResult?.value?.uppercase()
            }
            else -> null
        }

        return initials ?: FALLBACK_CONTACT_LETTER
    }

    companion object {
        private val EXTRACT_LETTER_PATTERN = Regex("\\p{L}\\p{M}*")
        private const val FALLBACK_CONTACT_LETTER = "?"
    }
}
