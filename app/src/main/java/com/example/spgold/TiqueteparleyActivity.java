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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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


public class TiqueteparleyActivity extends AppCompatActivity {

    private String mes;
    private String anio;
    private String dia;
    private String hora;
    private String minuto;
    private String fecha;
    private String Paga1;
    private String Paga2;
    private String tipo_lot;
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
    private EditText numero1;
    private EditText numero2;
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
    private String fecha_selectedS;
    private String mes_selectedS;
    private String anio_selectedS;
    private int fecha_selected;
    private int mes_selected;
    private int anio_selected;
    private String dispositivo;
    private TextView FECHA;
    private String flag_cadS;
    private boolean flag_cad;
    private Map<String, Integer> meses = new HashMap<String, Integer>();
    private String exed_monto;
    private int diferencia_exed;
    private String Spread_Sheet_Id;
    private String Spread_Sheet_Id_maniana;
    private String fecha_real;//Puede tomar los siguientes valores: "HOY" o "FUTURO"
    private String SHEET = "";
    private String SPREADSHEET_ID;
    private String addRowURL = "https://script.google.com/macros/s/AKfycbweyYb-DHVgyEdCWpKoTmvOxDGXleawjAN8Uw9AeJYbZ24t9arB/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tiqueteparley);

        monto = (EditText)findViewById(R.id.et_monto_par);
        numero1 = (EditText)findViewById(R.id.et_numero_par);
        numero2 = (EditText)findViewById(R.id.et_numero_par2);
        tiquete = (TextView)findViewById(R.id.tv_tiquete_par);
        cliente = (EditText)findViewById(R.id.et_cliente_par);
        gen_tiquete = (TextView)findViewById(R.id.tv_gentiquete_par);
        FECHA = (TextView)findViewById(R.id.textView_fecha_par);

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
        Numero_maquina = getIntent().getStringExtra("Numero_maquina");
        Comision_vendedor = getIntent().getStringExtra("Comision_vendedor");
        Loteria = getIntent().getStringExtra("Loteria");
        Horario = getIntent().getStringExtra("Horario");
        tipo_lot = getIntent().getStringExtra("tipo_lot");
        flag_cadS = getIntent().getStringExtra( "caduce");
        Spread_Sheet_Id = getIntent().getStringExtra("Spread_Sheet_Id");
        Spread_Sheet_Id_maniana = getIntent().getStringExtra("Spread_Sheet_Id_maniana");
        flag_cad = Boolean.parseBoolean(flag_cadS);

        fecha_selectedS = getIntent().getStringExtra("fecha_selected");
        mes_selectedS = getIntent().getStringExtra("mes_selected");
        anio_selectedS = getIntent().getStringExtra("anio_selected");

        if (Integer.parseInt(fecha_selectedS) == 0) {
            if (Integer.parseInt(mes_selectedS) == 0) {
                if (Integer.parseInt(anio_selectedS) == 0) {
                    FECHA.setText("HOY");
                    fecha_real = "HOY";
                    SPREADSHEET_ID = Spread_Sheet_Id;
                }
            }
        } else {
            FECHA.setText(fecha_selectedS + "/" + mes_selectedS + "/" + anio_selectedS);
            fecha_real = "FUTURO";
            SPREADSHEET_ID = Spread_Sheet_Id_maniana;
        }

        fecha_selected = Integer.parseInt(fecha_selectedS);
        mes_selected = Integer.parseInt(mes_selectedS);
        anio_selected = Integer.parseInt(anio_selectedS);


        llenar_mapa_meses();

        gen_tiquete.setText(Loteria + " " + Horario);

        dispositivo = check_device();

        exed_monto = "";
        diferencia_exed = 0;


        tiquete.setFocusableInTouchMode(false);//Hace que no se pueda escribir en el espacio donde aparecen los numeros que uno va agregando. Es como un carrito de compras.
        Date now = Calendar.getInstance().getTime();
        String ahora = now.toString();
        //se separan los campos de la fecha y hora para verificar que si se pueda realizar la venta.
        separar_fechaYhora(ahora);

        try {
            hora_juega = verificar_caducidad(true, flag_cad);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Se verifica si el archivo contable del dia existe.
        String fichName = Loteria + "_" + Horario + "_" + fecha + "_" + mes + ".txt";
        if (comprobar_fichero(fichName)) {
            //Se comprueba si existe tiquete sin completar

        } else {
            //Se crea el archivo contable del dia.

            int cero = 0;
            String cero_s = Integer.toString(cero);
            agregar_linea_archivo(fichName, "00" + "      " + "00" + "      " + "0");
            //archivo contable creado!!!
        }
        comprobar_archivo();

        try {
            subir_facturas_resagadas();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        numero2.setFocusableInTouchMode(false);

        //Implementacion de un text listener
        numero1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) {
                    if (monto.getText().toString().isEmpty()) {
                        imprimir_mensaje();
                        numero1.setText("");
                        numero2.setText("");
                        return;
                    } else {
                        String monto1 = monto.getText().toString();//Se parcea el valor a un string
                        //if (monto1.isEmpty()) {
                        //imprimir_mensaje();
                        //numero.setText("");
                        //return;
                        //}
                        int montoo = Integer.parseInt(monto1);
                        if (montoo <= 49) {
                            imprimir_mensaje();
                            numero1.setText("");
                            numero2.setText("");
                            return;
                            //numero1.setText("");
                        } else if (montoo > Integer.parseInt(Limite_maximo)) {
                            imprimir_mensaje33();
                            numero1.setText("");
                            numero2.setText("");
                            return;
                            //numero1.setText("");
                        } else {
                            //agregar_numero();
                            numero2.setFocusableInTouchMode(true);
                            numero2.setText("");
                            numero2.requestFocus();
                            //numero1.setFocusableInTouchMode(false);
                            //ocultar_teclado();
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        numero2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) {
                    String monto1 = monto.getText().toString();//Se parcea el valor a un string
                    if (monto1.isEmpty()) {
                        imprimir_mensaje();
                        numero1.setText("");
                        numero2.setText("");
                        return;
                    } else if (numero1.getText().toString().length() < 2) {
                        imprimir_mensaje3();
                        //numero1.setText("");
                        numero2.setText("");
                        numero1.requestFocus();
                        return;
                    }
                    int montoo = Integer.parseInt(monto1);
                    if (montoo <= 49){
                        imprimir_mensaje();
                        numero1.setText("");
                        numero2.setText("");
                        return;
                    } else if (montoo > Integer.parseInt(Limite_maximo)) {
                        imprimir_mensaje33();
                        //numero1.setText("");
                        //numero2.setText("");
                        return;
                    } else if (numero1.getText().toString().length() < 2){
                        imprimir_mensaje3();
                        //numero1.setText("");
                        //numero2.setText("");
                        return;
                    } else if (numero2.getText().toString().length() < 2) {
                        imprimir_mensaje3();
                        numero2.setFocusableInTouchMode(false);
                        numero1.requestFocus();
                        return;
                    } else if (Integer.parseInt(numero1.getText().toString()) > 99) {
                        imprimir_mensaje3();
                        numero1.setText("");
                        //numero2.setText("");
                        return;
                    } else if (Integer.parseInt(numero2.getText().toString()) > 99) {
                        imprimir_mensaje3();
                        //numero1.setText("");
                        numero2.setText("");
                        return;
                    } else if (Integer.parseInt(monto.getText().toString()) < 0) {
                        imprimir_mensaje();//
                        //numero1.setText("");
                        //numero2.setText("");
                        return;
                    } else {
                        agregar_numero();
                        numero1.requestFocus();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //imprimir_archivo(fichName);

    }

    private void msg(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private JSONObject generar_Json_resagadas(String file, String factura, String SSHHEETT, String SPREEADSHEET_ID) {
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
            jsonObject = TranslateUtil.string_to_Json(json_string, SPREEADSHEET_ID, SSHHEETT, factura);
        } catch (IOException | JSONException e) {
        }
        return jsonObject;
    }

    private JSONObject obtener_Json_otras_facturas() {

        JSONObject objeto_json = null;

        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("facturas_online.txt"));
            //imprimir_archivo("facturas_online.txt");
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            //String contenido = "";
            while (linea != null) {
                String[] split = linea.split(" ");
                if (split[0].equals("abajo")) {
                    //String[] split_hor = split[1].split("_separador_");
                    String[] split_name = split[1].split("_separador_");
                    String factura = split_name[6];// split_name[6] contiene el numero de la factura que se desea subir.

                    String SSHHEETT = split[3];
                    String SSPPRREEAADDSSHHEETT = split[2];
                    objeto_json = generar_Json_resagadas(split[1], factura, SSHHEETT, SSPPRREEAADDSSHHEETT);
                    //objeto_json = generar_Json_resagadas(split[1], factura, SSHHEETT);
                    subir_factura_resagadas(objeto_json);
                } else {
                    //Do nothing. No deberia llegar aqui.
                    //Toast.makeText(this, "Debug:\nNo deberia llegar aqui!!!", Toast.LENGTH_LONG).show();
                }
                linea = br.readLine();
            }

            br.close();
            archivo.close();
        } catch (IOException | JSONException e) {
        }
        return objeto_json;
    }

    private void subir_factura_resagadas(JSONObject jsonObject) throws JSONException {
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
        /*
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
         */

        //Toast.makeText(this, "Debug:\nConsecutivo: " + Consecutivo + "\nconsecutivo: " + consecutivo + "\nDeben ser iguales.", Toast.LENGTH_LONG).show();

        String url = addRowURL;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String[] split = response.toString().split("\"");
                        //msg("Json: 123 " + response.toString());
                        int length_split = split.length;
                        if (length_split > 3) {
                            if (split[2].equals(":")) {
                                //mensaje_confirma_subida(response.toString());
                                String factura_num = split[15];
                                //mensaje_confirma_subida("factura #" + factura_num + " se ha subido correctamente!");
                                cambiar_bandera (factura_num);
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

    private void subir_facturas_resagadas() throws JSONException {
        boolean flag_internet = verificar_internet();
        JSONObject objeto_Json_a_subir = null;
        if (flag_internet) {
            objeto_Json_a_subir = obtener_Json_otras_facturas();
            //msg("Json object:\n\n" + objeto_Json_a_subir.toString());
            //subir_factura_resagadas(objeto_Json_a_subir);
            //msg("Json object:\n\n" + objeto_Json_a_subir.toString());
        } else {
            //Toast.makeText(this, "Error al subir factura #" + consecutivo_str + ". \nVerifique su coneccion a Internet!!!", Toast.LENGTH_LONG).show();
        }
    }

    private void ocultar_teclado(){
        View view = this.getCurrentFocus();
        InputMethodManager imn = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imn.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
                Toast.makeText(getBaseContext(), "Bluetooth abierto!", Toast.LENGTH_SHORT).show();
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

    private int contar(String monto_act, String numero1_act, String numero2_act) {
        int exeso = 0;
        boolean flag_cont = true;//Bandera que ayuda a actualizar el archivo contable sin errores.

        int valor = Integer.parseInt(monto_act);//Se parcea el monto del numero jugado.
        int temp_val1 = Integer.parseInt(numero1_act);
        int temp_val2 = Integer.parseInt(numero2_act);
        int num1 = -1;
        int num2 = -1;
        if (temp_val1 > temp_val2) {// Se ordenan los numeros de menor a mayor
            num1 = temp_val2;
            num2 = temp_val1;
        } else if (temp_val1 < temp_val2) {
            num2 = temp_val2;
            num1 = temp_val1;
        } else if (temp_val2 == temp_val1) {
            num1 = temp_val1;
            num2 = temp_val2;
        } else {
            //Error!!!
            Toast.makeText(this, "Error en datos de venta!!!", Toast.LENGTH_LONG).show();
            //return;
        }
        //printIt();
        //imprimir_archivo(Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt");
        String archivos[] = fileList();
        if (ArchivoExiste(archivos, Loteria + "_" + Horario + "_" + fecha + "_" + mes + ".txt")) {//nombre del archivo CONTABle del dia
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(Loteria + "_" + Horario + "_" + fecha + "_" + mes + ".txt"));//Se abre archivo contable
                BufferedReader br = new BufferedReader(archivo);
                String TiqueteContable = "";//Aqui se lee el contenido del tiquete guardado.

                String linea = br.readLine();//Se lee archivo contable
                while (linea != null) {
                    String[] split = linea.split("      ");//Se separa el monto de los numeros guardados.
                    //Toast.makeText(this, "Debugueo (linea): " + linea + "\n\nSplit 1: " + split[0] + "\nSplit 2: " + split[1] + "\nSplit 3: " + split[2] + "\n\n", Toast.LENGTH_LONG).show();
                    if (Integer.parseInt(split[0]) == num1){
                        //Toast.makeText(this, "Debugueo primera coinsidencia (linea): \n\n" + linea, Toast.LENGTH_LONG).show();
                        if (Integer.parseInt(split[1]) == num2) {
                            //Toast.makeText(this, "Debugueo segunda coinsidencia (linea): \n\n" + linea, Toast.LENGTH_LONG).show();
                            flag_cont = false;
                            int monto_numero = Integer.parseInt(split[2]);

                            ///////////SE VERIFICA QUE NO EXEDA EL LIMITE PERMITIDO DE VENTAS//////////////////////////////////////


                            int amonu = monto_numero + valor;
                            if (amonu > Integer.parseInt(Limite_maximo)) {
                                exeso = amonu - Integer.parseInt(Limite_maximo);
                                monto_numero = amonu - exeso;
                                diferencia_exed = diferencia_exed + exeso;
                                exed_monto = exed_monto + "Maximo permitido para\nla parejita " + numero1_act + " " + numero2_act+  "\nse ha exedido!\nSe devuelven " + exeso + " colones. \n";
                                Toast.makeText(this, "Monto exede el maximo permi-\ntido para la parejita " + numero1_act + " " + numero2_act + "\nSe devuelven " + exeso + " colones. ", Toast.LENGTH_LONG).show();
                            } else {
                                monto_numero = monto_numero + valor;
                            }

                            //////////////////////////////////////////////////////////////////////////////////////////////////////

                            linea = String.valueOf(num1) + "      " + String.valueOf(num2) + "      " +  String.valueOf(monto_numero);
                            TiqueteContable = TiqueteContable + linea + "\n";
                        } else {
                            TiqueteContable = TiqueteContable + linea + "\n";
                        }
                    } else {
                        TiqueteContable = TiqueteContable + linea + "\n";
                    }
                    linea = br.readLine();
                }
                br.close();
                archivo.close();
                guardar(TiqueteContable, Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt");
            } catch (IOException e) {
            }
        }

        if (ArchivoExiste(archivos, Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt")) {//nombre del archivo CONTABle del dia
            if (flag_cont) {
                try {
                    InputStreamReader archivo = new InputStreamReader(openFileInput(Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt"));//Se abre archivo contable
                    BufferedReader br = new BufferedReader(archivo);
                    String TiqueteContable = "";//Aqui se lee el contenido del tiquete guardado.

                    String linea = br.readLine();//Se lee archivo contable
                    while (linea != null) {
                        //flag_cont = true;
                        //Toast.makeText(this, "Debugueo guardar linea sin coinsidencias (linea): \n\n" + linea, Toast.LENGTH_LONG).show();
                        TiqueteContable = TiqueteContable + linea + "\n";
                        linea = br.readLine();
                    }
                    br.close();
                    archivo.close();
                    linea = String.valueOf(num1) + "      " + String.valueOf(num2) + "      " +  monto_act;
                    TiqueteContable = TiqueteContable + linea + "\n";
                    guardar(TiqueteContable, Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt");
                } catch (IOException e) {
                }
            }
        }
        return exeso;
    }

    public void generar_pedido (View view){
        String jugador = cliente.getText().toString();

        /*if (jugador.isEmpty()){
            Toast.makeText(this, "Ingrese el nombre del cliente!!!", Toast.LENGTH_LONG).show();
            cliente.requestFocus();
            return;
        }*/

        String archivos[] = fileList();

        if (ArchivoExiste(archivos, "Tiquete" + Loteria + ".txt")) {//Archivo Tiquete_Nombre_loteria.txt es el archivo temporal que almacena cada venta.
            //imprimir_archivo("Tiquete" + Loteria + ".txt");
            //Toast.makeText(this, "Ahora el archivo contable: \n", Toast.LENGTH_LONG).show();
            //imprimir_archivo(Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt");

            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput("Tiquete" + Loteria + ".txt"));
                BufferedReader br = new BufferedReader(archivo);
                TiqueteCompleto = "";//Aqui se lee el contenido del tiquete guardado.

                String linea = br.readLine();

                while (linea != null) {
                    String[] split = linea.split("      ");//Se separa el monto del numero jugado.
                    int diff = contar(split[2], split[0], split[1]);//Aqui se llama a la funcion que agrega la venta al archivo contable.
                    int valor = Integer.parseInt(split[2]);
                    if (diff > 0) {
                        monto_venta = monto_venta + valor - diff;
                        linea = split[0] + "      " + split[1] + "      " + String.valueOf(Integer.parseInt(split[2]) - diff);
                    } else {
                        monto_venta = monto_venta + valor;
                    }
                    TiqueteCompleto = TiqueteCompleto + linea + "\n";
                    ///////////////////MORE ONLINE OPTIONS/////////////////////////

                    //Se crea fichero identico al fichero online

                    //agregar_linea_archivo(Numero_maquina + "_" + Loteria + "_" + Horario + "_" + fecha + "_" + mes + "_" + anio + "_.txt", linea);

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

                //String SHEET = Numero_maquina + Loteria + Horario;
                //String SPREADSHEET_ID = Spread_Sheet_Id;

                //JSONObject tiquete_Json = TranslateUtil.file_to_Json(TiqueteCompleto, SPREADSHEET_ID, SHEET);

                //////////////////////////////////////////////////////////////
                Toast.makeText(this, "Total: " + Integer.toString(monto_venta), Toast.LENGTH_SHORT).show();
                TiqueteCompleto = "";
                tiquete.setText(TiqueteCompleto);
                generar_tiquete_venta();
            }catch (IOException | JSONException e) {
            }
        } else {
            Toast.makeText(this, "Error!!!\nDebe ingresar datos", Toast.LENGTH_LONG).show();
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
        }
        //int ram_value = (int)(Math.random()*10+1);
        //String ram_value_str = String.valueOf(ram_value);

        //##########################################################################################

        String consecutivo_str = "0";
        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("invoice.txt"));
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            String linea_consecutivo = "";
            String[] split = linea.split(" ");
            consecutivo = Integer.parseInt(split[1]);
            consecutivo = consecutivo + 1;//Se incrementa el consecutivo de facturacion.
            consecutivo_str = String.valueOf(consecutivo);
            consecutivo_str = consecutivo_str.replace("\n","");
            String arreglo_cambio_linea = split[0];
            arreglo_cambio_linea = arreglo_cambio_linea.replace("\n","");
            linea = arreglo_cambio_linea + " " + consecutivo_str;
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

            if (flag_cont == 0) { //TODO: Evitar que el archivo de facturas cresca demedido y llene la memoria.
                //guardar(split[0] + " " + split[1],"invoice.txt");//Se elimina el cambio de linea si no se ha generado ni una sola factura. (Parece innecesario)
            }

            br.close();
            archivo.close();
        } catch (IOException e) {
        }

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

        String tempFile = jugador_act + "_separador_" + Loteria + "_separador_" + Horario + "_separador_" + fecha + "_separador_" + hora + "_separador_" + minuto + "_separador_" + consecutivo_str + "_separador_" + dia + "_separador_" + mes + "_separador_" + tipo_lot + "_separador_" + Paga1 + "_separador_" + Paga2 + "_separador_" + monto_venta + "_separador_" + anio + "_separador_null.txt";

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
        contenido = "";//Aqui se escribe el contenido del archivo guardado.
        linea_temp = "Factura # " + consecutivo_str + "\n";
        contenido = contenido + linea_temp;
        linea_temp = "\n       --->***********<---\n";
        contenido = contenido + linea_temp + "\n";
        linea_temp = "     ** Tiempos " + Nombre_puesto + " **  \n";
        contenido = contenido + linea_temp + "\n";
        linea_temp = "       Pagamos " + Paga1 + " veces!!!\n";
        contenido = contenido + linea_temp + "\n";
        linea_temp = "       Somos la banca mas \n        solida del pais.\n";
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
        contenido = contenido + linea_temp + "\n\n#############################";
        String archivos[] = fileList();
        if (ArchivoExiste(archivos, "Tiquete" + Loteria + ".txt")) {//Archivo nombre_archivo es el archivo que vamos a copiar
            try {
                InputStreamReader archivo24 = new InputStreamReader(openFileInput("Tiquete" + Loteria + ".txt"));//Se abre archivo
                BufferedReader br24 = new BufferedReader(archivo24);
                String linea = br24.readLine();//Se lee archivo

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                int counter = 0;
                String linea_tempo = "";
                while (linea != null) {
                    //String linea_adapt = linea.replace("      ", " ");//Adaptamos el archivo para subirlo a la nube.
                    //agregar_linea_archivo(factura_a_subir, linea_adapt);
                    String[] split = linea.split("      ");
                    String separacion_str = " ";
                    int separacion = 7 - split[2].length();
                    for (int i = 0; i < separacion; i++) {
                        separacion_str = separacion_str + " ";
                    }
                    counter++;
                    if (counter == 1) {
                        linea_tempo = split[0] + " " + split[1] + " " + separacion_str + split[2] + "|";
                    } else if (counter == 2) {
                        linea_tempo = linea_tempo + split[0] + " " + split[1] + " " + separacion_str + split[2];
                        counter = 0;
                    }

                    if (counter == 0) {
                        //linea = linea_tempo;
                        contenido = contenido + "\n" + linea_tempo;
                    }
                    agregar_linea_archivo(tempFile, linea);
                    linea = br24.readLine();
                }
                if (counter == 1) {
                    contenido = contenido + "\n" + linea_tempo;
                }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                String jugador_print = jugador.replace("x_x"," ");
                if (exed_monto.isEmpty()) {
                    linea_temp = "\n#############################\n\n Total:  " + monto_venta + " colones. \n\n#############################\n\nEstimado/a " + jugador_print + ", no\nolvide revisar su tiquete\nantes de retirarse del puesto.\n";
                } else {
                    linea_temp = "\n#############################\n\n" + exed_monto + "\n#############################\nTotal:  " + monto_venta + " colones. \n\n#############################\n\nEstimado/a " + jugador_print + ", no\nolvide revisar su tiquete\nantes de retirarse del puesto.\n";
                }
                contenido = contenido + linea_temp + "\n\n\n";
                monto_venta = 0;
                br24.close();
                archivo24.close();
            }catch (IOException e) {
            }
        }



        //verificar_tiquetes_online();
        boolean flag_internet = verificar_internet();
        JSONObject objeto_Json_a_subir = null;
        if (flag_internet) {
            objeto_Json_a_subir = obtener_Json(consecutivo_str);
        } else {
            //Toast.makeText(this, "Error al subir factura #" + consecutivo_str + ". \nVerifique su coneccion a Internet!!!", Toast.LENGTH_LONG).show();
        }

        if (!flag_internet) {
            //Toast.makeText(this, "-----*****************-----\n        NOTA DE SEGURIDAD:\n-----*****************-----\n\nError al cifrar archivo.\nArchivo no se sube!", Toast.LENGTH_LONG).show();
        } else {
            subir_factura(objeto_Json_a_subir, consecutivo_str);
        }



        impmir_tiquete(contenido);

        //**************************************************************************************************************

        //Al final, se elimina el archivo temporal de la venta:
        View view3 = null;
        borrar_archivo_lot_actual(view3);
    }

    private JSONObject obtener_Json(String Consecutivo) {

        //En esta funcion se verifica que se hallan subido todos los tiquetes. En caso contrario, se intentan subir.
        //boolean flag_subir = false;

        JSONObject objeto_json = null;

        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("facturas_online.txt"));
            //imprimir_archivo("facturas_online.txt");
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            //String contenido = "";
            while (linea != null) {
                String[] split = linea.split(" ");
                if (split[0].equals("abajo")) {
                    String[] split_name = split[1].split("_separador_");
                    String factura = split_name[6];// split_name[6] contiene el numero de la factura que se desea subir.
                    if (factura.equals(Consecutivo)) {
                        objeto_json = generar_Json(split[1], factura);
                    } else {
                        //Do nothing.
                    }

                } else {
                    //Do nothing. No deberia llegar aqui.
                    Toast.makeText(this, "Debug:\nNo deberia llegar aqui!!!", Toast.LENGTH_LONG).show();
                }
                linea = br.readLine();
            }

            br.close();
            archivo.close();
        } catch (IOException e) {
        }
        return objeto_json;
    }

    private JSONObject generar_Json(String file, String factura){
        //boolean flag_subir = false;
        JSONObject jsonObject = new JSONObject();

        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput(file));
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            String json_string = "";
            while (linea != null) {
                String[] split = linea.split("      ");
                //                            #1                 #2                 monto          ext. info        factura
                json_string = json_string + split[0] + "_n_" + split[1] + "_n_" + split[2] + "_n_" + "no" + "_n_" + factura + "_l_";
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

    private void mensaje_error_en_subida() {
        //Toast.makeText(this, "Error subiendo la factura a la base de datos!!!", Toast.LENGTH_LONG).show();
    }

    private void cambiar_bandera (String Consecutivo) {
        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("facturas_online.txt"));
            //imprimir_archivo("facturas_online.txt");
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            String contenido = "";
            while (linea != null) {
                //Toast.makeText(this, "Debug:\nFuncion cambiar_bandera, linea:\n" + linea, Toast.LENGTH_LONG).show();
                String[] split = linea.split(" ");
                if (split[0].equals("abajo")) {
                    String[] split_name = split[1].split("_separador_");
                    String factura = split_name[6];// split_name[6] contiene el numero de la factura que se desea subir.
                    if (factura.equals(Consecutivo)) {
                        //Do nothing. Aqui se elimina la linea que contiene la factura que se acaba de subir.
                    } else {
                        contenido = contenido + linea;
                    }

                } else {
                    //Do nothing. No deberia llegar aqui.
                    Toast.makeText(this, "ERROR!!!:\nNo deberia llegar aqui!!!", Toast.LENGTH_LONG).show();
                    Toast.makeText(this, "ERROR!!!:\nNo deberia llegar aqui!!!", Toast.LENGTH_LONG).show();
                    Toast.makeText(this, "ERROR!!!:\nNo deberia llegar aqui!!!", Toast.LENGTH_LONG).show();
                    //contenido = contenido + linea;
                }
                linea = br.readLine();
            }

            br.close();
            archivo.close();
            borrar_archivo("facturas_online.txt");
            //imprimir_archivo("facturas_online.txt");
            guardar(contenido, "facturas_online.txt");//Aqui se eliminan las lineas que corresponden a archivos que ya se han subido.
            //imprimir_archivo("facturas_online.txt");

        } catch (IOException e) {
        }
    }

    private void subir_factura(JSONObject jsonObject, String Consecutivo) throws JSONException {
        //flag_file_arriba = false;

        RequestQueue queue;
        queue = Volley.newRequestQueue(this);


        //Llamada POST usado Volley:
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
                        mensaje_confirma_subida("factura " + Consecutivo + " se ha subido correctamente!");
                        cambiar_bandera (Consecutivo);
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

    }

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
            numero1.setText("");
            numero2.setText("");
            //Intent Activity_ventas = new Intent(this, VentasActivity.class);
            //startActivity(Activity_ventas);
            //finish();
            //System.exit(0);
        }else {
            tiquete.setText("");//Le indicamos a la aplicacion que to-do eso lo coloque en el editText.
            cliente.setText("");
            monto.setText("");
            numero1.setText("");
            numero2.setText("");
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

    private void imprimiendoAnim() {
        //Toast.makeText(getApplicationContext(), "imprimiendo...",	Toast.LENGTH_SHORT).show();


    }

    private void imprimir_archivo(String nombre_archivo){
        String archivos[] = fileList();
        if (ArchivoExiste(archivos, nombre_archivo)) {//Archivo nombre_archivo es el archivo que vamos a imprimir
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(nombre_archivo));//Se abre archivo
                BufferedReader br = new BufferedReader(archivo);
                String contenido = "";//Aqui se lee el contenido del archivo guardado.

                String linea = br.readLine();//Se lee archivo
                while (linea != null) {
                    contenido = contenido + linea + "\n";
                    linea = br.readLine();
                    //return;
                }
                //printIt(contenido);
                Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                br.close();
                archivo.close();
            }catch (IOException e) {
            }
        }
    }

    private void agregar_numero () {
        String monto1 = monto.getText().toString();//Se parcea el valor a un string
        String primer_numero = numero1.getText().toString();
        String segundo_numero = numero2.getText().toString();

        tiquete_venta.put(monto1, primer_numero + "_" + segundo_numero); //TODO: Hacer esto pero que si hay apuestas a determinado numero, se sume el monto y no se reemplaze
                                                                            //TODO: Hacer el algoritmo.
        String archivos[] = fileList();

        if (ArchivoExiste(archivos, "Tiquete" + Loteria + ".txt")) {
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput("Tiquete" + Loteria + ".txt"));
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();
                TiqueteCompleto = "";//Aqui se lee el contenido del tiquete guardado.

                TiqueteCompleto = TiqueteCompleto + primer_numero + "      " + segundo_numero + "      " + monto1 + "\n";
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

        } else {//Do nothing.
            //guardar();
        }

        //Limpiar valores ingresados en los textView y en los ficheros
        //monto.setText("");
        numero1.setText("");
        numero2.setText("");
    }

    public void guardar (String Tcompleto, String nombre){
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre, Activity.MODE_PRIVATE));
            archivo.write(Tcompleto);
            archivo.flush();

        } catch (IOException e) {
        }
    }

    public void imprimir_mensaje3(){
        Toast.makeText(this, "Debe ingresar un numero valido!!!!!!", Toast.LENGTH_LONG).show();
    }

    public void imprimir_mensaje33(){
        Toast.makeText(this, "Monto sobrepasa el limite maximo permitido!!!", Toast.LENGTH_LONG).show();
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

        String fileName = "Tiquete" + Loteria + ".txt";


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
