package com.example.lumina.core.ml

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class WordPieceTokenizer(private val vocab: Map<String, Int>) {
    private val unkToken = "[UNK]"
    private val maxInputCharsPerWord = 100

    fun tokenize(text: String): List<String> {
        val outputTokens = mutableListOf<String>()
        // 1. Split by whitespace
        val words = text.split(Regex("\\s+"))
        
        for (word in words) {
            if (word.isEmpty()) continue
            
            // 2. Split by punctuation to handle URLs correctly (e.g., http://google.com -> http, :, /, /, google, ., com)
            val tokens = splitByPunctuation(word)
            
            for (token in tokens) {
                if (token.length > maxInputCharsPerWord) {
                    outputTokens.add(unkToken)
                    continue
                }

                // 3. Apply WordPiece algorithm to each sub-token
                var isBad = false
                var start = 0
                val subTokens = mutableListOf<String>()
                while (start < token.length) {
                    var end = token.length
                    var curSubstr: String? = null
                    while (start < end) {
                        var substr = token.substring(start, end)
                        if (start > 0) {
                            substr = "##$substr"
                        }
                        if (vocab.containsKey(substr)) {
                            curSubstr = substr
                            break
                        }
                        end--
                    }
                    if (curSubstr == null) {
                        isBad = true
                        break
                    }
                    subTokens.add(curSubstr)
                    start = end
                }

                if (isBad) {
                    outputTokens.add(unkToken)
                } else {
                    outputTokens.addAll(subTokens)
                }
            }
        }
        return outputTokens
    }

    private fun splitByPunctuation(text: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        for (char in text) {
            if (isPunctuation(char)) {
                if (current.isNotEmpty()) {
                    result.add(current.toString())
                    current.setLength(0)
                }
                result.add(char.toString())
            } else {
                current.append(char)
            }
        }
        if (current.isNotEmpty()) {
            result.add(current.toString())
        }
        return result
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

    fun convertTokensToIds(tokens: List<String>): List<Int> {
        return tokens.map { vocab[it] ?: vocab[unkToken] ?: 0 }
    }

    companion object {
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
