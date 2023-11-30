package com.example.locadine

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.locadine.ViewModels.RestaurantInfoViewModel
import com.example.locadine.adapters.MessageAdapter
import com.example.locadine.api.OpenAIApiService
import com.example.locadine.pojos.Message
import com.example.locadine.pojos.OpenAIMessage
import com.example.locadine.pojos.OpenAIRequest
import com.example.locadine.pojos.OpenAIResponse
import com.example.locadine.pojos.OpenAIRoleType
import com.example.locadine.pojos.RestaurantInfo
import com.example.locadine.pojos.SenderType
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ChatbotActivity : AppCompatActivity() {
    private lateinit var chatView: RecyclerView
    private lateinit var sendMessageButton: ImageButton
    private lateinit var userMessageEditText: EditText

    private lateinit var messageArrayList: ArrayList<Message>
    private lateinit var messageAdapter: MessageAdapter

    private lateinit var openAIApiService: OpenAIApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        chatView = findViewById(R.id.chatbot)
        sendMessageButton = findViewById(R.id.chat_send_button)
        userMessageEditText = findViewById(R.id.chat_message)

        messageArrayList = ArrayList()

        sendMessageButton.setOnClickListener(View.OnClickListener {
            if (userMessageEditText.text.isBlank()) {
                Toast.makeText(this, "Message cannot be blank", Toast.LENGTH_SHORT).show()
            } else {
                sendMessage(userMessageEditText.text.toString())
                userMessageEditText.setText("")
            }
        })

        messageAdapter = MessageAdapter(messageArrayList, this)
        chatView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        chatView.adapter = messageAdapter

        val retrofit = Util.getOpenAIRetrofitInstance()
        openAIApiService = retrofit.create(OpenAIApiService::class.java)
    }

    private fun sendMessage(userMessage: String) {
        addMessageAndScroll(Message(userMessage, SenderType.USER))

        val request = OpenAIRequest(
            model="gpt-3.5-turbo",
            messages = listOf(OpenAIMessage(OpenAIRoleType.USER.type, getPrePrompt() + userMessage)),
            temperature = 0.7
        )

        val call = openAIApiService.sendMessageToOpenAI(request)
        addMessageAndScroll(Message("Thinking...", SenderType.BOT))

        call.enqueue(object : Callback<OpenAIResponse> {
            override fun onResponse(call: Call<OpenAIResponse>, response: Response<OpenAIResponse>) {
                removeLastMessage()
                if (response.isSuccessful) {
                    val botResponse = response.body()!!.choices[0].message.content
                    addMessageAndScroll(Message(botResponse, SenderType.BOT))
                } else {
                    Toast.makeText(this@ChatbotActivity, "An error occurred. Please try again", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OpenAIResponse>, t: Throwable) {
                removeLastMessage()
                Toast.makeText(this@ChatbotActivity, "An error occurred. Please try again", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addMessageAndScroll(message: Message) {
        messageArrayList.add(message)
        messageAdapter.notifyDataSetChanged()
        chatView.scrollToPosition(messageAdapter.itemCount - 1)
    }

    private fun removeLastMessage() {
        messageArrayList.removeLast()
        messageAdapter.notifyDataSetChanged()
    }

    private fun getPrePrompt(): String {
        return getRestaurantInfo() + getChatHistory() + "Create an answer that uses 50 tokens or less\n\n"
    }

    private fun getChatHistory(): String {
        var history = "Here is our chat history so far:\n\n"
        for (message in messageArrayList) {
            if (message.sender == SenderType.USER) {
                history += "Me: ${message.message}\n\n"
            } else {
                history += "You: ${message.message}\n\n"
            }
        }
        return history
    }

    private fun getRestaurantInfo(): String {
        val restaurants = RestaurantInfoViewModel.restaurantInfoList
        return if (restaurants.isNotEmpty()) {
            "Here is information about nearby restaurants:\n\n" + getRestaurantsSummary(restaurants)
        } else {
            ""
        }
    }

    private fun getRestaurantsSummary(restaurants: List<RestaurantInfo>): String {
        var result = ""

        restaurants.forEach {
            result += "Name: ${it.name}\n"
            result += "Open now? ${it.opening_hours?.open_now}\n"
            result += "Price level: ${it.price_level}\n"
            result += "Business status: ${it.business_status}\n"
            result += "Average rating: ${it.rating}\n"
            result += "Number of ratings: ${it.user_ratings_total}\n\n"
        }

        return result
    }
}