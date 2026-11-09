package com.pulse.app

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import kotlin.random.Random

enum class FoodCat(val label: String) {
    VERDURA("Verduras"),
    FRUTA("Frutas"),
    PROTEINA("Proteínas"),
    LACTEO("Lácteos"),
    GRANO("Granos, harinas y tubérculos"),
    AZUCAR("Azúcares y bebidas"),
    GRASA("Grasas y semillas"),
    OTRO("Otros"),
}

/**
 * unit: lb, unid, L, paq, lata, frasco, caja, bolsa, manojo.
 * lbPerUnit: libras aproximadas por unidad (1 si la unidad es lb).
 * perish: días aproximados antes de que se eche a perder (para usar primero lo perecedero).
 * max: tope recomendado por quincena para 2 adultos y 2 niños pequeños.
 */
data class FoodItem(
    val id: String,
    val name: String,
    val cat: FoodCat,
    val unit: String,
    val step: Double,
    val lbPerUnit: Double,
    val perish: Int,
    val max: Double? = null,
)

data class CustomFood(val id: String, val name: String, val cat: FoodCat, val unit: String)

enum class MealType(val label: String) { DESAYUNO("Desayuno"), ALMUERZO("Almuerzo"), MERIENDA("Merienda"), CENA("Cena") }

data class PlannedMeal(val type: MealType, val recipeId: String, val missing: List<String>)
data class MenuDay(val day: Long, val meals: List<PlannedMeal>)

data class FoodData(
    val quincenaStart: Long? = null,
    val pantry: Map<String, Double> = emptyMap(),
    val custom: List<CustomFood> = emptyList(),
    val menu: List<MenuDay> = emptyList(),
    val seed: Int = 1,
)

const val ANY_VEG = "*veg"
const val ANY_FRUIT = "*fruit"
const val VEG_TARGET_LB = 25.0
const val FRUIT_TARGET_LB = 20.0

data class Ing(val food: String, val qty: Double, val optional: Boolean = false)
data class Recipe(val id: String, val name: String, val type: MealType, val protein: String, val ings: List<Ing>, val how: String, val note: String? = null)

private fun f(id: String, name: String, cat: FoodCat, unit: String, step: Double, lbPerUnit: Double, perish: Int, max: Double? = null) =
    FoodItem(id, name, cat, unit, step, lbPerUnit, perish, max)

val CATALOG: List<FoodItem> = listOf(
    f("tomate", "Tomate", FoodCat.VERDURA, "lb", 0.5, 1.0, 7),
    f("cebolla", "Cebolla", FoodCat.VERDURA, "lb", 0.5, 1.0, 30),
    f("chile", "Chile dulce", FoodCat.VERDURA, "unid", 1.0, 0.3, 10),
    f("zanahoria", "Zanahoria", FoodCat.VERDURA, "lb", 0.5, 1.0, 21),
    f("repollo", "Repollo", FoodCat.VERDURA, "unid", 1.0, 2.5, 14),
    f("lechuga", "Lechuga", FoodCat.VERDURA, "unid", 1.0, 1.0, 5),
    f("pepino", "Pepino", FoodCat.VERDURA, "unid", 1.0, 0.6, 7),
    f("brocoli", "Brócoli", FoodCat.VERDURA, "lb", 0.5, 1.0, 7),
    f("chayote", "Pataste (chayote)", FoodCat.VERDURA, "unid", 1.0, 0.7, 14),
    f("ayote", "Ayote", FoodCat.VERDURA, "lb", 1.0, 1.0, 21),
    f("espinaca", "Espinaca", FoodCat.VERDURA, "manojo", 1.0, 0.5, 4),
    f("ejotes", "Ejotes", FoodCat.VERDURA, "lb", 0.5, 1.0, 7),
    f("pipian", "Pipián", FoodCat.VERDURA, "lb", 0.5, 1.0, 7),
    f("remolacha", "Remolacha", FoodCat.VERDURA, "lb", 0.5, 1.0, 21),
    f("champinones", "Champiñones", FoodCat.VERDURA, "lb", 0.5, 1.0, 5),
    f("culantro", "Culantro", FoodCat.VERDURA, "manojo", 1.0, 0.1, 5),
    f("ajo", "Ajo", FoodCat.VERDURA, "unid", 1.0, 0.1, 60),

    f("banano", "Banano", FoodCat.FRUTA, "unid", 1.0, 0.35, 6),
    f("manzana", "Manzana", FoodCat.FRUTA, "unid", 1.0, 0.4, 21),
    f("naranja", "Naranja", FoodCat.FRUTA, "unid", 1.0, 0.5, 14),
    f("mango", "Mango", FoodCat.FRUTA, "unid", 1.0, 0.8, 7),
    f("papaya", "Papaya", FoodCat.FRUTA, "unid", 1.0, 3.0, 5),
    f("pina", "Piña", FoodCat.FRUTA, "unid", 1.0, 3.0, 6),
    f("sandia", "Sandía", FoodCat.FRUTA, "unid", 1.0, 10.0, 10),
    f("melon", "Melón", FoodCat.FRUTA, "unid", 1.0, 4.0, 7),
    f("fresas", "Fresas", FoodCat.FRUTA, "lb", 0.5, 1.0, 4),
    f("uvas", "Uvas", FoodCat.FRUTA, "lb", 0.5, 1.0, 7),
    f("guayaba", "Guayaba", FoodCat.FRUTA, "unid", 1.0, 0.3, 6),
    f("limon", "Limón", FoodCat.FRUTA, "unid", 1.0, 0.15, 21),

    f("huevo", "Huevos", FoodCat.PROTEINA, "unid", 6.0, 0.12, 30),
    f("pollo", "Pollo (pechuga o piezas)", FoodCat.PROTEINA, "lb", 1.0, 1.0, 5),
    f("res", "Carne de res", FoodCat.PROTEINA, "lb", 1.0, 1.0, 5),
    f("molida", "Carne molida", FoodCat.PROTEINA, "lb", 1.0, 1.0, 4),
    f("cerdo", "Lomo de cerdo", FoodCat.PROTEINA, "lb", 1.0, 1.0, 5),
    f("pescado", "Pescado", FoodCat.PROTEINA, "lb", 1.0, 1.0, 3),
    f("atun", "Atún en agua", FoodCat.PROTEINA, "lata", 1.0, 0.33, 365),
    f("frijoles", "Frijoles", FoodCat.PROTEINA, "lb", 1.0, 1.0, 365),
    f("lentejas", "Lentejas", FoodCat.PROTEINA, "lb", 1.0, 1.0, 365),

    f("leche", "Leche", FoodCat.LACTEO, "L", 1.0, 2.2, 7),
    f("queso", "Queso", FoodCat.LACTEO, "lb", 0.5, 1.0, 14),
    f("yogur", "Yogur natural", FoodCat.LACTEO, "L", 1.0, 2.2, 14),

    f("tortilla", "Tortillas de maíz (paquete de 10)", FoodCat.GRANO, "paq", 1.0, 1.0, 7, max = 12.0),
    f("tortilla_harina", "Tortillas de harina (paquete)", FoodCat.GRANO, "paq", 1.0, 1.0, 10, max = 3.0),
    f("arroz", "Arroz", FoodCat.GRANO, "lb", 1.0, 1.0, 365, max = 5.0),
    f("pan_blanco", "Pan blanco de molde", FoodCat.GRANO, "paq", 1.0, 1.3, 7, max = 2.0),
    f("pan_integral", "Pan integral", FoodCat.GRANO, "paq", 1.0, 1.3, 7, max = 3.0),
    f("avena", "Avena", FoodCat.GRANO, "lb", 1.0, 1.0, 180),
    f("pasta", "Pasta", FoodCat.GRANO, "lb", 1.0, 1.0, 365, max = 2.0),
    f("harina", "Harina de trigo", FoodCat.GRANO, "lb", 1.0, 1.0, 180, max = 2.0),
    f("maseca", "Harina de maíz", FoodCat.GRANO, "lb", 1.0, 1.0, 180, max = 4.0),
    f("cereal", "Cereal azucarado", FoodCat.GRANO, "caja", 1.0, 0.8, 90, max = 1.0),
    f("papa", "Papa", FoodCat.GRANO, "lb", 1.0, 1.0, 21),
    f("yuca", "Yuca", FoodCat.GRANO, "lb", 1.0, 1.0, 7),
    f("platano", "Plátano", FoodCat.GRANO, "unid", 1.0, 0.6, 7, max = 15.0),
    f("elote", "Elote", FoodCat.GRANO, "unid", 1.0, 0.6, 5),

    f("azucar", "Azúcar", FoodCat.AZUCAR, "lb", 1.0, 1.0, 365, max = 2.0),
    f("gaseosa", "Gaseosas", FoodCat.AZUCAR, "L", 1.0, 2.2, 90, max = 2.0),
    f("jugo", "Jugos envasados", FoodCat.AZUCAR, "L", 1.0, 2.2, 30, max = 2.0),
    f("pan_dulce", "Pan dulce o repostería", FoodCat.AZUCAR, "paq", 1.0, 1.0, 7, max = 2.0),
    f("galletas", "Galletas", FoodCat.AZUCAR, "paq", 1.0, 0.5, 90, max = 2.0),
    f("dulces", "Dulces y golosinas", FoodCat.AZUCAR, "bolsa", 1.0, 0.5, 180, max = 1.0),

    f("aguacate", "Aguacate", FoodCat.GRASA, "unid", 1.0, 0.6, 5),
    f("aceite", "Aceite", FoodCat.GRASA, "L", 1.0, 2.0, 365),
    f("mani", "Maní", FoodCat.GRASA, "lb", 0.5, 1.0, 120),
    f("mantequilla_mani", "Mantequilla de maní", FoodCat.GRASA, "frasco", 1.0, 1.0, 120),
)

private val catalogById = CATALOG.associateBy { it.id }

fun unitLabel(u: String): String = when (u) {
    "unid" -> "unid."
    "paq" -> "paq."
    else -> u
}

fun unitWord(u: String): String = when (u) {
    "unid" -> "unidades"
    "paq" -> "paquetes"
    "L" -> "litros"
    "caja" -> "cajas"
    "bolsa" -> "bolsas"
    "lata" -> "latas"
    "frasco" -> "frascos"
    "manojo" -> "manojos"
    else -> u
}

fun shortName(id: String, food: FoodData): String = foodName(id, food).substringBefore(" (").lowercase()

fun foodName(id: String, food: FoodData): String =
    catalogById[id]?.name ?: food.custom.firstOrNull { it.id == id }?.name ?: id

private fun catOf(id: String, food: FoodData): FoodCat? = catalogById[id]?.cat ?: food.custom.firstOrNull { it.id == id }?.cat

private fun lbPer(id: String, food: FoodData): Double {
    catalogById[id]?.let { return if (it.unit == "lb") 1.0 else it.lbPerUnit }
    val c = food.custom.firstOrNull { it.id == id } ?: return 0.5
    return if (c.unit == "lb") 1.0 else 0.5
}

private fun perishOf(id: String): Int = catalogById[id]?.perish ?: 14

fun composition(food: FoodData): Map<FoodCat, Double> {
    val out = mutableMapOf<FoodCat, Double>()
    food.pantry.forEach { (id, q) ->
        val c = catOf(id, food) ?: return@forEach
        out[c] = (out[c] ?: 0.0) + q * lbPer(id, food)
    }
    return out
}

data class FoodAlert(val level: Int, val text: String)

fun foodAlerts(food: FoodData): List<FoodAlert> {
    if (food.pantry.isEmpty()) return emptyList()
    val out = mutableListOf<FoodAlert>()
    food.pantry.forEach { (id, q) ->
        val item = catalogById[id] ?: return@forEach
        val max = item.max ?: return@forEach
        if (q > max) {
            val level = if (q >= max * 1.5) 2 else 1
            out.add(FoodAlert(level, "${item.name.substringBefore(" (")}: llevas ${fmtNum(q)} ${unitWord(item.unit)} y la referencia para la quincena es no pasar de ${fmtNum(max)}."))
        }
    }
    val drinks = (food.pantry["gaseosa"] ?: 0.0) + (food.pantry["jugo"] ?: 0.0)
    if (drinks > 0 && out.none { it.text.startsWith("Gaseosas") || it.text.startsWith("Jugos") }) {
        out.add(FoodAlert(1, "Las gaseosas y los jugos envasados son azúcar líquida, lo que más sube los triglicéridos. Agua o fresco natural sin azúcar es mejor."))
    }
    val comp = composition(food)
    val veg = comp[FoodCat.VERDURA] ?: 0.0
    val fruit = comp[FoodCat.FRUTA] ?: 0.0
    val carbs = (comp[FoodCat.GRANO] ?: 0.0) + (comp[FoodCat.AZUCAR] ?: 0.0)
    if (veg < VEG_TARGET_LB * 0.6) out.add(FoodAlert(1, "Faltan verduras: llevas ${fmtNum(veg)} lb de unas ${fmtNum(VEG_TARGET_LB)} lb recomendadas para la quincena."))
    if (fruit < FRUIT_TARGET_LB * 0.6) out.add(FoodAlert(1, "Faltan frutas: llevas ${fmtNum(fruit)} lb de unas ${fmtNum(FRUIT_TARGET_LB)} lb recomendadas."))
    if (carbs > veg + fruit && carbs > 5) out.add(FoodAlert(2, "La compra tiene más harinas, tubérculos y azúcares (${fmtNum(carbs)} lb) que frutas y verduras juntas (${fmtNum(veg + fruit)} lb)."))
    return out.sortedByDescending { it.level }
}

val RECIPES: List<Recipe> = listOf(
    Recipe("d_huevos_tomate", "Huevos revueltos con tomate y cebolla", MealType.DESAYUNO, "huevo",
        listOf(Ing("huevo", 8.0), Ing("tomate", 0.5), Ing("cebolla", 0.25, true), Ing("tortilla", 0.5), Ing("aguacate", 1.0, true)),
        "Sofríe el tomate y la cebolla en poco aceite y agrega los huevos batidos. Con tortilla y aguacate."),
    Recipe("d_tipico", "Desayuno típico ligero", MealType.DESAYUNO, "huevo",
        listOf(Ing("huevo", 6.0), Ing("frijoles", 0.5), Ing("queso", 0.25), Ing("tortilla", 0.5), Ing("aguacate", 1.0, true)),
        "Huevo estrellado con poco aceite, frijoles enteros en vez de fritos, un trozo de queso y aguacate."),
    Recipe("d_avena", "Avena con banano y canela", MealType.DESAYUNO, "lacteo",
        listOf(Ing("avena", 0.5), Ing("leche", 1.0), Ing("banano", 3.0)),
        "Cocina la avena en leche con canela, sin azúcar. El banano en rodajas le da el dulce."),
    Recipe("d_omelette", "Omelette de verduras", MealType.DESAYUNO, "huevo",
        listOf(Ing("huevo", 8.0), Ing(ANY_VEG, 0.75), Ing("queso", 0.2, true), Ing("tortilla", 0.4, true)),
        "Pica las verduras que tengas (chile, tomate, espinaca, cebolla) y cocínalas con el huevo."),
    Recipe("d_yogur", "Yogur natural con fruta y avena", MealType.DESAYUNO, "lacteo",
        listOf(Ing("yogur", 1.0), Ing(ANY_FRUIT, 1.5), Ing("avena", 0.25)),
        "Yogur sin azúcar con fruta picada y avena cruda o tostada en sartén."),
    Recipe("d_pan_huevo", "Pan integral con huevo y aguacate", MealType.DESAYUNO, "huevo",
        listOf(Ing("pan_integral", 0.5), Ing("huevo", 6.0), Ing("aguacate", 1.0, true), Ing("tomate", 0.3, true)),
        "Una rebanada de pan integral por persona con huevo y aguacate encima."),
    Recipe("d_licuado", "Licuado de fruta con huevo duro", MealType.DESAYUNO, "huevo",
        listOf(Ing("leche", 1.0), Ing(ANY_FRUIT, 1.5), Ing("huevo", 6.0), Ing("avena", 0.2, true)),
        "Licúa la leche con fruta, sin azúcar. Acompaña con un huevo duro por persona."),
    Recipe("d_platano", "Frijoles con queso, plátano y tomate", MealType.DESAYUNO, "frijol",
        listOf(Ing("frijoles", 0.5), Ing("queso", 0.3), Ing("platano", 2.0), Ing("tomate", 0.4)),
        "Plátano asado o cocido en vez de frito, con frijoles enteros, queso y rodajas de tomate."),

    Recipe("a_pollo_guisado", "Pollo guisado con verduras", MealType.ALMUERZO, "pollo",
        listOf(Ing("pollo", 2.0), Ing("papa", 1.0), Ing("zanahoria", 0.5), Ing(ANY_VEG, 0.5), Ing("arroz", 0.5)),
        "Guisa el pollo con tomate, cebolla y chile; agrega papa, zanahoria y las verduras al final."),
    Recipe("a_pollo_ensalada", "Pollo a la plancha con ensalada", MealType.ALMUERZO, "pollo",
        listOf(Ing("pollo", 2.0), Ing("lechuga", 1.0), Ing("tomate", 0.5), Ing("pepino", 1.0), Ing("arroz", 0.5), Ing("limon", 2.0, true)),
        "Pollo a la plancha con limón y ajo; ensalada grande de lechuga, tomate y pepino."),
    Recipe("a_res_guisada", "Carne de res guisada con verduras", MealType.ALMUERZO, "res",
        listOf(Ing("res", 2.0), Ing(ANY_VEG, 1.5), Ing("tortilla", 0.6)),
        "Res en trozos guisada a fuego lento con las verduras que tengas."),
    Recipe("a_sopa_frijol", "Sopa de frijoles con huevo y queso", MealType.ALMUERZO, "frijol",
        listOf(Ing("frijoles", 1.0), Ing("huevo", 4.0), Ing("queso", 0.3), Ing(ANY_VEG, 0.5), Ing("tortilla", 0.6)),
        "Sopa de frijoles con verduras y huevo cocido en el caldo. Sin chicharrón."),
    Recipe("a_pescado", "Pescado a la plancha con ensalada y plátano", MealType.ALMUERZO, "pescado",
        listOf(Ing("pescado", 2.0), Ing(ANY_VEG, 1.0), Ing("platano", 2.0), Ing("limon", 2.0, true)),
        "Pescado a la plancha con limón; plátano asado en vez de frito y ensalada."),
    Recipe("a_pollo_salteado", "Pollo salteado con vegetales", MealType.ALMUERZO, "pollo",
        listOf(Ing("pollo", 2.0), Ing(ANY_VEG, 2.0), Ing("arroz", 0.5)),
        "Pollo en tiras salteado a fuego alto con muchas verduras en tiras."),
    Recipe("a_albondigas", "Albóndigas en salsa de tomate", MealType.ALMUERZO, "res",
        listOf(Ing("molida", 1.5), Ing("tomate", 1.0), Ing("huevo", 1.0), Ing(ANY_VEG, 1.0), Ing("arroz", 0.5)),
        "Albóndigas de carne molida con huevo, cocidas en salsa de tomate natural, con verduras al vapor."),
    Recipe("a_sopa_res", "Sopa de res con verduras", MealType.ALMUERZO, "res",
        listOf(Ing("res", 2.0), Ing("yuca", 1.0), Ing("repollo", 0.5), Ing("zanahoria", 0.5), Ing(ANY_VEG, 0.5), Ing("elote", 2.0, true)),
        "Sopa de res con mucha verdura; la yuca y el elote cuentan como tu carbohidrato."),
    Recipe("a_tacos", "Tacos de pollo al horno con repollo", MealType.ALMUERZO, "pollo",
        listOf(Ing("pollo", 1.5), Ing("tortilla", 1.0), Ing("repollo", 0.5), Ing("tomate", 0.5)),
        "Taquitos de pollo deshebrado dorados al horno en vez de fritos, con repollo y tomate."),
    Recipe("a_pechuga_tomate", "Pechuga en salsa de tomate con brócoli y papa", MealType.ALMUERZO, "pollo",
        listOf(Ing("pollo", 2.0), Ing("tomate", 1.0), Ing("brocoli", 1.0), Ing("papa", 1.0)),
        "Pechuga en salsa de tomate natural, brócoli al vapor y puré de papa con un poco de leche."),
    Recipe("a_atun", "Ensalada grande de atún y huevo", MealType.ALMUERZO, "pescado",
        listOf(Ing("atun", 3.0), Ing("huevo", 4.0), Ing("lechuga", 1.0), Ing("tomate", 0.5), Ing("pepino", 1.0), Ing("tortilla", 0.5)),
        "Atún en agua con huevo duro, lechuga, tomate y pepino, aliñado con limón y poco aceite."),
    Recipe("a_lentejas", "Lentejas guisadas con verduras", MealType.ALMUERZO, "frijol",
        listOf(Ing("lentejas", 1.0), Ing(ANY_VEG, 1.0), Ing("zanahoria", 0.5, true), Ing("arroz", 0.5)),
        "Lentejas guisadas con zanahoria, tomate y cebolla. Lentejas con arroz son proteína completa."),
    Recipe("a_picadillo", "Picadillo de carne con verduras", MealType.ALMUERZO, "res",
        listOf(Ing("molida", 1.5), Ing("papa", 0.5), Ing("zanahoria", 0.5), Ing(ANY_VEG, 1.0), Ing("tortilla", 0.6)),
        "Carne molida con verduras picadas pequeñas; rinde más y los niños se la comen mejor."),
    Recipe("a_cerdo", "Lomo de cerdo a la plancha con ensalada y yuca", MealType.ALMUERZO, "cerdo",
        listOf(Ing("cerdo", 2.0), Ing(ANY_VEG, 1.0), Ing("yuca", 1.0)),
        "Lomo a la plancha, yuca cocida (no frita) y ensalada."),

    Recipe("c_baleada", "Baleadas ligeras", MealType.CENA, "frijol",
        listOf(Ing("tortilla_harina", 1.0), Ing("frijoles", 0.5), Ing("huevo", 4.0), Ing("aguacate", 1.0, true), Ing("queso", 0.2, true)),
        "Una por persona, con huevo y aguacate en vez de mantequilla rala."),
    Recipe("c_ensalada_pollo", "Ensalada de pollo con aguacate", MealType.CENA, "pollo",
        listOf(Ing("pollo", 1.5), Ing("lechuga", 1.0), Ing("tomate", 0.5), Ing("aguacate", 2.0)),
        "Pollo desmenuzado con lechuga, tomate y aguacate. A los niños, con su tortilla."),
    Recipe("c_sopa_verdura", "Sopa de verduras con pollo", MealType.CENA, "pollo",
        listOf(Ing("pollo", 1.0), Ing(ANY_VEG, 2.0), Ing("papa", 0.5, true)),
        "Caldo de pollo con muchas verduras. Cena ligera y fácil de digerir."),
    Recipe("c_huevos_verdura", "Huevos con vegetales y frijoles", MealType.CENA, "huevo",
        listOf(Ing("huevo", 8.0), Ing(ANY_VEG, 1.0), Ing("frijoles", 0.4), Ing("tortilla", 0.4)),
        "Revuelto de huevo con las verduras que queden y frijoles enteros."),
    Recipe("c_tortilla_queso", "Tortillas con queso, frijoles y ensalada de pepino", MealType.CENA, "frijol",
        listOf(Ing("tortilla", 0.6), Ing("queso", 0.4), Ing("frijoles", 0.4), Ing("pepino", 1.0), Ing("tomate", 0.4)),
        "Tortilla con queso derretido, frijoles y ensalada fresca de pepino y tomate."),
    Recipe("c_crema_ayote", "Crema de ayote con queso", MealType.CENA, "lacteo",
        listOf(Ing("ayote", 2.0), Ing("leche", 0.5), Ing("queso", 0.3), Ing("pan_integral", 0.3, true)),
        "Ayote cocido y licuado con leche, sin crema. Queso rallado encima."),
    Recipe("c_rollos", "Rollos de pollo y vegetales", MealType.CENA, "pollo",
        listOf(Ing("tortilla_harina", 0.6), Ing("pollo", 1.2), Ing(ANY_VEG, 1.0), Ing("aguacate", 1.0, true)),
        "Tortilla rellena de pollo, verduras salteadas y aguacate."),
    Recipe("c_platano_frijol", "Frijoles, plátano, queso y ensalada", MealType.CENA, "frijol",
        listOf(Ing("frijoles", 0.5), Ing("platano", 2.0), Ing("queso", 0.3), Ing(ANY_VEG, 0.75)),
        "Medio plátano asado por adulto, frijoles, queso y una ensalada."),
    Recipe("c_atun_tostadas", "Tostadas de atún con verduras", MealType.CENA, "pescado",
        listOf(Ing("atun", 2.0), Ing("tortilla", 0.6), Ing("tomate", 0.5), Ing(ANY_VEG, 0.5), Ing("limon", 2.0, true)),
        "Tortillas tostadas en el comal (no fritas) con atún, tomate y verduras picadas."),
    Recipe("c_tortilla_papa", "Tortilla de huevo con papa y verduras", MealType.CENA, "huevo",
        listOf(Ing("huevo", 8.0), Ing("papa", 0.75), Ing(ANY_VEG, 0.5)),
        "Papa cocida en cubos con verduras, cuajada con huevo en sartén tapado."),

    Recipe("m_fruta", "Fruta picada con limón", MealType.MERIENDA, "none",
        listOf(Ing(ANY_FRUIT, 2.0)),
        "La fruta que haya, picada. Sin azúcar ni chile en polvo azucarado."),
    Recipe("m_verdura", "Pepino y zanahoria con limón", MealType.MERIENDA, "none",
        listOf(Ing("pepino", 1.0), Ing("zanahoria", 0.5), Ing("limon", 2.0, true)),
        "En bastones, con limón y una pizca de sal."),
    Recipe("m_yogur", "Yogur con fruta", MealType.MERIENDA, "lacteo",
        listOf(Ing("yogur", 0.5), Ing(ANY_FRUIT, 1.0)),
        "Yogur natural con fruta picada."),
    Recipe("m_banano_mani", "Banano con mantequilla de maní", MealType.MERIENDA, "none",
        listOf(Ing("banano", 4.0), Ing("mantequilla_mani", 0.15)),
        "Banano en rodajas con una capa delgada de mantequilla de maní.",
        "A los niños, úntala delgada para que no se les pegue en la garganta."),
    Recipe("m_huevo", "Huevo duro y fruta", MealType.MERIENDA, "huevo",
        listOf(Ing("huevo", 4.0), Ing(ANY_FRUIT, 1.0)),
        "Un huevo duro por persona y una fruta."),
    Recipe("m_mango", "Mango con limón", MealType.MERIENDA, "none",
        listOf(Ing("mango", 2.0)),
        "Mango verde o maduro en tiras con limón."),
    Recipe("m_mani", "Maní tostado sin sal", MealType.MERIENDA, "none",
        listOf(Ing("mani", 0.25)),
        "Un puño pequeño por adulto.",
        "No le des maní entero a niños menores de 5 años: hay riesgo de atragantamiento. Para ellos, fruta."),
)

private val recipeById = RECIPES.associateBy { it.id }
fun recipeOf(id: String): Recipe? = recipeById[id]

fun plateTip(type: MealType): String = when (type) {
    MealType.DESAYUNO -> "Tu plato: proteína más fruta o verdura, y una tortilla o una rebanada de pan como máximo."
    MealType.ALMUERZO -> "Tu plato: mitad verduras, un cuarto proteína y un cuarto arroz, tortilla, papa o plátano."
    MealType.MERIENDA -> "Sin azúcar añadida. Agua en vez de jugo."
    MealType.CENA -> "Cena ligera: prioriza verduras y proteína; una tortilla basta."
}

const val KIDS_NOTE = "Niños: más o menos media porción de adulto. Ellos sí comen su arroz, tortilla o plátano completo; lo que se evita es el azúcar añadida y los jugos."

private fun catLb(remaining: Map<String, Double>, food: FoodData, cat: FoodCat): Double =
    remaining.entries.sumOf { (id, q) -> if (catOf(id, food) == cat) q * lbPer(id, food) else 0.0 }

fun missingFor(r: Recipe, remaining: Map<String, Double>, food: FoodData): List<String> {
    val out = mutableListOf<String>()
    r.ings.filter { !it.optional }.forEach { ing ->
        when (ing.food) {
            ANY_VEG -> if (catLb(remaining, food, FoodCat.VERDURA) < ing.qty) out.add("verduras")
            ANY_FRUIT -> if (catLb(remaining, food, FoodCat.FRUTA) < ing.qty) out.add("fruta")
            else -> if ((remaining[ing.food] ?: 0.0) < ing.qty) out.add(shortName(ing.food, food))
        }
    }
    return out
}

private fun consume(r: Recipe, remaining: MutableMap<String, Double>, food: FoodData) {
    r.ings.forEach { ing ->
        when (ing.food) {
            ANY_VEG, ANY_FRUIT -> {
                val cat = if (ing.food == ANY_VEG) FoodCat.VERDURA else FoodCat.FRUTA
                var need = ing.qty
                val ids = remaining.keys.filter { catOf(it, food) == cat && (remaining[it] ?: 0.0) > 0 }.sortedBy { perishOf(it) }
                for (id in ids) {
                    if (need <= 0) break
                    val per = lbPer(id, food)
                    val availLb = (remaining[id] ?: 0.0) * per
                    val take = minOf(availLb, need)
                    remaining[id] = (remaining[id] ?: 0.0) - take / per
                    need -= take
                }
            }
            else -> {
                val left = (remaining[ing.food] ?: 0.0) - ing.qty
                remaining[ing.food] = if (left < 0) 0.0 else left
            }
        }
    }
}

fun generateMenu(food: FoodData, from: LocalDate, days: Int, seed: Int): List<MenuDay> {
    val remaining = food.pantry.toMutableMap()
    val rnd = Random(seed)
    val lastUsed = mutableMapOf<String, Int>()
    var lastProtein = ""
    val out = mutableListOf<MenuDay>()
    for (d in 0 until days) {
        val meals = mutableListOf<PlannedMeal>()
        for (type in MealType.entries) {
            val gap = if (type == MealType.ALMUERZO || type == MealType.CENA) 4 else 2
            val scored = RECIPES.filter { it.type == type }.map { r ->
                val missing = missingFor(r, remaining, food)
                var score = rnd.nextDouble() * 2.0 - missing.size * 10.0
                val last = lastUsed[r.id]
                if (last != null && d - last < gap) score -= 6.0
                if (r.protein != "none" && r.protein == lastProtein) score -= 2.0
                if (d < 7) {
                    score += r.ings.sumOf { ing ->
                        val p = perishOf(ing.food)
                        if (!ing.food.startsWith("*") && p <= 7 && (remaining[ing.food] ?: 0.0) >= ing.qty) 1.5 else 0.0
                    }
                }
                Triple(r, score, missing)
            }
            val best = scored.maxByOrNull { it.second } ?: continue
            consume(best.first, remaining, food)
            lastUsed[best.first.id] = d
            if (best.first.protein != "none") lastProtein = best.first.protein
            meals.add(PlannedMeal(type, best.first.id, best.third))
        }
        out.add(MenuDay(from.plusDays(d.toLong()).toEpochDay(), meals))
    }
    return out
}

fun swapMeal(food: FoodData, day: Long, type: MealType): FoodData {
    val menuDay = food.menu.firstOrNull { it.day == day } ?: return food
    val current = menuDay.meals.firstOrNull { it.type == type } ?: return food
    val ranked = RECIPES.filter { it.type == type }
        .map { it to missingFor(it, food.pantry, food) }
        .sortedWith(compareBy({ it.second.size }, { it.first.id }))
    if (ranked.size < 2) return food
    val idx = ranked.indexOfFirst { it.first.id == current.recipeId }
    val next = ranked[(idx + 1) % ranked.size]
    val newMeals = menuDay.meals.map { if (it.type == type) PlannedMeal(type, next.first.id, next.second) else it }
    return food.copy(menu = food.menu.map { if (it.day == day) MenuDay(day, newMeals) else it })
}

fun menuWindow(food: FoodData, today: LocalDate): Pair<LocalDate, Int> {
    val start = food.quincenaStart?.let { LocalDate.ofEpochDay(it) } ?: today
    val end = start.plusDays(14)
    return if (end.isBefore(today)) today to 15 else {
        val from = if (start.isAfter(today)) start else today
        from to ((end.toEpochDay() - from.toEpochDay()).toInt() + 1)
    }
}

fun FoodData.toJson(): JSONObject {
    val o = JSONObject()
    quincenaStart?.let { o.put("quincenaStart", it) }
    val p = JSONObject()
    pantry.forEach { (k, v) -> p.put(k, v) }
    o.put("pantry", p)
    val c = JSONArray()
    custom.forEach { c.put(JSONObject().put("id", it.id).put("name", it.name).put("cat", it.cat.name).put("unit", it.unit)) }
    o.put("custom", c)
    val m = JSONArray()
    menu.forEach { day ->
        val meals = JSONArray()
        day.meals.forEach { pm ->
            val miss = JSONArray()
            pm.missing.forEach { miss.put(it) }
            meals.put(JSONObject().put("type", pm.type.name).put("recipe", pm.recipeId).put("missing", miss))
        }
        m.put(JSONObject().put("day", day.day).put("meals", meals))
    }
    o.put("menu", m)
    o.put("seed", seed)
    return o
}

fun parseFood(o: JSONObject?): FoodData {
    if (o == null) return FoodData()
    val pantry = mutableMapOf<String, Double>()
    o.optJSONObject("pantry")?.let { p ->
        val keys = p.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            pantry[k] = p.getDouble(k)
        }
    }
    val custom = mutableListOf<CustomFood>()
    o.optJSONArray("custom")?.let { a ->
        for (i in 0 until a.length()) {
            val j = a.getJSONObject(i)
            val cat = FoodCat.entries.firstOrNull { it.name == j.optString("cat") } ?: FoodCat.OTRO
            custom.add(CustomFood(j.getString("id"), j.getString("name"), cat, j.optString("unit", "unid")))
        }
    }
    val menu = mutableListOf<MenuDay>()
    o.optJSONArray("menu")?.let { a ->
        for (i in 0 until a.length()) {
            val j = a.getJSONObject(i)
            val meals = mutableListOf<PlannedMeal>()
            val ma = j.getJSONArray("meals")
            for (k in 0 until ma.length()) {
                val mj = ma.getJSONObject(k)
                val type = MealType.entries.firstOrNull { it.name == mj.optString("type") } ?: continue
                val miss = mutableListOf<String>()
                mj.optJSONArray("missing")?.let { ms -> for (x in 0 until ms.length()) miss.add(ms.getString(x)) }
                meals.add(PlannedMeal(type, mj.getString("recipe"), miss))
            }
            menu.add(MenuDay(j.getLong("day"), meals))
        }
    }
    return FoodData(
        quincenaStart = if (o.has("quincenaStart")) o.getLong("quincenaStart") else null,
        pantry = pantry,
        custom = custom,
        menu = menu,
        seed = o.optInt("seed", 1),
    )
}
