package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_sessions")
data class UserSession(
    @PrimaryKey val email: String,
    val displayName: String,
    val photoUrl: String,
    val provider: String, // "Google", "Facebook", "Apple", "Email"
    val isLoggedIn: Boolean = true,
    val bio: String = "Savoring every recipe with heart and passion.",
    val dietaryPreference: String = "None" // "None", "Vegetarian", "Gluten-Free", "Vegan", "Keto"
)

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val chefName: String,
    val imageUrl: String,
    val prepTime: String, // e.g. "25 mins"
    val difficulty: String, // e.g. "Easy", "Medium", "Hard"
    val category: String, // e.g. "Breakfast", "Lunch", "Dinner", "Desserts", "Healthy"
    val ingredientsString: String, // delimited by "||"
    val instructionsString: String, // delimited by "||"
    val isFavorite: Boolean = false,
    val totalCooked: Int = 0
) {
    fun getIngredients(): List<String> {
        if (ingredientsString.isBlank()) return emptyList()
        return ingredientsString.split("||").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun getInstructions(): List<String> {
        if (instructionsString.isBlank()) return emptyList()
        return instructionsString.split("||").map { it.trim() }.filter { it.isNotEmpty() }
    }

    // Dynamic heuristics checking culinary tags
    fun isVegetarian(): Boolean {
        val lowercaseTitle = title.lowercase()
        val lowercaseIngredients = ingredientsString.lowercase()
        // Check for non-vegetarian ingredients/headings (e.g., chicken, beef, pork, steak, salmon, tuna, ham, fish, meat)
        return !(lowercaseTitle.contains("chicken") || lowercaseTitle.contains("beef") || 
                 lowercaseTitle.contains("pork") || lowercaseTitle.contains("steak") || 
                 lowercaseTitle.contains("salmon") || lowercaseTitle.contains("tuna") || 
                 lowercaseTitle.contains("bolognese") || lowercaseTitle.contains("meat") || 
                 lowercaseIngredients.contains("chicken") || lowercaseIngredients.contains("beef") || 
                 lowercaseIngredients.contains("pork") || lowercaseIngredients.contains("steak") || 
                 lowercaseIngredients.contains("bacon") || lowercaseIngredients.contains("ham") || 
                 lowercaseIngredients.contains("shrimp") || lowercaseIngredients.contains("salmon"))
    }

    fun isGlutenFree(): Boolean {
        val lowercaseTitle = title.lowercase()
        val lowercaseIngredients = ingredientsString.lowercase()
        // Check for wheat flour, semolina, pasta, bread, toast, croissant, unless labeled gluten-free
        if (lowercaseTitle.contains("gluten-free") || lowercaseTitle.contains("gluten free")) return true
        return !(lowercaseTitle.contains("sourdough") || lowercaseTitle.contains("pancakes") || 
                 lowercaseTitle.contains("pasta") || lowercaseTitle.contains("bread") || 
                 lowercaseTitle.contains("toast") || lowercaseTitle.contains("flour") || 
                 lowercaseIngredients.contains("flour") || lowercaseIngredients.contains("sourdough") || 
                 lowercaseIngredients.contains("pasta") || lowercaseIngredients.contains("bread") || 
                 lowercaseIngredients.contains("wheat"))
    }

    fun isVegan(): Boolean {
        val lowercaseTitle = title.lowercase()
        val lowercaseIngredients = ingredientsString.lowercase()
        // Check for dairy, cheese, eggs, honey, milk, cream, meat, chicken, beef
        return isVegetarian() && 
               !(lowercaseTitle.contains("cheese") || lowercaseTitle.contains("butter") || 
                 lowercaseTitle.contains("pancakes") || lowercaseTitle.contains("cream") || 
                 lowercaseIngredients.contains("butter") || lowercaseIngredients.contains("cream") || 
                 lowercaseIngredients.contains("milk") || lowercaseIngredients.contains("cheese") || 
                 lowercaseIngredients.contains("egg") || lowercaseIngredients.contains("honey") || 
                 lowercaseIngredients.contains("parmesan") || lowercaseIngredients.contains("mozzarella"))
    }
}

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String = "Groceries",
    val isBought: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "meal_plans")
data class MealPlan(
    @PrimaryKey val dayOfWeek: String, // "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    val breakfastRecipeId: Int? = null,
    val breakfastCustomName: String? = null,
    val lunchRecipeId: Int? = null,
    val lunchCustomName: String? = null,
    val dinnerRecipeId: Int? = null,
    val dinnerCustomName: String? = null
)

@Entity(tableName = "recipe_comments")
data class RecipeComment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipeId: Int,
    val authorName: String,
    val authorPhoto: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "recipe_likes")
data class RecipeLike(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipeId: Int,
    val userEmail: String
)
