package com.example.alarmko.data.model

import com.example.alarmko.R

enum class PhotoCategory(
    val stringRes: Int,
    val emoji: String
) {
    KITCHEN(R.string.category_kitchen, "🍳"),
    BATHROOM(R.string.category_bathroom, "🚿"),
    HEALTH(R.string.category_health, "💊"),
    WORKSPACE(R.string.category_workspace, "📚"),
    LIVING_ROOM(R.string.category_living_room, "🏠")
}

enum class PhotoObject(
    val stringRes: Int,
    val emoji: String,
    val category: PhotoCategory,
    val keywords: List<String>
) {
    // Кухня
    GLASS(R.string.object_glass, "🥛", PhotoCategory.KITCHEN,
        listOf("Cup", "Glass", "Mug", "Drink")),
    FRIDGE(R.string.object_fridge, "🧊", PhotoCategory.KITCHEN,
        listOf("Refrigerator", "Fridge", "Freezer", "Appliance")),
    PLATE(R.string.object_plate, "🍽️", PhotoCategory.KITCHEN,
        listOf("Plate", "Dish", "Bowl", "Tableware")),
    WATER_BOTTLE(R.string.object_water_bottle, "🍶", PhotoCategory.KITCHEN,
        listOf("Bottle", "Water bottle", "Plastic bottle", "Water",
            "Tableware", "Product", "Liquid", "Container", "Drinkware")),
    COFFEE_CUP(R.string.object_coffee_cup, "☕", PhotoCategory.KITCHEN,
        listOf("Coffee cup", "Coffee", "Mug", "Espresso")),

    // Баня
    TOOTHBRUSH(R.string.object_toothbrush, "🪥", PhotoCategory.BATHROOM,
        listOf("Toothbrush", "Brush", "Dental", "Teeth")),
    SOAP(R.string.object_soap, "🧼", PhotoCategory.BATHROOM,
        listOf("Soap", "Bar soap", "Hygiene")),
    TOWEL(R.string.object_towel, "🏊", PhotoCategory.BATHROOM,
        listOf("Towel", "Textile", "Cloth", "Fabric")),
    MIRROR(R.string.object_mirror, "🪞", PhotoCategory.BATHROOM,
        listOf("Mirror", "Glass", "Reflection")),

    // Здраве
    PILL(R.string.object_pill, "💊", PhotoCategory.HEALTH,
        listOf("Pill", "Tablet", "Capsule", "Medicine", "Drug")),
    THERMOMETER(R.string.object_thermometer, "🌡️", PhotoCategory.HEALTH,
        listOf("Thermometer", "Temperature", "Medical")),
    VITAMINS(R.string.object_vitamins, "💉", PhotoCategory.HEALTH,
        listOf("Vitamin", "Supplement", "Pill", "Capsule")),

    // Работно място
    NOTEBOOK(R.string.object_notebook, "📓", PhotoCategory.WORKSPACE,
        listOf("Notebook", "Book", "Paper", "Journal")),
    PEN(R.string.object_pen, "🖊️", PhotoCategory.WORKSPACE,
        listOf("Pen", "Pencil", "Writing", "Stationery")),
    KEYBOARD(R.string.object_keyboard, "⌨️", PhotoCategory.WORKSPACE,
        listOf("Keyboard", "Computer", "Input device", "Technology")),
    MOUSE(R.string.object_mouse, "🖱️", PhotoCategory.WORKSPACE,
        listOf("Mouse", "Computer mouse", "Input device", "Technology")),

    // Всекидневна
    KEYS(R.string.object_keys, "🔑", PhotoCategory.LIVING_ROOM,
        listOf("Key", "Keys", "Door key", "Metal")),
    BOOK(R.string.object_book, "📚", PhotoCategory.LIVING_ROOM,
        listOf("Book", "Textbook", "Novel", "Reading")),
    REMOTE(R.string.object_remote, "📺", PhotoCategory.LIVING_ROOM,
        listOf("Remote control", "Remote", "Controller", "Electronic")),
    WINDOW(R.string.object_window, "🪟", PhotoCategory.LIVING_ROOM,
        listOf("Window", "Glass", "Light", "Curtain"));

    companion object {
        fun getByCategory(category: PhotoCategory): List<PhotoObject> {
            return values().filter { it.category == category }
        }

        fun verifyLabels(
            labels: List<String>,
            targetObject: PhotoObject
        ): Boolean {
            return targetObject.keywords.any { keyword ->
                labels.any { label ->
                    label.contains(keyword, ignoreCase = true) ||
                            keyword.contains(label, ignoreCase = true)
                }
            }
        }
    }
}