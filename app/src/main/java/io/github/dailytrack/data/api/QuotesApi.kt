package io.github.dailytrack.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

data class Quote(
    val text: String,
    val author: String
)

object QuotesApi {
    private val fallbackQuotes = listOf(
        Quote("The only way to do great work is to love what you do.", "Steve Jobs"),
        Quote("Success is not final, failure is not fatal: it is the courage to continue that counts.", "Winston Churchill"),
        Quote("Believe you can and you're halfway there.", "Theodore Roosevelt"),
        Quote("Hardships often prepare ordinary people for an extraordinary destiny.", "C.S. Lewis"),
        Quote("The future belongs to those who believe in the beauty of their dreams.", "Eleanor Roosevelt"),
        Quote("It is during our darkest moments that we must focus to see the light.", "Aristotle"),
        Quote("The best time to plant a tree was 20 years ago. The second best time is now.", "Chinese Proverb"),
        Quote("Your limitation—it's only your imagination.", "Unknown"),
        Quote("Push yourself, because no one else is going to do it for you.", "Unknown"),
        Quote("Great things never come from comfort zones.", "Unknown"),
        Quote("Dream it. Wish it. Do it.", "Unknown"),
        Quote("Success doesn't just find you. You have to go out and get it.", "Unknown"),
        Quote("The harder you work for something, the greater you'll feel when you achieve it.", "Unknown"),
        Quote("Don't stop when you're tired. Stop when you're done.", "Unknown"),
        Quote("Wake up with determination. Go to bed with satisfaction.", "Unknown"),
        Quote("Do what you can, with what you have, where you are.", "Theodore Roosevelt"),
        Quote("If you want to lift yourself up, lift up someone else.", "Booker T. Washington"),
        Quote("The only limit to our realization of tomorrow will be our doubts of today.", "Franklin D. Roosevelt"),
        Quote("It does not matter how slowly you go as long as you do not stop.", "Confucius"),
        Quote("Everything you've ever wanted is on the other side of fear.", "George Addair"),
        Quote("Success is walking from failure to failure with no loss of enthusiasm.", "Winston Churchill"),
        Quote("The way to get started is to quit talking and begin doing.", "Walt Disney"),
        Quote("If life were predictable it would cease to be life, and be without flavor.", "Eleanor Roosevelt"),
        Quote("If you look at what you have in life, you'll always have more.", "Oprah Winfrey"),
        Quote("Spread love everywhere you go. Let no one ever come to you without leaving happier.", "Mother Teresa"),
        Quote("When you reach the end of your rope, tie a knot in it and hang on.", "Franklin D. Roosevelt"),
        Quote("Always remember that you are absolutely unique. Just like everyone else.", "Margaret Mead"),
        Quote("The greatest glory in living lies not in never falling, but in rising every time we fall.", "Nelson Mandela"),
        Quote("Life is what happens when you're busy making other plans.", "John Lennon"),
        Quote("In the end, it's not the years in your life that count. It's the life in your years.", "Abraham Lincoln")
    )

    suspend fun getRandomQuote(): Quote {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://zenquotes.io/api/random")
                val connection = url.openConnection()
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                val inputStream = connection.getInputStream()
                val data = inputStream.bufferedReader().readText()
                inputStream.close()
                
                val json = data.trim('[', ']')
                val qMatch = Regex(""""q":"(.*?)"""").find(json)
                val aMatch = Regex(""""a":"(.*?)"""").find(json)
                
                if (qMatch != null && aMatch != null) {
                    Quote(qMatch.groupValues[1], aMatch.groupValues[1])
                } else {
                    fallbackQuotes.random()
                }
            } catch (e: Exception) {
                fallbackQuotes.random()
            }
        }
    }
}
