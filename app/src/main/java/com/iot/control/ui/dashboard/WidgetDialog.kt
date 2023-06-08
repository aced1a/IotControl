package com.iot.control.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iot.control.R
import com.iot.control.model.Dashboard
import com.iot.control.model.Widget
import com.iot.control.model.WidgetAndDevice
import com.iot.control.model.enums.WidgetType
import com.iot.control.ui.devices.NewItemList
import com.iot.control.ui.scripts.CallSelectDeviceDialog
import com.iot.control.ui.utils.ColorPickerDialog
import com.iot.control.ui.utils.DashedDivider
import com.iot.control.ui.utils.DeviceSelectField
import com.iot.control.ui.utils.GeneralField
import com.iot.control.ui.utils.LabeledValue
import com.iot.control.ui.utils.SelectIconDialog
import com.iot.control.ui.utils.SelectItemDialog
import com.iot.control.ui.utils.ToggleField
import com.iot.control.ui.utils.TopBarDialog
import com.iot.control.viewmodel.DashboardDialogState
import com.iot.control.viewmodel.DashboardUiState
import com.iot.control.viewmodel.DashboardViewModel

@Composable
fun WidgetDialog(
    open: MutableState<Boolean>,
    viewModel: DashboardViewModel,
    state: DashboardDialogState,
    uiState: DashboardUiState,
) {
    val mode = remember { mutableStateOf(true) }

    TopBarDialog(
        title = stringResource(R.string.widget_options),
        close = { open.value = false },
        save = {
            if(mode.value)
                viewModel.save()
            else
                viewModel.saveDashboard()
            open.value = false
        }
    ) {
        WidgetDialogBody(viewModel, state, uiState, viewModel::update, mode)
    }
}

@Composable
fun WidgetDialogBody(
    viewModel: DashboardViewModel,
    state: DashboardDialogState,
    uiState: DashboardUiState,
    update: (DashboardDialogState) -> Unit,
    mode: MutableState<Boolean>
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 10.dp) ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            TabRow(if(mode.value) 0 else 1) {
                Tab(
                    selected = mode.value, onClick = { mode.value = true },
                    selectedContentColor = MaterialTheme.colorScheme.secondary,
                    unselectedContentColor = MaterialTheme.colorScheme.secondary
                ) {
                    Text(stringResource(R.string.widget_label))
                }
                Tab(
                    selected = !mode.value, onClick = { mode.value = false },
                    selectedContentColor = MaterialTheme.colorScheme.secondary,
                    unselectedContentColor = MaterialTheme.colorScheme.secondary
                ) {
                    Text(stringResource(R.string.dashboard))
                }
            }
            Spacer(modifier = Modifier.height(5.dp))

            if(mode.value)
                WidgetEdit(state, uiState, viewModel::selectDashboard, update)
            else
                DashboardEdit(viewModel, state, uiState, update = update, widgetDialog = { mode.value = true })
        }
    }


}

@Composable
fun WidgetEdit(
    state: DashboardDialogState,
    uiState: DashboardUiState,
    select: (Dashboard) -> Unit,
    update: (DashboardDialogState) -> Unit
) {
    val deviceList = remember { mutableStateOf(false) }
    val typeList = remember { mutableStateOf(false) }

    OutlinedTextField(
        value = state.name,
        label = { Text(stringResource(R.string.name_label)) },
        onValueChange = { update(state.copy(name = it)) },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(5.dp))

    GeneralField(stringResource(R.string.dashboard)) {

        DashboardSelectField(
            uiState.dashboard,
            uiState.dashboards,
            select,
            modifier = Modifier.fillMaxWidth(), null
        )
    }

    DeviceSelectField(stringResource(R.string.device_label), state.device) { deviceList.value = true }

    GeneralField(stringResource(R.string.type_label)) {
        WidgetTypePreview(state.type) { typeList.value = true }
    }

    ToggleField(label = stringResource(R.string.expanded_text), checked = state.expanded, modifier = Modifier.fillMaxWidth()) {
        update(state.copy(expanded = it))
    }

    IconFields(state, update)

    if (state.type == WidgetType.Value) ValueFields(state, update)

    if(typeList.value) WidgetTypeDialog(typeList) {
        update(state.copy(type = it))
        typeList.value = false
    }

    if(deviceList.value) {
        CallSelectDeviceDialog(
            uiState.devices,
            visibility = deviceList,
            select = {
                update(state.copy(device = it))
                deviceList.value = false
            }
        )
    }
}

@Composable
fun DashboardSelectField(
    dashboard: Dashboard?,
    dashboards: List<Dashboard>,
    select: (Dashboard) -> Unit,
    modifier: Modifier = Modifier,
    new: (() -> Unit)?
) {
    val open = remember { mutableStateOf(false) }

    if(dashboard == null)
        NewItemList(title = stringResource(R.string.new_dashboard)) { open.value = true }
    else {
        Text(
            dashboard.name,
            modifier = modifier.padding(start = 10.dp, top = 3.dp).clickable { open.value = true }
        )
    }

    if(open.value) SelectDashboardDialog(open, dashboards, new, select)
}

@Composable
fun SelectDashboardDialog(
    open: MutableState<Boolean>,
    dashboards: List<Dashboard>,
    new: (() -> Unit)?,
    select: (Dashboard) -> Unit
) {
    SelectItemDialog(visibility = open) {
        if(new != null) {
            NewItemList(title = stringResource(R.string.new_dashboard), center = true) {
                new()
                open.value = false
            }
        }
        dashboards.forEach { item ->
            Text(
                item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        select(item)
                        open.value = false
                    }
            )
            DashedDivider()
        }
    }
}

@Composable
fun ValueFields(
    state: DashboardDialogState,
    update: (DashboardDialogState) -> Unit
) {
    GeneralField(stringResource(R.string.value_type_label)) {
        OutlinedTextField(
            state.formatter,
            label = { Text(stringResource(R.string.format_label)) },
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { update(state.copy(formatter = it)) }
        )
        OutlinedTextField(
            state.subtext,
            label = { Text(stringResource(R.string.subtext_label)) },
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { update(state.copy(subtext = it)) }
        )
    }
}


@Composable
fun IconFields(
    state: DashboardDialogState,
    update: (DashboardDialogState) -> Unit
) {
    GeneralField(stringResource(R.string.icon_options)) {
        IconSelect(state, update)

        if(state.type == WidgetType.State || state.type == WidgetType.Switch) {
            ColorSelectFields(state, update)

            ToggleField(
                label = stringResource(R.string.use_icon_text),
                checked = state.useIcon,
                modifier = Modifier.fillMaxWidth(),
                update = { update(state.copy(useIcon = it)) }
            )
        }
    }
}

@Composable
fun IconSelect(
    state: DashboardDialogState,
    update: (DashboardDialogState) -> Unit
){
    val openDialog = remember { mutableStateOf(false) }

    LabeledValue(stringResource(R.string.icon_label), value = state.icon.name, modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = { openDialog.value = true }) {
            Icon(painter = painterResource(state.icon.id), contentDescription = "Icon")
        }
    }

    if(openDialog.value) SelectIconDialog(openDialog) {
        openDialog.value = false
        update(state.copy(icon = it))
    }
}


@Composable
fun ColorSelectFields(
    state: DashboardDialogState,
    update: (DashboardDialogState) -> Unit
) {
    val pickerDialog = remember { mutableStateOf(false) }
    val offColorSelecting = remember { mutableStateOf(false) }
    
    LabeledValue(stringResource(R.string.on_label), state.hexOn(), modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = {
            pickerDialog.value = true
            offColorSelecting.value = false
        }) {
             Icon(
                 painter = painterResource(R.drawable.baseline_rectangle_24),
                 contentDescription = "Color picker",
                 tint = Color(state.onColor)
             )
        }
    }

    LabeledValue(stringResource(R.string.off_label), state.hexOff(), modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = {
            pickerDialog.value = true
            offColorSelecting.value = true
        }) {
            Icon(
                painter = painterResource(R.drawable.baseline_rectangle_24),
                contentDescription = "Color picker",
                tint = Color(state.offColor)
            )
        }
    }
    
    if(pickerDialog.value) ColorPickerDialog(state.onColor, pickerDialog) {
        if(offColorSelecting.value)
            update(state.copy(offColor = it))
        else
            update(state.copy(onColor = it))
        pickerDialog.value = false
    }

}


@Composable
fun WidgetTypeDialog(
    openDialog: MutableState<Boolean>,
    onSelect: (WidgetType) -> Unit
) {
    SelectItemDialog(openDialog) {

        LazyColumn(modifier = Modifier.padding(all = 10.dp)) {
            items(WidgetType.values()) { type ->
                WidgetTypePreview(type ) { onSelect(type) }
                DashedDivider()
            }
        }

    }
}


@Composable
fun DashboardEdit(
    viewModel: DashboardViewModel,
    state: DashboardDialogState,
    uiState: DashboardUiState,
    widgetDialog: () -> Unit,
    update: (DashboardDialogState) -> Unit
) {
    GeneralField(stringResource(R.string.dashboard)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 5.dp)
        ) {
            DashboardSelectField(if(state.newDashboard) null else uiState.dashboard, uiState.dashboards,
                select = {
                    viewModel.selectDashboard(it)
                }
            ) {
                update(state.copy(newDashboard=true))
            }
            if(!state.newDashboard) IconButton(onClick = { viewModel.deleteDashboard() }) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete dashboard")
            }
        }
    }

    OutlinedTextField(
        state.dashboardName,
        label = { Text(stringResource(R.string.name_label)) },
        onValueChange = { update(state.copy(dashboardName = it)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
    )


    GeneralField(stringResource(R.string.widgets_label)) {
        DashboardWidgetsList(
            viewModel.filter(uiState.widgets),
            {
                viewModel.editWidget(it)
                widgetDialog()
            },
            {
                viewModel.newWidget()
                widgetDialog()
            },
            viewModel::delete
        )
    }
}

@Composable
fun DashboardWidgetsList(
    widgets: List<WidgetAndDevice>,
    editWidget: (Widget) -> Unit,
    newWidget: () -> Unit,
    delete: (Widget) -> Unit
) {
    LazyColumn {

        item {
            NewItemList(stringResource(R.string.new_script_label), center = true) { newWidget() }
        }
        items(widgets) { item ->
            LabeledValue(
               item.widget.name, item.widget.type.name,
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .fillMaxWidth()
            ) {
                Row {
                    IconButton(onClick = { editWidget(item.widget) }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit widget")
                    }
                    IconButton(onClick = { delete(item.widget) }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete widget")
                    }
                }
            }
            DashedDivider()
        }
    }
}