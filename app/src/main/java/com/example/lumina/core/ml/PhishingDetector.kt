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

class PhishingDetector(private val context: Context) {
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null
    private var tokenizer: WordPieceTokenizer? = null
    private var isInitialized = false

    init {
        try {
            // Check for the .data file in assets. If it's there, we need to copy it too.
            // Based on the error, the model was exported with weights in a separate file.
            val modelName = "urlbert_phishing.onnx"
            val dataName = "$modelName.data"
            
            val modelFile = File(context.filesDir, modelName)
            val dataFile = File(context.filesDir, dataName)

            // Copy .onnx file
            if (!modelFile.exists()) {
                context.assets.open(modelName).use { input ->
                    FileOutputStream(modelFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            // Attempt to copy .data file if it exists in assets
            try {
                if (!dataFile.exists()) {
                    context.assets.open(dataName).use { input ->
                        FileOutputStream(dataFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.d("PhishingDetector", "Copied companion .data file")
                }
            } catch (e: Exception) {
                Log.w("PhishingDetector", "No companion .data file found in assets")
            }

            session = env.createSession(modelFile.absolutePath)
            
            // Log model input info for debugging
            session?.inputInfo?.forEach { (name, info) ->
                Log.d("PhishingDetector", "Model Input: $name, Info: $info")
            }

            tokenizer = WordPieceTokenizer.loadFromAssets(context, "vocab.txt")
            isInitialized = true
            Log.d("PhishingDetector", "✅ Model loaded successfully from internal storage ($modelName)")
        } catch (e: Exception) {
            Log.e("PhishingDetector", "❌ Failed to load phishing model: ${e.message}")
            isInitialized = false
        }
    }

    suspend fun predict(url: String): Boolean = withContext(Dispatchers.Default) {
        val currentSession = session
        val currentTokenizer = tokenizer
        
        if (!isInitialized || currentSession == null || currentTokenizer == null) {
            Log.w("PhishingDetector", "Prediction skipped: Model not initialized")
            return@withContext false
        }

        try {
            Log.d("PhishingDetector", "🔍 Analyzing URL: $url")
            val tokens = mutableListOf<String>()
            tokens.add("[CLS]")
            tokens.addAll(currentTokenizer.tokenize(url))
            tokens.add("[SEP]")

            val maxLen = 64 
            val inputIds = LongArray(maxLen) { 0L }
            val attentionMask = LongArray(maxLen) { 0L }
            val tokenTypeIds = LongArray(maxLen) { 0L } 

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

            // Dynamic input mapping based on what the model expects
            val inputs = mutableMapOf<String, OnnxTensor>()
            val expectedInputs = currentSession.inputNames
            
            if (expectedInputs.contains("input_ids")) inputs["input_ids"] = inputTensor
            if (expectedInputs.contains("attention_mask")) inputs["attention_mask"] = maskTensor
            if (expectedInputs.contains("token_type_ids")) inputs["token_type_ids"] = typeTensor
            
            // Handle some variants if necessary
            if (expectedInputs.contains("input.1") && !inputs.containsKey("input_ids")) inputs["input.1"] = inputTensor

            Log.d("PhishingDetector", "Passing inputs: ${inputs.keys}")

            currentSession.run(inputs).use { results ->
                val output = results[0].value as Array<FloatArray>
                val logits = output[0]
                val isPhishing = if (logits.size >= 2) logits[1] > logits[0] else false
                
                Log.d("PhishingDetector", "📊 Result for $url -> Is Phishing: $isPhishing (Score: ${if (logits.size >= 2) logits[1] else "N/A"})")
                isPhishing
            }
        } catch (e: Exception) {
            Log.e("PhishingDetector", "❌ Inference failed for $url: ${e.message}")
            false
        }
    }

    fun close() {
        try {
            session?.close()
            env.close()
        } catch (e: Exception) {
            Log.e("PhishingDetector", "Error closing session: ${e.message}")
        }
    }
}
