package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.MealPlan
import com.example.data.local.Recipe
import com.example.data.local.ShoppingItem
import com.example.data.local.UserSession
import com.example.data.local.RecipeComment
import com.example.data.local.RecipeLike
import com.example.data.repository.CookingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import org.json.JSONArray
import com.example.BuildConfig

class CookingViewModel(private val repository: CookingRepository) : ViewModel() {

    // Auth State
    val activeSession: StateFlow<UserSession?> = repository.activeSessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    // Database Flows
    val recipes: StateFlow<List<Recipe>> = repository.allRecipesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteRecipes: StateFlow<List<Recipe>> = repository.favoriteRecipesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shoppingList: StateFlow<List<ShoppingItem>> = repository.shoppingListFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mealPlans: StateFlow<List<MealPlan>> = repository.mealPlansFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Workout-Style Play Recipe Mode State Flow Definitions
    val playingRecipe = MutableStateFlow<Recipe?>(null)
    val currentStepIndex = MutableStateFlow(0)
    val stepTimeRemaining = MutableStateFlow(0)
    val stepTimeTotal = MutableStateFlow(1)
    val isStepTimerRunning = MutableStateFlow(false)

    // Culinary Ambient Audio System
    val currentBgSoundType = MutableStateFlow("None") // "None", "Music", "Local", "Radio"
    val currentBgSoundName = MutableStateFlow("")     // Custom descriptive subtitle

    private var playRecipeJob: Job? = null

    // Active Cooking Timer (Legacy / Standard)
    private val _timerRecipeId = MutableStateFlow<Int?>(null)
    val timerRecipeId: StateFlow<Int?> = _timerRecipeId.asStateFlow()

    private val _timerRemaining = MutableStateFlow<Int?>(null)
    val timerRemaining: StateFlow<Int?> = _timerRemaining.asStateFlow()

    private val _timerTotal = MutableStateFlow(0)
    val timerTotal: StateFlow<Int> = _timerTotal.asStateFlow()

    private val _timerIsRunning = MutableStateFlow(false)
    val timerIsRunning: StateFlow<Boolean> = _timerIsRunning.asStateFlow()

    private var timerJob: Job? = null

    // UI Feedback Message
    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    // Auth Actions
    fun handleSocialLogin(email: String, displayName: String, provider: String, photoUrl: String) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            delay(1500) // Realistic social login delay
            repository.login(email, displayName, provider, photoUrl)
            _isLoggingIn.value = false
            _uiMessage.emit("Successfully logged in via $provider")
        }
    }

    fun handleLogout() {
        viewModelScope.launch {
            stopTimer()
            repository.logout()
            _uiMessage.emit("Logged out safely")
        }
    }

    // Recipe Favorites
    fun toggleFavorite(recipeId: Int, isFav: Boolean) {
        viewModelScope.launch {
            repository.setRecipeFavorite(recipeId, isFav)
            _uiMessage.emit(if (isFav) "Added to Favorites!" else "Removed from Favorites")
        }
    }

    fun completeCook(recipeId: Int, recipeTitle: String) {
        viewModelScope.launch {
            repository.incrementCookCount(recipeId)
            _uiMessage.emit("Congrats! You cooked '$recipeTitle'!")
        }
    }

    // Shopping List Actions
    fun addIngredientToShopping(ingredientName: String) {
        viewModelScope.launch {
            repository.addShoppingItem(ingredientName, "Ingredients")
            _uiMessage.emit("Added '$ingredientName' to your Shopping list!")
        }
    }

    fun addCustomShoppingItem(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addShoppingItem(name, "Groceries")
            _uiMessage.emit("Added '$name'")
        }
    }

    fun toggleShoppingItemBought(item: ShoppingItem) {
        viewModelScope.launch {
            repository.updateShoppingItem(item.copy(isBought = !item.isBought))
        }
    }

    fun deleteShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
        }
    }

    fun clearCompletedShopping() {
        viewModelScope.launch {
            repository.clearCompletedShoppingItems()
            _uiMessage.emit("Cleared bought items")
        }
    }

    // Meal Plan Actions
    fun updateMealPlan(day: String, mealType: String, recipeId: Int?, customName: String?) {
        viewModelScope.launch {
            val plans = mealPlans.value
            val existing = plans.find { it.dayOfWeek == day } ?: MealPlan(day)
            
            val updated = when (mealType.lowercase()) {
                "breakfast" -> existing.copy(breakfastRecipeId = recipeId, breakfastCustomName = customName)
                "lunch" -> existing.copy(lunchRecipeId = recipeId, lunchCustomName = customName)
                "dinner" -> existing.copy(dinnerRecipeId = recipeId, dinnerCustomName = customName)
                else -> existing
            }
            repository.saveMealPlan(updated)
            _uiMessage.emit("Updated $mealType for $day!")
        }
    }

    // Comments, Likes, Sharing & Profile edits
    fun getCommentsForRecipe(recipeId: Int): Flow<List<RecipeComment>> = repository.getCommentsForRecipeFlow(recipeId)
    fun getLikesForRecipe(recipeId: Int): Flow<List<RecipeLike>> = repository.getLikesForRecipeFlow(recipeId)
    fun hasUserLikedRecipe(recipeId: Int, email: String): Flow<Boolean> = repository.hasUserLikedRecipeFlow(recipeId, email)

    fun addComment(recipeId: Int, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            repository.addCommentToRecipe(recipeId, session.displayName, session.photoUrl, text)
            _uiMessage.emit("Review added to recipe!")
        }
    }

    fun toggleLike(recipeId: Int) {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            repository.toggleRecipeSocialLike(recipeId, session.email)
        }
    }

    fun uploadRecipe(
        title: String,
        prepTime: String,
        difficulty: String,
        category: String,
        imageUrl: String,
        ingredients: String,
        instructions: String
    ) {
        if (title.isBlank() || ingredients.isBlank() || instructions.isBlank()) return
        viewModelScope.launch {
            val session = activeSession.value
            val chefName = session?.displayName ?: "Community Chef"
            val newRecipe = Recipe(
                title = title,
                chefName = chefName,
                imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1495521821757-a1efb6729352?w=600&auto=format&fit=crop" },
                prepTime = prepTime,
                difficulty = difficulty,
                category = category,
                ingredientsString = ingredients,
                instructionsString = instructions,
                isFavorite = false,
                totalCooked = 0
            )
            repository.shareUserRecipe(newRecipe)
            _uiMessage.emit("Successfully shared '$title' with the community!")
        }
    }

    fun updateProfile(displayName: String, photoUrl: String, bio: String, dietaryPreference: String) {
        if (displayName.isBlank()) return
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            repository.updateUserProfile(session.email, displayName, photoUrl, bio, dietaryPreference)
            _uiMessage.emit("Profile updated successfully!")
        }
    }

    // Gym Workout-Style Active "Play Recipe" Sequence
    fun selectAndPlayRecipe(recipe: Recipe) {
        playRecipeJob?.cancel()
        playingRecipe.value = recipe
        currentStepIndex.value = 0
        val instructions = recipe.getInstructions()
        if (instructions.isNotEmpty()) {
            setupStep(0)
        }
    }

    fun setupStep(index: Int) {
        val recipe = playingRecipe.value ?: return
        val instructions = recipe.getInstructions()
        if (index >= 0 && index < instructions.size) {
            currentStepIndex.value = index
            val text = instructions[index].lowercase()
            val seconds = when {
                text.contains("wash") || text.contains("rinse") -> 15
                text.contains("chop") || text.contains("slice") || text.contains("shred") || text.contains("peel") -> 25
                text.contains("whisk") || text.contains("mix") || text.contains("stir") || text.contains("blend") -> 30
                text.contains("preheat") || text.contains("heat") -> 40
                text.contains("sear") || text.contains("fry") || text.contains("sauté") || text.contains("brown") || text.contains("grill") -> 45
                text.contains("boil") || text.contains("simmer") || text.contains("sauce") || text.contains("cook") -> 60
                text.contains("bake") || text.contains("oven") || text.contains("roast") || text.contains("slow-cook") -> 90
                else -> 20
            }
            stepTimeRemaining.value = seconds
            stepTimeTotal.value = seconds
            isStepTimerRunning.value = true
            startStepTimerLoop()
        }
    }

    private fun startStepTimerLoop() {
        playRecipeJob?.cancel()
        playRecipeJob = viewModelScope.launch {
            while (stepTimeRemaining.value > 0) {
                delay(1000)
                if (isStepTimerRunning.value) {
                    stepTimeRemaining.value -= 1
                }
            }
            val instructions = playingRecipe.value?.getInstructions() ?: emptyList()
            if (currentStepIndex.value < instructions.size - 1) {
                _uiMessage.emit("Step ${currentStepIndex.value + 1} finalized! Let's handle the next step.")
                setupStep(currentStepIndex.value + 1)
            } else {
                _uiMessage.emit("Recipe completed! Splendid performance Chef!")
                isStepTimerRunning.value = false
                val rId = playingRecipe.value?.id ?: 0
                val rTitle = playingRecipe.value?.title ?: ""
                completeCook(rId, rTitle)
            }
        }
    }

    fun toggleStepTimer() {
        isStepTimerRunning.value = !isStepTimerRunning.value
    }

    fun nextStep() {
        val instructions = playingRecipe.value?.getInstructions() ?: return
        if (currentStepIndex.value < instructions.size - 1) {
            setupStep(currentStepIndex.value + 1)
        }
    }

    fun prevStep() {
        if (currentStepIndex.value > 0) {
            setupStep(currentStepIndex.value - 1)
        }
    }

    fun stopPlayingRecipe() {
        playRecipeJob?.cancel()
        playingRecipe.value = null
        isStepTimerRunning.value = false
    }

    fun setBackgroundSound(type: String, name: String) {
        currentBgSoundType.value = type
        currentBgSoundName.value = name
        if (type == "None") {
            _bgSoundFeedback("Background sound muted")
        } else {
            _bgSoundFeedback("Connected to $name")
        }
    }

    private fun _bgSoundFeedback(msg: String) {
        viewModelScope.launch {
            _uiMessage.emit(msg)
        }
    }

    // Timer Logic
    fun startTimer(recipeId: Int, durationSeconds: Int) {
        timerJob?.cancel()
        _timerRecipeId.value = recipeId
        _timerRemaining.value = durationSeconds
        _timerTotal.value = durationSeconds
        _timerIsRunning.value = true

        timerJob = viewModelScope.launch {
            while (_timerRemaining.value != null && _timerRemaining.value!! > 0) {
                delay(1000)
                if (_timerIsRunning.value) {
                    _timerRemaining.value = _timerRemaining.value!! - 1
                }
            }
            if (_timerRemaining.value == 0) {
                _uiMessage.emit("Timer finished! Perfect cooking time achieved!")
                stopTimer()
            }
        }
    }

    fun pauseResumeTimer() {
        _timerIsRunning.value = !_timerIsRunning.value
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerRecipeId.value = null
        _timerRemaining.value = null
        _timerIsRunning.value = false
    }

    // --- Gemini AI Recipe Generation ---
    private val _isGeneratingRecipe = MutableStateFlow(false)
    val isGeneratingRecipe: StateFlow<Boolean> = _isGeneratingRecipe.asStateFlow()

    private val _recipeGenerationError = MutableStateFlow<String?>(null)
    val recipeGenerationError: StateFlow<String?> = _recipeGenerationError.asStateFlow()

    fun generateRecipeFromPantry(ingredients: List<String>, onCompletion: (Recipe) -> Unit) {
        if (ingredients.isEmpty()) return
        _isGeneratingRecipe.value = true
        _recipeGenerationError.value = null

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
                    _recipeGenerationError.value = "Gemini API key is not configured. Please add your key in the Secrets panel."
                    _isGeneratingRecipe.value = false
                    return@launch
                }

                val ingredientListText = ingredients.joinToString(", ")
                val prompt = """
                    You are an elite, Michelin-star chef assistant. Write a unique, highly detailed, and mouth-watering recipe using some or all of these ingredients that the user has at home: $ingredientListText.
                    
                    You must output your complete response as a single, valid JSON object with EXACTLY this structure, with no markdown codeblock wraps (like ```json or indeed any backticks or headers), with zero commentary or extra outer texts. It must be directly parseable.
                    
                    JSON Structure:
                    {
                      "title": "A highly creative and appetite-inspiring recipe title",
                      "prepTime": "25 mins",
                      "difficulty": "Easy", // choose from "Easy", "Medium", "Hard"
                      "category": "Dinner", // choose from "Breakfast", "Lunch", "Dinner", "Desserts", "Healthy"
                      "ingredients": [
                        "Ingredient 1 with exact quantity",
                        "Ingredient 2 with exact quantity"
                      ],
                      "instructions": [
                        "Instruction step 1",
                        "Instruction step 2"
                      ]
                    }
                """.trimIndent()

                // Call REST API
                val client = OkHttpClient.Builder()
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

                val jsonPayload = JSONObject().apply {
                    val contentsArr = JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    }
                    put("contents", contentsArr)
                    put("generationConfig", JSONObject().apply {
                        put("responseMimeType", "application/json")
                    })
                }

                val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val apiResponse = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                if (!apiResponse.isSuccessful) {
                    _recipeGenerationError.value = "API call failed with code: ${apiResponse.code}. ${apiResponse.message}"
                    _isGeneratingRecipe.value = false
                    return@launch
                }

                val responseBody = apiResponse.body?.string() ?: ""
                if (responseBody.isBlank()) {
                    _recipeGenerationError.value = "Received empty response from Gemini."
                    _isGeneratingRecipe.value = false
                    return@launch
                }

                // Parse response
                val jsonObject = JSONObject(responseBody)
                val candidates = jsonObject.getJSONArray("candidates")
                if (candidates.length() == 0) {
                    _recipeGenerationError.value = "No recipe suggestions found."
                    _isGeneratingRecipe.value = false
                    return@launch
                }

                var partText = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                // Robust clean of backticks if returned in case mimeType config wasn't fully set
                partText = partText.trim()
                if (partText.startsWith("```")) {
                    val lines = partText.lines()
                    val cleanLines = lines.filter { !it.trim().startsWith("```") }
                    partText = cleanLines.joinToString("\n")
                }

                // Parse the inner JSON generated by the model
                val recipeJson = JSONObject(partText.trim())
                val title = recipeJson.getString("title")
                val prepTime = recipeJson.getString("prepTime")
                val difficulty = recipeJson.getString("difficulty")
                val category = recipeJson.getString("category")
                
                val ingJsonArr = recipeJson.getJSONArray("ingredients")
                val ingList = mutableListOf<String>()
                for (i in 0 until ingJsonArr.length()) {
                    ingList.add(ingJsonArr.getString(i))
                }
                
                val instJsonArr = recipeJson.getJSONArray("instructions")
                val instList = mutableListOf<String>()
                for (i in 0 until instJsonArr.length()) {
                    instList.add(instJsonArr.getString(i))
                }

                val generatedRecipe = Recipe(
                    title = title,
                    chefName = "Gemini Kitchen AI",
                    imageUrl = when (category.lowercase()) {
                        "breakfast" -> "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=600&auto=format&fit=crop"
                        "healthy" -> "https://images.unsplash.com/photo-1541532713592-79a0317b6b77?w=600&auto=format&fit=crop"
                        "desserts" -> "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600&auto=format&fit=crop"
                        "dinner" -> "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?w=600&auto=format&fit=crop"
                        else -> "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=600&auto=format&fit=crop"
                    },
                    prepTime = prepTime,
                    difficulty = difficulty,
                    category = category,
                    ingredientsString = ingList.joinToString("||"),
                    instructionsString = instList.joinToString("||")
                )

                // Save to local database so it displays in Discover immediately!
                repository.shareUserRecipe(generatedRecipe)

                // Retrieve the latest list of recipes and find the generated one to get its ID!
                delay(300) // Small delay to guarantee database insertion commit
                val allRecs = repository.allRecipesFlow.first()
                val savedRecipe = allRecs.find { it.title == title && it.chefName == "Gemini Kitchen AI" } ?: generatedRecipe

                _uiMessage.emit("Successfully generated chef-special: '$title'!")
                onCompletion(savedRecipe)

            } catch (e: Exception) {
                _recipeGenerationError.value = "Failed to parse recipe: ${e.localizedMessage}"
            } finally {
                _isGeneratingRecipe.value = false
            }
        }
    }
}

class CookingViewModelFactory(private val repository: CookingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CookingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CookingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
