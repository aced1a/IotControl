package com.iot.control.ui.scripts

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iot.control.R
import com.iot.control.model.Timer
import com.iot.control.ui.utils.DurablePicker
import com.iot.control.ui.utils.GeneralField
import com.iot.control.ui.utils.LabeledValue
import com.iot.control.ui.utils.SelectItemDialog
import com.iot.control.ui.utils.ToggleField
import com.iot.control.ui.utils.TopBarDialog
import com.iot.control.viewmodel.ScriptDialogUiState
import com.iot.control.viewmodel.ScriptUiState

@Composable
fun TimerTriggeredScriptDialog(
    state: ScriptDialogUiState,
    screenState: ScriptUiState,
    updateTime: (Int, Int) -> Unit,
    updateDate: (Long?) -> Unit,
    update: (ScriptDialogUiState) -> Unit,
    onSelectTimer: (Timer) -> Unit,
    openDialog: MutableState<Boolean>,
    save: () -> Unit
) {
    TopBarDialog(
        title = stringResource(R.string.new_script_label),
        close = { openDialog.value = false },
        save = {
            save()
            openDialog.value = false
        }
    ) {
        TimerScriptDialogBody(state, screenState, updateTime, updateDate, update, onSelectTimer)
    }
}

@Composable
fun TimerScriptDialogBody(
    state: ScriptDialogUiState,
    screenState: ScriptUiState,
    updateTime: (Int, Int) -> Unit,
    updateDate: (Long?) -> Unit,
    update: (ScriptDialogUiState) -> Unit,
    onSelectTimer: (Timer) -> Unit
) {

    val timerMode = remember { mutableStateOf(state.newTimer) }
    val selectTimer = remember { mutableStateOf(false) }

    Surface(modifier = Modifier.padding(horizontal = 30.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            SelectTimerMode(mode = timerMode) {

                if(timerMode.value) {
                    TimerOptions(state, update, updateDate, updateTime)
                } else {
                    GeneralField(stringResource(R.string.timer_label)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        SimpleTimer(
                            screenState.timer,
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .fillMaxWidth()
                        ) {
                            selectTimer.value = true
                        }
                    }
                }

            }
            Spacer(modifier = Modifier.height(10.dp))

            TargetCommandFields(state, screenState, update)
        }
    }

    if(selectTimer.value)
        SelectTimerDialog(screenState.timers.keys.toList(), selectTimer, onSelectTimer)
}

@Composable
fun SelectTimerMode(
    mode: MutableState<Boolean>,
    content: @Composable () -> Unit
) {

    TabRow(selectedTabIndex = if(mode.value) 0 else 1) {
            Tab(
                selected = (mode.value),
                onClick = { mode.value = true  },
                modifier = Modifier.padding(all = 3.dp),
                selectedContentColor = MaterialTheme.colorScheme.secondary,
                unselectedContentColor = MaterialTheme.colorScheme.secondary
            ) {
                Text(stringResource(R.string.new_timer_label))
            }
            Tab(
                selected = (!mode.value),
                onClick = { mode.value = false },
                modifier = Modifier.padding(all = 3.dp),
                selectedContentColor = MaterialTheme.colorScheme.secondary,
                unselectedContentColor = MaterialTheme.colorScheme.secondary
            ) {
                Text(stringResource(R.string.existing_label))
            }
        }

    Column(modifier = Modifier.padding(horizontal = 5.dp)) {

        Spacer(modifier = Modifier.height(15.dp))
        content()
        Spacer(modifier = Modifier.height(15.dp))
    }

}
@Composable
fun TimerOptions(
    state: ScriptDialogUiState,
    update: (ScriptDialogUiState) -> Unit,
    updateDate: (Long?) -> Unit,
    updateTime: (Int, Int) -> Unit
) {
    ToggleField(stringResource(R.string.on_boot_label), checked = state.onBoot, modifier = Modifier.fillMaxWidth()) {
        update(state.copy(onBoot = it))
    }
    DateSettings(state, update, updateDate, updateTime)
    RepeatSettings(state, update)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSettings(
    state: ScriptDialogUiState,
    update: (ScriptDialogUiState) -> Unit,
    updateDate: (Long?) -> Unit,
    updateTime: (Int, Int) -> Unit
) {
    var timePickerActive by remember { mutableStateOf(false) }
    var datePickerActive by remember { mutableStateOf(false) }

    GeneralField(stringResource(R.string.date_label)) {
        ToggleField(stringResource(R.string.use_date_label), state.useDate, modifier = Modifier.fillMaxWidth()) {
            update(state.copy(useDate = it))
        }

        if(state.useDate) {
            LabeledValue(
                stringResource(R.string.date_label),
                "${state.day}.${state.month}.${state.year}",
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { datePickerActive = true }) {
                    Icon(imageVector = Icons.Filled.DateRange, contentDescription = "DatePicker")
                }
            }

            LabeledValue(
                stringResource(R.string.time_label),
                "${state.hours}:${state.minutes}",
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { timePickerActive = true }) {
                    Icon(painter = painterResource(R.drawable.baseline_access_time_24), contentDescription = "TimePicker")
                }
            }
        }
    }

    val timerPicker = TimePickerDialog(
        LocalContext.current,
        {_, hour : Int, minute: Int ->
            updateTime(hour, minute)
            timePickerActive = false
        }, state.hours, state.minutes, false
    )
    val datePicker = rememberDatePickerState()

    if(datePickerActive)
        DatePickerDialog(
            onDismissRequest = { datePickerActive = false }
            , confirmButton = {
                TextButton(onClick = {
                    datePickerActive = false
                    updateDate(datePicker.selectedDateMillis)
                }) {
                    Text(stringResource(R.string.save_label))
                }
            }) {
            DatePicker(state = datePicker)
        }

    if(timePickerActive) {
        timerPicker.show()
        timePickerActive = false
    }

}

@Composable
fun RepeatSettings(
    state: ScriptDialogUiState,
    update: (ScriptDialogUiState) -> Unit
) {
    val durationPicker = remember { mutableStateOf(false) }

    GeneralField(stringResource(R.string.repeat_label)) {

        ToggleField(stringResource(R.string.repeat_label), checked = state.repeat, modifier = Modifier.fillMaxWidth()) {
            update(state.copy(repeat = it))
        }

        if(state.repeat) LabeledValue(
            label = stringResource(R.string.interval_label),
            value = stringResource(R.string.hms_format, state.hourInterval, state.minuteInterval, state.secondInterval),
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { durationPicker.value = true }) {
                Icon(painter = painterResource(R.drawable.baseline_timer_24), contentDescription = "IntervalPicker")
            }
        }
    }

    if(durationPicker.value)
        DurablePicker(
            durationPicker,
            state.hourInterval,
            state.minuteInterval,
            state.secondInterval
        ) { hour, minute, second ->
            update(state.copy(hourInterval = hour, minuteInterval = minute, secondInterval = second))
        }
}

@Composable
fun SelectTimerDialog(
    timers: List<Timer>,
    visible: MutableState<Boolean>,
    select: (Timer) -> Unit
) {

    SelectItemDialog(visible ) {
        LazyColumn(modifier = Modifier.padding(all = 10.dp)) {
            items(timers) { timer ->
                SimpleTimer(timer, modifier = Modifier.fillMaxWidth()) {
                    select(timer)
                }
                Divider()
            }
        }
    }
}