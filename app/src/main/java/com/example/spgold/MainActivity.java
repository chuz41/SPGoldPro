package com.example.spgold;
/*
import static com.example.spgold.Util.FuncionesComunesUtil.imprimir_archivo;
import static com.example.spgold.Util.FuncionesComunesUtil.check_device;
import static com.example.spgold.Util.FuncionesComunesUtil.agregar_linea_archivo;
import static com.example.spgold.Util.FuncionesComunesUtil.crear_archivo;
import static com.example.spgold.Util.FuncionesComunesUtil.archivo_existe;
import static com.example.spgold.Util.FuncionesComunesUtil.borrar_archivo;
import static com.example.spgold.Util.FuncionesComunesUtil.get_impresora;
import static com.example.spgold.Util.FuncionesComunesUtil.guardar;
*/
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
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
import com.example.spgold.Util.TranslateUtil;
//import com.example.spgold.Util.FuncionesComunesUtil;
import com.example.spgold.Util.FuncionesInternetUtil;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private TextView inicio;
    private String dia;
    private String mes;
    private String anio;
    private String ahora;
    private String fecha;
    private String hora;
    private String minuto;
    private String nombre_puesto;
    private TextView tv_active;
    private EditText et_ID;
    private TextView textView_esperar;
    private Button button_ventas;
    private Button button_config;
    private Button button_reportes;
    private Button boton_admin;
    private String dispositivo;
    private EditText passET;
    private HashMap<String, String> abajos2 = new HashMap<String, String>();
    private String addRowURL = "https://script.google.com/macros/s/AKfycbweyYb-DHVgyEdCWpKoTmvOxDGXleawjAN8Uw9AeJYbZ24t9arB/exec";
    private String readRowURL_activ = "https://script.google.com/macros/s/AKfycbxJNCrEPYSw8CceTwPliCscUtggtQ2l_otieFmE/exec?spreadsheetId=";
    private String sid_activ = "1nK20H2wMoLMqD8XRaNpgxz-rw476XctiWemj6dyuOlo";
    private String sid_vendidas;
    private String sid_loterias;
    private String s_activ = "maquinas";
    private String maqui;
    private Map<String, Integer> meses = new HashMap<String, Integer>();
    private String facturas_diarias = "facturas_diarias.txt";
    private String historial_facturas = "historial_facturas.txt";
    private String contabilidad = "contabilidad.txt";
    private String nombre_dia;
    private int REQUEST_CODE = 200;

    @Override
    protected void onPause() {
        super.onPause();

        try {
            subir_facturas_resagadas();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            subir_facturas_resagadas();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        inicio = (TextView) findViewById(R.id.tv_inicio);
        passET = (EditText) findViewById(R.id.et_password);
        button_ventas = (Button) findViewById(R.id.button_ventas);
        button_config = (Button) findViewById(R.id.button_config);
        button_reportes = (Button) findViewById(R.id.button_reportes);
        boton_admin = (Button) findViewById(R.id.boton_admin);
        textView_esperar = (TextView) findViewById(R.id.textView_esperar);
        et_ID = (EditText) findViewById(R.id.et_ID);
        tv_active = (TextView) findViewById(R.id.tv_active);


        passET.setFocusableInTouchMode(false);

        verificarPermisos();
        verificarPermisos2();
        //check_activation();
        ocultar_todito("OnCreate");
        Date now = Calendar.getInstance().getTime();
        ahora = now.toString();
        separar_fechaYhora();



        try {
            crear_archivos_config();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dispositivo = check_device();

        String archivos[] = fileList();
        boolean crear_lot = true;
        for (int i = 0; i < archivos.length; i++) {
            Pattern pattern = Pattern.compile("loteria_sfile", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound) {
                //abrir archivo y leerlo crear_loteria_de crear_loteria_demo mo
                Log.v("ErrorCrearLoterias", "Archivo encontrado: " + archivos[i]);
                crear_lot = false;
                break;
            }
        }
        if (crear_lot) {
            crear_archivo_activation();
        } else {
            try {
                ocultar_todito("Oncreate2");
                InputStreamReader archivo = new InputStreamReader(openFileInput("vent_active.txt"));
                //imprimir_archivo("facturas_online.txt");
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();

                Log.v("chequear no internet", ".\nLinea: " + linea + "\n\n");
                //Toast.makeText(this, "Debug:\nFuncion cambiar_bandera, linea:\n" + linea, Toast.LENGTH_LONG).show();
                String[] split = linea.split("_separador_");
                maqui = split[1];
                sid_loterias = split[2];
                sid_vendidas = split[3];
                linea = br.readLine();
                //ocultar_todito();
                br.close();
                archivo.close();
                check_activation();


            } catch (IOException e) {
            }
            mostrar_active_vend();
        }

        boolean crear_invoice_file = true;
        for (int i = 0; i < archivos.length; i++) {
            Pattern pattern = Pattern.compile("invoice", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound) {
                crear_invoice_file = false;
                break;
            }
        }
        if (crear_invoice_file) {
            try {
                generar_invoice_file();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        Log.v("Error_main01", "Archivo:\n\n" + imprimir_archivo("facturas_online.txt"));

       /* try {
            subir_facturas_resagadas();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/


        //Implementacion de un text listener
        passET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 4) {
                    ocultar_teclado();
                    //mensaje_config();
                    View view = null;
                    config(view);
                    passET.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void verificarPermisos () {
        int permisosfiles = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permisosfiles == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso consedido", Toast.LENGTH_LONG).show();
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void verificarPermisos2 () {
        int permisionfiles = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permisionfiles == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso consedido", Toast.LENGTH_LONG).show();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE);
        }
    }

    private void separar_fechaYhora(){
        String[] split = ahora.split(" ");
        nombre_dia = split[0];
        dia = split[2];
        mes = String.valueOf(meses.get(split[1]));
        anio = split[5];
        String hora_completa = split[3];
        fecha = split[2];
        split = hora_completa.split(":");
        minuto = split[1];
        hora = split[0];
    }

    private void crear_archivo_activation() {

        String archivos[] = fileList();
        Log.v("Crear file active ", "Se crea el archivo vent_active.txt");
        boolean crear_file_activ = true;
        for (int i = 0; i < archivos.length; i++){
            Pattern pattern = Pattern.compile("vent_active", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound){
                Log.v("Crear file active ", "Archivo vent_active.txt se ha encontrado");
                crear_file_activ = false;
                break;
            }
        }
        if (crear_file_activ){
            Log.v("crear archivo activ", "Se creara el archivo \"vent_active.txt\" (mentira)");
            text_listener();
        } else {
            check_activation();
        }

    }

    private void generar_active_file() throws IOException {
        crear_archivo("vent_active.txt");
        //crear_archivo("vent_active.txt");
        agregar_linea_archivo("vent_active.txt","FALSE_separador_" + maqui + "_separador_" + sid_loterias + "_separador_" + sid_vendidas + "_separador_0_separador_00:11:22:33:44:55");
        Log.v("file active ", ".\nContenido del archivo vent_active.txt: " + imprimir_archivo("vent_active.txt"));
    }

    private void text_listener() {


        et_ID.setText("");
        ocultar_active_vend();
        et_ID.setFocusableInTouchMode(true);
        et_ID.requestFocus();

        //Implementacion de un text listener
        et_ID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String codigo = et_ID.getText().toString();//Se parcea el valor a un string
                    if (codigo.length() == 11) {
                        boolean aceptado = verificar_codigo(codigo);
                        if (aceptado) {
                            vendedor_real(codigo);
                            //gen_personalized_vars();
                        } else {
                            tv_active.setText("Debe ingresar un codigo valido!");
                            msg("Debe ingresar un codigo valido!");
                            et_ID.setText("");
                            et_ID.setFocusableInTouchMode(true);
                            et_ID.requestFocus();
                            text_listener();
                        }
                    } else {
                        //
                    }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //check_activation();
    }

    private void vendedor_real(String codigo) {//Aqui agarran valor maqui, sid_loterias,
        et_ID.setFocusableInTouchMode(false);
        ocultar_teclado();
        tv_active.setText("Buscando vendedor en la base de datos...\nPor favor espere...");
        if (verificar_internet()) {
            RequestQueue requestQueue;

            // Instantiate the cache
            Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

            // Set up the network to use HttpURLConnection as the HTTP client.
            Network network = new BasicNetwork(new HurlStack());

            // Instantiate the RequestQueue with the cache and network.
            requestQueue = new RequestQueue(cache, network);

            // Start the queue
            requestQueue.start();

            String url = readRowURL_activ + sid_activ + "&sheet=" + s_activ;

            Log.v("Crear file active URL ", ".\nurl: " + url + "\n.");

            // Formulate the request and handle the response.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onResponse(String response) {
                            // Do something with the response
                            Log.v("vendedor real 0", ".\nResponse:\n" + response);
                            String[] split_test = response.split("\"");
                            Log.v("vendedor real 1", ".\nSplit_test:\n" + split_test[0] + ", " + split_test[1] + ", " + split_test[2] + ", " + split_test[3] + "\n");
                            if (response != null & split_test.length > 3) {
                                String[] split = response.split("maquina");//Se separa el objeto Json
                                Log.v("vendedor real 2", ".\nSplit:\n" + split[0] + ", " + split[1] + ", " + split[2] + "\n");
                                boolean flag_real = false;
                                for (int i = 1; i < split.length; i++) {
                                    String[] split2 = split[i].split("\"");
                                    Log.v("vendedor real 3" + String.valueOf(i+3), ".\nSplit:\nSplit2: " + split2[2] + ", Split18: " + split2[18] + "\nMaquina: " + split2[2] + "\nEstado: " + split2[18] + "\n.");
                                    if (split2[6].equals(codigo)) {
                                        flag_real = true;
                                        maqui = split2[2];
                                        sid_loterias = split2[14];
                                        sid_vendidas = split2[22];
                                        nombre_puesto = split2[26];
                                        leer_loterias();
                                    } else {
                                        //Do nothing, continue with the for loop.
                                    }
                                }
                                if (flag_real) {

                                } else {
                                    msg("Vendedor no registrado!");
                                    tv_active.setText("Vendedor no registrado!");
                                    et_ID.setText("");
                                    et_ID.setFocusableInTouchMode(true);
                                    et_ID.requestFocus();
                                    return;
                                }
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
        } else {
            tv_active.setText("Debe estar conectado a una red de internet!");
            msg("Debe estar conectado a una red de internet!");
            et_ID.setFocusableInTouchMode(true);
            et_ID.requestFocus();
        }
    }

    private void crear_loteria(String sid, String rloteria, String rpaga1, String rpaga2, String rmaniana, String rtarde, String rdia, String rnoche, String rhorajuegoM, String rhorajuegoD, String rhorajuegoT, String rhorajuegoN, String rhoralistaM, String rhoralistaD, String rhoralistaT, String rhoralistaN, String limite, String nombrepuesto, String apodoM, String apodoD, String apodoT, String apodoN, String comisionV, String tipojuego) {

        if (rhoralistaD.length() == 3) {
            rhoralistaD = "0" + rhoralistaD;
        } else {
            //Do nothing.
        }
        if (rhoralistaT.length() == 3) {
            rhoralistaT = "0" + rhoralistaT;
        } else {
            //Do nothing.
        }
        if (rhoralistaN.length() == 3) {
            rhoralistaN = "0" + rhoralistaN;
        } else {
            //Do nothing.
        }
        if (rhoralistaM.length() == 3) {
            rhoralistaM = "0" + rhoralistaM;
        } else {
            //Do nothing.
        }
        String lot_demo = rloteria;
        try {
            crear_archivo("loteria_sfile" + lot_demo + "_sfile.txt");
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Paga1  " + rpaga1);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Paga2  " + rpaga2);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Maniana  " + rmaniana);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_juego_M  " + rhorajuegoM);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_lista_M  " + rhoralistaM);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Dia  " + rdia);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_juego_D  " + rhorajuegoD);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_lista_D  " + rhoralistaD);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Tarde  " + rtarde);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_juego_T  " + rhorajuegoT);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_lista_T  " + rhoralistaT);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Noche  " + rnoche);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_juego_N  " + rhorajuegoN);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_lista_N  " + rhoralistaN);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Limite_maximo  " + limite);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Nombre_puesto  " + nombrepuesto);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Apodo_M  " + apodoM);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Apodo_D  " + apodoD);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Apodo_T  " + apodoT);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Apodo_N  " + apodoN);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Numero_maquina  " + maqui);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Comision_vendedor  " + comisionV);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Tipo_juego  " + tipojuego);//Puede ser monazos, parley, reventados o regular
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Spread_Sheet_Id  " + sid);
            agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");
        } catch (Exception e) {

        }
    }

    private void leer_loterias() {
        if (verificar_internet()) {
            RequestQueue requestQueue;

            // Instantiate the cache
            Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

            // Set up the network to use HttpURLConnection as the HTTP client.
            Network network = new BasicNetwork(new HurlStack());

            // Instantiate the RequestQueue with the cache and network.
            requestQueue = new RequestQueue(cache, network);

            // Start the queue
            requestQueue.start();

            String url = readRowURL_activ + sid_loterias + "&sheet=" + "loterias";

            Log.v("Crear file active URL ", ".\nurl: " + url + "\n.");

            // Formulate the request and handle the response.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onResponse(String response) {
                            // Do something with the response
                            Log.v("loterias ", ".\nResponse:\n" + response);
                            String[] split_test = response.split("\"");
                            //Log.v("vendedor real 1", ".\nSplit_test:\n" + split_test[0] + ", " + split_test[1] + ", " + split_test[2] + ", " + split_test[3] + "\n");
                            if (response != null & split_test.length > 3) {
                                String[] split = response.split("loteria");//Se separa el objeto Json
                                //Log.v("vendedor real 2", ".\nSplit:\n" + split[0] + ", " + split[1] + ", " + split[2] + "\n");
                                //boolean flag_real = false;
                                String control_loteria_actual = "";
                                String contro_loteria_anterior = "";
                                boolean flag_inicio = true;
                                //int contador = 0;//debe llegar a 3 (4 valores incluido el cero!)

                                //Se crean las variables:
                                //
                                String sid = "x";
                                String rloteria = "Sin_nombre";
                                String rpaga1 = "0";
                                String rpaga2 = "0";
                                String rmaniana = "false";
                                String rtarde = "false";
                                String rdia = "false";
                                String rnoche = "false";
                                String rhorajuegoM = "00:00";
                                String rhorajuegoD = "00:00";
                                String rhorajuegoT = "00:00";
                                String rhorajuegoN = "00:00";
                                String rhoralistaM = "0000";
                                String rhoralistaD = "0000";
                                String rhoralistaT = "0000";
                                String rhoralistaN = "0000";
                                String limite = "0";
                                String nombrepuesto = "sin_nombre";
                                String apodoM = "maniana";
                                String apodoD = "dia";
                                String apodoT = "tarde";
                                String apodoN = "noche";
                                String comisionV = "0";
                                String tipojuego = "regular";
                                //boolean flag_siguientes = false;

                                //crear_loteria(String sid, String rloteria, String rpaga1, String rpaga2, String rmaniana, String rtarde, String rdia, String rnoche, String rhorajuegoM, String rhorajuegoD, String rhorajuegoT, String rhorajuegoN, String rhoralistaM, String rhoralistaD, String rhoralistaT, String rhoralistaN, String limite, String nombrepuesto, String apodoM, String apodoD, String apodoT, String apodoN, String comisionV, String tipojuego)

                                for (int i = 1; i < split.length; i++) {
                                    String horari = "";
                                    String[] split2 = split[i].split("\"");
                                    if (flag_inicio) {
                                        control_loteria_actual = split2[2];
                                        contro_loteria_anterior = control_loteria_actual;
                                        flag_inicio = false;
                                    } else {
                                        contro_loteria_anterior = control_loteria_actual;
                                        control_loteria_actual = split2[2];
                                    }
                                    horari = split2[6];

                                    if (contro_loteria_anterior.equals(control_loteria_actual)) {
                                        //meter los valores
                                        sid = split2[14];
                                        rloteria = split2[2];
                                        rpaga1 = split2[26];
                                        rpaga2 = split2[30];
                                        limite = split2[42];
                                        nombrepuesto = nombre_puesto;
                                        comisionV = split2[46];
                                        tipojuego = split2[50];
                                        if (horari.equals("dia")) {
                                            rdia = "true";
                                            rhorajuegoD = split2[34];
                                            rhoralistaD = split2[38];
                                            apodoD = split2[54];
                                        } else if (horari.equals("tarde")) {
                                            rtarde = "true";
                                            rhorajuegoT = split2[34];
                                            rhoralistaT = split2[38];
                                            apodoT = split2[54];
                                        } else if (horari.equals("noche")) {
                                            rnoche = "true";
                                            rhorajuegoN = split2[34];
                                            rhoralistaN = split2[38];
                                            apodoN = split2[54];
                                        } else if (horari.equals("maniana")) {
                                            rmaniana = "true";
                                            rhorajuegoM = split2[34];
                                            rhoralistaM = split2[38];
                                            apodoM = split2[54];
                                        } else {
                                            //do nothing.
                                        }
                                    } else {
                                        //enviar a crear la loteria
                                        Log.v("Crear loteria archivo", ".\nsid: " + sid + ", rloteria: " + rloteria + ", rpaga1: " + rpaga1 +
                                                ",\nrpaga2: " + rpaga2 + ", rmaniana " + rmaniana + ", rtarde: " + rtarde +
                                                ",\nrdia: " + rdia + ", rnoche: " + rnoche + ", rhorajuegoM: " + rhorajuegoM +
                                                ",\nrhorajuegoD: " + rhorajuegoD + ", rhorajuegoT: " + rhorajuegoT  + ", rhorajuegoN: " + rhorajuegoN +
                                                ",\nrhoralistaM: " + rhoralistaM + ", rhoralistaD: " + rhoralistaD + ", rhoralistaT: " + rhoralistaT +
                                                ",\nrhoralistaN: " + rhoralistaN + ", limite: " + limite + ", nombrepuesto: " + nombrepuesto +
                                                ",\napodoM: " + apodoM + ", apodoD: " + apodoD + ", apodoT: " + apodoT +
                                                ",\napodoN: " + apodoN + ", comisionV: " + comisionV + ", tipojuego: " + tipojuego + ".");
                                        crear_loteria(sid, rloteria, rpaga1, rpaga2, rmaniana, rtarde, rdia, rnoche, rhorajuegoM, rhorajuegoD, rhorajuegoT, rhorajuegoN, rhoralistaM, rhoralistaD, rhoralistaT, rhoralistaN, limite, nombrepuesto, apodoM, apodoD, apodoT, apodoN, comisionV, tipojuego);
                                        //reiniciar los valores

                                        rmaniana = "false";
                                        rtarde = "false";
                                        rdia = "false";
                                        rnoche = "false";
                                        rhorajuegoM = "00:00";
                                        rhorajuegoD = "00:00";
                                        rhorajuegoT = "00:00";
                                        rhorajuegoN = "00:00";
                                        rhoralistaM = "0000";
                                        rhoralistaD = "0000";
                                        rhoralistaT = "0000";
                                        rhoralistaN = "0000";
                                        apodoM = "maniana";
                                        apodoD = "dia";
                                        apodoT = "tarde";
                                        apodoN = "noche";

                                        //meter los valores
                                        sid = split2[14];
                                        rloteria = split2[2];
                                        rpaga1 = split2[26];
                                        rpaga2 = split2[30];
                                        limite = split2[42];
                                        nombrepuesto = nombre_puesto;
                                        comisionV = split2[46];
                                        tipojuego = split2[50];
                                        if (horari.equals("dia")) {
                                            rdia = "true";
                                            rhorajuegoD = split2[34];
                                            rhoralistaD = split2[38];
                                            apodoD = split2[54];
                                        } else if (horari.equals("tarde")) {
                                            rtarde = "true";
                                            rhorajuegoT = split2[34];
                                            rhoralistaT = split2[38];
                                            apodoT = split2[54];
                                        } else if (horari.equals("noche")) {
                                            rnoche = "true";
                                            rhorajuegoN = split2[34];
                                            rhoralistaN = split2[38];
                                            apodoN = split2[54];
                                        } else if (horari.equals("maniana")) {
                                            rmaniana = "true";
                                            rhorajuegoM = split2[34];
                                            rhoralistaM = split2[38];
                                            apodoM = split2[54];
                                        } else {
                                            //do nothing.
                                        }
                                    }
                                }
                                Log.v("loterias creadas", "Se han creado todas las loterias!!!");
                                try {
                                    generar_active_file();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                mostrar_active_vend();
                                check_activation();
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
        } else {
            tv_active.setText("Debe estar conectado a una red de internet!");
            msg("Debe estar conectado a una red de internet!");
            et_ID.setFocusableInTouchMode(true);
            et_ID.requestFocus();
        }
    }

    private Boolean verificar_codigo(String codigo){

        boolean retorno = true;
        for (int i = 0; i < codigo.length(); i++){
            if (i == 0) {
                String valor = String.valueOf(codigo.charAt(i));
                Log.v("verificar_codigo " + String.valueOf(i), "Valor a evaluar: " + valor);
                if (valor.equals("V")) {
                    //Do nothing. todo bien!
                } else {
                    retorno = false;
                    break;
                }
            } else {
                String valor = String.valueOf(codigo.charAt(i));
                Log.v("verificar_codigo " + String.valueOf(i), "Valor a evaluar: " + valor);
                boolean isNumeric = (valor != null && valor.matches("[0-9]"));
                if (isNumeric) {
                    //Do nothing. Todo bien!
                } else {
                    retorno = false;
                    break;
                }
            }
        }

        return retorno;
    }

    private void check_activation() {

        ocultar_todito("check_activation");
        Log.v("Check active ", "Se va a chequear si el vendedor esta activo o inactivo. ");
        if (verificar_internet()) {

            boolean new_flag = false;
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput("vent_active.txt"));
                //imprimir_archivo("facturas_online.txt");
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();
                while (linea != null) {
                    //Log.v("chequear no internet", ".\nLinea: " + linea + "\n\n");
                    //Toast.makeText(this, "Debug:\nFuncion cambiar_bandera, linea:\n" + linea, Toast.LENGTH_LONG).show();
                    String[] split = linea.split("_separador_");
                    if (split[0].equals("TRUE")) {
                        if (Integer.parseInt(split[4]) != Integer.parseInt(fecha)) {
                            new_flag = true;
                        } else {
                            mostrar_todito();
                            //Do nothing.
                        }
                    } else {
                        new_flag = true;
                    }
                    linea = br.readLine();
                }

                br.close();
                archivo.close();

            } catch (IOException e) {}

            if (new_flag) {
                RequestQueue requestQueue;

                // Instantiate the cache
                Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

                // Set up the network to use HttpURLConnection as the HTTP client.
                Network network = new BasicNetwork(new HurlStack());

                // Instantiate the RequestQueue with the cache and network.
                requestQueue = new RequestQueue(cache, network);

                // Start the queue
                requestQueue.start();

                String url = readRowURL_activ + sid_activ + "&sheet=" + s_activ;

                Log.v("Crear file active URL ", ".\nurl: " + url + "\n.");

                // Formulate the request and handle the response.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onResponse(String response) {
                                // Do something with the response
                                Log.v("ver_activ config 0", ".\nResponse:\n" + response);
                                String[] split_test = response.split("\"");
                                Log.v("ver_activ config 1", ".\nSplit_test:\n" + split_test[0] + ", " + split_test[1] + ", " + split_test[2] + ", " + split_test[3] + "\n");
                                if (response != null & split_test.length > 3) {
                                    String[] split = response.split("maquina");//Se separa el objeto Json
                                    Log.v("ver_activ config 2", ".\nSplit:\n" + split[0] + ", " + split[1] + ", " + split[2] + "\n");
                                    for (int i = 1; i < split.length; i++) {
                                        String[] split2 = split[i].split("\"");
                                        Log.v("ver_activ config 2", ".\nSplit:\nSplit2: " + split2[2] + ", Split18: " + split2[18] + "\n");

                                        if (split2[2].equals(maqui)) {
                                            Log.v("ver_activ config 3", ".\nMaquina: " + split2[2] + "\nEstado: " + split2[18] + "\nImpresora: " + split2[30]);
                                            if (split2[18].equals("TRUE")) {
                                                try {
                                                    borrar_archivo("vent_active.txt");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                //crear_archivo("vent_active.txt");
                                                agregar_linea_archivo("vent_active.txt", "TRUE_separador_" + maqui + "_separador_" + sid_loterias + "_separador_" + sid_vendidas + "_separador_" + fecha + "_separador_" + split2[30]);

                                                mostrar_todito();
                                                break;
                                                //Continua trabajando con la app.
                                            } else {//Vendedor inactivo. Se cierra la app.
                                                try {
                                                    borrar_archivo("vent_active.txt");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                //crear_archivo("vent_active.txt");
                                                agregar_linea_archivo("vent_active.txt", "FALSE_separador_" + maqui + "_separador_" + sid_loterias + "_separador_" + sid_vendidas + "_separador_" + fecha + "_separador_" + split2[30]);
                                                textView_esperar.setText("Vendedor inactivo. La app se cierra ahora...");
                                                esperar();
                                                break;
                                            }
                                        } else {
                                            //Do nothing, continue
                                        }
                                    }
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
            } else {
                mostrar_todito();
                //Do nothing.
            }
        } else {//No hay internet.
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput("vent_active.txt"));
                //imprimir_archivo("facturas_online.txt");
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();
                while (linea != null) {
                    Log.v("chequear no internet", ".\nLinea: " + linea + "\n\n");
                    //Toast.makeText(this, "Debug:\nFuncion cambiar_bandera, linea:\n" + linea, Toast.LENGTH_LONG).show();
                    String[] split = linea.split("_separador_");
                    if (split[0].equals("TRUE")) {
                        mostrar_todito();
                        //Do nothing. Continue
                    } else {
                        textView_esperar.setText("Vendedor inactivo. La app se cierra ahora...");
                        esperar();
                    }
                    linea = br.readLine();
                }

                br.close();
                archivo.close();

            } catch (IOException e) {}
        }

    }

    private void mostrar_active_vend() {


        textView_esperar.setText("");
        textView_esperar.setVisibility(View.INVISIBLE);
        inicio.setVisibility(View.VISIBLE);
        passET.setVisibility(View.VISIBLE);
        button_ventas.setVisibility(View.VISIBLE);
        button_config.setVisibility(View.VISIBLE);
        button_reportes.setVisibility(View.VISIBLE);
        boton_admin.setVisibility(View.VISIBLE);
        et_ID.setVisibility(View.INVISIBLE);
        tv_active.setVisibility(View.INVISIBLE);

    }

    private void ocultar_active_vend() {


        textView_esperar.setVisibility(View.INVISIBLE);
        //textView_esperar.setText("Verificando vendedor activo...\n\n         Por favor espere...");
        inicio.setVisibility(View.INVISIBLE);
        passET.setVisibility(View.INVISIBLE);
        button_ventas.setVisibility(View.INVISIBLE);
        button_config.setVisibility(View.INVISIBLE);
        button_reportes.setVisibility(View.INVISIBLE);
        boton_admin.setVisibility(View.INVISIBLE);
        et_ID.setVisibility(View.VISIBLE);
        tv_active.setVisibility(View.VISIBLE);

    }

    private void mostrar_todito() {


        textView_esperar.setText("");
        textView_esperar.setVisibility(View.INVISIBLE);

        inicio.setVisibility(View.VISIBLE);
        passET.setVisibility(View.VISIBLE);
        button_ventas.setVisibility(View.VISIBLE);
        button_config.setVisibility(View.VISIBLE);
        button_reportes.setVisibility(View.VISIBLE);
        boton_admin.setVisibility(View.VISIBLE);
        tv_active.setVisibility(View.INVISIBLE);
        et_ID.setVisibility(View.INVISIBLE);
    }

    private void ocultar_todito(String trace) {


        Log.v("ocultar_todito " + trace, "Se hace todo invisible");
        textView_esperar.setVisibility(View.VISIBLE);
        textView_esperar.setText("Verificando vendedor activo...\n\n         Por favor espere...");
        inicio.setVisibility(View.INVISIBLE);
        passET.setVisibility(View.INVISIBLE);
        button_ventas.setVisibility(View.INVISIBLE);
        button_config.setVisibility(View.INVISIBLE);
        button_reportes.setVisibility(View.INVISIBLE);
        boton_admin.setVisibility(View.INVISIBLE);
        tv_active.setVisibility(View.INVISIBLE);
        et_ID.setVisibility(View.INVISIBLE);

    }

    private void mostrar_todo() {

        /*
        textView_esperar.setText("");
        textView_esperar.setVisibility(View.INVISIBLE);

        inicio.setVisibility(View.VISIBLE);
        passET.setVisibility(View.VISIBLE);
        button_ventas.setVisibility(View.VISIBLE);
        button_config.setVisibility(View.VISIBLE);
        button_reportes.setVisibility(View.VISIBLE);
        boton_admin.setVisibility(View.VISIBLE);

         */

    }

    private void ocultar_todo() {

        /*
        textView_esperar.setVisibility(View.VISIBLE);
        textView_esperar.setText("   Conectando...\n\nPor favor espere...");

        inicio.setVisibility(View.INVISIBLE);
        passET.setVisibility(View.INVISIBLE);
        button_ventas.setVisibility(View.INVISIBLE);
        button_config.setVisibility(View.INVISIBLE);
        button_reportes.setVisibility(View.INVISIBLE);
        boton_admin.setVisibility(View.INVISIBLE);

         */

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

    private JSONObject generar_Json_resagadas(String file, String factura, String SSHHEETT, String SPREEADSHEET_ID, String tipo_lote) {
        //boolean flag_subir = false;
        JSONObject jsonObject = new JSONObject();

        //Debug:
        //imprimir_archivo(file);
        boolean flagsita = true;

        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput(facturas_diarias));
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            String json_string = "";
            Log.v("facturas_diarias_Json", ".\n\nLinea leida:\n\n" + linea + "\n\n.");
            while (linea != null) {
                String[] split = linea.split("      ");
                if (split[3].equals(file)) {
                    String information = split[0];
                    if (split[0].equals("BORRADA")) {
                        Log.v("ErrorBorrada", "Factura esta borrada, no hacer nada!!!");
                        flagsita = false;
                    } else if (tipo_lote.equals("Regular") | tipo_lote.equals("Reventados")) {
                        Log.v("Regular_reventados", ".\n\nLinea:\n\n" + linea + "\n\n.");
                        String[] split2 = information.split("__");
                        for (int i = 0; i < split2.length; i++) {
                            String[] split3 = split2[i].split("_");
                            //                            #1                 #2              monto          ext. info         factura
                            json_string = json_string + split3[0] + "_n_" + "no" + "_n_" + split3[1] + "_n_" + "no" + "_n_" + factura + "_l_";
                        }
                    } else if (tipo_lote.equals("Monazos")) {
                        String[] split2 = information.split("__");
                        for (int i = 0; i < split2.length; i++) {
                            String[] split3 = split2[i].split("_");
                            //                            #1                 #2             monto              ext. info           factura
                            json_string = json_string + split3[0] + "_n_" + "no" + "_n_" + split3[2] + "_n_" + split3[1] + "_n_" + factura + "_l_";
                        }
                    } else if (tipo_lote.equals("Parley")) {
                        String[] split2 = information.split("__");
                        for (int i = 0; i < split2.length; i++) {
                            String[] split3 = split2[i].split("_");
                            //                            #1                  #2                 monto           ext. info         factura
                            json_string = json_string + split3[0] + "_n_" + split3[1] + "_n_" + split3[2] + "_n_" + "no" + "_n_" + factura + "_l_";
                        }
                    } else {
                        //Do nothing. Nunca llega aqui!
                    }
                } else {
                    //Do nothing. Continue.
                }
                linea = br.readLine();
            }
            br.close();
            archivo.close();
            if (flagsita) {
                Log.v("jsonObjec", ".\n\njson_string: " + json_string + "\n\nSPREADSHEET_ID: " + SPREEADSHEET_ID + "\n\nSSHHEETT: " + SSHHEETT + "\n\nFactura: " + factura + "\n\n.");
                jsonObject = TranslateUtil.string_to_Json(json_string, SPREEADSHEET_ID, SSHHEETT, factura);
            }
            else {
                Log.v("Error21", "Factura ha sido borrada!!!");
            }
        } catch (IOException | JSONException e) {
        }
        return jsonObject;
    }

    private void abajiar() throws JSONException {

        //ocultar_todo();

        for (String key : abajos2.keySet()) {
            JSONObject objeto_json = new JSONObject();
            String SSHHEETT = "";
            String SSPPRREEAADDSSHHEETT = "";
            String tipo_lot = "";
            String factura = "";
            String file = abajos2.get(key);
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(facturas_diarias));
                //imprimir_archivo("facturas_online.txt");
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();

                while (linea != null) {
                    String[] split = linea.split("      ");
                    if (file.equals(split[3])) {
                        String[] split_name = file.split("_separador_");
                        Log.v("ERROR9001_facturas", "Linea leida: " + linea);
                        if (linea.equals("BORRADA")) {
                            Log.v("abajiar_linea_borrada", "\n\nLinea: " + linea);
                            //Do nothing.
                        } else if (linea.isEmpty()) {
                            Log.v("FALLA_linea_empty", "\n\nLinea: " + linea);
                            //Do nothing.
                        } else if (split_name[14].equals("equi.txt")) {
                            Log.v("abajiar_nombre_equi.txt", "\n\nLinea: " + linea);
                            tipo_lot = split_name[9];
                            SSPPRREEAADDSSHHEETT = split[1];
                            SSHHEETT = split[2];
                            factura = split_name[6];
                            //file = abajos2.get(key);
                            Log.v("Errequilibrar_facturas", "\n\nFinal del nombre del archivo: " + split_name[14] + "\n\nTipo loteria: " + tipo_lot + "\nSpreadSheet: " + SSPPRREEAADDSSHHEETT + "\nSheet: " + SSHHEETT + "\nFactura numero: " + factura + "file: " + file);
                            br.close();
                            archivo.close();
                            equilibrar(SSPPRREEAADDSSHHEETT, SSHHEETT, file, factura, tipo_lot, key);
                            break;
                        } else if (split_name[14].equals("null.txt")) {
                            Log.v("abajiar_nombre_null.txt", "\n\nLinea: " + linea);
                            tipo_lot = split_name[9];
                            SSPPRREEAADDSSHHEETT = split[1];
                            SSHHEETT = split[2];
                            factura = split_name[6];
                            //file = abajos2.get(key);\n
                            Log.v("Error9003_pre", "\n\nTipo_lot: " + tipo_lot + "\nSpreadSheet: " + SSPPRREEAADDSSHHEETT + "\nSheet: " + SSHHEETT + "\nFactura numero: " + factura + "\n\n.");
                            objeto_json = generar_Json_resagadas(file, factura, SSHHEETT, SSPPRREEAADDSSHHEETT, tipo_lot);
                            Log.v("Error9003_facturas", "\n\nTipo loteria: " + tipo_lot + "\nSpreadSheet: " + SSPPRREEAADDSSHHEETT + "\nSheet: " + SSHHEETT + "\nFactura numero: " + factura + "file: " + file);
                            abajos2.remove(key);
                            br.close();
                            archivo.close();
                            subir_factura_resagadas(objeto_json, "nothing");
                            break;
                        } else {
                            Log.v("abajiar_ninguna_opcion", " Error!!! Nunca deberia llegar aqui!!!\n\nLinea: " + linea);
                            //Do nothing.
                        }
                    }
                    linea = br.readLine();
                }

                br.close();
                archivo.close();
            } catch (IOException e) {

            }
        }
        //mostrar_todo();
    }

    private void obtener_Json_otras_facturas() throws JSONException {

        boolean flag = true;

        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("facturas_online.txt"));
            //imprimir_archivo("facturas_online.txt");
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            //String contenido = "";
            abajos2.clear();
            Integer countercito = 0;

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
                } else {
                    Log.v("OJOF_(error): ", "\n\n(No deberia llegar aqui!!!\n\nLinea: " + linea + " Fin de linea!!!");
                    //Do nothing.
                }
                linea = br.readLine();
            }
            archivo.close();
            br.close();


        } catch (IOException e) {
        }

        if (flag) {
            mostrar_todo();
            return;
        } else {
            //Do nothing. Continue with the work
        }

        abajiar();
        //return objeto_json;
    }

    private void subir_factura_resagadas(JSONObject jsonObject, String tag) throws JSONException {
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
                                try {
                                    abajiar();
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

    private void equilibrar(String SpreadSheet, String Sheet, String file, String factura, String tipo_lot, String key) {//Este metodo revisa si se ha subido parte del tiquete a la nube.
        RequestQueue requestQueue;//Se llama a la SpreadSheet que contiene la loteria actual para verificar que no hay errores en la subida de datos. Usar: Method.get
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
                            //crear_archivo(new_name);
                            //agregar_linea_archivo("facturas_online.txt", "abajo " + new_name + " " + SpreadSheet + " " + Sheet + " " + tipo_lot);
                            //Se hace que file sea un archivo igual a cualquier factura para subirla. Se guarda la informacion necesaria en el file.
                            String linea_leida = "";
                            for (String key : hashMap.keySet()) {
                                Log.v("E0 for hashMap", "\nKey: " + key + "\nValue: " + hashMap.get(key) + "\ntipo_lot: " + tipo_lot + "\n");
                                String[] splity = key.split("ojo-rojo_ojo-rojo");
                                //msg("Factura: " + key + "\nValor: " + hashMap.get(key) + "\n");
                                int otnom = Integer.parseInt(splity[5]) * -1;
                                if (tipo_lot.equals("Monazos")) {
                                    linea_leida = linea_leida + splity[1] + "_" + splity[3] + "_" + String.valueOf(otnom) + "__";
                                    //agregar_linea_archivo(new_name, splity[1] + "      " + String.valueOf(otnom) + "      " + splity[3] + "      " + SpreadSheet + "      " + Sheet);
                                    flagsitilla = true;
                                } else if(tipo_lot.equals("Parley")) {
                                    linea_leida = linea_leida + splity[1] + "_" + splity[2] + "_" + String.valueOf(otnom) + "__";
                                    //agregar_linea_archivo(new_name, splity[1] + "      " + splity[2] + "      " + String.valueOf(otnom) + "      " + SpreadSheet + "      " + Sheet);
                                    flagsitilla = true;
                                } else if (tipo_lot.equals("Reventados")) {
                                    linea_leida = linea_leida + splity[1] + "_" + String.valueOf(otnom) + "__";
                                    //agregar_linea_archivo(new_name, splity[1] + "      " + String.valueOf(otnom) + "      " + SpreadSheet + "      " + Sheet);
                                    flagsitilla = true;
                                } else if (tipo_lot.equals("Regular")) {
                                    linea_leida = linea_leida + splity[1] + "_" + String.valueOf(otnom) + "__";
                                    flagsitilla = true;
                                } else {
                                    //Do nothing.
                                }
                            }
                            String fecha_invoice = anio + mes + fecha + "_" + nombre_dia;
                            String linea_escribir = linea_leida + "      " + SpreadSheet + "      " + Sheet + "      " + new_name + "      " + fecha;
                            String linea_escribir2 = linea_leida + "      " + SpreadSheet + "      " + Sheet + "      " + new_name + "      " + fecha_invoice;
                            agregar_linea_archivo(facturas_diarias, linea_escribir);
                            agregar_linea_archivo(historial_facturas, linea_escribir2);

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
                                    subir_factura_resagadas(objeto_json, "equi");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            } else {
                                Log.v("equilibrar flagsitilla", "Todo or do nothing! I don't know right now :-|");
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

    private void msg(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
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

    private void subir_facturas_resagadas() throws JSONException {
        boolean flag_internet = verificar_internet();
        //JSONObject objeto_Json_a_subir = null;
        if (flag_internet) {
            ocultar_todo();
            obtener_Json_otras_facturas();
        } else {
            //Toast.makeText(this, "Verifique su coneccion a Internet!!!", Toast.LENGTH_LONG).show();
        }
    }

    private void agregar_fact_online(String file, String spid, String sheet, String tip_lot) {
        String linea_agrgar = "abajo " + file + " " + spid + " " + sheet + " " + tip_lot;//agregar_linea_archivo("facturas_online.txt", "abajo " + file + " " + SPREADSHEET_ID + " " + SHEET + " " + tipo_lot);
        Log.v("Error800", "Agregar a facturas_online.txt :\n\n" + imprimir_archivo(file));
        agregar_linea_archivo("facturas_online.txt", linea_agrgar);
        Log.v("Error111", "SpreadSheet ID: " + spid + "\nSheet: " + sheet + "\nTipo lot: " + tip_lot + "\nFile name: " + file);
        Log.v("Error110", "facturas_online.txt:\n\n" + imprimir_archivo("facturas_online.txt"));
    }

    private void generar_invoice_file() throws IOException {
        agregar_linea_archivo("invoice.txt","Contador 0");
    }

    @Override
    public void onBackPressed(){
        boton_atras();
    }

    private void boton_atras() {
        //ocultar_teclado();
        finish();
        System.exit(0);
    }

    private void boton_atras_active() {
        //ocultar_teclado();



        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        e.printStackTrace();
        }
        try {
            Thread.sleep(1500);
            finish();
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void esperar () {
        ocultar_todito("esperar");
        Toast.makeText(this, "Vendedor inactivo. La app se cierra ahora...", Toast.LENGTH_LONG).show();
        textView_esperar.setText("Vendedor inactivo. La app se cierra ahora...");

        for (int i = 0; i > 10; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mostrar_todito();
        boton_atras_active();
    }

    private void crear_archivos_config() throws IOException {


        ///////////////Se crea el archivo password.txt//////////
        String password = "password.txt";
        String archivos[] = fileList();
        if (archivo_existe(archivos, password)) {
            //Do nothing.
        } else {
            crear_archivo(password);
            String drowssap = "0144";
            agregar_linea_archivo(password, drowssap);
        }
        ////////////////////////////////////////////////////////

        ///////////////Se crea el archivo device.txt////////////
        String device = "device.txt";
        //String archivos[] = fileList();
        if (archivo_existe(archivos, device)) {
            //Do nothing.
        } else {
            crear_archivo(device);
            String ecived = "Celular";
            agregar_linea_archivo(device, ecived);
        }
        ////////////////////////////////////////////////////////

        ///////////////Se crea el archivo facturas_online.txt//////////
        String facturas_online = "facturas_online.txt";
        if (archivo_existe(archivos, facturas_online)) {
            //Do nothing.
        } else {
            crear_archivo(facturas_online);
        }
        ////////////////////////////////////////////////////////

        ///////////////Se crea el archivo facturas_diarias.txt//////////
        String facturas_diarias = "facturas_diarias.txt";
        if (archivo_existe(archivos, facturas_diarias)) {
            //Do nothing.
        } else {
            crear_archivo(facturas_diarias);
        }
        ////////////////////////////////////////////////////////

        ///////////////Se crea el archivo historial_facturas.txt//////////
        String historial_facturas = "historial_facturas.txt";
        if (archivo_existe(archivos, historial_facturas)) {
            //Do nothing.
        } else {
            crear_archivo(historial_facturas);
        }
        ////////////////////////////////////////////////////////

        ///////////////Se crea el archivo contabilidad.txt//////////
        String contabilidad = "contabilidad.txt";
        if (archivo_existe(archivos, contabilidad)) {
            //Do nothing.
        } else {
            crear_archivo(contabilidad);
        }
        ////////////////////////////////////////////////////////

    }

    public void ventas(View view){
        Intent Ventas = new Intent(this, VentasActivity.class);
        Ventas.putExtra("sid_vendidas", sid_vendidas);
        startActivity(Ventas);
        finish();
        System.exit(0);
    }

    public void reportes(View view){
        Intent Reportes = new Intent(this, ReportesActivity.class);
        startActivity(Reportes);
        finish();
        System.exit(0);
    }

    public void administrar(View view){
        Intent Administrar = new Intent(this, AdministrarActivity.class);
        startActivity(Administrar);
        finish();
        System.exit(0);
    }

    public void config(View view){
        if (passwor_is_correct()) {
            ocultar_teclado();
            Intent Config = new Intent(this, ConfigActivity.class);
            //Config.putExtra("Password", "burro");
            startActivity(Config);
            String mensaje = "\n\n\n\n   |*****CONFIGURACION!*****|\n\n\n\n\n\n\n";//
            if (dispositivo.equals("Maquina")) {
                printIt(mensaje);//               ****************************************************************************
                finish();//                       ************ACTIVAR EL printIt PARA ENTREGAR********************************
                System.exit(0);//           ****************************************************************************
            } else {
                finish();
                System.exit(0);
            }
        } else {
            if (passET.getText().toString().isEmpty()) {
                Toast.makeText(this, "Escriba su password...", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Password incorrecto!!!\nEscriba su password de nuevo...", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean passwor_is_correct() {
        passET.setFocusableInTouchMode(true);
        passET.requestFocus();
        mostrar_teclado();
        String linea = "";
        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("password.txt"));
            BufferedReader br = new BufferedReader(archivo);
            linea = br.readLine();
            linea = linea.replace("\n","");
            br.close();
            archivo.close();
        } catch (IOException e) {
        }
        while (true) {
            String pass = passET.getText().toString();
            //passET.setFocusableInTouchMode(true);
            //passET.requestFocus();
            //mostrar_teclado();
            if (pass.equals(linea)) {
                ocultar_teclado();

                return true;
            } else {
                return false;
            }
        }

    }

    private void ocultar_teclado(){
        View view = this.getCurrentFocus();
        InputMethodManager imn = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imn.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void mostrar_teclado(){
        //View view = this.getCurrentFocus();
        InputMethodManager imn = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imn.showSoftInput(passET, InputMethodManager.SHOW_IMPLICIT);
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
                Toast.makeText(getBaseContext(), "Open Bluetooth", Toast.LENGTH_SHORT).show();
                return;
            }
            // Get sunmi InnerPrinter BluetoothDevice
            String impresora = get_impresora();
            BluetoothDevice device = BluetoothUtil.getDevice(btAdapter, impresora);
            if (device == null) {
                Toast.makeText(getBaseContext(), "Make Sure Bluetooth have InnterPrinter", Toast.LENGTH_LONG).show();
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

    public  String check_device() {
        String archivos[] = fileList();
        dispositivo = null;
        if (archivo_existe(archivos, "device.txt")) {
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

    public  void crear_archivo(String nombre_archivo) {
        try{
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre_archivo, Activity.MODE_PRIVATE));
            archivo.flush();
            archivo.close();
        }catch (IOException e) {
        }
    }

    public  void agregar_linea_archivo (String file_name, String new_line) {
        String archivos[] = fileList();
        String ArchivoCompleto = "";//Aqui se lee el contenido del archivo guardado.

        if (archivo_existe(archivos, file_name)) {
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

    public  boolean archivo_existe (String[] archivos, String file_name){
        for (int i = 0; i < archivos.length; i++) {

            if (file_name.equals(archivos[i])) {
                return true;
            }
        }
        return false;
    }

    public  void borrar_archivo(String file) throws IOException {
        File archivo = new File(file);
        String empty_string = "";
        guardar(empty_string, file);
        archivo.delete();
    }

    public  void guardar (String contenido, String file_name) throws IOException {
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(file_name, Activity.MODE_PRIVATE));
            archivo.write(contenido);
            archivo.flush();
            archivo.close();

        } catch (IOException e) {
        }
    }

    public  String imprimir_archivo(String file_name){

        String archivos[] = fileList();
        String contenido = "";//Aqui se lee el contenido del archivo guardado.
        if (archivo_existe(archivos, file_name)) {//Archivo nombre_archivo es el archivo que vamos a imprimir
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(file_name));//Se abre archivo
                BufferedReader br = new BufferedReader(archivo);


                String linea = br.readLine();//Se lee archivo
                while (linea != null) {
                    contenido = contenido + linea + "\n";
                    linea = br.readLine();
                    //return;
                }
                br.close();
                archivo.close();
            } catch (IOException e) {
            }
        }
        return contenido;
    }

    public  String get_impresora() {
        String impresora = "00:11:22:33:44:55";
        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("vent_active.txt"));
            //imprimir_archivo("facturas_online.txt");
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            Log.v("chequear no internet", ".\nLinea: " + linea + "\n\n");
            //Toast.makeText(this, "Debug:\nFuncion cambiar_bandera, linea:\n" + linea, Toast.LENGTH_LONG).show();
            String[] split = linea.split("_separador_");
            impresora = split[5];
            br.close();
            archivo.close();
        } catch (IOException e) {}
        return impresora;
    }

}
