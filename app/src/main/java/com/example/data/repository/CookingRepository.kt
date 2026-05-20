package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class CookingRepository(private val database: CookingDatabase) {

    private val userSessionDao = database.userSessionDao()
    private val recipeDao = database.recipeDao()
    private val shoppingDao = database.shoppingDao()
    private val mealPlanDao = database.mealPlanDao()
    private val recipeCommentDao = database.recipeCommentDao()
    private val recipeLikeDao = database.recipeLikeDao()

    // Flow Streams
    val activeSessionFlow: Flow<UserSession?> = userSessionDao.getActiveSessionFlow()
    val allRecipesFlow: Flow<List<Recipe>> = recipeDao.getAllRecipes()
    val favoriteRecipesFlow: Flow<List<Recipe>> = recipeDao.getFavoriteRecipes()
    val shoppingListFlow: Flow<List<ShoppingItem>> = shoppingDao.getShoppingList()
    val mealPlansFlow: Flow<List<MealPlan>> = mealPlanDao.getMealPlansFlow()

    // Comments & Social Like Flow Access
    fun getCommentsForRecipeFlow(recipeId: Int): Flow<List<RecipeComment>> = recipeCommentDao.getCommentsForRecipeFlow(recipeId)
    fun getLikesForRecipeFlow(recipeId: Int): Flow<List<RecipeLike>> = recipeLikeDao.getLikesForRecipeFlow(recipeId)
    fun hasUserLikedRecipeFlow(recipeId: Int, email: String): Flow<Boolean> = recipeLikeDao.hasUserLikedRecipeFlow(recipeId, email)

    suspend fun getActiveSession(): UserSession? {
        return userSessionDao.getActiveSession()
    }

    // Authentication Operations
    suspend fun login(email: String, displayName: String, provider: String, photoUrl: String) {
        withContext(Dispatchers.IO) {
            userSessionDao.clearActiveSessions()
            val session = UserSession(
                email = email,
                displayName = displayName,
                photoUrl = photoUrl,
                provider = provider,
                isLoggedIn = true
            )
            userSessionDao.insertSession(session)
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            userSessionDao.clearActiveSessions()
        }
    }

    // Community Profile Operations
    suspend fun updateUserProfile(email: String, displayName: String, photoUrl: String, bio: String, dietaryPreference: String) {
        withContext(Dispatchers.IO) {
            val session = UserSession(
                email = email,
                displayName = displayName,
                photoUrl = photoUrl,
                provider = "Email",
                isLoggedIn = true,
                bio = bio,
                dietaryPreference = dietaryPreference
            )
            userSessionDao.insertSession(session)
        }
    }

    // Recipe Sharing Social Interactions
    suspend fun addCommentToRecipe(recipeId: Int, authorName: String, authorPhoto: String, text: String) {
        withContext(Dispatchers.IO) {
            val comment = RecipeComment(
                recipeId = recipeId,
                authorName = authorName,
                authorPhoto = authorPhoto,
                text = text
            )
            recipeCommentDao.insertComment(comment)
        }
    }

    suspend fun toggleRecipeSocialLike(recipeId: Int, email: String) {
        withContext(Dispatchers.IO) {
            val likes = recipeLikeDao.getLikesForRecipeFlow(recipeId).first()
            val hasLiked = likes.any { it.userEmail == email }
            if (hasLiked) {
                recipeLikeDao.removeLike(recipeId, email)
            } else {
                recipeLikeDao.insertLike(RecipeLike(recipeId = recipeId, userEmail = email))
            }
        }
    }

    suspend fun shareUserRecipe(recipe: Recipe) {
        withContext(Dispatchers.IO) {
            recipeDao.insertRecipe(recipe)
        }
    }

    // Recipe Operations
    suspend fun setRecipeFavorite(recipeId: Int, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            recipeDao.setFavorite(recipeId, isFavorite)
        }
    }

    suspend fun incrementCookCount(recipeId: Int) {
        withContext(Dispatchers.IO) {
            recipeDao.incrementCooked(recipeId)
        }
    }

    // Shopping List Operations
    suspend fun addShoppingItem(name: String, category: String = "Groceries") {
        withContext(Dispatchers.IO) {
            shoppingDao.insertItem(ShoppingItem(name = name, category = category))
        }
    }

    suspend fun updateShoppingItem(item: ShoppingItem) {
        withContext(Dispatchers.IO) {
            shoppingDao.updateItem(item)
        }
    }

    suspend fun deleteShoppingItem(item: ShoppingItem) {
        withContext(Dispatchers.IO) {
            shoppingDao.deleteItem(item)
        }
    }

    suspend fun clearCompletedShoppingItems() {
        withContext(Dispatchers.IO) {
            shoppingDao.clearBoughtItems()
        }
    }

    // Meal Planning Operations
    suspend fun saveMealPlan(plan: MealPlan) {
        withContext(Dispatchers.IO) {
            mealPlanDao.insertMealPlan(plan)
        }
    }

    // Data Seeding
    suspend fun seedDatabaseIfEmpty() {
        withContext(Dispatchers.IO) {
            val existing = allRecipesFlow.first()
            if (existing.isEmpty()) {
                val seedRecipes = listOf(
                    Recipe(
                        title = "Garlic Butter Tuscan Chicken",
                        chefName = "Chef Isabella Romano",
                        imageUrl = "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?w=600&auto=format&fit=crop",
                        prepTime = "30 mins",
                        difficulty = "Medium",
                        category = "Dinner",
                        ingredientsString = "4 Chicken Breasts||3 cloves Garlic minced||1/2 cup Heavy Cream||1/2 cup Chicken Broth||1/2 cup Sundried Tomatoes drained||2 cups Baby Spinach fresh||2 tbsp Butter||1/2 cup Grated Parmesan||1 tsp Dried Oregano||2 tbsp Olive Oil||Salt & Pepper to taste",
                        instructionsString = "Season chicken breasts generously with salt, pepper, and oregano.||In a large skillet, melt butter with olive oil over medium-high heat. Sear chicken for 5-6 minutes per side until golden and cooked through. Remove chicken and wrap in foil to keep warm.||In the same skillet, add minced garlic and saute for 1 minute until fragrant. Add chicken broth, heavy cream, and grated parmesan cheese. Bring to a simmer for 3 minutes until sauce begins to thicken.||Stir in sundried tomatoes and baby spinach. Simmer for 2 minutes until spinach is fully wilted.||Return the chicken to the skillet, spooning the rich sauce over each breast. Simmer for another 2-3 minutes to allow flavors to meld together. Serve hot with pasta or crusty bread!",
                        isFavorite = true,
                        totalCooked = 12
                    ),
                    Recipe(
                        title = "Golden Buttermilk Pancakes",
                        chefName = "Grandpa Jack",
                        imageUrl = "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=600&auto=format&fit=crop",
                        prepTime = "20 mins",
                        difficulty = "Easy",
                        category = "Breakfast",
                        ingredientsString = "2 cups All-Purpose Flour||2 tbsp Sugar||2 tsp Baking Powder||1 tsp Baking Soda||1/2 tsp Salt||2 cups Real Buttermilk||2 Large Eggs||1/4 cup Melted Butter||1 tsp Vanilla Extract||Maple Syrup for serving",
                        instructionsString = "Whisk the flour, sugar, baking powder, baking soda, and salt together in a large mixing bowl.||In a separate jug, whisk buttermilk, eggs, melted butter, and vanilla extract until fully blended.||Pour the wet ingredients into the dry mixture. Use a wooden spoon to fold gently until just combined. The batter should still be relatively lumpy for fluffiest results.||Heat a non-stick griddle over medium heat and grease lightly with butter. Pour 1/4 cup of batter per pancake.||Cook until healthy bubbles form on the surface and edges look set (about 3 mins). Flip and cook for another 2 mins until golden brown. Serve hot with butter and real maple syrup!",
                        isFavorite = false,
                        totalCooked = 45
                    ),
                    Recipe(
                        title = "Avocado Caprese Sourdough",
                        chefName = "Artisan Baker Liam",
                        imageUrl = "https://images.unsplash.com/photo-1541532713592-79a0317b6b77?w=600&auto=format&fit=crop",
                        prepTime = "10 mins",
                        difficulty = "Easy",
                        category = "Healthy",
                        ingredientsString = "2 thick slices Sourdough Bread||1 ripe Avocado||1 large Heirloom Tomato sliced||4-6 slices Fresh Mozzarella||Fresh Basil leaves||2 tbsp Extra Virgin Olive Oil||1 tbsp Balsamic Glaze||1 clove Garlic peeled||Flaky Sea Salt & Chili flakes",
                        instructionsString = "Pop sourdough slices into a toaster or grill on a skillet with olive oil until beautifully charred and crispy.||Gently rub the peeled raw garlic clove over the warm, toasted bread to impart a subtle, authentic garlic flavor.||Halve the avocado, scoop out the flesh into a bowl, and mash with olive oil, flaky salt, and chili flakes. Spread thickly onto sourdough toasts.||Layer fresh tomato slices and fresh mozzarella cheese over the mashed avocado.||Top with torn basil leaves, drizzle generously with balsamic glaze, and sprinkle more flaky sea salt on top before serving.",
                        isFavorite = true,
                        totalCooked = 8
                    ),
                    Recipe(
                        title = "Decadent Chocolate Lava Cakes",
                        chefName = "Chocolatier Sophie",
                        imageUrl = "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600&auto=format&fit=crop",
                        prepTime = "25 mins",
                        difficulty = "Hard",
                        category = "Desserts",
                        ingredientsString = "4 oz Premium Semi-Sweet Chocolate||1/2 cup Unsalted Butter||2 Large Eggs||2 Egg Yolks||1/4 cup White Sugar||1/8 tsp Salt||2 tbsp All-Purpose Flour||Powdered sugar for dusting||Vanilla ice cream",
                        instructionsString = "Preheat oven to 425 degrees F (218 degrees C). Butter four 6-ounce ramekins generously and dust with cocoa powder, tapping out any excess.||Melt the chocolate and butter together in a double boiler over low heat, or in a microwave using 20-second bursts. Stir until silky smooth.||In a medium bowl, whisk together the eggs, egg yolks, sugar, and salt at high speed until thick, pale yellow, and airy (about 4 mins).||Gently fold the melted chocolate and butter, along with the flour, into the egg mixture. Fold until just uniform; do not overmix.||Divide batter evenly among prepared ramekins. Bake for 12-14 minutes, until the cake edges are firm but center is soft and jiggly.||Cool in ramekins for 1 minute, place an inversion plate on top, flip carefully, and lift the ramekin. Serve warm dusted with powdered sugar and a scoop of vanilla bean ice cream!",
                        isFavorite = false,
                        totalCooked = 3
                    ),
                    Recipe(
                        title = "Slow-Cooked Beef Bolognese",
                        chefName = "Nonna Maria",
                        imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=600&auto=format&fit=crop",
                        prepTime = "120 mins",
                        difficulty = "Hard",
                        category = "Dinner",
                        ingredientsString = "1 lb Ground Beef (80/20)||1/2 lb Ground Pork||1 Large Yellow Onion finely chopped||2 Carrots finely chopped||2 Celery ribs finely chopped||4 cloves Garlic minced||1 cup Whole Milk||1 cup Dry White Wine||28 oz San Marzano Tomatoes crushed||2 tbsp Tomato Paste||Fresh Oregano & Rosemary||Tagliatelle pasta for serving",
                        instructionsString = "In a heavy-bottomed Dutch oven, heat olive oil over medium heat. Sauté onion, carrots, and celery (soffritto) until soft and sweet (about 8 mins).||Add minced garlic and cook for 1 minute. Add ground beef and ground pork, breaking up with a wooden spoon. Brown thoroughly until caramelized, pouring off excess fat if preferred.||Pour in the dry white wine and simmer until alcohol scent dissipates and wine reduces completely (about 5-8 mins).||Stir in milk and simmer gently until fully absorbed by the meat. This tenderizes the beef. Then stir in tomato paste and cook for 2 mins.||Add the crushed San Marzano tomatoes and whole sprigs of fresh oregano and rosemary. Reduce heat to very low, cover slightly, and let low-simmer for at least 1.5 to 2 hours, stirring occasionally. Add water or broth if it gets too thick.||Serve tossed over fresh al dente Tagliatelle with standard shaved Parmigiano-Reggiano on top!",
                        isFavorite = false,
                        totalCooked = 27
                    ),
                    Recipe(
                        title = "Vibrant Acai Bowl",
                        chefName = "Nutritionist Kai",
                        imageUrl = "https://images.unsplash.com/photo-1590301157890-4810ed352733?w=600&auto=format&fit=crop",
                        prepTime = "15 mins",
                        difficulty = "Easy",
                        category = "Healthy",
                        ingredientsString = "2 unsweetened frozen Acai packets||1/2 cup Unsweetened Almond Milk||1 Frozen Banana||1 cup Frozen Mixed Berries||1 tbsp Organic Almond Butter||Topping: Granola||Topping: Chia seeds||Topping: Fresh Strawberries sliced||Topping: Fresh Blueberries||Topping: Shaved coconut",
                        instructionsString = "Slightly thaw frozen acai packets under warm water for 5 seconds, cut open, and break into chunks.||Add acai chunks, frozen banana, frozen mixed berries, almond milk, and almond butter into a high-powered blender.||Blend on low, using the tamper to push ingredients into the blades, increasing speed slowly until the mixture is thick, frosty, and spoonable (like sorbet). Add a splash more almond milk only if absolutely necessary.||Spoon the thick blended acai immediately into a serving bowl.||Style your toppings beautifully in clean, parallel stripes across the surface: crunch granola, fresh strawberries, fresh blueberries, chia seeds, and coconut shavings. Enjoy immediately with a spoon!",
                        isFavorite = true,
                        totalCooked = 18
                    )
                )
                recipeDao.insertRecipes(seedRecipes)
            }

            // Seed default meal plans if empty
            val existingPlans = mealPlansFlow.first()
            if (existingPlans.isEmpty()) {
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                days.forEach { day ->
                    mealPlanDao.insertMealPlan(
                        MealPlan(
                            dayOfWeek = day,
                            breakfastRecipeId = if (day == "Mon" || day == "Sat") 2 else null,
                            lunchRecipeId = if (day == "Wed" || day == "Sun") 3 else null,
                            dinnerRecipeId = if (day == "Mon" || day == "Fri") 1 else null
                        )
                    )
                }
            }
        }
    }
}
