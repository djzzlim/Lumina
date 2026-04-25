package com.example.lumina.core.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.LongBuffer
import kotlin.math.exp

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
            Log.d("PhishingDetector", "✅ Model loaded successfully")
        } catch (e: Exception) {
            Log.e("PhishingDetector", "❌ Failed to load phishing model: ${e.message}")
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
     * @param url The URL to analyze.
     * @return True if the URL is classified as phishing based on the model's threshold, false otherwise.
     */
    suspend fun predict(url: String): Boolean = withContext(Dispatchers.Default) {
        val currentSession = session
        val currentTokenizer = tokenizer
        
        if (!isInitialized || currentSession == null || currentTokenizer == null) {
            return@withContext false
        }

        try {
            // 1. Normalization (Match Python logic: no forced trailing slash)
            val normalizedUrl = url.lowercase().trim()
            
            // Check whitelist before running ML model
            val host = try { android.net.Uri.parse(normalizedUrl).host?.removePrefix("www.") } catch (_: Exception) { null }
            if (host != null && trustedDomains.contains(host)) {
                Log.d("PhishingDetector", "✅ URL whitelisted: $normalizedUrl")
                return@withContext false
            }

            Log.d("PhishingDetector", "🔍 Analyzing URL: $normalizedUrl")
            
            // 2. Tokenization matching BERT special tokens
            val tokens = mutableListOf<String>()
            tokens.add("[CLS]")
            tokens.addAll(currentTokenizer.tokenize(normalizedUrl))
            tokens.add("[SEP]")

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
            
            // Match input names exactly as model expects
            if (expectedInputs.contains("input_ids")) inputs["input_ids"] = inputTensor
            if (expectedInputs.contains("attention_mask")) inputs["attention_mask"] = maskTensor
            if (expectedInputs.contains("token_type_ids")) inputs["token_type_ids"] = typeTensor
            
            // Handle common ONNX export variants
            if (expectedInputs.contains("input.1") && !inputs.containsKey("input_ids")) inputs["input.1"] = inputTensor

            currentSession.run(inputs).use { results ->
                @Suppress("UNCHECKED_CAST")
                val output = results[0].value as Array<FloatArray>
                val logits = output[0]
                
                // 3. Convert Logits to Probabilities (Softmax logic from sanity check)
                val phishingProbability: Float = if (logits.size == 1) {
                    sigmoid(logits[0])
                } else {
                    val probs = softmax(logits)
                    probs[1] // Index 1 is the 'Phish' label in sanity check
                }

                val threshold = 0.9f
                val isPhishing = phishingProbability >= threshold

                Log.d("PhishingDetector", "📊 URL: $normalizedUrl | Prob: ${String.format("%.4f", phishingProbability)} | Block: $isPhishing")
                isPhishing
            }
        } catch (e: Exception) {
            Log.e("PhishingDetector", "❌ Inference failed for $url: ${e.message}")
            false
        }
    }

    /**
     * Closes the ONNX session and environment to release resources.
     */
    fun close() {
        try {
            session?.close()
            env.close()
        } catch (e: Exception) {
            Log.e("PhishingDetector", "Error closing session: ${e.message}")
        }
    }
}
