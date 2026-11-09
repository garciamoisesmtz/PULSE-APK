package com.pulse.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun FoodScreen(store: Store) {
    var tab by remember { mutableIntStateOf(0) }
    val food = store.data.food
    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        ScreenTitle(
            "Comida",
            food.quincenaStart?.let { "Quincena desde el ${fmtDateMed(LocalDate.ofEpochDay(it))}" } ?: "Despensa y menú de la familia",
        )
        SegmentedTabs(listOf("Despensa", "Menú"), tab) { tab = it }
        Gap(20)
        if (tab == 0) PantrySection(store, onGoMenu = { tab = 1 }) else MenuSection(store)
        Gap(24)
    }
}

private fun updateFood(store: Store, f: (FoodData) -> FoodData) {
    store.update { d -> d.copy(food = f(d.food)) }
}

@Composable
private fun PantrySection(store: Store, onGoMenu: () -> Unit) {
    val food = store.data.food
    var query by remember { mutableStateOf("") }
    var onlyMine by remember { mutableStateOf(false) }
    var addCustom by remember { mutableStateOf(false) }
    var confirmNew by remember { mutableStateOf(false) }
    val today = LocalDate.now()

    BalanceCard(food)
    Gap(16)
    Row {
        if (food.pantry.isNotEmpty()) {
            PrimaryButton("Ver menú", icon = Lucide.Utensils, modifier = Modifier.weight(1f), onClick = onGoMenu)
            HGap(8)
        }
        SecondaryButton("Nueva quincena", icon = Lucide.Refresh, modifier = Modifier.weight(1f), onClick = { confirmNew = true })
    }
    Gap(24)

    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        placeholder = { Text("Buscar producto", style = Tx.Body.copy(color = Pc.TextSecondary)) },
        leadingIcon = { Icon(Lucide.Search, contentDescription = null, tint = Pc.TextSecondary, modifier = Modifier.size(18.dp)) },
        singleLine = true,
        textStyle = Tx.Body,
        shape = ButtonShape,
        colors = fieldColors(),
        modifier = Modifier.fillMaxWidth(),
    )
    Gap(10)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Chip("Todo", selected = !onlyMine) { onlyMine = false }
        Chip("Lo que compré", selected = onlyMine) { onlyMine = true }
    }
    Gap(20)

    val q = query.trim().lowercase()
    FoodCat.entries.forEach { cat ->
        val rows = CATALOG.filter { it.cat == cat }.map { Triple(it.id, it.name, it.unit) to it.step } +
            food.custom.filter { it.cat == cat }.map { Triple(it.id, it.name, it.unit) to (if (it.unit == "lb") 0.5 else 1.0) }
        val visible = rows.filter { (t, _) ->
            (q.isEmpty() || t.second.lowercase().contains(q)) && (!onlyMine || (food.pantry[t.first] ?: 0.0) > 0)
        }
        if (visible.isEmpty()) return@forEach
        val catLb = composition(food)[cat] ?: 0.0
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(cat.label, style = Tx.H3, modifier = Modifier.weight(1f))
            if (catLb > 0) Text("${fmtNum(catLb)} lb aprox.", style = Tx.Small)
        }
        Gap(8)
        Column(
            Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(Pc.Surface)
                .border(1.dp, Pc.Border, CardShape),
        ) {
            visible.forEachIndexed { i, (t, step) ->
                if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(Pc.Border))
                val (id, name, unit) = t
                val qty = food.pantry[id] ?: 0.0
                val max = CATALOG.firstOrNull { it.id == id }?.max
                PantryRow(name, qty, unit, over = max != null && qty > max) { delta ->
                    val nq = (qty + delta * step).coerceAtLeast(0.0)
                    updateFood(store) { fd ->
                        val p = fd.pantry.toMutableMap()
                        if (nq <= 0.0) p.remove(id) else p[id] = nq
                        fd.copy(pantry = p, quincenaStart = fd.quincenaStart ?: today.toEpochDay())
                    }
                }
            }
        }
        Gap(20)
    }
    SecondaryButton("Agregar otro producto", icon = Lucide.Plus, onClick = { addCustom = true })

    if (addCustom) {
        CustomFoodDialog(onDismiss = { addCustom = false }) { name, cat, unit ->
            updateFood(store) { fd ->
                val id = "custom_" + (fd.custom.size + 1) + "_" + System.currentTimeMillis() % 100000
                fd.copy(custom = fd.custom + CustomFood(id, name, cat, unit))
            }
            addCustom = false
            query = ""
        }
    }
    if (confirmNew) {
        ConfirmDialog(
            "Nueva quincena",
            "Se vacía la despensa y el menú para empezar la compra de hoy. Los productos que agregaste a mano se conservan en la lista.",
            "Empezar",
            onDismiss = { confirmNew = false },
            onConfirm = {
                confirmNew = false
                updateFood(store) { fd -> fd.copy(quincenaStart = today.toEpochDay(), pantry = emptyMap(), menu = emptyList()) }
            },
        )
    }
}

@Composable
private fun PantryRow(name: String, qty: Double, unit: String, over: Boolean, onStep: (Int) -> Unit) {
    Row(
        Modifier.padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(name, style = Tx.Body.copy(color = if (qty > 0) Pc.TextPrimary else Pc.TextSecondary))
            if (over) Text("Más de lo recomendado", style = Tx.Small.copy(color = Pc.Warning))
        }
        StepButton(Lucide.Minus, enabled = qty > 0) { onStep(-1) }
        Text(
            if (qty > 0) "${fmtNum(qty)} ${unitLabel(unit)}" else "–",
            style = Tx.NumSmall.copy(fontSize = 15.sp, color = if (over) Pc.Warning else if (qty > 0) Pc.TextPrimary else Pc.TextSecondary),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 64.dp),
        )
        StepButton(Lucide.Plus, enabled = true) { onStep(1) }
    }
}

@Composable
private fun StepButton(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Pc.SurfaceElevated)
                .border(1.dp, Pc.Border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = if (enabled) Pc.TextPrimary else Pc.TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun BalanceCard(food: FoodData) {
    val comp = composition(food)
    val alerts = foodAlerts(food)
    PCard {
        Text("Balance de la compra", style = Tx.H3)
        Gap(4)
        if (food.pantry.isEmpty()) {
            Text("Agrega lo que compraron en la quincena. Aquí vas a ver si la compra está balanceada y si te pasaste en azúcar o harinas.", style = Tx.BodySecondary)
            return@PCard
        }
        Text("Referencia para 2 adultos y 2 niños pequeños durante 15 días.", style = Tx.Small)
        Gap(16)
        TargetBar("Verduras", comp[FoodCat.VERDURA] ?: 0.0, VEG_TARGET_LB)
        Gap(14)
        TargetBar("Frutas", comp[FoodCat.FRUTA] ?: 0.0, FRUIT_TARGET_LB)
        Gap(14)
        Row {
            Column(Modifier.weight(1f)) {
                Text("Proteínas", style = Tx.Small)
                Text("${fmtNum(comp[FoodCat.PROTEINA] ?: 0.0)} lb", style = Tx.NumSmall)
            }
            Column(Modifier.weight(1f)) {
                Text("Harinas y tubérculos", style = Tx.Small)
                Text("${fmtNum(comp[FoodCat.GRANO] ?: 0.0)} lb", style = Tx.NumSmall)
            }
            Column(Modifier.weight(1f)) {
                Text("Azúcares", style = Tx.Small)
                Text("${fmtNum(comp[FoodCat.AZUCAR] ?: 0.0)} lb", style = Tx.NumSmall.copy(color = if ((comp[FoodCat.AZUCAR] ?: 0.0) > 3) Pc.Warning else Pc.TextPrimary))
            }
        }
        if (alerts.isNotEmpty()) {
            Gap(16)
            Box(Modifier.fillMaxWidth().height(1.dp).background(Pc.Border))
            alerts.forEach { a ->
                Gap(12)
                Row {
                    Icon(
                        Lucide.Alert,
                        contentDescription = null,
                        tint = if (a.level >= 2) Pc.Error else Pc.Warning,
                        modifier = Modifier.padding(top = 2.dp).size(16.dp),
                    )
                    HGap(10)
                    Text(a.text, style = Tx.Body.copy(fontSize = 14.sp, lineHeight = 20.sp))
                }
            }
        } else {
            Gap(14)
            Text("La compra se ve balanceada.", style = Tx.Body.copy(color = Pc.Accent))
        }
    }
}

@Composable
private fun TargetBar(label: String, value: Double, target: Double) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(label, style = Tx.Body, modifier = Modifier.weight(1f))
        Text("${fmtNum(value)} de ${fmtNum(target)} lb", style = Tx.Small)
    }
    Gap(6)
    ProgressLine((value / target).toFloat())
}

@Composable
private fun MenuSection(store: Store) {
    val food = store.data.food
    val today = LocalDate.now()

    if (food.pantry.isEmpty()) {
        PCard {
            Text("Primero llena la despensa", style = Tx.H3)
            Gap(6)
            Text("El menú se arma con lo que compraron en la quincena.", style = Tx.BodySecondary)
        }
        return
    }

    fun generate(seed: Int) {
        updateFood(store) { fd ->
            val (from, days) = menuWindow(fd, today)
            fd.copy(menu = generateMenu(fd, from, days, seed), seed = seed, quincenaStart = fd.quincenaStart ?: today.toEpochDay())
        }
    }

    if (food.menu.isEmpty()) {
        PCard {
            Text("Menú de la quincena", style = Tx.H2)
            Gap(6)
            Text(
                "Desayuno, almuerzo, merienda y cena para 4 personas con lo que hay en la despensa. Se usa primero lo que se echa a perder rápido.",
                style = Tx.BodySecondary,
            )
            Gap(16)
            PrimaryButton("Generar menú", icon = Lucide.Utensils, onClick = { generate(food.seed) })
        }
        return
    }

    val days = food.menu.map { it.day }
    var selected by remember(days) {
        mutableLongStateOf(if (today.toEpochDay() in days) today.toEpochDay() else days.first())
    }
    val covered = food.menu.count { d -> d.meals.all { it.missing.isEmpty() } }

    Text("$covered de ${food.menu.size}", style = Tx.Display.copy(fontSize = 44.sp, lineHeight = 48.sp))
    Text("días completos con lo que hay en la despensa", style = Tx.BodySecondary)
    Gap(20)

    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        food.menu.forEach { md ->
            val d = LocalDate.ofEpochDay(md.day)
            val sel = md.day == selected
            val complete = md.meals.all { it.missing.isEmpty() }
            val shape = RoundedCornerShape(10.dp)
            Column(
                Modifier
                    .width(52.dp)
                    .clip(shape)
                    .background(if (sel) Pc.Accent else Pc.Surface)
                    .border(1.dp, if (sel) Pc.Accent else Pc.Border, shape)
                    .clickable { selected = md.day }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(DAY_LETTERS[d.dayOfWeek.value - 1], style = Tx.Small.copy(color = if (sel) Pc.OnAccent else Pc.TextSecondary))
                Text("${d.dayOfMonth}", style = Tx.NumSmall.copy(color = if (sel) Pc.OnAccent else Pc.TextPrimary))
                Gap(4)
                Box(
                    Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                sel -> Pc.OnAccent.copy(alpha = if (complete) 1f else 0f)
                                complete -> Pc.Accent
                                else -> Pc.Warning
                            },
                        ),
                )
            }
        }
    }
    Gap(20)

    val md = food.menu.firstOrNull { it.day == selected } ?: food.menu.first()
    Text(fmtDateLong(LocalDate.ofEpochDay(md.day)), style = Tx.H2)
    Gap(12)
    md.meals.forEach { pm ->
        val r = recipeOf(pm.recipeId) ?: return@forEach
        PCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(pm.type.label, style = Tx.Label, modifier = Modifier.weight(1f))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { updateFood(store) { fd -> swapMeal(fd, md.day, pm.type) } }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Lucide.Refresh, contentDescription = null, tint = Pc.Accent, modifier = Modifier.size(14.dp))
                    HGap(6)
                    Text("Cambiar", style = Tx.Small.copy(color = Pc.Accent))
                }
            }
            Gap(4)
            Text(r.name, style = Tx.H3)
            Gap(6)
            Text(r.how, style = Tx.BodySecondary)
            Gap(8)
            Text(
                r.ings.joinToString(", ") { ing ->
                    when (ing.food) {
                        ANY_VEG -> "verduras ${fmtNum(ing.qty)} lb"
                        ANY_FRUIT -> "fruta ${fmtNum(ing.qty)} lb"
                        else -> {
                            val item = CATALOG.firstOrNull { it.id == ing.food }
                            "${shortName(ing.food, food)} ${fmtNum(ing.qty)} ${unitLabel(item?.unit ?: "")}".trim() + if (ing.optional) " (opcional)" else ""
                        }
                    }
                },
                style = Tx.Small,
            )
            if (pm.missing.isNotEmpty()) {
                Gap(8)
                Text("Te falta: ${pm.missing.joinToString(", ")}", style = Tx.Small.copy(color = Pc.Warning))
            }
            if (r.note != null) {
                Gap(8)
                Text(r.note, style = Tx.Small.copy(color = Pc.Accent))
            }
        }
        Gap(10)
    }
    Gap(10)
    PCard {
        Text("Cómo servir", style = Tx.H3)
        Gap(8)
        listOf(MealType.DESAYUNO, MealType.ALMUERZO, MealType.CENA).forEach {
            Text(plateTip(it), style = Tx.BodySecondary)
            Gap(6)
        }
        Text(KIDS_NOTE, style = Tx.BodySecondary)
    }
    Gap(16)

    val shopping = food.menu.flatMap { d -> d.meals.flatMap { it.missing } }.groupingBy { it }.eachCount()
        .entries.sortedByDescending { it.value }
    if (shopping.isNotEmpty()) {
        PCard {
            Text("Para completar la quincena", style = Tx.H3)
            Gap(4)
            Text("Lo que falta en el menú, empezando por lo que más se necesita.", style = Tx.Small)
            Gap(10)
            shopping.take(10).forEach { (name, n) ->
                Row(Modifier.padding(vertical = 3.dp)) {
                    Text(name.replaceFirstChar { it.uppercase() }, style = Tx.Body, modifier = Modifier.weight(1f))
                    Text("$n ${if (n == 1) "comida" else "comidas"}", style = Tx.Small)
                }
            }
        }
        Gap(16)
    }
    SecondaryButton("Regenerar menú", icon = Lucide.Refresh, onClick = { generate(food.seed + 1) })
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Pc.Accent,
    unfocusedBorderColor = Pc.Border,
    focusedTextColor = Pc.TextPrimary,
    unfocusedTextColor = Pc.TextPrimary,
    cursorColor = Pc.Accent,
    focusedLabelColor = Pc.Accent,
    unfocusedLabelColor = Pc.TextSecondary,
    focusedContainerColor = Pc.Surface,
    unfocusedContainerColor = Pc.Surface,
)

@Composable
private fun CustomFoodDialog(onDismiss: () -> Unit, onSave: (String, FoodCat, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf(FoodCat.VERDURA) }
    var unit by remember { mutableStateOf("lb") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pc.Surface,
        title = { Text("Otro producto", style = Tx.H2) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    label = { Text("Nombre", style = Tx.Small) },
                    singleLine = true,
                    textStyle = Tx.Body,
                    shape = ButtonShape,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Gap(14)
                Text("Categoría", style = Tx.Small)
                Gap(8)
                FoodCat.entries.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                        pair.forEach { c -> Chip(c.label.substringBefore(","), selected = c == cat) { cat = c } }
                    }
                }
                Gap(6)
                Text("Se mide en", style = Tx.Small)
                Gap(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("Libras", selected = unit == "lb") { unit = "lb" }
                    Chip("Unidades", selected = unit == "unid") { unit = "unid" }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onSave(name.trim(), cat, unit) }) {
                Text("Agregar", style = Tx.Button.copy(color = if (name.isNotBlank()) Pc.Accent else Pc.TextSecondary))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", style = Tx.Button.copy(color = Pc.TextSecondary)) } },
    )
}
