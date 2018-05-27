package com.mack.bruno.arduinoconnectionbluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*;
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    //Para manipular a conexao do bluetooth
    private val TURN_ON_BLUETOOTH: Int = 0 //TURN_BLUETOOTH_CONNECTION
    private val REQUEST_ENABLE_BT:Int = 1 //ACTION_REQUEST_DISCOVERABLE
    private val MESSAGE_READ:Int = 2 // NOTIFY IF STATUS CHANCE
    private val CONNECTION_STATUS = 3 // HOW ITS CONNECTION

    //variavel que manipula o bluetooth
    private var mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBTSocket:BluetoothSocket? = null
    //Cria a lista
    private var mBTArrayAdapter:ArrayAdapter<String>? = null

    //Faz o broadcast
    //Cria o broadcast com o aparelho
    private var blReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if(BluetoothDevice.ACTION_FOUND ==action ){
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                mBTArrayAdapter?.add("${device.name} ${device.address}")
                //Avisa da alteracao
                mBTArrayAdapter?.notifyDataSetChanged()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkboxLED1.setOnClickListener(View.OnClickListener {
            var i = 0;
            while(i < 1000){
                mBTSocket?.outputStream?.write("1".toByteArray())
                i = i + 1
            }
        })

        //Cria a lista
        mBTArrayAdapter = ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1)
        //ListView pega da pagina,coloca na lista o adapter de lista
        devicesListView.adapter = mBTArrayAdapter
        //transforma em objeto clicaveis
        devicesListView.onItemClickListener = AdapterView.OnItemClickListener{ parent, view, position, id ->
            //val selectedItem = parent.getItemAtPosition(position) as String
            //exibirMensagem(this, selectedItem, Toast.LENGTH_SHORT)

            if(!mBTAdapter.isEnabled){
                exibirMensagem(this, "Bluetooth desligado", Toast.LENGTH_SHORT)
            }else{
                bluetoothStatus.text = "Conectando..."

                val info:String = (view as TextView).text.toString()
                val address = info.substring(info.length -17)
                val name = info.substring(0, info.length -17)

                exibirMensagem(this,address, Toast.LENGTH_LONG)
                exibirMensagem(this,name, Toast.LENGTH_LONG)

                var fail:Boolean = false
                var adress:String? = null
                for( i in mBTAdapter.bondedDevices){
                    if(i.name == name){
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        exibirMensagem(this, "Caiu aqui", Toast.LENGTH_LONG)
                        mBTAdapter = BluetoothAdapter.getDefaultAdapter()
                        adress = i.address
                        break
                    }
                }
                try {
                    var dispositivo = mBTAdapter.getRemoteDevice(address)
                    mBTSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"))
                    exibirMensagem(this,"Caiu aqui", Toast.LENGTH_LONG)
                    //mBTSocket = (mBTAdapter.getRemoteDevice(address)).createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                    //mBTSocket = (mBTAdapter.getRemoteDevice(address)).createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                }catch (e: IOException ){
                    fail = true
                    Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                }
                try{
                    mBTSocket?.connect()
                }catch (e: IOException){
                    fail = true
                    Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show()
                }
                if(fail == false){
                    exibirMensagem(this, "Conexao criada com sucesso", Toast.LENGTH_LONG)
                }
            }
        }

        simpleSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener {
            buttonView, isChecked ->
            if(isChecked){
                //Se clicar no botao de ligar o bluetooth
                if(!mBTAdapter.isEnabled){
                    //Intent like a socket
                    val enableBIntent:Intent  = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    //Chama essa conexao bluetooth
                    startActivityForResult(enableBIntent,TURN_ON_BLUETOOTH)
                    exibirMensagem(this, "Bluetooth Ligado", Toast.LENGTH_LONG)
                }else {
                    Toast.makeText(this, "Já está ligado", Toast.LENGTH_LONG)
                }
                //Se clicar no botao desligar
                }else {
                mBTAdapter.disable()
                exibirMensagem(this, "Bluetooth Desligado", Toast.LENGTH_LONG)
                mBTArrayAdapter?.clear()
            }
        })


        //Busca por bluetooths perto
        discover.setOnClickListener(View.OnClickListener {
            //checa se ja esta procurando redes
            if(mBTAdapter.isDiscovering){
                mBTAdapter.cancelDiscovery()
                exibirMensagem(this, "Busca parada", Toast.LENGTH_LONG)
            }else{
                //Caso ja esteja ligado
                if(mBTAdapter.isEnabled){
                    //limpa a lista
                    mBTArrayAdapter?.clear()
                    //inicia a busca
                    mBTAdapter.startDiscovery()
                    exibirMensagem(this, "Busca iniciada", Toast.LENGTH_SHORT)
                    registerReceiver(blReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
                }else{
                    exibirMensagem(this, "Bluetooth esta desligado", Toast.LENGTH_SHORT)
                }
            }
        })

        PairedBtn.setOnClickListener(View.OnClickListener {
            var mPairedDevices = mBTAdapter.getBondedDevices();
            if(mBTAdapter.isEnabled){
                mBTArrayAdapter?.clear()
                for(device in mPairedDevices){
                    mBTArrayAdapter?.add(device.name + "\n" + device.address);
                    mBTArrayAdapter?.notifyDataSetChanged()
                }

                Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        })
    }

    private fun exibirMensagem(context:Context, msg : String, duration: Int){
        Toast.makeText(context,msg,duration).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == TURN_ON_BLUETOOTH || requestCode == REQUEST_ENABLE_BT){
            //Verifica quando faz a conexao e aposta ativo ou desativado
            bluetoothStatus.text = if(resultCode == Activity.RESULT_OK) "Habilidado" else "Desabilidado"
        }
    }
}
