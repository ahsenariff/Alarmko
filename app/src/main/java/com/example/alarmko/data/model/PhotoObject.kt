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
        listOf("Cup", "Glass", "Mug", "Drink", "Drinkware",
            "Tableware", "Vessel", "Container", "Tumbler")),
    FRIDGE(R.string.object_fridge, "🧊", PhotoCategory.KITCHEN,
        listOf("Refrigerator", "Fridge", "Freezer", "Appliance",
            "Home appliance", "Kitchen appliance", "Major appliance")),
    PLATE(R.string.object_plate, "🍽️", PhotoCategory.KITCHEN,
        listOf("Plate", "Dish", "Bowl", "Tableware", "Platter",
            "Dinnerware", "Serveware", "Food")),
    WATER_BOTTLE(R.string.object_water_bottle, "🍶", PhotoCategory.KITCHEN,
        listOf("Bottle", "Water bottle", "Plastic bottle", "Water",
            "Tableware", "Product", "Liquid", "Container", "Drinkware", "Drink",
             "Jug", "Flask")),
    COFFEE_CUP(R.string.object_coffee_cup, "☕", PhotoCategory.KITCHEN,
        listOf("Coffee cup", "Coffee", "Mug", "Espresso", "Cup",
            "Drink", "Hot drink", "Caffeine", "Tableware", "Drinkware")),

    // Баня
    TOOTHBRUSH(R.string.object_toothbrush, "🪥", PhotoCategory.BATHROOM,
        listOf("Toothbrush", "Brush", "Dental", "Teeth", "Oral care",
            "Hygiene", "Bathroom", "Personal care")),
    SOAP(R.string.object_soap, "🧼", PhotoCategory.BATHROOM,
        listOf("Soap", "Bar soap", "Hygiene", "Cleaner", "Cleaning",
            "Personal care", "Bathroom", "Washing")),
    TOWEL(R.string.object_towel, "🏊", PhotoCategory.BATHROOM,
        listOf("Towel", "Textile", "Cloth", "Fabric", "Linens",
            "Bath towel", "Hand towel", "Bathroom")),
    MIRROR(R.string.object_mirror, "🪞", PhotoCategory.BATHROOM,
        listOf("Mirror", "Glass", "Reflection", "Bathroom",
            "Looking glass", "Wall")),

    // Здраве
    PILL(R.string.object_pill, "💊", PhotoCategory.HEALTH,
        listOf("Pill", "Tablet", "Capsule", "Medicine", "Drug", "Pharmaceutical", "Medication", "Medical", "Health",
            "Vitamin", "Supplement", "Paper", "Tableware", "Pattern")),
    THERMOMETER(R.string.object_thermometer, "🌡️", PhotoCategory.HEALTH,
        listOf("Thermometer", "Temperature", "Medical", "Health",
            "Fever", "Measurement", "Device", "Instrument")),
    VITAMINS(R.string.object_vitamins, "💉", PhotoCategory.HEALTH,
        listOf("Vitamin", "Supplement", "Pill", "Capsule", "Tablet",
            "Medicine", "Health", "Nutrition", "Bottle", "Tableware")),

    // Работно място
    NOTEBOOK(R.string.object_notebook, "📓", PhotoCategory.WORKSPACE,
        listOf("Notebook", "Book", "Paper", "Journal", "Diary",
            "Writing", "Stationery", "Office", "Spiral notebook")),
    PEN(R.string.object_pen, "🖊️", PhotoCategory.WORKSPACE,
        listOf("Pen", "Pencil", "Writing", "Stationery", "Office",
            "Ballpoint", "Marker", "Instrument", "Writing implement")),
    KEYBOARD(R.string.object_keyboard, "⌨️", PhotoCategory.WORKSPACE,
        listOf("Keyboard", "Computer", "Input device", "Technology", "Office", "Typing", "Electronic", "Computer keyboard")),
    MOUSE(R.string.object_mouse, "🖱️", PhotoCategory.WORKSPACE,
        listOf("Mouse", "Computer mouse", "Input device", "Technology", "Office", "Electronic", "Computer", "Peripheral")),

    // Всекидневна
    KEYS(R.string.object_keys, "🔑", PhotoCategory.LIVING_ROOM,
        listOf("Key", "Keys", "Door key", "Metal", "Lock",
            "Security", "House key", "Keychain")),
    BOOK(R.string.object_book, "📚", PhotoCategory.LIVING_ROOM,
        listOf("Book", "Textbook", "Novel", "Reading", "Literature",
            "Publication", "Paperback", "Hardcover", "Text")),
    REMOTE(R.string.object_remote, "📺", PhotoCategory.LIVING_ROOM,
        listOf("Remote control", "Remote", "Controller", "Electronic", "Television", "TV", "Device", "Gadget")),
    WINDOW(R.string.object_window, "🪟", PhotoCategory.LIVING_ROOM,
        listOf("Window", "Glass", "Light", "Curtain", "Blinds",
            "Architecture", "Building", "Wall"));

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