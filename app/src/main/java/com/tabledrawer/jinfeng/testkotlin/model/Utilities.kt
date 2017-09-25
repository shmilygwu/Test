package com.tabledrawer.jinfeng.testkotlin.model

import android.content.ContentValues.TAG
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.Log

import com.google.gson.JsonObject
//import com.gwu.trivia.model.Answer
//import com.gwu.trivia.model.GameData
//import com.gwu.trivia.model.Question
import com.koushikdutta.ion.Ion
import com.tabledrawer.jinfeng.testkotlin.model.Answer
import com.tabledrawer.jinfeng.testkotlin.model.GameData
import com.tabledrawer.jinfeng.testkotlin.model.Question

import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.charset.Charset
import java.util.ArrayList
import java.util.Collections
import java.util.Random

object Utilities {
    private const val TAG = "Utilities"

    fun AssetManager.fileAsString(filename: String): String {
        return open(filename).use {
            it.readBytes().toString(Charset.defaultCharset())
        }
    }

    fun loadGameData(fileName: String, context: Context): GameData {
        val questions = ArrayList<Question>()
        var triviaCategory = ""

        val randomGenerator = Random()

        try {
            val csvString = context.assets.fileAsString("presidents.csv")
            val lines = csvString.split(",\r".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            triviaCategory = lines[0]

            for (i in 1..lines.size - 1) {
                val wrongAnswers = ArrayList<Answer>()

                while (wrongAnswers.size < 3) {
                    val randomIndex = randomGenerator
                            .nextInt(lines.size - 1) + 1
                    val answerString = lines[randomIndex]

                    if (randomIndex != i && !wrongAnswers.map { it.answer }.contains(answerString)) {
                        wrongAnswers.add(Answer(answerString, false))
                    }
                }

                // create model objects
                val correctAnswer = Answer(lines[i], true)
                Collections.shuffle(wrongAnswers)

                val question = Question(wrongAnswers,correctAnswer)

                questions.add(question)
            }

        } catch (e: Exception) {
            Log.e("generateQuestions", e.message)
        }

        return GameData(questions, triviaCategory)
    }

    fun parseURLFromBingJSON(jsonObject: JsonObject, desiredOrientation: Int): URL? {
        val imageResults = jsonObject.getAsJsonArray("value")
        if (imageResults != null && imageResults.size() > 0) {
            for (i in 0..imageResults.size() - 1) {
                val imageResult = imageResults.get(i).asJsonObject
                val tooBig = Integer.parseInt(imageResult.get("contentSize").asString.replace(" B", "")) > Constants.MAX_IMAGE_FILE_SIZE_IN_BYTES

                if (!tooBig) {
                    val width = imageResult.get("width").asInt
                    val height = imageResult.get("height").asInt

                    if (desiredOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        if (height > width) {
                            return URL(imageResult.get("contentUrl").asString)
                        }
                    } else if (desiredOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        if (width > height) {
                            return URL(imageResult.get("contentUrl").asString)
                        }
                    }
                }
            }
        }

        Log.e(TAG, "No image results found")
        return null
    }

    fun queryBingForImage(query: String, context: Context): JsonObject? {
        try {
            return Ion.with(context).load(Constants.BING_SEARCH_URL)
                    .addHeader("Ocp-Apim-Subscription-Key", Constants.BING_SEARCH_API_TOKEN)
                    .addQuery("q", query)
                    .addQuery("safeSearch", "Strict")
                    .addQuery("mkt", "en-us")
                    .asJsonObject().get()
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            return null
        }

    }

    fun saveReactionImage(bitmap: Bitmap, directory: File): Boolean {
        val image = File(directory, Constants.REACTION_IMAGE_FILE_NAME)

        val outStream: FileOutputStream
        try {

            outStream = FileOutputStream(image)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream)

            outStream.flush()
            outStream.close()
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            return false
        }

        return true
    }
}
