package com.example.spgold;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.spgold.Util.BluetoothUtil;
import com.example.spgold.Util.SubirFacturaUtil;
import com.example.spgold.Util.TranslateUtil;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class TiqueteActivity extends AppCompatActivity {

    private TextView textView_esperar;
    private Button button;
    private Button button4;
    private String mes;
    private String anio;
    private String dia;
    private String hora;
    private String minuto;
    private String fecha;
    private String fecha_poner;
    private String Paga1;
    private String Paga2;
    private String Maniana;
    private String Hora_juego_M;
    private String Hora_lista_M;
    private String Dia;
    private String Hora_juego_D;
    private String Hora_lista_D;
    private String Tarde;
    private String Hora_juego_T;
    private String Hora_lista_T;
    private String Noche;
    private String Hora_juego_N;
    private String Hora_lista_N;
    private String Limite_maximo;
    private String Nombre_puesto;
    private String Numero_maquina;
    private String Comision_vendedor;
    private String Loteria;
    private String Horario;
    private EditText monto;
    private String tipo_lot;
    private EditText numero;
    private EditText cliente;
    private TextView tiquete;
    private TextView gen_tiquete;
    private String TiqueteCompleto = "";
    private HashMap tiquete_venta = new HashMap();
    private int monto_venta = 0;
    private int hora_juega = 0;
    private String juega_hora = "";
    private String contenido = "";
    private int consecutivo = 0;
    private String dispositivo;
    private String fecha_selectedS;
    private String mes_selectedS;
    private String anio_selectedS;
    private int fecha_selected;
    private int mes_selected;
    private int anio_selected;
    private TextView FECHA;
    private String flag_cadS;
    private boolean flag_cad;
    private Map<String, Integer> meses = new HashMap<String, Integer>();
    private String exed_monto;
    private int diferencia_exed;
    private String Spread_Sheet_Id;
    private String Spread_Sheet_Id_maniana;
    private String fecha_realistic;
    private String fecha_real;//Puede tomar los siguientes valores: "HOY" o "FUTURO"
    private HashMap<String, String> abajos2 = new HashMap<String, String>();
    private String SHEET = "";
    private String SPREADSHEET_ID;
    private String spreadSheet_loterias = "1a65hsEUcJ8w5Xl13bsPPwF8FuvqPy3s-KCG5BiuG7fo";
    private String addRowURL = "https://script.google.com/macros/s/AKfycbweyYb-DHVgyEdCWpKoTmvOxDGXleawjAN8Uw9AeJYbZ24t9arB/exec";
    private String Apodo_M;
    private String Apodo_D;
    private String Apodo_T;
    private String Apodo_N;

    @Override
    protected void onPause() {
        super.onPause();
        try {
            subir_facturas_resagadas("nada");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            subir_facturas_resagadas("nada");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ////////////COMIENZA INICIO GLOBAL////////////////////////////////////////////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tiquete);
        monto = (EditText)findViewById(R.id.et_monto);
        numero = (EditText)findViewById(R.id.et_numero);
        tiquete = (TextView)findViewById(R.id.tv_tiquete);
        cliente = (EditText)findViewById(R.id.et_cliente);
        gen_tiquete = (TextView)findViewById(R.id.tv_gentiquete);
        FECHA = (TextView)findViewById(R.id.textView_fecha);
        Paga1 = getIntent().getStringExtra("Paga1");
        Paga2 = getIntent().getStringExtra("Paga2");
        Maniana = getIntent().getStringExtra("Maniana");
        Hora_juego_M = getIntent().getStringExtra("Hora_juego_M");
        Hora_lista_M = getIntent().getStringExtra("Hora_lista_M");
        Dia = getIntent().getStringExtra("Dia");
        Hora_juego_D = getIntent().getStringExtra("Hora_juego_D");
        Hora_lista_D = getIntent().getStringExtra("Hora_lista_D");
        Tarde = getIntent().getStringExtra("Tarde");
        Hora_juego_T = getIntent().getStringExtra("Hora_juego_T");
        Hora_lista_T = getIntent().getStringExtra("Hora_lista_T");
        Noche = getIntent().getStringExtra("Noche");
        Hora_juego_N = getIntent().getStringExtra("Hora_juego_N");
        Hora_lista_N = getIntent().getStringExtra("Hora_lista_N");
        Limite_maximo = getIntent().getStringExtra("Limite_maximo");
        Nombre_puesto = getIntent().getStringExtra("Nombre_puesto");
        Apodo_M = getIntent().getStringExtra("Apodo_M");
        Apodo_D = getIntent().getStringExtra("Apodo_D");
        Apodo_T = getIntent().getStringExtra("Apodo_T");
        Apodo_N = getIntent().getStringExtra("Apodo_N");
        Numero_maquina = getIntent().getStringExtra("Numero_maquina");
        Comision_vendedor = getIntent().getStringExtra("Comision_vendedor");
        Spread_Sheet_Id = getIntent().getStringExtra("Spread_Sheet_Id");
        Spread_Sheet_Id_maniana = getIntent().getStringExtra("Spread_Sheet_Id_maniana");
        Loteria = getIntent().getStringExtra("Loteria");
        Horario = getIntent().getStringExtra("Horario");
        tipo_lot = getIntent().getStringExtra("tipo_lot");
        flag_cadS = getIntent().getStringExtra( "caduce");
        flag_cad = Boolean.parseBoolean(flag_cadS);
        fecha_selectedS = getIntent().getStringExtra("fecha_selected");
        mes_selectedS = getIntent().getStringExtra("mes_selected");
        anio_selectedS = getIntent().getStringExtra("anio_selected");
        textView_esperar = (TextView) findViewById(R.id.textView_esperar);
        button = (Button) findViewById(R.id.button);
        button4 = (Button) findViewById(R.id.button4);

        //El siguiente algoritmo de ifs se entera si se cambio o no la fecha de ventas.
        if (Integer.parseInt(fecha_selectedS) == 0) {
            if (Integer.parseInt(mes_selectedS) == 0) {
                if (Integer.parseInt(anio_selectedS) == 0) {
                    FECHA.setText("HOY");
                    fecha_real = "HOY";
                    SPREADSHEET_ID = Spread_Sheet_Id;
                }
            }
        } else {
            FECHA.setText("HOY");
            fecha_real = "HOY";
            SPREADSHEET_ID = Spread_Sheet_Id;
           /* FECHA.setText(fecha_selectedS + "/" + mes_selectedS + "/" + anio_selectedS);
            fecha_real = "FUTURO";
            SPREADSHEET_ID = Spread_Sheet_Id_maniana; */
        }

        fecha_selected = Integer.parseInt(fecha_selectedS);
        mes_selected = Integer.parseInt(mes_selectedS);
        anio_selected = Integer.parseInt(anio_selectedS);
        gen_tiquete.setText(Loteria + " " + Horario);
        dispositivo = check_device();
        llenar_mapa_meses();
        exed_monto = "";
        diferencia_exed = 0;
        /////////////FIN INICIO GLOBAL///////////////////////////////////////////////////////////////


        ///////////COMIENZA LA CONFIGURACION INICIAL/////////////////////////////////////////////////
        tiquete.setFocusableInTouchMode(false);//Hace que no se pueda escribir en el espacio donde aparecen los numeros que uno va agregando. Es como un carrito de compras.
        Date now = Calendar.getInstance().getTime();
        String ahora = now.toString();
        //se separan los campos de la fecha y hora para verificar que si se pueda realizar la venta.
        separar_fechaYhora(ahora);

        try {
            hora_juega = verificar_caducidad(false, flag_cad);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Se verifica si el archivo contable del dia existe.
        String fichName = Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt";
        if (comprobar_fichero(fichName)) {
            //Se comprueba si existe tiquete sin completar

        } else {
            //Se crea el archivo contable del dia.
            //Se llena archivo contable con todos sus valores en cero.
            int cero = 0;
            String cero_s = Integer.toString(cero);
            for (int i = 0; i <= 99; i++){
                String ii = Integer.toString(i);
                if (i == 0) {
                    agregar_linea_archivo(fichName, "00" + "      " + "0");
                }else if (i == 1) {
                    agregar_linea_archivo(fichName, "01" + "      " + "0");
                }else if (i == 2) {
                    agregar_linea_archivo(fichName, "02" + "      " + "0");
                }else if (i == 3) {
                    agregar_linea_archivo(fichName, "03" + "      " + "0");
                }else if (i == 4) {
                    agregar_linea_archivo(fichName, "04" + "      " + "0");
                }else if (i == 5) {
                    agregar_linea_archivo(fichName, "05" + "      " + "0");
                }else if (i == 6) {
                    agregar_linea_archivo(fichName, "06" + "      " + "0");
                }else if (i == 7) {
                    agregar_linea_archivo(fichName, "07" + "      " + "0");
                }else if (i == 8) {
                    agregar_linea_archivo(fichName, "08" + "      " + "0");
                }else if (i == 9) {
                    agregar_linea_archivo(fichName, "09" + "      " + "0");
                }else {
                    agregar_linea_archivo(fichName, ii + "      " + "0");
                }
            }//archivo contable creado!!!

        }
        comprobar_archivo();

        try {
            subir_facturas_resagadas("nada");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Implementacion de un text listener
        numero.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) {
                    String monto1 = monto.getText().toString();//Se parcea el valor a un string
                    if (monto1.isEmpty()) {
                        imprimir_mensaje();
                        numero.setText("");
                        return;
                    }
                    int monto = Integer.parseInt(monto1);
                    if (monto <= 49){
                        imprimir_mensaje();
                        numero.setText("");
                    } else {
                        agregar_numero();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /////////////FINALIZA LA CONFIGURACION INICIAL//////////////////////////////////////////////////

    }

    private void mostrar_todito() {

        textView_esperar.setText("");
        textView_esperar.setVisibility(View.INVISIBLE);

        monto.setVisibility(View.VISIBLE);
        numero.setVisibility(View.VISIBLE);
        tiquete.setVisibility(View.VISIBLE);
        cliente.setVisibility(View.VISIBLE);
        gen_tiquete.setVisibility(View.VISIBLE);
        FECHA.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
        button4.setVisibility(View.VISIBLE);

    }

    private void ocultar_todito() {

        textView_esperar.setVisibility(View.VISIBLE);
        textView_esperar.setText("   Conectando...\n\nPor favor espere...");

        monto.setVisibility(View.INVISIBLE);
        numero.setVisibility(View.INVISIBLE);
        tiquete.setVisibility(View.INVISIBLE);
        cliente.setVisibility(View.INVISIBLE);
        gen_tiquete.setVisibility(View.INVISIBLE);
        FECHA.setVisibility(View.INVISIBLE);
        button.setVisibility(View.INVISIBLE);
        button4.setVisibility(View.INVISIBLE);

    }

    private void mostrar_todo() {
/*
        textView_esperar.setText("");
        textView_esperar.setVisibility(View.INVISIBLE);

        monto.setVisibility(View.VISIBLE);
        numero.setVisibility(View.VISIBLE);
        tiquete.setVisibility(View.VISIBLE);
        cliente.setVisibility(View.VISIBLE);
        gen_tiquete.setVisibility(View.VISIBLE);
        FECHA.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
        button4.setVisibility(View.VISIBLE);
*/
    }

    private void ocultar_todo() {
/*
        textView_esperar.setVisibility(View.VISIBLE);
        textView_esperar.setText("   Conectando...\n\nPor favor espere...");

        monto.setVisibility(View.INVISIBLE);
        numero.setVisibility(View.INVISIBLE);
        tiquete.setVisibility(View.INVISIBLE);
        cliente.setVisibility(View.INVISIBLE);
        gen_tiquete.setVisibility(View.INVISIBLE);
        FECHA.setVisibility(View.INVISIBLE);
        button.setVisibility(View.INVISIBLE);
        button4.setVisibility(View.INVISIBLE);
*/
    }

    /*Personalizacion de la navegacion hacia atras!!
    #################################################################################################*/
    @Override
    public void onBackPressed(){
        boton_atras();
    }

    private void boton_atras() {
        Intent Main = new Intent(this, MainActivity.class);
        startActivity(Main);
        finish();
        System.exit(0);
    }
    //#################################################################################################

    private String check_device() {
        String archivos[] = fileList();
        dispositivo = null;
        if (ArchivoExiste(archivos, "device.txt")) {
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput("device.txt"));
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();

                if (linea != null) {
                    //Se lee el dispositivo guardado
                    dispositivo = linea;
                    dispositivo = dispositivo.replace("\n", "");
                } else {
                    //Error!!!
                }
                br.close();
                archivo.close();
            } catch (IOException e) {
            }
        }
        return dispositivo;
    }

    public void printIt(String Mensaje) {

        if (dispositivo.equals("Celular")) {

            Intent Activity_ver = new Intent(this, VerActivity.class);
            Activity_ver.putExtra("mensaje", Mensaje);
            startActivity(Activity_ver);

        } else if (dispositivo.equals("Maquina")) {
            BluetoothSocket socket;
            socket = null;
            byte[] data = Mensaje.getBytes();

            //Get BluetoothAdapter
            BluetoothAdapter btAdapter = BluetoothUtil.getBTAdapter();
            if (btAdapter == null) {
                Toast.makeText(getBaseContext(), "BlueTooth abierto!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Get sunmi InnerPrinter BluetoothDevice
            BluetoothDevice device = BluetoothUtil.getDevice(btAdapter);
            if (device == null) {
                Toast.makeText(getBaseContext(), "Asegurese de tener conectada una impresora!!!", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                socket = BluetoothUtil.getSocket(device);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                assert socket != null;
                BluetoothUtil.sendData(data, socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Error en tipo de dispositivo", Toast.LENGTH_LONG).show();
        }
    }

    private int contar(String monto_act, String numero_act) {
        int exeso = 0;
        int valor = Integer.parseInt(monto_act);//Se parcea el monto del numero jugado.
        String archivos[] = fileList();
        if (ArchivoExiste(archivos, Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt")) {//nombre del archivo CONTABle del dia
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt"));//Se abre archivo contable
                BufferedReader br = new BufferedReader(archivo);
                String TiqueteContable = "";//Aqui se lee el contenido del tiquete guardado.

                String linea = br.readLine();//Se lee archivo contable
                while (linea != null) {
                    String[] split = linea.split("      ");//Se separa el monto del numero guardado.

                    if (Integer.parseInt(split[0]) == Integer.parseInt(numero_act)){
                        int monto_numero = Integer.parseInt(split[1]);

                        ///////////SE VERIFICA QUE NO EXEDA EL LIMITE PERMITIDO DE VENTAS//////////////////////////////////////

                        int amonu = monto_numero + valor;
                        if (amonu > Integer.parseInt(Limite_maximo)) {
                            exeso = amonu - Integer.parseInt(Limite_maximo);
                            monto_numero = amonu - exeso;
                            diferencia_exed = diferencia_exed + exeso;
                            exed_monto = exed_monto + "Maximo permitido para\nel numero " + numero_act + " exedido!\nSe devuelven " + exeso + " colones. \n";
                            Toast.makeText(this, "Monto exede el maximo permi-\ntido para el numero " + numero_act + "\nSe devuelven " + exeso + " colones. ", Toast.LENGTH_LONG).show();
                        } else {
                            monto_numero = monto_numero + valor;
                        }

                        //////////////////////////////////////////////////////////////////////////////////////////////////////

                        linea = numero_act + "      " + String.valueOf(monto_numero);
                    }

                    TiqueteContable = TiqueteContable + linea + "\n";
                    linea = br.readLine();
                    //return;
                }
                br.close();
                archivo.close();
                guardar(TiqueteContable, Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt");
            }catch (IOException e) {
            }
        }
        return exeso;
    }

    public void generar_pedido (View view){
        String jugador = cliente.getText().toString();

       /* if (jugador.isEmpty()){
            Toast.makeText(this, "Ingrese el nombre del cliente!!!", Toast.LENGTH_LONG).show();
            cliente.requestFocus();
            return;
        }

        */

        String archivos[] = fileList();
        Log.v("Generar_pedido()","Se va a generar un pedido");

        if (ArchivoExiste(archivos, "Tiquete" + Loteria + ".txt")) {//Archivo Tiquete_Nombre_loteria.txt es el archivo temporal que almacena cada venta.

            JSONObject tiquete_Json;

            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput("Tiquete" + Loteria + ".txt"));
                BufferedReader br = new BufferedReader(archivo);
                TiqueteCompleto = "";//Aqui se lee el contenido del tiquete guardado.

                String linea = br.readLine();
                int cont_log = 0;
                while (linea != null) {
                    Log.v("Gen_ped..#" + String.valueOf(cont_log), "Linea de tiquete: " + linea + "\n");
                    cont_log++;
                    String[] split = linea.split("      ");//Se separa el monto del numero jugado.
                    int diff = contar(split[1], split[0]);//Aqui se llama a la funcion que agrega la venta al archivo contable y devuelve la diferencia si se sobrepasa el limite permitido en algun numero.
                    int valor = Integer.parseInt(split[1]);
                    if (diff > 0) {
                        monto_venta = monto_venta + valor - diff;
                        linea = split[0] + "      " + String.valueOf(Integer.parseInt(split[1]) - diff);
                    } else {
                        monto_venta = monto_venta + valor;
                    }
                    TiqueteCompleto = TiqueteCompleto + linea + "\n";
                    ///////////////////MORE ONLINE OPTIONS/////////////////////////

                    //Se crea fichero identico al archivo online

                    /*
                    if (fecha_real.equals("HOY")) {
                        agregar_linea_archivo(Numero_maquina + "_" + Loteria + "_" + Horario + "_" + fecha + "_" + mes + "_" + anio + "_.txt", linea);
                    } else {
                        //No guardar tiquete vendido a futuro.
                    }
                     */

                    //////////////////////////////////////////////////////////////
                    linea = br.readLine();
                }
                br.close();
                archivo.close();
                guardar(TiqueteCompleto, "Tiquete" + Loteria + ".txt");
                String tcompleto = TiqueteCompleto;
                //////////////////ONLINE OPTIONS///////////////////////////////
                //Se inicia la variable SHEET que corresponde a la hoja que se va a editar.

                if (Horario.equals("Maniana")) {
                    SHEET = "maniana";
                } else if (Horario.equals("Dia")) {
                    SHEET = "dia";
                } else if (Horario.equals("Tarde")) {
                    SHEET = "tarde";
                } else if (Horario.equals("Noche")) {
                    SHEET = "noche";
                } else {
                    //Nunca debe llegar aqui. !!!
                }
                //String SPREADSHEET_ID = Spread_Sheet_Id;
                //tiquete_Json = TranslateUtil.file_to_Json(tcompleto, SPREADSHEET_ID, SHEET);

                //////////////////////////////////////////////////////////////
                Toast.makeText(this, "Total: " + Integer.toString(monto_venta), Toast.LENGTH_SHORT).show();
                TiqueteCompleto = "";
                tiquete.setText(TiqueteCompleto);
                pre_generar();
                generar_tiquete_venta();
            }catch (IOException | JSONException e) {
            }
        } else {
            Toast.makeText(this, "Error!!!\nDebe ingresar datos", Toast.LENGTH_LONG).show();
        }
    }

    private void abajoniar() {
        try {//Se analiza el archivo a ver si ya hay ventas de la loteria en cuestion.

            InputStreamReader archivio = new InputStreamReader(openFileInput("fileloterias_vendidasfile.txt"));
            BufferedReader br = new BufferedReader(archivio);
            //String new_line = "";//Aqui se escribe el nuevo contenido del archivo.
            String linea = br.readLine();
            Log.v("abajoniar ", "Archivo: " + "fileloterias_vendidasfile.txt" + "\n\nContenido de archivio: \n\n" + imprimir_archivo("fileloterias_vendidasfile.txt"));
            //int log_file = 0;
            while (linea != null) {
                //linea_archivo(fund_file, "abajo   " + Loteria + "   " + Horario + "   " + fecha_poner);
                String[] split = linea.split("   ");
                //Log.v("linea #" + log_file + " Archivio: ", linea + "\n");
                Log.v("fecha_realistic: ", fecha_realistic + "\n");
                Log.v("fecha_poner: ", fecha_poner + "\n");
                Log.v("fecha_selectedS: ", fecha_selectedS + "\n");
                Log.v("split[3]: ", split[3] + ", puede ser mayor, menor o igual a fecha_realistic. \nfecha_realistic: " + fecha_realistic + "\n");
                if (Integer.parseInt(split[3]) < Integer.parseInt(fecha_realistic)) {
                    //Do nothin. Con esto se hace que la linea sea borrada. Es una factura vieja.
                } else if (Integer.parseInt(split[3]) > Integer.parseInt(fecha_realistic)) {//Se deja la linea igual. Es una factura futura.
                    //new_line = new_line + linea + "\n";
                } else if (Integer.parseInt(split[3]) == Integer.parseInt(fecha_realistic)) {//Se modifica, se elimina o se deja igual.
                    if (split[0].equals("abajo")) {
                        if (verificar_internet()) {
                            //if (split[1].equals(Loteria) & split[2].equals(Horario)) {

                            //#################Vamos a tratar de subir la informacion a la nube: #######################################

                            RequestQueue queue;
                            queue = Volley.newRequestQueue(this);

                            //Debug:
                            //mensaje_confirma_subida(jsonObject.toString());

                            //Llamada POST usando Volley:
                            RequestQueue requestQueue;

                            // Instantiate the cache
                            Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

                            // Set up the network to use HttpURLConnection as the HTTP client.
                            Network network = new BasicNetwork(new HurlStack());

                            // Instantiate the RequestQueue with the cache and network.
                            requestQueue = new RequestQueue(cache, network);

                            // Start the queue
                            requestQueue.start();

                            //Toast.makeText(this, "Debug:\nConsecutivo: " + Consecutivo + "\nconsecutivo: " + consecutivo + "\nDeben ser iguales.", Toast.LENGTH_LONG).show();

                            String url = addRowURL;

                            JSONObject jsonObject = TranslateUtil.vendidas_to_Json(split[1], split[2], spreadSheet_loterias);

                            ocultar_todito();
                            br.close();
                            archivio.close();

                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                    (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Log.v("abajoniar response: ", "Response:\n\n" + response.toString() + "\n\n");
                                            try {//Se analiza el archivo a ver si ya hay ventas de la loteria en cuestion.
                                                InputStreamReader archivioo = new InputStreamReader(openFileInput("fileloterias_vendidasfile.txt"));
                                                BufferedReader bro = new BufferedReader(archivioo);
                                                String new_lineo = "";//Aqui se escribe el nuevo contenido del archivo.
                                                String lineao = bro.readLine();

                                                while (lineao != null) {
                                                    String[] splity = lineao.split("   ");
                                                    if (Integer.parseInt(splity[3]) < Integer.parseInt(fecha_realistic)) {
                                                        //Do nothin. Con esto se hace que la linea sea borrada. Es una factura vieja.
                                                    } else if (Integer.parseInt(splity[3]) > Integer.parseInt(fecha_realistic)) {//Se deja la linea igual. Es una factura futura
                                                        new_lineo = new_lineo + lineao + "\n";
                                                    } else if (Integer.parseInt(splity[3]) == Integer.parseInt(fecha_realistic)) {//Se modifica, se elimina o se deja igual.
                                                        if (splity[1].equals(split[1]) & splity[2].equals(split[2])) {
                                                            if (splity[0].equals("abajo")) {
                                                                Log.v("camb band abajoniar", "Linea: " + lineao + "\n");
                                                                lineao = lineao.replace("abajo", "arriba");
                                                                new_lineo = new_lineo + lineao + "\n";
                                                            } else {
                                                                new_lineo = new_lineo + lineao + "\n"; //Es una factura "arriba"
                                                            }
                                                        } else {
                                                            new_lineo = new_lineo + lineao + "\n";
                                                        }
                                                    } else {
                                                        //Do nothing here!!!
                                                    }
                                                    lineao = bro.readLine();
                                                }

                                                bro.close();
                                                archivioo.close();
                                                borrar_archivo("fileloterias_vendidasfile.txt");
                                                crear_archivo("fileloterias_vendidasfile.txt");
                                                guardar(new_lineo, "fileloterias_vendidasfile.txt");
                                                mostrar_todito();
                                                pre_generar();
                                            } catch (IOException | JSONException e) {
                                            }
                                        }
                                    }, new Response.ErrorListener() {

                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            // TODO: Handle error
                                            //mensaje_error_en_subida();
                                            mostrar_todito();
                                            try {
                                                pre_generar();
                                            } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                            // Add the request to the RequestQueue.
                            requestQueue.add(jsonObjectRequest);
                            break;
                            //##########################################################################################################
                        } else {
                            //new_line = new_line + linea + "\n";//Si no hay internet, se queda como este, aunque sea "abajo".
                            //Continue. Do nothing here!
                        }

                    } else {
                        //new_line = new_line + linea + "\n"; //Se dejan las "arribas"
                    }
                } else {
                    //Do nothing here!!!
                }
                linea = br.readLine();
            }

            br.close();
            archivio.close();
        } catch (IOException | JSONException e) {}
    }

    private void pre_generar() throws FileNotFoundException, JSONException {

        String archivos[] = fileList();
        boolean crear_file = true;
        String fund_file = "fileloterias_vendidasfile.txt";
        for (int i = 0; i < archivos.length; i++){
            Log.v("pre_generar #" + String.valueOf(i), "Archivo: " + archivos[i] + "\n");
            Pattern pattern = Pattern.compile("loterias_vendidas", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound){
                //marcar bandera crear_file.
                Log.v("ErrorCrearfile", "Archivo encontrado: " + archivos[i] + "\nContenido del archivo:\n\n" + imprimir_archivo(archivos[i]));
                crear_file = false;
                break;
            }
        }
        Log.v("pre_gen_ cambio flag", "Bandera crear archivo: " + crear_file + "\n");
        if (crear_file){
            crear_archivo(fund_file);
        }

        boolean loteria_encontrada = false;
        boolean loteria_abajo = false;
        String estado_subidas = "desconocido";
        try {//Se analiza el archivo a ver si ya hay ventas de la loteria en cuestion.

            InputStreamReader archivio = new InputStreamReader(openFileInput(fund_file));
            BufferedReader br = new BufferedReader(archivio);
            String new_line = "";//Aqui se escribe el nuevo contenido del archivo.
            String linea = br.readLine();
            Log.v("pre_gen #0", "Archivio: " + fund_file + "\n\nContenido de archivio: \n\n" + imprimir_archivo(fund_file));
            int log_file = 0;
            while (linea != null) {
                String[] split = linea.split("   ");
                Log.v("linea #" + log_file + " Archivio: ", linea + "\n");
                Log.v("fecha_realistic: ", fecha_realistic + "\n");
                Log.v("fecha_poner: ", fecha_poner + "\n");
                Log.v("fecha_selectedS: ", fecha_selectedS + "\n");
                Log.v("split[3]: ", split[3] + ", puede ser mayor, menor o igual a fecha_realistic. \nfecha_realistic: " + fecha_realistic + "\n");
                if (Integer.parseInt(split[3]) < Integer.parseInt(fecha_realistic)) {
                    //Do nothin. Con esto se hace que la linea sea borrada. Es una factura vieja.
                } else if (Integer.parseInt(split[3]) > Integer.parseInt(fecha_realistic)) {//Se deja la linea igual. Es una factura futura
                    new_line = new_line + linea + "\n";
                } else if (Integer.parseInt(split[3]) == Integer.parseInt(fecha_realistic)) {//Se modifica, se elimina o se deja igual.
                    if (split[0].equals("abajo")) {
                        //if (split[1].equals(Loteria) & split[2].equals(Horario)) {
                        if (split[1].equals(Loteria) & split[2].equals(Horario)) {
                            //if (split[0].equals("abajo")) {
                            loteria_abajo = true;
                            loteria_encontrada = true;
                            new_line = new_line + linea + "\n";
                        } else {
                            estado_subidas = "abajo";
                            new_line = new_line + linea + "\n";
                        }
                    } else if (split[1].equals(Loteria) & split[2].equals(Horario)) {
                        new_line = new_line + linea + "\n"; //Se dejan las "arribas"
                        loteria_encontrada = true;
                    } else {
                        new_line = new_line + linea + "\n"; //Se dejan las "arribas"
                    }
                } else {
                    //Do nothing here!!!
                }
                linea = br.readLine();
            }

            br.close();
            archivio.close();
            borrar_archivo(fund_file);
            crear_archivo(fund_file);
            guardar(new_line, fund_file);
            Log.v("pre_gen_final:", "Archivo completo despues de leerlo: " + imprimir_archivo(fund_file) + "\n");
        } catch (IOException e) {}

        Log.v("pre_gen banderas ", ".\nloteria_encontrada: " + loteria_encontrada + "\nloteria_abajo: " + loteria_abajo + "\n");

        if (estado_subidas.equals("abajo")) {
            if (verificar_internet()) {
                abajoniar();
            } else {
                //Continue. Do nothing here!
            }
        } else {
            //Continue. Do nothing here!
        }

        if (loteria_encontrada) {
            if (loteria_abajo) {
                if (verificar_internet()) {

                    //#################Vamos a tratar de subir la informacion a la nube: #######################################

                    RequestQueue queue;
                    queue = Volley.newRequestQueue(this);

                    //Debug:
                    //mensaje_confirma_subida(jsonObject.toString());

                    //Llamada POST usando Volley:
                    RequestQueue requestQueue;

                    // Instantiate the cache
                    Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

                    // Set up the network to use HttpURLConnection as the HTTP client.
                    Network network = new BasicNetwork(new HurlStack());

                    // Instantiate the RequestQueue with the cache and network.
                    requestQueue = new RequestQueue(cache, network);

                    // Start the queue
                    requestQueue.start();

                    //Toast.makeText(this, "Debug:\nConsecutivo: " + Consecutivo + "\nconsecutivo: " + consecutivo + "\nDeben ser iguales.", Toast.LENGTH_LONG).show();

                    String url = addRowURL;

                    JSONObject jsonObject = TranslateUtil.vendidas_to_Json(Loteria, Horario, spreadSheet_loterias);

                    ocultar_todito();

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                            (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.v("pre_gen response: ", "Response:\n\n" + response.toString() + "\n\n");
                                    try {//Se analiza el archivo a ver si ya hay ventas de la loteria en cuestion.
                                        InputStreamReader archivio = new InputStreamReader(openFileInput(fund_file));
                                        BufferedReader br = new BufferedReader(archivio);
                                        String new_line = "";//Aqui se escribe el nuevo contenido del archivo.
                                        String linea = br.readLine();

                                        while (linea != null) {
                                            String[] split = linea.split("   ");
                                            if (Integer.parseInt(split[3]) < Integer.parseInt(fecha_realistic)) {
                                                //Do nothin. Con esto se hace que la linea sea borrada. Es una factura vieja.
                                            } else if (Integer.parseInt(split[3]) > Integer.parseInt(fecha_realistic)) {//Se deja la linea igual.
                                                new_line = new_line + linea + "\n";
                                            } else if (Integer.parseInt(split[3]) == Integer.parseInt(fecha_realistic)) {//Se modifica o se deja igual.
                                                if (split[1].equals(Loteria) & split[2].equals(Horario)) {
                                                    if (split[0].equals("abajo")) {
                                                        Log.v("camb band pre_gen", "Linea: " + linea + "\n");
                                                        linea = linea.replace("abajo", "arriba");
                                                        new_line = new_line + linea + "\n";
                                                    } else {
                                                        new_line = new_line + linea + "\n";
                                                    }
                                                } else {
                                                    new_line = new_line + linea + "\n";
                                                }
                                            } else {
                                                //Do nothing here!!!
                                            }
                                            linea = br.readLine();
                                        }

                                        br.close();
                                        archivio.close();
                                        borrar_archivo(fund_file);
                                        crear_archivo(fund_file);
                                        guardar(new_line, fund_file);
                                        mostrar_todito();
                                        //generar_tiquete_venta();
                                    } catch (IOException e) {
                                    }
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // TODO: Handle error
                                    //mensaje_error_en_subida();

                                }
                            });

                    // Add the request to the RequestQueue.
                    requestQueue.add(jsonObjectRequest);

                    //##########################################################################################################

                } else {
                    //generar_tiquete_venta();
                }
            }
            else {
                //generar_tiquete_venta();
            }
        } else {
            agregar_linea_archivo(fund_file, "abajo   " + Loteria + "   " + Horario + "   " + fecha_poner);
            pre_generar();
        }
    }

    private void generar_tiquete_venta() throws FileNotFoundException, JSONException {

        //**************************************************************************************************************

        Date now = Calendar.getInstance().getTime();
        String ahora = now.toString();
        //se separan los campos de la fecha y hora para verificar que si se pueda realizar la venta.
        separar_fechaYhora(ahora);

        try {
            hora_juega = verificar_caducidad(true, flag_cad);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String jugador = cliente.getText().toString();
        if (jugador.isEmpty()){
            jugador = "Cliente nuevo";
        } else if (jugador.length() > 10) {
            jugador = "Cliente nuevo";
            Toast.makeText(this, "Nombre del cliente demasiado largo...", Toast.LENGTH_LONG);
            //return;
        }
        //int ram_value = (int)(Math.random()*10+1);


        //##########################################################################################

        String consecutivo_str = "";
        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("invoice.txt"));
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            String linea_consecutivo = "";
            String[] split = linea.split(" ");
            consecutivo = Integer.parseInt(split[1]);
            consecutivo = consecutivo + 1;//Se incrementa el consecutivo de facturacion.
            consecutivo_str = String.valueOf(consecutivo);
            linea = split[0] + " " + consecutivo_str;
            linea_consecutivo = linea_consecutivo + linea + "\n";
            int flag_cont = 0;
            linea = br.readLine();//Se lee la segunda linea del archivo
            while (linea != null) {

                linea = linea.replace("\n", "");

                //Debuggeo
                //Toast.makeText(this, "linea: \n\n" + linea, Toast.LENGTH_LONG).show();

                linea_consecutivo = linea_consecutivo + linea + "\n";
                linea = br.readLine();
                flag_cont++;


            }
            guardar(linea_consecutivo, "invoice.txt");//Se actualiza el contador de consecutivos.
            if (flag_cont == 0) { //TODO: Evitar que el archivo de facturas cresca desmedido y llene la memoria.
                //guardar(split[0] + " " + split[1],"invoice.txt");//Se elimina el cambio de linea si no se ha generado ni una sola factura. (Parece innecesario)
            }

            br.close();
            archivo.close();
            //imprimir_archivo("invoice.txt");
        } catch (IOException e) {
        }

        //*** La siguiente linea se debe colocar despues de generar el nombre del archivo tiquete factura.
        //agregar_linea_archivo("invoice.txt", consecutivo_str + " " + tempFile);

        //##########################################################################################



        //Aqui se acondiciona el nombre del jugador para que no hallan errores al guardar los archivos
        String jugador_act = "";
        String[] split_nom_parts_vertical = jugador.split("\n");
        int size_nom_parts_vert = split_nom_parts_vertical.length;
        for (int i = 0; i < size_nom_parts_vert; i++){
            if (split_nom_parts_vertical[i] == " "){
                //do nothing.
            }else if (split_nom_parts_vertical[i] == "\n"){
                //do nothing.
            }else if(size_nom_parts_vert == (i + 1)) {
                jugador_act = jugador_act + split_nom_parts_vertical[i];
                //Toast.makeText(this, "Parte del nombre: " + jugador_act + "\nCiclo for 1. ", Toast.LENGTH_LONG).show();
            }else {
                jugador_act = jugador_act + split_nom_parts_vertical[i] + "x_x";
            }
        }
        //jugador_act = "";
        String[] split_nom_parts = jugador_act.split(" ");
        int size_nom_parts = split_nom_parts.length;
        jugador_act = "";
        for (int i = 0; i < size_nom_parts; i++){
            if (split_nom_parts[i] == " "){
                //do nothing.
            }else if (split_nom_parts[i] == "\n"){
                //do nothing.
            }else if (size_nom_parts == (i + 1)){
                jugador_act = jugador_act + split_nom_parts[i];
                //Toast.makeText(this, "Parte del nombre: " + jugador_act + "\nCiclo for 1. ", Toast.LENGTH_LONG).show();
            }else {
                jugador_act = jugador_act + split_nom_parts[i] + "x_x";
            }
        }

        //TODO: Evitar caracteres especiales que puedan generar un error a la hora de guardar el archivo.
        //Evitar que si el vendedor mete 2 espacios en el nombre se valla a generar un error. (Completed)

        String hoora = "00";
        String miinuto = "00";
        if (Horario.equals("Maniana")) {
            String[] spplit = Hora_juego_M.split(":");
            hoora = spplit[0];
            miinuto = spplit[1];
        } else if (Horario.equals("Dia")) {
            String[] spplit = Hora_juego_D.split(":");
            hoora = spplit[0];
            miinuto = spplit[1];
        } else if (Horario.equals("Tarde")) {
            String[] spplit = Hora_juego_T.split(":");
            hoora = spplit[0];
            miinuto = spplit[1];
        } else if (Horario.equals("Noche")) {
            String[] spplit = Hora_juego_N.split(":");
            hoora = spplit[0];
            miinuto = spplit[1];
        } else {
            //Nothing to do here!!!
        }

        String tempFile = jugador_act + "_separador_" + Loteria + "_separador_" + Horario + "_separador_" + fecha + "_separador_" + hoora + "_separador_" + miinuto + "_separador_" + consecutivo_str + "_separador_" + dia + "_separador_" + mes + "_separador_" + tipo_lot + "_separador_" + Paga1 + "_separador_" + Paga2 + "_separador_" + monto_venta + "_separador_" + anio + "_separador_null.txt";

        agregar_linea_archivo("facturas_online.txt", "abajo " + tempFile + " " + SPREADSHEET_ID + " " + SHEET + " " + tipo_lot);

        tempFile = tempFile.replace("\n","");
        consecutivo_str = consecutivo_str.replace("\n","");

        ///////////////SOME ONLINE OPTIONS//////////////////////////////////////////

        //String factura_a_subir = "factura_a_subir_" + consecutivo_str + "_xxx.txt";
        //crear_archivo(factura_a_subir);
        //String SHEET = Numero_maquina + Loteria + Horario;
        //agregar_linea_archivo(factura_a_subir, "tierra " + SPREADSHEET_ID + " " + SHEET + " null");

        ///////////////////////////////////////////////////////////////////////////

        crear_archivo(tempFile);
        String tod = consecutivo_str + " " + tempFile;
        tod = tod.replace("\n","");
        agregar_linea_archivo("invoice.txt", tod);

        String linea_temp = "";
        contenido = "";//Aqui se lee el contenido del archivo guardado.
        linea_temp = "Factura # " + consecutivo_str + "\n";
        contenido = contenido + linea_temp;
        linea_temp = "\n      --->***********<---";
        contenido = contenido + linea_temp + "\n";
        linea_temp = "    **     Tiempos " + Nombre_puesto + "   **  ";
        contenido = contenido + linea_temp + "\n";
        if (tipo_lot.equals("Reventados")) {
            linea_temp = "Pagamos " + Paga1 + " veces bolita roja!\n    y " + Paga2 + " veces bolita verde!";
        } else {
            linea_temp = "       Pagamos " + Paga1 + " veces!!!";
        }
        contenido = contenido + linea_temp + "\n";
        linea_temp = "       Somos la banca mas \n        solida del pais";
        contenido = contenido + linea_temp + "\n";
        linea_temp = "       --->***********<---\n";
        contenido = contenido + linea_temp + "\n";
        linea_temp = Loteria + " " + Horario;
        contenido = contenido + linea_temp + "\n";
        linea_temp = "Juega a las " + String.valueOf(juega_hora);
        contenido = contenido + linea_temp + "\n";
        if (!flag_cad) {
            linea_temp = "el dia: " + fecha + "/" + meses.get(mes) + "/" + anio;
        } else {
            linea_temp = "el dia: " + fecha + "/" + mes + "/" + anio;
        }
        contenido = contenido + linea_temp + "\n#############################";
        String archivos[] = fileList();
        if (ArchivoExiste(archivos, "Tiquete" + Loteria + ".txt")) {//Archivo nombre_archivo es el archivo que vamos a copiar

            try {
                InputStreamReader archivo24 = new InputStreamReader(openFileInput("Tiquete" + Loteria + ".txt"));//Se abre archivo
                BufferedReader br24 = new BufferedReader(archivo24);
                //contenido = "";//Aqui se lee el contenido del archivo guardado.

                String linea = br24.readLine();//Se lee archivo

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                int counter = 0;
                String linea_tempo = "";
                while (linea != null) {
                    //String linea_adapt = linea.replace("      ", " ");//Adaptamos el archivo para subirlo a la nube.
                    //agregar_linea_archivo(factura_a_subir, linea_adapt);
                    String[] split = linea.split("      ");
                    String separacion_str = "";
                    int separacion = 6 - split[1].length();
                    for (int i = 0; i < separacion; i++) {
                        separacion_str = separacion_str + " ";
                    }
                    counter++;
                    if (counter == 1) {
                        linea_tempo = split[0] + separacion_str + split[1] + "| ";
                    } else if (counter == 2) {
                        linea_tempo = linea_tempo + split[0] + separacion_str + split[1] + "| ";
                    } else if (counter == 3) {
                        linea_tempo = linea_tempo + split[0] + separacion_str + split[1];
                        counter = 0;
                    }

                    if (counter == 0) {
                        //linea = linea_tempo;
                        contenido = contenido + "\n" + linea_tempo;
                    }
                    String liniesilla = linea + "      " + SPREADSHEET_ID + "      " + SHEET;
                    agregar_linea_archivo(tempFile, liniesilla);
                    linea = br24.readLine();
                }
                if (counter == 1) {
                    contenido = contenido + "\n" + linea_tempo;
                } else if (counter == 2) {
                    contenido = contenido + "\n" + linea_tempo;
                }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                String jugador_print = jugador.replace("x_x"," ");

                if (exed_monto.isEmpty()) {
                    linea_temp = "\n#############################\n Total:  " + monto_venta + " colones. \n#############################\nEstimado/a " + jugador_print + ", no\nolvide revisar su tiquete\nantes de retirarse del puesto.\n";
                } else {
                    linea_temp = "\n#############################\n" + exed_monto + "\n#############################\nTotal:  " + monto_venta + " colones. \n#############################\nEstimado/a " + jugador_print + ", no\nolvide revisar su tiquete\nantes de retirarse del puesto.\n";
                }


                contenido = contenido + linea_temp + "\n\n\n";
                monto_venta = 0;
                br24.close();
                archivo24.close();
            }catch (IOException e) {
            }
        }

        try {
            subir_facturas_resagadas(contenido);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //**************************************************************************************************************

        //Al final, se elimina el archivo temporal de la venta:
        View view3 = null;
        borrar_archivo_lot_actual(view3);
    }

    private void msg(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private void subir_facturas_resagadas(String conteni) throws JSONException {
        boolean flag_internet = verificar_internet();
        //JSONObject objeto_Json_a_subir = null;
        if (flag_internet) {
            ocultar_todo();
            obtener_Json_otras_facturas(conteni);
        } else {
            //Toast.makeText(this, "Verifique su coneccion a Internet!!!", Toast.LENGTH_LONG).show();
            if (conteni.equals("nada")) {
                //Do nothing.
            } else {
                impmir_tiquete(conteni);
            }
        }
    }

    private void abajiar(String conteni) throws JSONException {

        //ocultar_todo();

        for (String key : abajos2.keySet()) {
            JSONObject objeto_json = new JSONObject();
            String SSHHEETT = "";
            String SSPPRREEAADDSSHHEETT = "";
            String tipo_lot = "";
            String factura = "";
            String file = abajos2.get(key);
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(abajos2.get(key)));
                //imprimir_archivo("facturas_online.txt");
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();

                if (linea != null){
                    String[] split = linea.split("      ");
                    String[] split_name = abajos2.get(key).split("_separador_");
                    Log.v("ERROR9001_facturas", "Linea leida: " + linea);
                    if (linea.equals("BORRADA")) {
                        Log.v("abajiar_linea_borrada", "\n\nLinea: " + linea);
                        //Do nothing.
                    } else if (linea.isEmpty()) {
                        Log.v("abajiar_linea_empty", "\n\nLinea: " + linea);
                        //Do nothing.
                    } else if (split_name[14].equals("equi.txt")) {
                        Log.v("abajiar_nombre_equi.txt", "\n\nLinea: " + linea);
                        tipo_lot= split_name[9];
                        if (tipo_lot.equals("Monazos")) {
                            SSPPRREEAADDSSHHEETT = split[3];
                            SSHHEETT = split[4];
                        } else if (tipo_lot.equals("Parley")) {
                            SSPPRREEAADDSSHHEETT = split[3];
                            SSHHEETT = split[4];
                        } else if (tipo_lot.equals("Reventados")) {
                            SSPPRREEAADDSSHHEETT = split[2];
                            SSHHEETT = split[3];
                        } else if (tipo_lot.equals("Regular")) {
                            SSPPRREEAADDSSHHEETT = split[2];
                            SSHHEETT = split[3];
                        } else {
                            //Nothing here never
                        }
                        factura = split_name[6];
                        //file = abajos2.get(key);
                        Log.v("Errequilibrar_facturas", "\n\nFinal del nombre del archivo: " + split_name[14] + "\n\nTipo loteria: " + tipo_lot + "\nSpreadSheet: " + SSPPRREEAADDSSHHEETT + "\nSheet: " + SSHHEETT + "\nFactura numero: " + factura + "file: " + file);
                        br.close();
                        archivo.close();
                        equilibrar(SSPPRREEAADDSSHHEETT, SSHHEETT, file, factura, tipo_lot, key);
                        break;
                    } else if (split_name[14].equals("null.txt")) {
                        Log.v("abajiar_nombre_null.txt", "\n\nLinea: " + linea);
                        tipo_lot= split_name[9];
                        if (tipo_lot.equals("Monazos")) {
                            SSPPRREEAADDSSHHEETT = split[3];
                            SSHHEETT = split[4];
                        } else if (tipo_lot.equals("Parley")) {
                            SSPPRREEAADDSSHHEETT = split[3];
                            SSHHEETT = split[4];
                        } else if (tipo_lot.equals("Reventados")) {
                            SSPPRREEAADDSSHHEETT = split[2];
                            SSHHEETT = split[3];
                        } else if (tipo_lot.equals("Regular")) {
                            SSPPRREEAADDSSHHEETT = split[2];
                            SSHHEETT = split[3];
                        } else {
                            //Nothing here never
                        }
                        factura = split_name[6];
                        //file = abajos2.get(key);\n
                        Log.v("Error9003_pre", "\n\nTipo_lot: " + tipo_lot + "\nSpreadSheet: " + SSPPRREEAADDSSHHEETT + "\nSheet: " + SSHHEETT + "\nFactura numero: " + factura + "\n\n.");
                        objeto_json = generar_Json_resagadas(file, factura, SSHHEETT, SSPPRREEAADDSSHHEETT, tipo_lot);
                        Log.v("Error9003_facturas", "\n\nTipo loteria: " + tipo_lot + "\nSpreadSheet: " + SSPPRREEAADDSSHHEETT + "\nSheet: " + SSHHEETT + "\nFactura numero: " + factura + "file: " + file);
                        abajos2.remove(key);
                        br.close();
                        archivo.close();
                        subir_factura_resagadas(objeto_json, "nothing", conteni);
                        break;
                    } else {
                        Log.v("abajiar_ninguna_opcion", " Error!!! Nunca deberia llegar aqui!!!\n\nLinea: " + linea);
                        //Do nothing.
                    }
                }
                br.close();
                archivo.close();
            } catch (IOException e) {

            }
        }
        //mostrar_todo();
    }

    private void obtener_Json_otras_facturas(String conteni) throws JSONException {

        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("facturas_online.txt"));
            //imprimir_archivo("facturas_online.txt");
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            //String contenido = "";
            abajos2.clear();
            Integer countercito = 0;
            boolean flag = true;
            while (linea != null) {
                countercito++;
                String count = String.valueOf(countercito);
                String[] split = linea.split(" ");
                if (split[0].equals("abajo")) {
                    Log.v("OJOF_abajo: ", "\n\nLinea: " + linea + " Fin de linea!!!");
                    abajos2.put(count, split[1]);
                    flag = false;
                } else if (split[0].equals("BORRADA")) {
                    Log.v("OJOF_BORRADA: ", "\n\nLinea: " + linea + " Fin de linea!!!");
                    //TODO: Pensar que hacer!!!
                } else if (split[0].equals("arriba")) {
                    Log.v("OJOF_arriba: ", "\n\nLinea: " + linea + " Fin de linea!!!");
                    //TODO: Pensar que hacer!!!
                }  else {
                    Log.v("OJOF_(error): ", "\n\n(No deberia llegar aqui!!!\n\nLinea: " + linea + " Fin de linea!!!");
                    //Do nothing.
                }
                linea = br.readLine();
            }

            if (flag) {
                mostrar_todo();
                return;
            } else {
                //Do nothing. Continue with the work
            }

            archivo.close();
            br.close();

        } catch (IOException e) {
        }

        abajiar(conteni);
        //return objeto_json;
    }

    private JSONObject generar_Json_resagadas(String file, String factura, String SSHHEETT, String SPREEADSHEET_ID, String tipo_lote) {
        //boolean flag_subir = false;
        JSONObject jsonObject = new JSONObject();

        //Debug:
        //imprimir_archivo(file);
        boolean flagsita = true;

        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput(file));
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            String json_string = "";
            while (linea != null) {
                String[] split = linea.split("      ");
                if (linea.equals("BORRADA")) {
                    Log.v("ErrorBorrada", "Factura esta borrada, no hacer nada!!!");
                    flagsita = false;
                } else if (tipo_lote.equals("Regular") | tipo_lote.equals("Reventados")) {
                    //                            #1                #2             monto          ext. info         factura
                    json_string = json_string + split[0] + "_n_" + "no" + "_n_" + split[1] + "_n_" + "no" + "_n_" + factura + "_l_";
                } else if (tipo_lote.equals("Monazos")) {
                    //                            #1                #2             monto            ext. info           factura
                    json_string = json_string + split[0] + "_n_" + "no" + "_n_" + split[1] + "_n_" + split[2] + "_n_" + factura + "_l_";
                } else if (tipo_lote.equals("Parley")) {
                    //                            #1                #2                 monto           ext. info        factura
                    json_string = json_string + split[0] + "_n_" + split[1] + "_n_" + split[2] + "_n_" + "no" + "_n_" + factura + "_l_";
                } else {
                    //Do nothing. Nunca llega aqui!
                }
                linea = br.readLine();
            }
            br.close();
            archivo.close();
            if (flagsita) {
                jsonObject = TranslateUtil.string_to_Json(json_string, SPREEADSHEET_ID, SSHHEETT, factura);
            }
            else {
                Log.v("Error21", "Factura ha sido borrada!!!");
            }
        } catch (IOException | JSONException e) {
        }
        return jsonObject;
    }

    private JSONObject generar_Json(String file, String factura){
        //boolean flag_subir = false;
        JSONObject jsonObject = new JSONObject();

        //Debug:
        //imprimir_archivo(file);

        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput(file));
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            String json_string = "";
            while (linea != null) {
                String[] split = linea.split("      ");
                //                            #1                #2             monto          ext. info         factura
                json_string = json_string + split[0] + "_n_" + "no" + "_n_" + split[1] + "_n_" + "no" + "_n_" + factura + "_l_";
                linea = br.readLine();
            }
            br.close();
            archivo.close();
            jsonObject = TranslateUtil.string_to_Json(json_string, SPREADSHEET_ID, SHEET, factura);
        } catch (IOException | JSONException e) {
        }
        return jsonObject;
    }

    private void mensaje_confirma_subida(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    public void borrar_archivo(String file) {
        File archivo = new File(file);
        String empty_string = "";
        guardar(empty_string, file);
        archivo.delete();
    }

    private void equilibrar(String SpreadSheet, String Sheet, String file, String factura, String tipo_lot, String key) {//Este metodo revisa si se ha subido parte del tiquete a la nube.
        //Se llama a la SpreadSheet que contiene la loteria actual para verificar que no hay errores en la subida de datos. Usar: Method.get
        RequestQueue requestQueue;
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        String readRowURL = "https://script.google.com/macros/s/AKfycbxJNCrEPYSw8CceTwPliCscUtggtQ2l_otieFmE/exec?spreadsheetId=" + SpreadSheet +"&sheet=" + Sheet;

        String url = readRowURL;

        ocultar_todo();

        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        // Do something with the response
                        //ML_ver.setText(response);
                        //msg(response);

                        //HashMap<String, String> premios = new HashMap<String, String>();
                        //msg("Response: " + response);
                        if (response != null) {
                            //response.replace("loteria", "_sepa_");

                            //String[] split = response.split("loteria");//Se separa el objeto Json
                            //TODO: Algoritmo que revisa la spreadsheet para ver si se ha subido de manera parcial un tiquete cualquiera, pero que no se ha terminado de subir, por lo tanto aparece como "abajo"
                            Log.v("Equilibrar onResponse", " Response: \n\n" + response);
                            //debug:
                            //msg("Response: (Debe ser toda la SpreadSheet a la cual se ha subido el tiquete anterior, mostrado en \"Atencion1\"\n\n" + response);
                            //TODO: Se debe modificar el archivo "file" con la informacion de numeros negativizados encontrados en la spreadsheet con el mismo numero de factura.

                            boolean flagsitilla = false;

                            String[] split = response.split("numero1");//Se separa el objeto Json "response"
                            //Se llena un HashMap local con las ventas, las cuales se bajan de la nube.
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            int new_monto = 0;
                            for (int i = 1; i < split.length; i++) {//En este loop se filtran los valores repetidos en la tabla.

                                String[] split2 = split[i].split("\"");
                                String nu1 = split2[2];
                                String nu2 = split2[6];
                                String monto = split2[10];//10
                                String extra_info = split2[14];//14
                                String factura_leida = split2[18];//Numero de factura
                                Log.v("Equilibrar fact_leida", "\n\nFactura leida: " + factura_leida);
                                String iD = split2[22];
                                Log.v("E6", "split[18]: " + split2[18] + " split[14]: " + split2[14]);
                                String key_factura = "ojo-rojo_ojo-rojo" + nu1 + "ojo-rojo_ojo-rojo" + nu2 + "ojo-rojo_ojo-rojo" + extra_info + "ojo-rojo_ojo-rojo" + iD + "ojo-rojo_ojo-rojo" + monto + "ojo-rojo_ojo-rojo";
                                String valor_factura = factura_leida;//numero de factura
                                //Debug:
                                //msg("Key: " + key_factura + "\nValue: " + valor_factura);

                                //////////////////////////////////////////////////////////////////////////
                                ///////Algoritmo que verifica si hay IDs iguales para omitirlos!//////////
                                //////////////////////////////////////////////////////////////////////////
                                if (hashMap.containsKey(key_factura)) {
                                    Log.v("equilibrar ID repetido", "\n\nID: " + iD + ". (No se hace nada!)\n\n");
                                    //Do nothing!
                                    //Si llega aqui significa que ha encontrado una linea repetida en la spreadsheet, por lo tanto la omite.
                                } else {//   numeros jugados  monto jugado

                                    int un_fact = Integer.parseInt(factura);
                                    int dos_fact = Integer.parseInt(factura_leida);
                                    Log.v("equilibrar en el for", "\n\nFactura: " + un_fact + " Factura leida: " + dos_fact);
                                    if (un_fact == dos_fact) {
                                        new_monto = new_monto + Integer.parseInt(monto);
                                        hashMap.put(key_factura, valor_factura);//Lo que hay en este hashMap es la informacion que se pudo haber subido de manera parcial y haber dejado el mensaje: "abajo"
                                    } else {
                                        //Do nothing.
                                    }
                                }
                                //////////////////////////////////////////////////////////////////////////
                            }

                            //borrar_archivo(file);

                            String[] splityto = file.split("_separador_");
                            String factoura = String.valueOf((Integer.parseInt(splityto[6])) * -1);
                            String montitito = String.valueOf(new_monto * -1);
                            String new_name = splityto[0] + "_separador_" + splityto[1] + "_separador_" + splityto[2] + "_separador_" + splityto[3] + "_separador_" + splityto[4] + "_separador_" + splityto[5] + "_separador_" + factoura + "_separador_" + splityto[7] + "_separador_" + splityto[8] + "_separador_" + splityto[9] + "_separador_" + splityto[10] + "_separador_" + splityto[11] + "_separador_" + montitito + "_separador_" + splityto[13] + "_separador_" + "null.txt";
                            crear_archivo(new_name);
                            //agregar_linea_archivo("facturas_online.txt", "abajo " + new_name + " " + SpreadSheet + " " + Sheet + " " + tipo_lot);
                            //Se hace que file sea un archivo igual a cualquier factura para subirla. Se guarda la informacion necesaria en el file.
                            for (String key : hashMap.keySet()) {
                                Log.v("E0 for hashMap", "\nKey: " + key + "\nValue: " + hashMap.get(key) + "\ntipo_lot: " + tipo_lot + "\n");
                                String[] splity = key.split("ojo-rojo_ojo-rojo");
                                //msg("Factura: " + key + "\nValor: " + hashMap.get(key) + "\n");
                                int otnom = Integer.parseInt(splity[5]) * -1;
                                if (tipo_lot.equals("Monazos")) {
                                    agregar_linea_archivo(new_name, splity[1] + "      " + String.valueOf(otnom) + "      " + splity[3] + "      " + SpreadSheet + "      " + Sheet);
                                    flagsitilla = true;
                                } else if(tipo_lot.equals("Parley")) {
                                    agregar_linea_archivo(new_name, splity[1] + "      " + splity[2] + "      " + String.valueOf(otnom) + "      " + SpreadSheet + "      " + Sheet);
                                    flagsitilla = true;
                                } else if (tipo_lot.equals("Reventados")) {
                                    agregar_linea_archivo(new_name, splity[1] + "      " + String.valueOf(otnom) + "      " + SpreadSheet + "      " + Sheet);
                                    flagsitilla = true;
                                } else if (tipo_lot.equals("Regular")) {
                                    agregar_linea_archivo(new_name, splity[1] + "      " + String.valueOf(otnom) + "      " + SpreadSheet + "      " + Sheet);
                                    flagsitilla = true;
                                } else {
                                    //Do nothing.
                                }
                            }

                            if (flagsitilla) {
                                //agregar_linea_archivo("facturas_online.txt", "abajo " + new_name + " " + SpreadSheet + " " + Sheet + " " + tipo_lot);//Se hace que file sea un archivo igual a cualquier factura para subirla. Se guarda la informacion necesaria en el file.
                                agregar_fact_online(new_name, SpreadSheet, Sheet, tipo_lot);
                                Log.v("flagsitilla: ", "se cambiara la bandera de la factura # " + String.valueOf((Integer.parseInt(splityto[6]))));
                                cambiar_bandera(String.valueOf((Integer.parseInt(splityto[6]))), "equi");
                                //Aqui se crea un tiquete con algun tipo de id que sirva para equilibrar alguna factura que se subio de manera parcial.
                                int factur = Integer.parseInt(factura) * -1;
                                JSONObject objeto_json = generar_Json_resagadas(new_name, String.valueOf(factur), Sheet, SpreadSheet, tipo_lot);
                                abajos2.remove(key);
                                //cambiar_bandera(String.valueOf(factur), "equi");
                                Log.v("Error9003_facturas", "\n\nTipo loteria: " + tipo_lot + "\nSpreadSheet: " + SpreadSheet + "\nSheet: " + Sheet + "\nFactura numero: " + factura + "file: " + new_name);
                                try {
                                    subir_factura_resagadas(objeto_json, "equi", "nada");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            } else {
                                //Todo or do nothing! I don't know right now :-|
                            }
                            //cambiar_bandera(String.valueOf(factura), "equi");
                            //cambiar_bandera(String.valueOf(factura), "equi");

                        } else {
                            Log.v("Error de respuesta", ".\nResponse:\n--> " + response + " <--\n\n.");
                            //Respuesta es null. No deberia pasar nunca.
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                    }
                });
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    private void mensaje_error_en_subida() {
        //Toast.makeText(this, "Error subiendo la factura a la base de datos!!!", Toast.LENGTH_LONG).show();
    }

    private void cambiar_bandera (String Consecutivo, String tag) {
        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("facturas_online.txt"));
            //imprimir_archivo("facturas_online.txt");
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();

            String contenido = "";
            while (linea != null) {
                //String[] split_perreo = linea.split(" ");
                Log.v("cambiar_bandera_fact", "  Linea: " + linea + "\nTag: " + tag + "\n\n");
                //Toast.makeText(this, "Debug:\nFuncion cambiar_bandera, linea:\n" + linea, Toast.LENGTH_LONG).show();
                String[] split = linea.split(" ");
                String[] split_perreo = split[1].split("_separador_");
                if (split_perreo[14].equals("equi.txt")) {
                    Log.v("Cambiar_band_equi", " Se ha intentado cambiar bandera a archivo \"equi.txt\"\nLinea: " + linea + "\n\n               Tag: " + tag + "\n\n");
                    //Do nothing for now!! maybe later do a TODO.
                    if (tag.equals("equi")) {
                        if (split[0].equals("BORRADA")) {
                            //TODO: Pensar que hacer!!!
                        } else if (split[0].equals("abajo")) {
                            String[] split_name = split[1].split("_separador_");
                            String factura = split_name[6];// split_name[6] contiene el numero de la factura que se desea subir.
                            if (factura.equals(Consecutivo)) {
                                linea = linea.replace("abajo", "arriba");
                                contenido = contenido + linea + "\n";
                            } else {
                                contenido = contenido + linea + "\n";
                            }
                        } else if (split[0].equals("arriba")) {
                            //No hacer nada garantiza que se borra la linea que ya esta arriba
                        } else {
                            //Do nothing. No deberia llegar aqui.
                        }
                    } else {
                        contenido = contenido + linea + "\n";
                    }

                } else {
                    Log.v("Cambiar_band_null.txt", " Se intentara cambiar bandera a archivo \"null.txt\"\nLinea: " + linea);
                    if (tag.equals("BORRADA")) {
                        if (split[0].equals("BORRADA")) {
                            //Do nothing guaranties that the line will be erased
                        } else if (split[0].equals("abajo")) {
                            String[] split_name = split[1].split("_separador_");
                            String factura = split_name[6];// split_name[6] contiene el numero de la factura que se desea subir.
                            if (factura.equals(Consecutivo)) {
                                linea = linea.replace("abajo", tag);
                                contenido = contenido + linea + "\n";
                            } else {
                                contenido = contenido + linea + "\n";
                            }
                        } else if (split[0].equals("arriba")) {
                            /*String[] split_name = split[1].split("_separador_");
                            String factura = split_name[6];// split_name[6] contiene el numero de la factura que se desea subir.
                            if (factura.equals(Consecutivo)) {
                                linea = linea.replace("arriba", tag);
                                contenido = contenido + linea + "\n";
                            } else {
                                contenido = contenido + linea + "\n";
                            }*/
                            //No hacer nada garantiza el borrado de la linea que contiene "arriba"
                        } else {
                            //Do nothing. No deberia llegar aqui.
                        }
                    } else {
                        if (split[0].equals("BORRADA")) {
                            //No hacer nada para eliminar la linea!
                        } else if (split[0].equals("abajo")) {
                            String[] split_name = split[1].split("_separador_");
                            String factura = split_name[6];// split_name[6] contiene el numero de la factura que se desea subir.
                            if (factura.equals(Consecutivo)) {
                                linea = linea.replace("abajo", "arriba");
                                contenido = contenido + linea + "\n";
                            } else {
                                contenido = contenido + linea + "\n";
                            }
                        } else if (split[0].equals("arriba")) {
                            //No hacer nada para eliminar la linea!
                        } else {
                            //Do nothing. No deberia llegar aqui.
                        }
                    }
                }
                linea = br.readLine();
            }

            br.close();
            archivo.close();
            borrar_archivo("facturas_online.txt");
            //imprimir_archivo("facturas_online.txt");
            guardar(contenido, "facturas_online.txt");//Aqui se eliminan las lineas que corresponden a archivos que ya se han subido.
            Log.v("cambiar_band_result", "\n\nArchivo \"facturas_online.txt\":\n\n" + imprimir_archivo("facturas_online.txt"));
            //imprimir_archivo("facturas_online.txt");

        } catch (IOException e) {
        }
    }

    private void subir_factura_resagadas(JSONObject jsonObject, String tag, String conteni) throws JSONException {
        //flag_file_arriba = false;

        //msg("Json: 123 " + jsonObject);

        RequestQueue queue;
        queue = Volley.newRequestQueue(this);

        //Debug:
        //mensaje_confirma_subida(jsonObject.toString());

        //Llamada POST usando Volley:
        RequestQueue requestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        //Toast.makeText(this, "Debug:\nConsecutivo: " + Consecutivo + "\nconsecutivo: " + consecutivo + "\nDeben ser iguales.", Toast.LENGTH_LONG).show();

        String url = addRowURL;

        ocultar_todo();
        if (conteni.equals("nada")) {
            //Do nothing.
        } else {
            impmir_tiquete(conteni);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String[] split = response.toString().split("\"");
                        //msg("Json: 123 " + response.toString());
                        int length_split = split.length;
                        Log.v("info_sub_fact_resag: ", "\n\n" + response + "\n\n");
                        if (length_split > 3) {
                            if (split[2].equals(":")) {
                                //mensaje_confirma_subida(response.toString());
                                String factura_num = split[15];

                                //mensaje_confirma_subida("factura #" + factura_num + " se ha subido correctamente!");
                                cambiar_bandera (factura_num, tag);
                                mostrar_todo();
                                if (conteni.equals("nada")) {
                                    //Do nothing.
                                } else {
                                    //impmir_tiquete(conteni);
                                }
                                try {
                                    abajiar("nada");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                String factura_num = split[15];
                                //mensaje_confirma_subida("Factura #" + factura_num + " no se ha subido!");
                            }
                        } else {
                            //No se subio correctamente!
                            String factura_num = split[15];
                            //mensaje_confirma_subida("Factura #" + factura_num + " no se ha subido!");
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        //mensaje_error_en_subida();

                    }
                });

        // Add the request to the RequestQueue.
        requestQueue.add(jsonObjectRequest);
        //Toast.makeText(this, "Debug:\nBandera flag_file_arriba (antes del return): " + String.valueOf(flag_file_arriba), Toast.LENGTH_LONG).show();

        //Toast.makeText(this, "Debug:\nEste mensaje debe aparecer despues del mensaje de funcion cambiar_bandera.\nSi aparece antes es que no es sincronico!!!", Toast.LENGTH_LONG).show();

    }
/*
    private void subir_factura(JSONObject jsonObject, String Consecutivo) throws JSONException {
        //flag_file_arriba = false;

        RequestQueue queue;
        queue = Volley.newRequestQueue(this);

        //Debug:
        //mensaje_confirma_subida(jsonObject.toString());

        //Llamada POST usando Volley:
        RequestQueue requestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        //Toast.makeText(this, "Debug:\nConsecutivo: " + Consecutivo + "\nconsecutivo: " + consecutivo + "\nDeben ser iguales.", Toast.LENGTH_LONG).show();

        String url = addRowURL;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String[] split = response.toString().split("\"");
                        int length_split = split.length;
                        if (length_split > 3) {
                            if (split[3].equals(SHEET)) {
                                //mensaje_confirma_subida(response.toString());
                                mensaje_confirma_subida("factura #" + Consecutivo + " se ha subido correctamente!");
                                cambiar_bandera (Consecutivo);
                            } else {
                                //mensaje_confirma_subida("Factura " + Consecutivo + " no se ha subido!");
                            }
                        } else {
                            //No se subio correctamente!
                            mensaje_confirma_subida("Factura " + Consecutivo + " no se ha subido!");
                        }

                        //flag_file_arriba = true;

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        mensaje_error_en_subida();

                    }
                });

        // Add the request to the RequestQueue.
        requestQueue.add(jsonObjectRequest);
        //Toast.makeText(this, "Debug:\nBandera flag_file_arriba (antes del return): " + String.valueOf(flag_file_arriba), Toast.LENGTH_LONG).show();

        //Toast.makeText(this, "Debug:\nEste mensaje debe aparecer despues del mensaje de funcion cambiar_bandera.\nSi aparece antes es que no es sincronico!!!", Toast.LENGTH_LONG).show();

    }*/

    private boolean verificar_internet() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            //Toast.makeText(this, "Debe estar conectado a una red WiFi o datos mobiles.", Toast.LENGTH_LONG).show();
            return false;
        } else {
            //Si esta conectado a internet.
            //Toast.makeText(this, "Conectado a internet!", Toast.LENGTH_LONG).show();
            return true;
        }
    }

    private void llenar_mapa_meses() {

        meses.put("Jan",1);
        meses.put("Feb",2);
        meses.put("Mar",3);
        meses.put("Apr",4);
        meses.put("May",5);
        meses.put("Jun",6);
        meses.put("Jul",7);
        meses.put("Aug",8);
        meses.put("Sep",9);
        meses.put("Oct",10);
        meses.put("Nov",11);
        meses.put("Dic",12);
        meses.put("1",1);
        meses.put("2",2);
        meses.put("3",3);
        meses.put("4",4);
        meses.put("5",5);
        meses.put("6",6);
        meses.put("7",7);
        meses.put("8",8);
        meses.put("9",9);
        meses.put("10",10);
        meses.put("11",11);
        meses.put("12",12);

    }



    public void borrar_archivo_lot_actual(View view) throws FileNotFoundException {

        String archivos[] = fileList();
        File archivo = new File("Tiquete" + Loteria + ".txt");
        String archivo_name = "Tiquete" + Loteria + ".txt";

        if (ArchivoExiste(archivos, "Tiquete" + Loteria + ".txt")) {

            for (int i = 0; i < archivos.length; i++) {
                if (archivo_name.equals(archivos[i])) {
                    String aa = "";
                    guardar(aa, "Tiquete" + Loteria + ".txt");
                    archivo.delete();
                }
            }
        }

        if (view == null){
            tiquete.setText("");//Le indicamos a la aplicacion que to-do eso lo coloque en el editText.
            cliente.setText("");
            monto.setText("");
            numero.setText("");
            //Intent Activity_ventas = new Intent(this, VentasActivity.class);
            //startActivity(Activity_ventas);
            //finish();
            //System.exit(0);
        }else {
            tiquete.setText("");//Le indicamos a la aplicacion que to-do eso lo coloque en el editText.
            cliente.setText("");
            monto.setText("");
            numero.setText("");
            //Intent Activity_ventas = new Intent(this, VentasActivity.class);
            //startActivity(Activity_ventas);
            //finish();
            //System.exit(0);
        }
    }

    private void impmir_tiquete(String s) {

        imprimiendoAnim();
        printIt(s);

    }

    private void imprimiendoAnim() {
        //Toast.makeText(getApplicationContext(), "imprimiendo...",	Toast.LENGTH_SHORT).show();
    }

    private void agregar_fact_online(String file, String spid, String sheet, String tip_lot) {
        String linea_agrgar = "abajo " + file + " " + spid + " " + sheet + " " + tip_lot;//agregar_linea_archivo("facturas_online.txt", "abajo " + file + " " + SPREADSHEET_ID + " " + SHEET + " " + tipo_lot);
        Log.v("Error800", "Agregar a facturas_online.txt :\n\n" + imprimir_archivo(file));
        agregar_linea_archivo("facturas_online.txt", linea_agrgar);
        Log.v("Error111", "SpreadSheet ID: " + spid + "\nSheet: " + sheet + "\nTipo lot: " + tip_lot + "\nFile name: " + file);
        //msg("Error111 SpreadSheet ID: " + spid + "\nSheet: " + sheet + "\nTipo lot: " + tip_lot + "\nFile name: " + file);
        //debug
        //imprimir_archivo(file);
        Log.v("Error110", "facturas_online.txt:\n\n" + imprimir_archivo("facturas_online.txt"));
        //msg("Error110\n" + imprimir_archivo(imprimir_archivo(file)));
    }

    private String imprimir_archivo(String nombre_archivo){
        String archivos[] = fileList();
        String contenido = "";//Aqui se lee el contenido del archivo guardado.
        if (ArchivoExiste(archivos, nombre_archivo)) {//Archivo nombre_archivo es el archivo que vamos a imprimir
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(nombre_archivo));//Se abre archivo
                BufferedReader br = new BufferedReader(archivo);


                String linea = br.readLine();//Se lee archivo
                while (linea != null) {
                    contenido = contenido + linea + "\n";
                    linea = br.readLine();
                    //return;
                }
                //Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                //Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                //Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                //Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                br.close();
                archivo.close();
            } catch (IOException e) {
            }
        }
        return contenido;
    }

    private void agregar_numero () {
        String monto1 = monto.getText().toString();//Se parcea el valor a un string
        String numero1 = numero.getText().toString();

        tiquete_venta.put(monto1, numero1); //Hacer esto pero que si hay apuestas a determinado numero, se sume el monto y no se reemplaze

        String archivos[] = fileList();

        if (ArchivoExiste(archivos, "Tiquete" + Loteria + ".txt")) {
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput("Tiquete" + Loteria + ".txt"));
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();
                TiqueteCompleto = "";//Aqui se lee el contenido del tiquete guardado.

                TiqueteCompleto = TiqueteCompleto + numero1 + "      " + monto1 + "\n";
                while (linea != null) {
                    TiqueteCompleto = TiqueteCompleto + linea + "\n";
                    linea = br.readLine();
                }

                br.close();
                archivo.close();
                tiquete.setText(TiqueteCompleto);//Le indicamos a la aplicacion que to-do eso lo coloque en el editText.
                guardar(TiqueteCompleto, "Tiquete" + Loteria + ".txt");
                TiqueteCompleto = "";
            } catch (IOException e) {
            }

        } else {
            //guardar();
        }

        //Limpiar valores ingresados en los textView y en los ficheros
        //monto.setText("");
        numero.setText("");
    }

    public void guardar (String Tcompleto, String nombre){
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre, Activity.MODE_PRIVATE));
            archivo.write(Tcompleto);
            archivo.flush();
        } catch (IOException e) {
        }
    }

    public void imprimir_mensaje(){
        Toast.makeText(this, "Debe ingresar un monto valido!!!", Toast.LENGTH_LONG).show();
    }

    private void agregar_linea_archivo (String file_name, String new_line) {
        String archivos[] = fileList();
        String ArchivoCompleto = "";//Aqui se lee el contenido del archivo guardado.

        if (ArchivoExiste(archivos, file_name)) {
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(file_name));
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();

                while (linea != null) {
                    ArchivoCompleto = ArchivoCompleto + linea + "\n";
                    linea = br.readLine();
                }
                ArchivoCompleto = ArchivoCompleto + new_line + "\n";
                br.close();
                archivo.close();
            } catch (IOException e) {
            }
        } else {
            crear_archivo(file_name);
            agregar_linea_archivo(file_name, new_line);
            return;
        }
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(file_name, Activity.MODE_PRIVATE));
            archivo.write(ArchivoCompleto);
            archivo.flush();
        } catch (IOException e) {
        }
    }

    private void comprobar_archivo(){
        String archivos[] = fileList();

        String fileName = "Tiquete" + Loteria + ".txt";//Espacio de memoria donde se almacena la loteria de manera temporal.


        if (ArchivoExiste(archivos, "Tiquete" + Loteria + ".txt")) {
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput("Tiquete" + Loteria + ".txt"));
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();
                TiqueteCompleto = "";//Aqui se lee el contenido del tiquete guardado.

                while (linea != null) {
                    TiqueteCompleto = TiqueteCompleto + linea + "\n";
                    linea = br.readLine();
                }
                br.close();
                archivo.close();
                tiquete.setText(TiqueteCompleto);//Le indicamos a la aplicacion que to-do eso lo coloque en el editText.
            }catch (IOException e) {
            }
        } else {
            crear_archivo(fileName);
        }
    }
    private void crear_archivo(String nombre_archivo) {
        try{
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre_archivo, Activity.MODE_PRIVATE));
            archivo.flush();
            archivo.close();
        }catch (IOException e) {
        }
    }

    private boolean comprobar_fichero(String fichName) {
        String archivos[] = fileList();
        boolean flag = false;
        if (ArchivoExiste(archivos, fichName)) {
            flag = true;
        } else {
        }
        return flag;
    }

    private boolean ArchivoExiste (String[] archivos, String Tiquet){
        for (int i = 0; i < archivos.length; i++) {

            if (Tiquet.equals(archivos[i])) {
                return true;
            }
        }
        return false;
    }

    private void separar_fechaYhora(String ahora) {
        String[] split = ahora.split(" ");

        int mesecillo = meses.get(split[1]);
        String meseciyillo = "";
        if (mesecillo <= 9) {
            meseciyillo = "0" + String.valueOf(mesecillo);
        } else {
            meseciyillo = String.valueOf(mesecillo);
        }
        fecha_realistic = split[5] + meseciyillo + split[2];
        String anio_helper = split[5];

        if (!flag_cad) {
            mes = String.valueOf(meses.get(split[1]));
            anio = split[5];
            fecha = split[2];
            dia = split[2];
        } else {
            mes = mes_selectedS;
            anio = anio_selectedS;
            fecha = fecha_selectedS;
            dia = fecha_selectedS;
        }
        hora = split[3];
        split = hora.split(":");
        minuto = split[1];
        hora = split[0];

        int mesito = Integer.parseInt(mes);
        String mesitoS = "";
        if (mesito <= 9) {
            mesitoS = "0" + String.valueOf(mesito);
        } else {
            mesitoS = String.valueOf(mesito);
        }
        fecha_poner = anio + mesitoS + dia;

    }

    private int verificar_caducidad(boolean flag, boolean flag_caducidad) throws FileNotFoundException {
        int hora_int = Integer.parseInt(hora);
        int min_int = Integer.parseInt(minuto);
        int comparador_intern = hora_int * 100 + min_int;
        String comparadorHoraString = "";
        switch (Horario) {
            case "Dia":
                comparadorHoraString = Hora_lista_D;
                juega_hora = Hora_juego_D;
                break;
            case "Maniana":
                comparadorHoraString = Hora_lista_M;
                juega_hora = Hora_juego_M;
                break;
            case "Tarde":
                comparadorHoraString = Hora_lista_T;
                juega_hora = Hora_juego_T;
                break;
            case "Noche":
                comparadorHoraString = Hora_lista_N;
                juega_hora = Hora_juego_N;
                break;
            default:
                Toast.makeText(this, "Error!! Hora lista dia: \n" + Hora_lista_D, Toast.LENGTH_SHORT).show();
        }
        int comparadorHora = Integer.parseInt(comparadorHoraString);

        if ((comparador_intern > comparadorHora) & !flag_caducidad) {

            Toast.makeText(this, "Sorteo ha expirado", Toast.LENGTH_LONG).show();
            if (flag){
                View view = null;
                borrar_archivo_lot_actual(view);
            }
            Intent Activity_ventas = new Intent(this, VentasActivity.class);
            Activity_ventas.putExtra("mensaje_toast", "Sorteo " + Loteria + " " + Horario + " ha expirado!!!");
            startActivity(Activity_ventas);
            finish();
            System.exit(0);

        } else {
            //Toast.makeText(this, "Venta permitida!!!", Toast.LENGTH_LONG).show();
        }


        return comparadorHora;
    }

}
