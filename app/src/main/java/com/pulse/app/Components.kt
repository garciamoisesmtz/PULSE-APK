package com.pulse.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val CardShape = RoundedCornerShape(12.dp)
val ButtonShape = RoundedCornerShape(10.dp)

@Composable
fun Gap(h: Int) = Spacer(Modifier.height(h.dp))

@Composable
fun HGap(w: Int) = Spacer(Modifier.width(w.dp))

@Composable
fun PulseMark(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val p = Path().apply {
            moveTo(0f, h * 0.55f)
            lineTo(w * 0.28f, h * 0.55f)
            lineTo(w * 0.40f, h * 0.12f)
            lineTo(w * 0.56f, h * 0.92f)
            lineTo(w * 0.68f, h * 0.38f)
            lineTo(w * 0.76f, h * 0.55f)
            lineTo(w, h * 0.55f)
        }
        drawPath(p, Pc.Accent, style = Stroke(width = h * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun PulseLogo(textSize: TextUnit = 28.sp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        PulseMark(Modifier.size(width = (textSize.value * 1.25f).dp, height = (textSize.value * 0.85f).dp))
        HGap(10)
        Text(
            "PULSE",
            style = TextStyle(
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = textSize,
                letterSpacing = (textSize.value * 0.08f).sp,
                color = Pc.TextPrimary,
            ),
        )
    }
}

@Composable
fun PCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Pc.Surface)
            .border(1.dp, Pc.Border, CardShape)
            .padding(16.dp),
        content = content,
    )
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, icon: ImageVector? = null) {
    val fg = if (enabled) Pc.OnAccent else Pc.TextSecondary
    Row(
        modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(ButtonShape)
            .background(if (enabled) Pc.Accent else Pc.SurfaceElevated)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
            HGap(8)
        }
        Text(text, style = Tx.Button.copy(color = fg))
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, icon: ImageVector? = null) {
    val fg = if (enabled) Pc.TextPrimary else Pc.TextSecondary.copy(alpha = 0.5f)
    Row(
        modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(ButtonShape)
            .background(Pc.SurfaceElevated)
            .border(1.dp, Pc.Border, ButtonShape)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
            HGap(8)
        }
        Text(text, style = Tx.Button.copy(color = fg))
    }
}

@Composable
fun Chip(text: String, selected: Boolean = false, onClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(8.dp)
    var m = Modifier
        .clip(shape)
        .background(if (selected) Pc.Accent else Pc.SurfaceElevated)
        .border(1.dp, if (selected) Pc.Accent else Pc.Border, shape)
    if (onClick != null) m = m.clickable(onClick = onClick)
    Box(m.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(text, style = Tx.Small.copy(color = if (selected) Pc.OnAccent else Pc.TextPrimary))
    }
}

@Composable
fun ProgressLine(fraction: Float, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Pc.Border),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(Pc.Accent),
        )
    }
}

@Composable
fun ScreenTitle(title: String, subtitle: String? = null) {
    Column(Modifier.padding(top = 20.dp, bottom = 20.dp)) {
        Text(title, style = Tx.H1)
        if (subtitle != null) {
            Gap(4)
            Text(subtitle, style = Tx.BodySecondary)
        }
    }
}

@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier, unit: String? = null) {
    Column(
        modifier
            .clip(CardShape)
            .background(Pc.Surface)
            .border(1.dp, Pc.Border, CardShape)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = Tx.Num.copy(fontSize = 26.sp))
            if (unit != null) {
                HGap(3)
                Text(unit, style = Tx.Small, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
        Gap(2)
        Text(label, style = Tx.Small)
    }
}

@Composable
fun NumberField(value: String, onChange: (String) -> Unit, label: String, suffix: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> onChange(v.filter { it.isDigit() || it == '.' || it == ',' }.take(6)) },
        label = { Text(label, style = Tx.Small) },
        suffix = { Text(suffix, style = Tx.Small) },
        singleLine = true,
        textStyle = Tx.NumSmall,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = ButtonShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Pc.Accent,
            unfocusedBorderColor = Pc.Border,
            focusedTextColor = Pc.TextPrimary,
            unfocusedTextColor = Pc.TextPrimary,
            cursorColor = Pc.Accent,
            focusedLabelColor = Pc.Accent,
            unfocusedLabelColor = Pc.TextSecondary,
            focusedContainerColor = Pc.Surface,
            unfocusedContainerColor = Pc.Surface,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun TimeButton(h: Int, m: Int, onClick: () -> Unit, icon: ImageVector = Lucide.Clock) {
    Row(
        Modifier
            .clip(ButtonShape)
            .background(Pc.SurfaceElevated)
            .border(1.dp, Pc.Border, ButtonShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Pc.TextSecondary, modifier = Modifier.size(18.dp))
        HGap(8)
        Text(fmtTime(h, m), style = Tx.NumSmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDialog(h: Int, m: Int, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = h, initialMinute = m, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pc.Surface,
        confirmButton = { TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("Guardar", style = Tx.Button.copy(color = Pc.Accent)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", style = Tx.Button.copy(color = Pc.TextSecondary)) } },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = Pc.SurfaceElevated,
                    selectorColor = Pc.Accent,
                    containerColor = Pc.Surface,
                    clockDialSelectedContentColor = Pc.OnAccent,
                    clockDialUnselectedContentColor = Pc.TextPrimary,
                    periodSelectorBorderColor = Pc.Border,
                    periodSelectorSelectedContainerColor = Pc.Accent,
                    periodSelectorSelectedContentColor = Pc.OnAccent,
                    periodSelectorUnselectedContainerColor = Pc.Surface,
                    periodSelectorUnselectedContentColor = Pc.TextSecondary,
                    timeSelectorSelectedContainerColor = Pc.Accent,
                    timeSelectorSelectedContentColor = Pc.OnAccent,
                    timeSelectorUnselectedContainerColor = Pc.SurfaceElevated,
                    timeSelectorUnselectedContentColor = Pc.TextPrimary,
                ),
            )
        },
    )
}

@Composable
fun ConfirmDialog(title: String, text: String, confirm: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pc.Surface,
        title = { Text(title, style = Tx.H2) },
        text = { Text(text, style = Tx.BodySecondary) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirm, style = Tx.Button.copy(color = Pc.Error)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", style = Tx.Button.copy(color = Pc.TextSecondary)) } },
    )
}
