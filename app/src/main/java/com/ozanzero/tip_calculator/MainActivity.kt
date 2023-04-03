package com.ozanzero.tip_calculator

import android.media.midi.MidiOutputPort
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.NumberPicker.OnValueChangeListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ozanzero.tip_calculator.components.InputField
import com.ozanzero.tip_calculator.ui.theme.TipCalculatorTheme
import com.ozanzero.tip_calculator.widgets.RoundIconButton

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TipCalculatorTheme {
                // A surface container using the 'background' color from the theme
                MyApp()
            }
        }
    }
}
@Composable
fun MyApp(){
    Surface(modifier = Modifier, color = MaterialTheme.colors.background) {
        Column() {
            MainContent()
        }


    }
}


@Composable
fun TopHeader(totalPerPerson: Double = 0.0) {
    Surface(modifier = Modifier
        .fillMaxWidth()
        .padding(15.dp)
        .height(150.dp)
        .clip(shape = CircleShape.copy(all = CornerSize(22.dp))),
        color = Color(0xFFE9D7f7)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val total = "%.2f".format(totalPerPerson)
            Text(text = "Total Per Person",
            style = MaterialTheme.typography.h4)
            Text(text = "$$total",
                style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.ExtraBold
            )
        }

    }

}
@Preview
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainContent() {

BillForm(){billAmt ->
    Log.d("AMT","MainContent: $billAmt")

}

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BillForm(modifier: Modifier = Modifier,
             onValueChanged: (String) -> Unit = {}) {

    val totalPerson = remember {
        mutableStateOf(1)
    }

    val totalBillState = remember {
        mutableStateOf("")
    }
    val validState = remember (totalBillState.value){
        totalBillState.value.trim().isNotEmpty()

    }
    val sliderPositionState = remember {
        mutableStateOf(0f)
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val tipPercentage = (sliderPositionState.value * 100).toInt()
    val tipAmountState = remember {
        mutableStateOf(0.00)
    }
    val totalPerPersonState = remember {
        mutableStateOf(0.0)
    }

    TopHeader(totalPerPerson = totalPerPersonState.value)

    Surface(modifier = Modifier
        .padding(2.dp)
        .fillMaxWidth(),
        shape = RoundedCornerShape(corner = CornerSize(8.dp)),
        border = BorderStroke(width = 1.dp, color= Color.LightGray)
    ){
        Column(modifier =Modifier.padding(6.dp),
        verticalArrangement = Arrangement.Top,) {
            InputField(valueState = totalBillState, labelId ="Enter Bill" , enabled = true , isSingleLine =true,
                onAction = KeyboardActions {

                    if(!validState) return@KeyboardActions
                    onValueChanged(totalBillState.value.trim())
                    keyboardController?.hide()
                    totalPerPersonState.value =
                        calculateTotalPerPerson(totalBill = totalBillState.value.toDouble(),
                            splitBy = totalPerson.value, tipPercentage = tipPercentage)
                })
            if (validState) {
                Row(modifier = Modifier.padding(3.dp),verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Split", modifier = Modifier.align(
                        alignment = Alignment.CenterVertically))
                    Spacer(modifier = Modifier.weight(1f))
                    // sağ kısım butonlar
                    Row(modifier = Modifier.padding(horizontal = 3.dp),){
                        RoundIconButton( imageVector = Icons.Default.Clear, onClick = { Log.d("Icon",
                            "BillForm: Removed")
                        totalPerson.value =
                            if (totalPerson.value > 1) totalPerson.value - 1 else 1
                            totalPerPersonState.value =
                                calculateTotalPerPerson(totalBill = totalBillState.value.toDouble(),
                                    splitBy = totalPerson.value, tipPercentage = tipPercentage)
                        })

                        Text(text = totalPerson.value.toString(), modifier = Modifier
                            .align((Alignment.CenterVertically))
                            .padding(horizontal = 9.dp))
                        RoundIconButton( imageVector = Icons.Default.Add, onClick = { Log.d("IcoN",
                            "BillForm: ${totalBillState.value}")
                            totalPerson.value +=1
                            totalPerPersonState.value =
                                calculateTotalPerPerson(totalBill = totalBillState.value.toDouble(),
                                    splitBy = totalPerson.value, tipPercentage = tipPercentage)})
                    }
                }
                Row(modifier = Modifier.padding(horizontal = 3.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Tip")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "$${tipAmountState.value}",modifier=Modifier.padding(end = 45.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(text = "%$tipPercentage")
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Slider(value = sliderPositionState.value,
                        onValueChange = {newVal ->
                            sliderPositionState.value = newVal
                            tipAmountState.value =
                                calculateTotalTip(totalBillState.value.toDouble(), tipPercentage)

                            totalPerPersonState.value =
                                calculateTotalPerPerson(totalBill = totalBillState.value.toDouble(),
                                splitBy = totalPerson.value, tipPercentage = tipPercentage)


                        } ,
                    modifier = Modifier.padding(start = 16.dp, end  = 16.dp),
                    steps = 5,
                    onValueChangeFinished = {

                    })
                    
                }
            }
        }
    }
}

fun calculateTotalTip(totalBill: Double, tipPercentage: Int): Double {
return if (totalBill > 1 && totalBill.toString().isNotEmpty())
    (totalBill * tipPercentage) / 100 else 0.0
}

fun calculateTotalPerPerson(
    totalBill: Double,
    splitBy: Int,
    tipPercentage: Int):Double{
    val bill = calculateTotalTip(totalBill,tipPercentage) + totalBill
    return (bill / splitBy)

}

@Preview
@Composable
fun DefaultPreview() {
    TipCalculatorTheme {
        MyApp()

    }
}