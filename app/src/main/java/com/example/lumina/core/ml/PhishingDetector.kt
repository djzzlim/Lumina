package com.example.lumina.core.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.LongBuffer
import kotlin.math.exp

import java.util.Arrays

/**
 * PhishingDetector is responsible for analyzing URLs using a local ONNX machine learning model
 * to determine if they are potential phishing attempts.
 *
 * It uses a URL-BERT model exported to ONNX format and performs inference on-device
 * for maximum privacy.
 *
 * @property context The application context used for asset management and file operations.
 */
class PhishingDetector(private val context: Context) {
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null
    private var tokenizer: WordPieceTokenizer? = null
    private var isInitialized = false

    // Whitelist of trusted domains to reduce false positives for common sites
    private val trustedDomains = setOf(
        "google.com", "google.co.jp", "google.co.uk", "google.de", "google.fr",
        "bing.com",
        "duckduckgo.com",
        "yahoo.com",
        "baidu.com",
        "yandex.ru",
        "instagram.com",
        "facebook.com",
        "twitter.com", "x.com",
        "linkedin.com",
        "apple.com", "icloud.com",
        "microsoft.com", "outlook.com",
        "github.com",
        "amazon.com", "amazon.co.uk", "amazon.de", "amazon.co.jp",
        "wikipedia.org",
        "mozilla.org",
        "android.com",
        "youtube.com",
        "netflix.com",
        "spotify.com",
        "reddit.com",
        "twitch.tv"
    )

    init {
        try {
            val modelName = "urlbert_phishing.onnx"
            val dataName = "$modelName.data"
            
            val modelFile = File(context.filesDir, modelName)
            val dataFile = File(context.filesDir, dataName)

            if (!modelFile.exists()) {
                context.assets.open(modelName).use { input ->
                    FileOutputStream(modelFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            try {
                if (!dataFile.exists()) {
                    context.assets.open(dataName).use { input ->
                        FileOutputStream(dataFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore if .data doesn't exist
            }

            session = env.createSession(modelFile.absolutePath)
            tokenizer = WordPieceTokenizer.loadFromAssets(context, "vocab.txt")
            isInitialized = true
        } catch (_: Exception) {
            isInitialized = false
        }
    }

    private fun sigmoid(x: Float): Float {
        return (1.0f / (1.0f + exp(-x)))
    }

    private fun softmax(logits: FloatArray): FloatArray {
        // Numerically stable softmax
        val maxLogit = logits.maxOrNull() ?: 0f
        val exps = logits.map { exp(it - maxLogit) }
        val sumExps = exps.sum()
        return exps.map { it / sumExps }.toFloatArray()
    }

    /**
     * Predicts whether a given URL is a phishing URL.
     *
     * This method performs tokenization, prepares the input tensors, runs the ONNX inference,
     * and converts the output logits to a probability score.
     *
     * @param url The URL to analyze as a character array.
     * @return True if the URL is classified as phishing based on the model's threshold, false otherwise.
     */
    suspend fun predict(url: CharArray): Boolean = withContext(Dispatchers.Default) {
        val currentSession = session
        val currentTokenizer = tokenizer
        
        if (!isInitialized || currentSession == null || currentTokenizer == null) {
            return@withContext false
        }

        // We make a working copy so we can normalize and then wipe it
        val normalizedUrl = url.copyOf()
        try {
            // 1. Normalization (In-place lowercase and basic trim)
            var writeIdx = 0
            for (i in normalizedUrl.indices) {
                val char = normalizedUrl[i]
                if (!char.isWhitespace()) {
                    normalizedUrl[writeIdx++] = char.lowercaseChar()
                }
            }
            
            // The effective normalized URL is normalizedUrl[0..writeIdx-1]
            // We still need a temporary string for whitelist/URI parsing, 
            // but we keep it local and short-lived.
            val tempUrlString = String(normalizedUrl, 0, writeIdx)
            val host = try { android.net.Uri.parse(tempUrlString).host?.removePrefix("www.") } catch (_: Exception) { null }
            if (host != null && trustedDomains.contains(host)) {
                return@withContext false
            }
            
            // 2. Tokenization matching BERT special tokens
            val tokens = mutableListOf<String>()
            tokens.add("[CLS]")
            // We pass the CharArray segment to tokenizer
            val segment = if (writeIdx == normalizedUrl.size) normalizedUrl else normalizedUrl.copyOfRange(0, writeIdx)
            tokens.addAll(currentTokenizer.tokenize(segment))
            tokens.add("[SEP]")
            
            // Wipe segment if it was a copy
            if (segment !== normalizedUrl) Arrays.fill(segment, '\u0000')

            val maxLen = 64 
            val inputIds = LongArray(maxLen)
            val attentionMask = LongArray(maxLen)
            val tokenTypeIds = LongArray(maxLen)

            val tokenIds = currentTokenizer.convertTokensToIds(tokens)
            for (i in 0 until minOf(tokenIds.size, maxLen)) {
                inputIds[i] = tokenIds[i].toLong()
                attentionMask[i] = 1L
            }

            val shape = longArrayOf(1, maxLen.toLong())
            val inputIdsBuffer = LongBuffer.wrap(inputIds)
            val attentionMaskBuffer = LongBuffer.wrap(attentionMask)
            val tokenTypeIdsBuffer = LongBuffer.wrap(tokenTypeIds)

            val inputTensor = OnnxTensor.createTensor(env, inputIdsBuffer, shape)
            val maskTensor = OnnxTensor.createTensor(env, attentionMaskBuffer, shape)
            val typeTensor = OnnxTensor.createTensor(env, tokenTypeIdsBuffer, shape)

            val inputs = mutableMapOf<String, OnnxTensor>()
            val expectedInputs = currentSession.inputNames
            
            if (expectedInputs.contains("input_ids")) inputs["input_ids"] = inputTensor
            if (expectedInputs.contains("attention_mask")) inputs["attention_mask"] = maskTensor
            if (expectedInputs.contains("token_type_ids")) inputs["token_type_ids"] = typeTensor
            
            if (expectedInputs.contains("input.1") && !inputs.containsKey("input_ids")) inputs["input.1"] = inputTensor

            currentSession.run(inputs).use { results ->
                @Suppress("UNCHECKED_CAST")
                val output = results[0].value as Array<FloatArray>
                val logits = output[0]
                
                val phishingProbability: Float = if (logits.size == 1) {
                    sigmoid(logits[0])
                } else {
                    val probs = softmax(logits)
                    probs[1]
                }

                val threshold = 0.9f
                phishingProbability >= threshold
            }
        } catch (_: Exception) {
            false
        } finally {
            Arrays.fill(normalizedUrl, '\u0000')
        }
    }

    /**
     * Closes the ONNX session and environment to release resources.
     */
    fun close() {
        try {
            session?.close()
            env.close()
        } catch (_: Exception) {
            // Silently close
        }
    }
}
