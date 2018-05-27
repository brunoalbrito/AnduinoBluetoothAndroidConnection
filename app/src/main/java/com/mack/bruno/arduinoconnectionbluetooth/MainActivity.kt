package com.mack.bruno.arduinoconnectionbluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*;

class MainActivity : AppCompatActivity() {

    //Para manipular a conexao do bluetooth
    private val TURN_ON_BLUETOOTH: Int = 0 //TURN_BLUETOOTH_CONNECTION
    private val REQUEST_ENABLE_BT:Int = 1 //ACTION_REQUEST_DISCOVERABLE
    private val MESSAGE_READ:Int = 2 // NOTIFY IF STATUS CHANCE
    private val CONNECTION_STATUS = 3 // HOW ITS CONNECTION

    //TextViews
    private val mBluetoothStatus = bluetoothStatus
    private val mReadBuffer = readBuffer
    //Buttons
    private val mScanBtn = scan
    private val mOffBtn = off
    private val mDiscoverBtn = discover
    private val mListPairedDevicesBtn = PairedBtn
    //Checks
    private val mLED1 = checkboxLED1
    //variavel que manipula o bluetooth
    private val mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    //ListView pega da pagina
    private var mDevicesListView =  devicesListView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Cria a lista
        val mBTArrayAdapter = ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1)
        //coloca na lista o adapter de lista
        mDevicesListView.adapter = mBTArrayAdapter
        //transforma em objeto clicaveis
        mDevicesListView.onItemClickListener = AdapterView.OnItemClickListener{ parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as String

            exibirMensagem(this, selectedItem, Toast.LENGTH_SHORT)
        }



        //Se clicar no botao de ligar o bluetooth
        mScanBtn?.setOnClickListener(View.OnClickListener {
            if(!mBTAdapter.isEnabled){
                //Intent like a socket
                val enableBIntent:Intent  = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                //Chama essa conexao bluetooth
                startActivityForResult(enableBIntent,TURN_ON_BLUETOOTH)
                exibirMensagem(this, "Bluetooth Ligado", Toast.LENGTH_LONG)
            }else{
                Toast.makeText(this, "Já está ligado", Toast.LENGTH_LONG)
            }
        })

        //Se clicar no botao desligar
        mOffBtn.setOnClickListener(View.OnClickListener {
            mBTAdapter.disable()
            exibirMensagem(this, "Bluetooth Desligado", Toast.LENGTH_LONG)
        })
    }

    private fun exibirMensagem(context:Context, msg : String, duration: Int){
        Toast.makeText(context,msg,duration).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == TURN_ON_BLUETOOTH || requestCode == REQUEST_ENABLE_BT){
            //Verifica quando faz a conexao e aposta ativo ou desativado
            mBluetoothStatus.text = if(resultCode == Activity.RESULT_OK) "Habilidado" else "Desabilidado"
        }
    }
}
