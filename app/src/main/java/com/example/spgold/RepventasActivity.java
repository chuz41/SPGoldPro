package com.example.spgold;

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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepventasActivity extends AppCompatActivity {

    private String mes;
    private String anio;
    private String dia;
    private String hora;
    private String minuto;
    private String fecha;
    private String contenido;
    private TextView lot;
    private TextView hor;
    private Spinner loteria;
    private Spinner horario;
    private String[] loterias;//Informacion que aparecera en el spinner de loterias
    private String[] horarios;//Informacion que aparecera en el spinner de horarios
    private String[] archivos_lot;
    private TextView titulo;
    Map<String, String> loter = new HashMap<String, String>();
    private Map<String, Integer> meses = new HashMap<String, Integer>();
    private String dispositivo;
    private HashMap<String, String> abajos2 = new HashMap<String, String>();
    private String addRowURL = "https://script.google.com/macros/s/AKfycbweyYb-DHVgyEdCWpKoTmvOxDGXleawjAN8Uw9AeJYbZ24t9arB/exec";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repventas);

        titulo = (TextView) findViewById(R.id.tv_repventas);
        lot = (TextView) findViewById(R.id.tv_ingrese_lot_rev);
        hor = (TextView) findViewById(R.id.tv_ingrese_hor_rev);
        loteria = (Spinner) findViewById(R.id.spinner_lot);
        horario = (Spinner) findViewById(R.id.spinner_hor);

        //titulo.setText("Reporte de ventas");

        llenar_mapa_meses();

        dispositivo = check_device();

        Date now = Calendar.getInstance().getTime();
        String ahora = now.toString();
        //se separan los campos de la fecha y hora para verificar que si se pueda realizar la venta.
        separar_fechaYhora(ahora);

        //Crear diccionarios de loterias configuradas:
        String archivos[] = fileList();
        int a = 0;
        for (int i = 0; i < archivos.length; i++) {
            Pattern pattern = Pattern.compile("loteria_sfile", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound) {
                if (lotActiva(archivos[i])) {
                    a = a + 1;
                }
            }
        }
        loterias = new String[a+1];
        archivos_lot = new String[a];
        a = 0;
        loterias[a] = "Elija una loteria...";
        for (int i = 0; i < archivos.length; i++) {
            Pattern pattern = Pattern.compile("loteria_sfile", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound) {
                if (lotActiva(archivos[i])) {
                    //Toast.makeText(this, "Loteria encontrada: " + archivos[i], Toast.LENGTH_LONG).show();
                    a = a + 1;
                    String[] split = archivos[i].split("_sfile");
                    loterias[a] = split[1];
                    archivos_lot[a - 1] = archivos[i];
                }
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, loterias);
        loteria.setAdapter(adapter);

        loteria.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //Crear diccionario con la informacion de la loteria seleccionada
                        String seleccion = loteria.getSelectedItem().toString();
                        if (seleccion.equals("Elija una loteria...")) {
                            //Do nothing!
                        }else {
                            crearDiccionario();//Meter aqui la loteria seleccionada en el spinner
                            //Agregar horarios al otro Spinner
                            crear_array_horarios();
                            llenar_spinner_hor();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

        horario.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //Crear diccionario con la informacion de la loteria seleccionada
                        String seleccion = horario.getSelectedItem().toString();
                        if (seleccion.equals("Escoja el horario...")) {
                            //Do nothing!
                        }else {
                            //Ir a pagina de ventas
                            String seleccion2 = loteria.getSelectedItem().toString();
                            if (seleccion2.equals("Elija una loteria...")) {
                                //Do nothing!
                            }else {
                                tirar_reporte();
                            }

                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

        try {
            subir_facturas_resagadas();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed(){
        boton_atras();
    }

    private void subir_facturas_resagadas() throws JSONException {
        boolean flag_internet = verificar_internet();
        //JSONObject objeto_Json_a_subir = null;
        if (flag_internet) {
            //ocultar_todo();
            obtener_Json_otras_facturas();
        } else {
            //Toast.makeText(this, "Verifique su coneccion a Internet!!!", Toast.LENGTH_LONG).show();
        }
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
                //mostrar_todo();
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

        //ocultar_todo();

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
                                //mostrar_todo();
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

        //ocultar_todo();

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

    private void crear_archivo(String nombre_archivo) {
        try{
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre_archivo, Activity.MODE_PRIVATE));
            archivo.flush();
            archivo.close();
        }catch (IOException e) {
        }
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

    private void boton_atras() {

        Intent Main = new Intent(this, MainActivity.class);
        startActivity(Main);
        finish();
        System.exit(0);
    }


    private void separar_fechaYhora(String ahora) {
        String[] split = ahora.split(" ");

        mes = String.valueOf(meses.get(split[1]));
        anio = split[5];
        fecha = split[2];
        dia = split[2];

        hora = split[3];
        split = hora.split(":");
        minuto = split[1];
        hora = split[0];
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


    private boolean lotActiva(String archivo) {
        try {
            InputStreamReader Archivo = new InputStreamReader(openFileInput(archivo));//Se abre archivo
            BufferedReader br = new BufferedReader(Archivo);
            String linea = br.readLine();//Se lee la primera linea del archivo
            //Toast.makeText(this, "Linea: " + linea, Toast.LENGTH_LONG).show();
            Pattern pattern = Pattern.compile("BORRADA", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(linea);//Se verifica si la loteria se ha borrado
            boolean matchFound = matcher.find();
            if (matchFound) {
                br.close();
                Archivo.close();
                return false;
            }
            br.close();
            Archivo.close();
        }catch (IOException e) {
        }
        return true;
    }


    private void tirar_reporte(){

        String Loteria = loteria.getSelectedItem().toString();
        String Horario = horario.getSelectedItem().toString();
        int total = 0;
        String archivos[] = fileList();

        if (loter.get("Tipo_juego").equals("Parley")) {
            if (ArchivoExiste(archivos, Loteria + "_" + Horario + "_" + fecha + "_" + mes + ".txt")) {//Archivo nombre_archivo es el archivo contable

                try {
                    InputStreamReader archivo24 = new InputStreamReader(openFileInput(Loteria + "_" + Horario + "_" + fecha + "_" + mes + ".txt"));//Se abre archivo
                    BufferedReader br24 = new BufferedReader(archivo24);
                    contenido = "Reporte de ventas\n" + Loteria + " " + Horario + "\nFecha: " + fecha + "/" + mes + "/" + anio + ":\n\n#############################";//Aqui se guarda el reporte que se va a imprimir

                    String linea = br24.readLine();//Se lee la primera linea del archivo
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    int counter = 0;
                    String linea_tempo = "";
                    while (linea != null) {
                        String separacion_str = " ";
                        String[] split = linea.split("      ");//Se separa el monto del numero guardado.
                        if (Integer.parseInt(split[2]) > 0){
                            total = total + Integer.parseInt(split[2]);

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
                        }
                        linea = br24.readLine();
                    }
                    if (counter == 1) {
                        contenido = contenido + "\n" + linea_tempo + "\n";
                    } else {
                        contenido = contenido + "\n";
                    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    contenido = contenido + "#############################\n\n#############################\n Total:  " + String.valueOf(total) + " colones.\n#############################";
                    contenido = contenido + "\n\n\n\n";
                    br24.close();
                    archivo24.close();
                }catch (IOException e) {
                }

                printIt(contenido);
                guardar_pdf();

                //Al final, se elimina el archivo temporal del reporte:TODO:... (Revisar si la siguiente linea afecta el buen funcionamiento del sistema. Creo que no se debe borrar, por aquello de los adicionales.
                //borrar_archivo(Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt");
                // ACTIVAR LAS LINEAS SIGUIENTES CUANDO HALLA ROLLO.
                //Esto queda para el final...
                //Intent rep = new Intent(this, ReportesActivity.class);
                //startActivity(rep);
                //finish();
                llenar_spinner_hor();
                String seleccion = loteria.getSelectedItem().toString();
                if (seleccion.equals("Elija una loteria...")) {
                    //Do nothing!
                }else {
                    crearDiccionario();//Meter aqui la loteria seleccionada en el spinner
                    //Agregar horarios al otro Spinner
                    crear_array_horarios();
                    llenar_spinner_hor();
                }
                //System.exit(0);
            } else {
                Toast.makeText(this, "No se encontraron ventas para " + Loteria + " " + Horario + ".\nVerifique y/o llame a soporte tecnico.\nTelefono: 85258108", Toast.LENGTH_LONG).show();
            }

        } else if (loter.get("Tipo_juego").equals("Monazos")) {
            if (ArchivoExiste(archivos, Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt")) {//Archivo nombre_archivo es el archivo contable

                try {
                    InputStreamReader archivo24 = new InputStreamReader(openFileInput(Loteria + "_" + Horario + "_" + fecha + "_" + mes + ".txt"));//Se abre archivo
                    BufferedReader br24 = new BufferedReader(archivo24);
                    contenido = "Reporte de ventas\n" + Loteria + " " + Horario + "\nFecha: " + fecha + "/" + mes + "/" + anio + ":\n\n#############################";//Aqui se guarda el reporte que se va a imprimir

                    String linea = br24.readLine();//Se lee la primera linea del archivo
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    int counter = 0;
                    String linea_tempo = "";
                    while (linea != null) {
                        String[] split = linea.split("      ");//Se separa el monto del numero guardado.
                        if (Integer.parseInt(split[1]) != 0) {
                            total = total + Integer.parseInt(split[1]);

                            String separacion_str = " ";
                            int separacion = 5 - split[1].length();
                            for (int i = 0; i < separacion; i++) {
                                separacion_str = separacion_str + " ";
                            }
                            String ord_desord = "";
                            if (split[3].equals("Orden")) {
                                ord_desord = "Ord.";
                            } else if (split[3].equals("Desorden")) {
                                ord_desord = "Des.";
                            } else {
                                //Do nothing.
                            }
                            counter++;
                            String helper = "";
                            if (split[0].length() == 1) {
                                helper = "00" + split[0];
                            } else if (split[0].length() == 2) {
                                helper = "0" + split[0];
                            } else if (split[0].length() == 3) {
                                helper = split[0];
                            } else {
                                //Do nothing. Nunca deberia llegar aqui.
                            }
                            if (counter == 1) {
                                linea_tempo = helper + separacion_str + split[1] + " " + ord_desord + "|";
                            } else if (counter == 2) {
                                linea_tempo = linea_tempo + helper + separacion_str + split[1] + " " + ord_desord;
                                counter = 0;
                            }
                            if (counter == 0) {
                                contenido = contenido + "\n" + linea_tempo;
                            }
                        }
                        linea = br24.readLine();
                    }
                    if (counter == 1) {
                        contenido = contenido + "\n" + linea_tempo;
                    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    contenido = contenido + "\n#############################\n\n#############################\n Total:  " + String.valueOf(total) + " colones.\n#############################";
                    contenido = contenido + "\n\n\n\n";
                    br24.close();
                    archivo24.close();
                } catch (IOException e) {
                }

                printIt(contenido);
                guardar_pdf();

                //Al final, se elimina el archivo temporal del reporte:
                //borrar_archivo(Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt");
                //ACTIVAR LAS LINEAS SIGUIENTES CUANDO HALLA ROLLO.
                //Esto queda para el final...
                //Intent rep = new Intent(this, ReportesActivity.class);
                //startActivity(rep);
                //finish();
                //System.exit(0);
                llenar_spinner_hor();
            } else {
                Toast.makeText(this, "No se encontraron ventas para " + Loteria + " " + Horario + ".\nVerifique y/o llame a soporte tecnico.\nTelefono: 85258108", Toast.LENGTH_LONG).show();
            }

        } else {
            if (ArchivoExiste(archivos, Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt")) {//Archivo nombre_archivo es el archivo contable

                try {
                    InputStreamReader archivo24 = new InputStreamReader(openFileInput(Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt"));//Se abre archivo
                    BufferedReader br24 = new BufferedReader(archivo24);
                    contenido = "Reporte de ventas\n" + Loteria + " " + Horario + "\nFecha: " + fecha + "/" + mes + "/" + anio + ":\n\n#############################";//Aqui se guarda el reporte que se va a imprimir

                    String linea = br24.readLine();//Se lee la primera linea del archivo
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    HashMap<String, String> vendi = new HashMap<String, String>();//Aqui se van a almacenar de manera temporal las ventas.
                    while (linea != null) {//En este while se leen todas las ventas del dia
                        String[] split = linea.split("      ");//Se separa el monto del numero guardado.
                        if (Integer.parseInt(split[1]) != 0) {//Si tiene algun valor distinto de cero significa que se han vendido tiquetes correspondientes a ese numero.
                            total = total + Integer.parseInt(split[1]);//Se almacena la suma de las ventas total.
                            //TODO NOW!!! Ordenar los numeros en ascendente. 9/7/2022 DONE!!!
                            vendi.put(split[0], split[1]);//key: numero, value: monto

                        }
                        linea = br24.readLine();
                    }//Aqui termina el while!!!

                    //Aqui haremos el acomodo de los numeros!!!
                    int cosciente = 0;
                    cosciente = (vendi.size())/3;//Numero entero de datos por columna (3 columnas)
                    int modulo = 0;
                    modulo = ((vendi.size())%3);//Resto de la division (modulo).
                    //creamos un tree map
                    TreeMap<String, String> treeMap = new TreeMap<String, String>();
                    treeMap.putAll(vendi);

                    int colum_size1, colum_size2, colum_size3;//Tamanio de cada columna

                    if (modulo == 1) {
                        colum_size1 = cosciente + 1;
                        colum_size2 = cosciente;
                        colum_size3 = cosciente;
                    } else if (modulo == 2) {
                        colum_size1 = cosciente + 1;
                        colum_size2 = cosciente + 1;
                        colum_size3 = cosciente;
                    } else {
                        colum_size1 = cosciente;
                        colum_size2 = cosciente;
                        colum_size3 = cosciente;
                    }

                    //Crear 3 hashmaps.
                    HashMap<String, String> hm1 = new HashMap<String, String>();
                    HashMap<String, String> hm2 = new HashMap<String, String>();
                    HashMap<String, String> hm3 = new HashMap<String, String>();

                    //int param_for = 0;//Se usa como elemento de control de los ciclos for siguientes:
                    for (int param_for = 0; param_for < colum_size1; param_for++) {
                        for (String key : treeMap.keySet()) {
                            hm1.put(key, treeMap.get(key));
                            treeMap.remove(key);
                            break;
                        }
                    }

                    for (int param_for = 0; param_for < colum_size2; param_for++) {
                        for (String key : treeMap.keySet()) {
                            hm2.put(key, treeMap.get(key));
                            treeMap.remove(key);
                            break;
                        }
                    }

                    for (int param_for = 0; param_for < colum_size3; param_for++) {
                        for (String key : treeMap.keySet()) {
                            hm3.put(key, treeMap.get(key));
                            treeMap.remove(key);
                            break;
                        }
                    }

                    //creamos tres tree maps
                    TreeMap<String, String> treeMap1 = new TreeMap<String, String>();
                    treeMap1.putAll(hm1);
                    TreeMap<String, String> treeMap2 = new TreeMap<String, String>();
                    treeMap2.putAll(hm2);
                    TreeMap<String, String> treeMap3 = new TreeMap<String, String>();
                    treeMap3.putAll(hm3);
                    int counter = 0;
                    String linea_tempo = "";
                    String contenido_temp = "";
                    for (String key : treeMap1.keySet()) {
                        String separacion_str = "";
                        int separacion = 6 - treeMap1.get(key).length();
                        for (int i = 0; i < separacion; i++) {
                            separacion_str = separacion_str + " ";
                        }
                        counter++;
                        linea_tempo = key + separacion_str + treeMap1.get(key) + "| ";
                        for (String key2 : treeMap2.keySet()) {
                            String separacion_str2 = "";
                            int separacion2 = 6 - treeMap2.get(key2).length();
                            for (int i = 0; i < separacion2; i++) {
                                separacion_str2 = separacion_str2 + " ";
                            }
                            linea_tempo = linea_tempo + key2 + separacion_str2 + treeMap2.get(key2) + "| ";
                            treeMap2.remove(key2);
                            for (String key3 : treeMap3.keySet()) {
                                String separacion_str3 = "";
                                int separacion3 = 6 - treeMap3.get(key3).length();
                                for (int i = 0; i < separacion3; i++) {
                                    separacion_str3 = separacion_str3 + " ";
                                }
                                linea_tempo = linea_tempo + key3 + separacion_str3 + treeMap3.get(key3);
                                treeMap3.remove(key3);
                                counter = 0;
                                break;
                            }
                            break;
                        }
                        if (counter == 0) {
                            //linea = linea_tempo;
                            contenido_temp = contenido_temp + "\n" + linea_tempo;
                        }
                    }
                    contenido = contenido + contenido_temp;

                    if (counter == 1) {
                        contenido = contenido + "\n" + linea_tempo;
                    } else if (counter == 2) {
                        contenido = contenido + "\n" + linea_tempo;
                    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    contenido = contenido + "\n#############################\n\n#############################\n Total:  " + String.valueOf(total) + " colones.\n#############################";
                    contenido = contenido + "\n\n\n\n";
                    br24.close();
                    archivo24.close();
                } catch (IOException e) {
                }

                printIt(contenido);
                guardar_pdf();

                //Al final, se elimina el archivo temporal del reporte:
                //borrar_archivo(Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt");
                //ACTIVAR LAS LINEAS SIGUIENTES CUANDO HALLA ROLLO.
                //Esto queda para el final...
                //Intent rep = new Intent(this, ReportesActivity.class);
                //startActivity(rep);
                //finish();
                //System.exit(0);
                llenar_spinner_hor();
            } else {
                Toast.makeText(this, "No se encontraron ventas para " + Loteria + " " + Horario + ".\nVerifique y/o llame a soporte tecnico.\nTelefono: 85258108", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void guardar_pdf(){
        //Respaldo digital del reporte contable.
    }

    private void borrar_archivo(String s){
        //Se borra el archivo contable.
    }

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

    private String get_impresora() {
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

    private void llenar_spinner_hor() {
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, horarios);
        horario.setAdapter(adapter2);
    }

    private void crear_array_horarios(){
        //String archivos[] = fileList();
        int a = 0;
        for (String i : loter.keySet()) {
            if (loter.get(i).equals("true")){
                a++;
            }
        }
        horarios = new String[a+1];
        a = 0;
        horarios[0] = "Escoja el horario...";
        for (String i : loter.keySet()) {

            if (loter.get(i).equals("true")){
                horarios[a+1] = i;
                a++;
            }
        }
    }

    private void crearDiccionario() {

        String archivos[] = fileList();
        //String ArchivoCompleto = "";//Aqui se lee el contenido del archivo guardado.
        String selec_lot = loteria.getSelectedItem().toString();
        String archivo_leido = "";
        String matcheo = selec_lot + "_sfile";
        for (int i = 0; i < archivos.length; i++) {
            Pattern pattern = Pattern.compile(matcheo, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound) {
                archivo_leido = archivos[i];
            }
        }

        if (ArchivoExiste(archivos, archivo_leido)) {
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(archivo_leido));
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();

                while (linea != null) {
                    String[] split = linea.split("  ");
                    loter.put(split[0], split[1]);
                    //ArchivoCompleto = ArchivoCompleto + linea + "\n";
                    linea = br.readLine();
                }
                //ArchivoCompleto = ArchivoCompleto + new_line + "\n";
                br.close();
                archivo.close();
            } catch (IOException e) {
            }
        }
    }

    private boolean ArchivoExiste (String archivos [],String Tiquete){
        for (int i = 0; i < archivos.length; i++)
            if (Tiquete.equals(archivos[i]))
                return true;
        return false;
    }
}
