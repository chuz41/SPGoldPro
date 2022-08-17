package com.example.spgold;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private TextView inicio;
    private TextView textView_esperar;
    private Button button_ventas;
    private Button button_config;
    private Button button_reportes;
    private Button boton_admin;
    private String dispositivo;
    private EditText passET;
    private HashMap<String, String> abajos2 = new HashMap<String, String>();
    private String addRowURL = "https://script.google.com/macros/s/AKfycbweyYb-DHVgyEdCWpKoTmvOxDGXleawjAN8Uw9AeJYbZ24t9arB/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicio = (TextView)findViewById(R.id.tv_inicio);
        passET = (EditText) findViewById(R.id.et_password);
        button_ventas = (Button) findViewById(R.id.button_ventas);
        button_config = (Button) findViewById(R.id.button_config);
        button_reportes = (Button) findViewById(R.id.button_reportes);
        boton_admin = (Button) findViewById(R.id.boton_admin);
        textView_esperar = (TextView) findViewById(R.id.textView_esperar);


        passET.setFocusableInTouchMode(false);

        try {
            crear_archivos_config();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dispositivo = check_device();

        String archivos[] = fileList();
        boolean crear_lot = true;
        for (int i = 0; i < archivos.length; i++){
            Pattern pattern = Pattern.compile("loteria_sfile", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound){
                //abrir archivo y leerlocrear_loteria_decrear_loteria_demomo
                Log.v("ErrorCrearLoterias", "Archivo encontrado: " + archivos[i]);
                crear_lot = false;
                break;
            }
        }
        if (crear_lot){
            crear_loteria_demo();
        }

        boolean crear_invoice_file = true;
        for (int i = 0; i < archivos.length; i++){
            Pattern pattern = Pattern.compile("invoice", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound){
                crear_invoice_file = false;
                break;
            }
        }
        if (crear_invoice_file){
            generar_invoice_file();
        }


        Log.v("Error_main01", "Archivo:\n\n" + imprimir_archivo("facturas_online.txt"));

        try {
            subir_facturas_resagadas();
        } catch (JSONException e) {
            e.printStackTrace();
        }


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

    private void mostrar_todo() {

        textView_esperar.setText("");
        textView_esperar.setVisibility(View.INVISIBLE);

        inicio.setVisibility(View.VISIBLE);
        passET.setVisibility(View.VISIBLE);
        button_ventas.setVisibility(View.VISIBLE);
        button_config.setVisibility(View.VISIBLE);
        button_reportes.setVisibility(View.VISIBLE);
        boton_admin.setVisibility(View.VISIBLE);

    }

    private void ocultar_todo() {

        textView_esperar.setVisibility(View.VISIBLE);
        textView_esperar.setText("   Conectando...\n\nPor favor espere...");

        inicio.setVisibility(View.INVISIBLE);
        passET.setVisibility(View.INVISIBLE);
        button_ventas.setVisibility(View.INVISIBLE);
        button_config.setVisibility(View.INVISIBLE);
        button_reportes.setVisibility(View.INVISIBLE);
        boton_admin.setVisibility(View.INVISIBLE);

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
                        subir_factura_resagadas(objeto_json, "nothing");
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

    private void obtener_Json_otras_facturas() throws JSONException {

        JSONObject objeto_json = null;

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
                } else {
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

    public void guardar (String Tcompleto, String nombre){
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre, Activity.MODE_PRIVATE));
            archivo.write(Tcompleto);
            archivo.flush();
            //archivo.close();
        } catch (IOException e) {
        }
    }

    public void borrar_archivo(String file) {
        File archivo = new File(file);
        String empty_string = "";
        guardar(empty_string, file);
        archivo.delete();
    }

    private void subir_facturas_resagadas() throws JSONException {
        boolean flag_internet = verificar_internet();
        //JSONObject objeto_Json_a_subir = null;
        if (flag_internet) {
            ocultar_todo();
            obtener_Json_otras_facturas();
        } else {
            Toast.makeText(this, "Verifique su coneccion a Internet!!!", Toast.LENGTH_LONG).show();
        }
    }

    private void mensaje_config() {
        Toast.makeText(getBaseContext(), "Presione CONFIGURACION nuevamente...", Toast.LENGTH_LONG).show();
        Toast.makeText(getBaseContext(), "Presione CONFIGURACION nuevamente...", Toast.LENGTH_LONG).show();
    }

    private String check_device() {
        String archivos[] = fileList();
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

    private void agregar_fact_online(String file, String spid, String sheet, String tip_lot) {
        String linea_agrgar = "abajo " + file + " " + spid + " " + sheet + " " + tip_lot;//agregar_linea_archivo("facturas_online.txt", "abajo " + file + " " + SPREADSHEET_ID + " " + SHEET + " " + tipo_lot);
        Log.v("Error800", "Algo raro pasa, linea agrgar:\n\n" + linea_agrgar);
        agregar_linea_archivo("facturas_online.txt", linea_agrgar);
        Log.v("Error111", "SpreadSheet ID: " + spid + "\nSheet: " + sheet + "\nTipo lot: " + tip_lot + "\nFile name: " + file);
        //msg("Error111 SpreadSheet ID: " + spid + "\nSheet: " + sheet + "\nTipo lot: " + tip_lot + "\nFile name: " + file);
        //debug
        //imprimir_archivo(file);
        Log.v("Error110", "facturas_online.txt:\n\n" + imprimir_archivo("facturas_online.txt"));
        //msg("Error110\n" + imprimir_archivo(imprimir_archivo(file)));
    }

    private void generar_invoice_file() {
        crear_archivo("invoice.txt");
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

    private void crear_archivos_config() throws IOException {

        String files[] = fileList();

        ///////////////Se crea el archivo password.txt//////////
        String password = "password.txt";
        if (ArchivoExiste(files, password)) {
            /*Log.v("Error80", "Entro en el if que no debia!!!");
            borrar_archivo("password.txt");
            crear_archivo(password);
            String drowssap = "0144";
            agregar_linea_archivo(password, drowssap);*/
        } else {
            //Se crea el archivo password
            crear_archivo(password);
            String drowssap = "0144";
            agregar_linea_archivo(password, drowssap);
        }
        ////////////////////////////////////////////////////////

        ///////////////Se crea el archivo device.txt////////////
        String device = "device.txt";
        if (ArchivoExiste(files, device)) {
            //Do nothing
        } else {
            //Se crea el archivo password
            crear_archivo(device);
            String ecived = "Celular";
            agregar_linea_archivo(device, ecived);
        }
        ////////////////////////////////////////////////////////

        ///////////////Se crea el archivo facturas_online.txt//////////
        String facturas_online = "facturas_online.txt";
        if (ArchivoExiste(files, facturas_online)) {
            //Do nothing
        } else {
            //Se crea el archivo facturas_online.txt
            crear_archivo(facturas_online);
        }
        ////////////////////////////////////////////////////////
    }

    private boolean ArchivoExiste (String archivos [],String Tiquete){
        for (int i = 0; i < archivos.length; i++)
            if (Tiquete.equals(archivos[i]))
                return true;
        return false;
    }

    private void crear_archivo(String nombre_archivo) {
        try{
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre_archivo, Activity.MODE_PRIVATE));
            archivo.flush();
            archivo.close();
        }catch (IOException e) {
        }
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

    private void crear_loteria_demo() {
        String lot_demo = "Tica";
        crear_archivo("loteria_sfile" + lot_demo + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Paga1  " + "90");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_juego_D  " + "23:55");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_lista_D  " + "2350");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_juego_T  " + "16:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_lista_T  " + "1624");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_juego_N  " + "19:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_lista_N  " + "1924");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Comision_vendedor  " + "5");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Spread_Sheet_Id  " + "18tZu3c2sWugGgGFXRgX1_uPg-kCEAIyOKeSZ9nJjlI0");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "1Naf0UZeQ2KvujDe9to5ZfD66OwORmFXO9fxtFKGT9rM");

        String lot_demo5 = "Primera";
        crear_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Maniana  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_juego_M  " + "10:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_lista_M  " + "0955");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Dia  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_juego_D  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_lista_D  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_juego_T  " + "18:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_lista_T  " + "1755");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Noche  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_juego_N  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_lista_N  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Spread_Sheet_Id  " + "1Lws5DHQ7wOqwfW_xwmruldHNDsOJ9p57T2QYeGswf7Y");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "1Sug8WPQ1I4rOXTIpFqNIVRXVAEMsr5lceV-LfxAu40U");

        String lot_demo6 = "New_York";
        crear_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_juego_D  " + "12:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_lista_D  " + "1224");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Tarde  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_juego_T  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_lista_T  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_juego_N  " + "20:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_lista_N  " + "2024");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Spread_Sheet_Id  " + "1LuEvywgRHVQYkr7ef1TxFhZgD0SJfjqod5B3O4J2ixk");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "1QT2-nuiVpfNlewxaaTQpR_8HKcrOxSQAxOcwImuyzbw");

        String lot_demo7 = "Domi";
        crear_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_juego_D  " + "12:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_lista_D  " + "1224");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_juego_T  " + "16:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_lista_T  " + "1554");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_juego_N  " + "19:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_lista_N  " + "1854");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Spread_Sheet_Id  " + "1lNUcB-FoNjSmwaYlnM-2mqjmUma6b2kRn-p06IisZsg");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "1b106cwghfxwsRT09RhG0Pa37g6V5RH0ixT_6O0lw1hw");

        String lot_demo8 = "Panama";
        crear_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_juego_D  " + "12:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_lista_D  " + "1154");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Tarde  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_juego_T  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_lista_T  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Noche  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_juego_N  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_lista_N  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Spread_Sheet_Id  " + "1FrJGxFh4KZXhDu60KIc5oT5UbpsjlZVM-hBBP1YVxRg");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "1upj7tHs2PMs-YcL4umkVkUp02Z1AjGeeCM9qbX5-cqk");

        String lot_demo9 = "Loteka";
        crear_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Dia  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_juego_D  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_lista_D  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_juego_T  " + "17:55");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_lista_T  " + "1749");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Noche  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_juego_N  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_lista_N  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Spread_Sheet_Id  " + "1S0lN-mowVTtzAXPsYmNpLTJLi-aMXtDCTMCPUF3Aj_s");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "1QT2-nuiVpfNlewxaaTQpR_8HKcrOxSQAxOcwImuyzbw");

        String lot_demo10 = "Honduras";
        crear_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Maniana  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_juego_M  " + "11:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_lista_M  " + "1054");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Dia  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_juego_D  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_lista_D  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_juego_T  " + "15:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_lista_T  " + "1454");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_juego_N  " + "21:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_lista_N  " + "2054");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Spread_Sheet_Id  " + "1T1lSXClnnvZr3gARo_rkC8e4aKhFTqZpe_xctUqdXtY");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "1w44ESJFAM9e67ho8_V_XijTOD9MBbV9qprqmq3TEEvY");


        String lot_demo1 = "Reventados";
        crear_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt");
        //agregar_linea_archivo("loteria_s" + lot_demo + "_s.txt", "Nombre: " + "Reventados");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Paga1  " + "200");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Paga2  " + "80");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_juego_D  " + "12:55");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_lista_D  " + "2359");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_juego_T  " + "16:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_lista_T  " + "1624");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_juego_N  " + "19:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_lista_N  " + "1924");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Limite_maximo  " + "25000");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Tipo_juego  " + "Reventados");//Puede ser monazos, parley, reventados o regular
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Spread_Sheet_Id  " + "1qA6GozxLA9P4P7O_FSG5GYac5ZVXxouDAwfGF03NKWU");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "1_Se9Dc5qBNG4azqY-cXXJnibuHD61s3S-865nN6Kme0");

        String lot_demo2 = "Nica";
        crear_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt");
        //agregar_linea_archivo("loteria_s" + lot_demo + "_s.txt", "Nombre: " + "Tica");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Maniana  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_juego_M  " + "11:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_lista_M  " + "1054");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_juego_D  " + "15:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_lista_D  " + "1454");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_juego_T  " + "18:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_lista_T  " + "1754");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_juego_N  " + "21:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_lista_N  " + "2054");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Spread_Sheet_Id  " + "1u0o5K6AHCl666WNYd_p8ekUn1BWsTgUolTaW2J8eRSs");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "1KcjNINPy9s1y5vXZ5PsoG1BQP5-4uX1xF6CMSV3_ocQ");


        String lot_demo3 = "Parley";
        crear_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt");
        //agregar_linea_archivo("loteria_s" + lot_demo + "_s.txt", "Nombre: " + "Tica");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Paga1  " + "500");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_juego_D  " + "12:55");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_lista_D  " + "2359");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_juego_T  " + "16:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_lista_T  " + "1624");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_juego_N  " + "19:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_lista_N  " + "1924");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Limite_maximo  " + "25000");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Tipo_juego  " + "Parley");//Puede ser monazos, parley, reventados o regular
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Spread_Sheet_Id  " + "1hq75pRHQ-u0owb83CBaiz2OQOm9eLFgsK-A-CFxBvDo");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "1obZ7uWljU-265O5saZCoRDRAnC5cGnD25ScI5F4P4X4");




        String lot_demo4 = "Monazos";
        crear_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt");
        //agregar_linea_archivo("loteria_s" + lot_demo + "_s.txt", "Nombre: " + "Tica");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Paga1  " + "700");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Paga2  " + "115");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_juego_D  " + "12:55");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_lista_D  " + "2359");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_juego_T  " + "16:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_lista_T  " + "1624");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_juego_N  " + "19:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_lista_N  " + "1924");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Limite_maximo  " + "25000");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Tipo_juego  " + "Monazos");//Puede ser monazos, parley, reventados o regular
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Spread_Sheet_Id  " + "1tH2cO2ivsLYP0fRPVktlezEN-1Vyl2hY0XRN0ttDd60");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "13_uhLbZjC7Li_JaKMYIuzKape8E9tUpHQNd4JuN1DLE");


        String lot_demo11 = "Revancha";
        crear_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_juego_D  " + "12:55");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_lista_D  " + "1249");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_juego_T  " + "16:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_lista_T  " + "1624");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_juego_N  " + "19:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_lista_N  " + "1924");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Spread_Sheet_Id  " + "1D3uSXT_uppdwqdXUgo2qyA3anhaZ80cRzwtBM_vvCfg");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "17Y5mPY6J5QmATEtJdE56Kzv7NGNpMl3jwR5cerIj6gs");
/*
        String lot_demo12 = "Prueba_regular";
        crear_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Maniana  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_juego_M  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_lista_M  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_juego_D  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_lista_D  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_juego_T  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_lista_T  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_juego_N  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_lista_N  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Spread_Sheet_Id  " + "18tZu3c2sWugGgGFXRgX1_uPg-kCEAIyOKeSZ9nJjlI0");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "1Naf0UZeQ2KvujDe9to5ZfD66OwORmFXO9fxtFKGT9rM");

        String lot_demo13 = "Prueba_paarley";
        crear_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Paga1  " + "500");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Maniana  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_juego_M  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_lista_M  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_juego_D  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_lista_D  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_juego_T  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_lista_T  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_juego_N  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_lista_N  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Tipo_juego  " + "Parley");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Spread_Sheet_Id  " + "1hq75pRHQ-u0owb83CBaiz2OQOm9eLFgsK-A-CFxBvDo");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "1obZ7uWljU-265O5saZCoRDRAnC5cGnD25ScI5F4P4X4");

        String lot_demo14 = "Prueba_moonazos";
        crear_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Paga1  " + "700");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Paga2  " + "115");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Maniana  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_juego_M  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_lista_M  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_juego_D  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_lista_D  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_juego_T  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_lista_T  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_juego_N  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_lista_N  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Tipo_juego  " + "Monazos");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Spread_Sheet_Id  " + "1tH2cO2ivsLYP0fRPVktlezEN-1Vyl2hY0XRN0ttDd60");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "13_uhLbZjC7Li_JaKMYIuzKape8E9tUpHQNd4JuN1DLE");

        String lot_demo15 = "Prueba_reeventados";
        crear_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Paga1  " + "200");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Paga2  " + "80");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Maniana  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_juego_M  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_lista_M  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_juego_D  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_lista_D  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_juego_T  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_lista_T  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_juego_N  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_lista_N  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Tipo_juego  " + "Reventados");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Spread_Sheet_Id  " + "1qA6GozxLA9P4P7O_FSG5GYac5ZVXxouDAwfGF03NKWU");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "1_Se9Dc5qBNG4azqY-cXXJnibuHD61s3S-865nN6Kme0");
*/

        /*
    private void crear_loteria_demo() {
        String lot_demo = "Tica";
        crear_archivo("loteria_sfile" + lot_demo + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Paga1  " + "90");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_juego_D  " + "12:55");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_lista_D  " + "1249");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_juego_T  " + "16:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_lista_T  " + "1624");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_juego_N  " + "19:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_lista_N  " + "1924");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Comision_vendedor  " + "5");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");

        String lot_demo5 = "Primera";
        crear_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Maniana  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_juego_M  " + "10:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_lista_M  " + "0955");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Dia  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_juego_D  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_lista_D  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_juego_T  " + "18:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_lista_T  " + "1755");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Noche  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_juego_N  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Hora_lista_N  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo5 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");

        String lot_demo6 = "New_York";
        crear_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_juego_D  " + "12:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_lista_D  " + "1224");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Tarde  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_juego_T  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_lista_T  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_juego_N  " + "20:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Hora_lista_N  " + "2024");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo6 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");

        String lot_demo7 = "Domi";
        crear_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_juego_D  " + "12:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_lista_D  " + "1224");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_juego_T  " + "16:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_lista_T  " + "1554");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_juego_N  " + "19:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Hora_lista_N  " + "1854");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo7 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");

        String lot_demo8 = "Panama";
        crear_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_juego_D  " + "12:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_lista_D  " + "1154");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Tarde  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_juego_T  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_lista_T  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Noche  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_juego_N  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Hora_lista_N  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo8 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");

        String lot_demo9 = "Loteka";
        crear_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Dia  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_juego_D  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_lista_D  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_juego_T  " + "17:55");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_lista_T  " + "1749");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Noche  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_juego_N  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Hora_lista_N  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo9 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");

        String lot_demo10 = "Honduras";
        crear_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Maniana  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_juego_M  " + "11:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_lista_M  " + "1054");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Dia  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_juego_D  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_lista_D  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_juego_T  " + "15:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_lista_T  " + "1454");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_juego_N  " + "21:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Hora_lista_N  " + "2054");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo10 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");


        String lot_demo1 = "Reventados";
        crear_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt");
        //agregar_linea_archivo("loteria_s" + lot_demo + "_s.txt", "Nombre: " + "Reventados");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Paga1  " + "200");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Paga2  " + "80");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_juego_D  " + "12:55");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_lista_D  " + "1249");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_juego_T  " + "16:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_lista_T  " + "1624");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_juego_N  " + "19:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Hora_lista_N  " + "1924");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Limite_maximo  " + "25000");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Tipo_juego  " + "Reventados");//Puede ser monazos, parley, reventados o regular
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo1 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");

        String lot_demo2 = "Nica";
        crear_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt");
        //agregar_linea_archivo("loteria_s" + lot_demo + "_s.txt", "Nombre: " + "Tica");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Maniana  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_juego_M  " + "11:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_lista_M  " + "1054");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_juego_D  " + "15:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_lista_D  " + "1454");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_juego_T  " + "18:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_lista_T  " + "1754");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_juego_N  " + "21:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Hora_lista_N  " + "2054");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo2 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");


        String lot_demo3 = "Parley";
        crear_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt");
        //agregar_linea_archivo("loteria_s" + lot_demo + "_s.txt", "Nombre: " + "Tica");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Paga1  " + "500");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_juego_D  " + "12:55");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_lista_D  " + "1249");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_juego_T  " + "16:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_lista_T  " + "1624");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_juego_N  " + "19:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Hora_lista_N  " + "1924");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Limite_maximo  " + "25000");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Tipo_juego  " + "Parley");//Puede ser monazos, parley, reventados o regular
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo3 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");




        String lot_demo4 = "Monazos";
        crear_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt");
        //agregar_linea_archivo("loteria_s" + lot_demo + "_s.txt", "Nombre: " + "Tica");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Paga1  " + "700");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Paga2  " + "115");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_juego_D  " + "12:55");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_lista_D  " + "1249");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_juego_T  " + "16:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_lista_T  " + "1624");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_juego_N  " + "19:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Hora_lista_N  " + "1924");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Limite_maximo  " + "25000");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Tipo_juego  " + "Monazos");//Puede ser monazos, parley, reventados o regular
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo4 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");


        String lot_demo11 = "Revancha";
        crear_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Maniana  " + "false");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_juego_M  " + "00:00");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_lista_M  " + "0000");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_juego_D  " + "12:55");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_lista_D  " + "1249");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_juego_T  " + "16:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_lista_T  " + "1624");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_juego_N  " + "19:30");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Hora_lista_N  " + "1924");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo11 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");

        String lot_demo12 = "Prueba_regular";
        crear_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Paga1  " + "85");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Maniana  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_juego_M  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_lista_M  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_juego_D  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_lista_D  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_juego_T  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_lista_T  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_juego_N  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Hora_lista_N  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Tipo_juego  " + "Regular");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo12 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");

        String lot_demo13 = "Prueba_paarley";
        crear_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Paga1  " + "500");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Paga2  " + "0");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Maniana  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_juego_M  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_lista_M  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_juego_D  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_lista_D  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_juego_T  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_lista_T  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_juego_N  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Hora_lista_N  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Tipo_juego  " + "Parley");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo13 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");

        String lot_demo14 = "Prueba_moonazos";
        crear_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Paga1  " + "700");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Paga2  " + "115");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Maniana  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_juego_M  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_lista_M  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_juego_D  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_lista_D  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_juego_T  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_lista_T  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_juego_N  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Hora_lista_N  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Tipo_juego  " + "Monazos");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo14 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");

        String lot_demo15 = "Prueba_reeventados";
        crear_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Paga1  " + "200");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Paga2  " + "80");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Maniana  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_juego_M  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_lista_M  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Dia  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_juego_D  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_lista_D  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Tarde  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_juego_T  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_lista_T  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Noche  " + "true");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_juego_N  " + "23:59");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Hora_lista_N  " + "2358");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Limite_maximo  " + "99999");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Nombre_puesto  " + "Chuz");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Numero_maquina  " + "25");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Comision_vendedor  " + "10");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Tipo_juego  " + "Reventados");//Puede ser monazos, parley, reventados o regular

        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Spread_Sheet_Id  " + "x");
        agregar_linea_archivo("loteria_sfile" + lot_demo15 + "_sfile.txt", "Spread_Sheet_Id_maniana  " + "x");

*/

    }

    private boolean archivo_existe(){
        boolean flag = true;
        return flag;
    }

    public void ventas(View view){
        Intent Ventas = new Intent(this, VentasActivity.class);
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
                printIt(mensaje);//       ****************************************************************************
                finish();//               ************ACTIVAR EL printIt PARA ENTREGAR********************************
                System.exit(0);//   ****************************************************************************
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
            BluetoothDevice device = BluetoothUtil.getDevice(btAdapter);
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

}
