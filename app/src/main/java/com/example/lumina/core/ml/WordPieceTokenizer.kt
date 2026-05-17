package com.example.lumina.core.ml

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * A WordPiece tokenizer implementation for Android, compatible with BERT-style models.
 * This tokenizer splits text into sub-word units based on a provided vocabulary.
 *
 * @property vocab A map of tokens to their corresponding IDs.
 */
class WordPieceTokenizer(private val vocab: Map<String, Int>) {
    private val unkToken = "[UNK]"
    private val maxInputCharsPerWord = 100

    /**
     * Tokenizes a character array into a list of WordPiece tokens.
     * It handles whitespace splitting, punctuation splitting, and the WordPiece algorithm.
     *
     * @param text The input character array to tokenize.
     * @return A list of tokens.
     */
    fun tokenize(text: CharArray): List<String> {
        val outputTokens = mutableListOf<String>()
        
        var wordStart = -1
        for (i in text.indices) {
            val char = text[i]
            if (char.isWhitespace()) {
                if (wordStart != -1) {
                    processWord(text, wordStart, i, outputTokens)
                    wordStart = -1
                }
            } else {
                if (wordStart == -1) {
                    wordStart = i
                }
            }
        }
        if (wordStart != -1) {
            processWord(text, wordStart, text.size, outputTokens)
        }
        
        return outputTokens
    }

    private fun processWord(text: CharArray, start: Int, end: Int, outputTokens: MutableList<String>) {
        var subStart = start
        for (i in start until end) {
            if (isPunctuation(text[i])) {
                if (subStart < i) {
                    applyWordPiece(text, subStart, i, outputTokens)
                }
                // Punctuation is a token itself
                applyWordPiece(text, i, i + 1, outputTokens)
                subStart = i + 1
            }
        }
        if (subStart < end) {
            applyWordPiece(text, subStart, end, outputTokens)
        }
    }

    private fun applyWordPiece(text: CharArray, start: Int, end: Int, outputTokens: MutableList<String>) {
        val length = end - start
        if (length > maxInputCharsPerWord) {
            outputTokens.add(unkToken)
            return
        }

        var isBad = false
        var curStart = start
        val subTokens = mutableListOf<String>()
        
        while (curStart < end) {
            var curEnd = end
            var curSubstr: String? = null
            while (curStart < curEnd) {
                // We unfortunately need a String for Map lookup, but we keep it local
                val substr = if (curStart > start) {
                    "##" + String(text, curStart, curEnd - curStart)
                } else {
                    String(text, curStart, curEnd - curStart)
                }

                if (vocab.containsKey(substr)) {
                    curSubstr = substr
                    break
                }
                curEnd--
            }

            if (curSubstr == null) {
                isBad = true
                break
            }
            subTokens.add(curSubstr)
            curStart = curEnd
        }

        if (isBad) {
            outputTokens.add(unkToken)
        } else {
            outputTokens.addAll(subTokens)
        }
    }

    private fun isPunctuation(char: Char): Boolean {
        val cp = char.toInt()
        if ((cp >= 33 && cp <= 47) || (cp >= 58 && cp <= 64) ||
            (cp >= 91 && cp <= 96) || (cp >= 123 && cp <= 126)
        ) {
            return true
        }
        val type = Character.getType(char).toByte()
        return type == Character.CONNECTOR_PUNCTUATION ||
                type == Character.DASH_PUNCTUATION ||
                type == Character.END_PUNCTUATION ||
                type == Character.FINAL_QUOTE_PUNCTUATION ||
                type == Character.INITIAL_QUOTE_PUNCTUATION ||
                type == Character.OTHER_PUNCTUATION ||
                type == Character.START_PUNCTUATION
    }

    /**
     * Converts a list of string tokens into their corresponding integer IDs from the vocabulary.
     *
     * @param tokens The list of tokens to convert.
     * @return A list of integer IDs.
     */
    fun convertTokensToIds(tokens: List<String>): List<Int> {
        return tokens.map { vocab[it] ?: vocab[unkToken] ?: 0 }
    }

    companion object {
        /**
         * Loads a vocabulary from an asset file and creates a [WordPieceTokenizer].
         *
         * @param context The application context.
         * @param fileName The name of the vocabulary file in the assets folder.
         * @return An initialized [WordPieceTokenizer].
         */
        fun loadFromAssets(context: Context, fileName: String): WordPieceTokenizer {
            val vocab = mutableMapOf<String, Int>()
            context.assets.open(fileName).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var index = 0
                    var line: String? = reader.readLine()
                    while (line != null) {
                        val token = line.trim()
                        if (token.isNotEmpty() || line == "") { // Handle empty line if it represents a token
                            vocab[line] = index
                        }
                        index++
                        line = reader.readLine()
                    }
                }
            }
            return WordPieceTokenizer(vocab)
        }
    }
}
