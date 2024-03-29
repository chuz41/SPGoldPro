package com.example.spgold;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FacturaseditActivity extends AppCompatActivity {

    private TextView textView_facturas;
    private TextView textView_esperar;
    private Button button_cambiar_fecha;
    private Button button_borrar_factura;
    private Button button_reimprimir;
    //private Button button_archivos;
    //private TextView tv_aux;
    //private Button boton_aux;
    private HashMap<Integer, JSONObject> abajos = new HashMap<Integer, JSONObject>();
    //private HashMap<String, String> abajos2 = new HashMap<String, String>();
    private TextView textView_fecha;
    private EditText editText_listar_facturas;
    private EditText edit_Text_numero_factura;
    private String addRowURL = "https://script.google.com/macros/s/AKfycbweyYb-DHVgyEdCWpKoTmvOxDGXleawjAN8Uw9AeJYbZ24t9arB/exec";
    private int contador;
    private String Loteria;
    private String tipo_lot;
    private String Horario;
    private String dia;
    private String mes;
    private String anio;
    private String mes_selectedS;
    private String anio_selectedS;
    private String fecha_selectedS;
    private int mes_selected;
    private int anio_selected;
    private int fecha_selected;
    private String hora_completa;
    private String hora;
    private String minuto;
    private String Paga1;
    private String Paga2;
    private String num_factura;
    private String player;
    private String fecha;
    private String total;
    private String dispositivo;
    private Map<String, Integer> meses = new HashMap<String, Integer>();
    private Map<Integer, String> numero_de_facturas = new HashMap<Integer, String>();
    private boolean flag_cad = false;
    private String SPREADSHEET_ID;
    private String SHEET;
    private String facturas_diarias = "facturas_diarias.txt";
    private String historial_facturas = "historial_facturas.txt";
    private String contabilidad = "contabilidad.txt";
    private String nombre_dia;

    /*@Override
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
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facturasedit);

        textView_facturas = (TextView) findViewById(R.id.textView_facturas);
        textView_fecha = (TextView) findViewById(R.id.textView_fecha_facturas);
        editText_listar_facturas = (EditText) findViewById(R.id.editText_listar_facturas);
        edit_Text_numero_factura = (EditText) findViewById(R.id.edit_Text_numero_factura);
        editText_listar_facturas.setFocusableInTouchMode(false);
        dispositivo = check_device();
        //boton_aux = (Button) findViewById(R.id.buttonauxi1);
        //tv_aux = (TextView) findViewById(R.id.textView_aux);
        //tv_aux.setVisibility(View.INVISIBLE);
        button_cambiar_fecha = (Button) findViewById(R.id.button_cambiar_fecha);
        button_borrar_factura = (Button) findViewById(R.id.button_borrar_factura);
        button_reimprimir = (Button) findViewById(R.id.button_reimprimir);
        textView_esperar = (TextView) findViewById(R.id.textView_esperar);
        //button_archivos = (Button) findViewById(R.id.button_archivos);
        textView_esperar.setVisibility(View.INVISIBLE);

        llenar_mapa_meses();


        //###########################################################################################

        Date now = Calendar.getInstance().getTime();
        String ahora = now.toString();
        //se separan los campos de la fecha y hora para verificar que si se pueda realizar la venta.
        separar_fechaYhora(ahora);

        mes_selected = Integer.parseInt(mes);
        anio_selected = Integer.parseInt(anio);
        fecha_selected = Integer.parseInt(fecha);

        mes_selectedS = mes;
        anio_selectedS = anio;
        fecha_selectedS = fecha;

        textView_fecha.setText(fecha + "/" + String.valueOf(meses.get(mes)) + "/" + anio);

        //###########################################################################################

        ////////////////////////////////////////////////////////////////////////////////////
        cargar_facturas(fecha_selected, mes_selected, anio_selected);

        textView_facturas.setText("Lista de facturas del dia: ");

    }

    public void archivos(View view) {
        String archivos[] = fileList();
        String ver = "";
        for (int i = 0; i < archivos.length; i++) {
            ver = ver + archivos[i] + "\n";
        }
    }

    private void mostrar_todo() {
        textView_esperar.setText("");
        textView_esperar.setVisibility(View.INVISIBLE);
        textView_facturas.setVisibility(View.VISIBLE);
        textView_fecha.setVisibility(View.VISIBLE);
        editText_listar_facturas.setVisibility(View.VISIBLE);
        edit_Text_numero_factura.setVisibility(View.VISIBLE);
        //boton_aux.setVisibility(View.VISIBLE);
        //tv_aux.setVisibility(View.VISIBLE);
        button_cambiar_fecha.setVisibility(View.VISIBLE);
        button_borrar_factura.setVisibility(View.VISIBLE);
        button_reimprimir.setVisibility(View.VISIBLE);
        //button_archivos.setVisibility(View.VISIBLE);
    }

    private void ocultar_todo() {
        textView_esperar.setVisibility(View.VISIBLE);
        textView_esperar.setText("   Conectando...\n\nPor favor espere...");
        textView_facturas.setVisibility(View.INVISIBLE);
        textView_fecha.setVisibility(View.INVISIBLE);
        editText_listar_facturas.setVisibility(View.INVISIBLE);
        edit_Text_numero_factura.setVisibility(View.INVISIBLE);
        //boton_aux.setVisibility(View.INVISIBLE);
        //tv_aux.setVisibility(View.INVISIBLE);
        button_cambiar_fecha.setVisibility(View.INVISIBLE);
        button_borrar_factura.setVisibility(View.INVISIBLE);
        button_reimprimir.setVisibility(View.INVISIBLE);
        //button_archivos.setVisibility(View.INVISIBLE);
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

    public void aux_button(View v) {

        textView_facturas.setVisibility(View.INVISIBLE);
        textView_fecha.setVisibility(View.INVISIBLE);
        editText_listar_facturas.setVisibility(View.INVISIBLE);
        edit_Text_numero_factura.setVisibility(View.INVISIBLE);
        //boton_aux.setVisibility(View.INVISIBLE);
        //tv_aux.setVisibility(View.VISIBLE);
        button_cambiar_fecha.setVisibility(View.INVISIBLE);
        button_borrar_factura.setVisibility(View.INVISIBLE);
        button_reimprimir.setVisibility(View.INVISIBLE);

    }

    private void obtener_Json_otras_facturas() throws JSONException {

        HashMap<String, String> abajos3 = new HashMap<String, String>();
        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("facturas_online.txt"));
            //imprimir_archivo("facturas_online.txt");
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            //String contenido = "";
            //abajos2.clear();
            Integer countercito = 0;
            boolean flag = true;

            while (linea != null) {
                countercito++;
                String count = String.valueOf(countercito);
                String[] split = linea.split(" ");
                if (split[0].equals("abajo")) {
                    Log.v("OJOF_abajo: ", "\n\nLinea: " + linea + " Fin de linea!!!");
                    abajos3.put(count, split[1]);
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
                return;
            } else {
                //Do nothing. Continue with the work
            }

            archivo.close();
            br.close();

        } catch (IOException e) {
        }

        abajiar(abajos3);
    }

    private void abajiar(HashMap<String, String> abajos3) throws JSONException {

        ocultar_todo();
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (String key : abajos3.keySet()) {
            JSONObject objeto_json = new JSONObject();
            String SSHHEETT = "";
            String SSPPRREEAADDSSHHEETT = "";
            String tipo_lot = "";
            String factura = "";
            String file = abajos3.get(key);
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(facturas_diarias));
                //imprimir_archivo("facturas_online.txt");
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();

                while (linea != null) {
                    String[] split = linea.split("      ");
                    if (file.equals(split[3])) {
                        String[] split_name = file.split("_separador_");
                        Log.v("ERROR9001_facturas", ".\n\nLinea leida:\n" + linea + "\n\n.");
                        if (linea.equals("BORRADA")) {
                            Log.v("abajiar_linea_borrada", "\n\nLinea: " + linea + "\n\n.");
                            //Do nothing.
                        } else if (linea.isEmpty()) {
                            Log.v("abajiar_linea_empty", "\n\nLinea: " + linea + "\n\n.");
                            //Do nothing.
                        } else if (split_name[14].equals("equi.txt")) {
                            Log.v("abajiar_nombre_equi.txt", "\n\nLinea: " + linea + "\n\n.");
                            tipo_lot = split_name[9];
                            SSPPRREEAADDSSHHEETT = split[1];
                            SSHHEETT = split[2];
                            factura = split_name[6];
                            //file = abajos2.get(key);
                            Log.v("Errequilibrar_facturas", "\n\nFinal del nombre del archivo: " + split_name[14] + "\n\nTipo loteria: " + tipo_lot + "\nSpreadSheet: " + SSPPRREEAADDSSHHEETT + "\nSheet: " + SSHHEETT + "\nFactura numero: " + factura + "file: " + file);
                            br.close();
                            archivo.close();
                            equilibrar(SSPPRREEAADDSSHHEETT, SSHHEETT, file, factura, tipo_lot, key, abajos3);
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
                            Log.v("Error9003_facturas", ".\n\nTipo loteria: " + tipo_lot + "\nSpreadSheet: " + SSPPRREEAADDSSHHEETT + "\nSheet: " + SSHHEETT + "\nFactura numero: " + factura + "\nfile: " + file);
                            abajos3.remove(key);
                            br.close();
                            archivo.close();
                            subir_factura_resagadas(objeto_json, "nothing", abajos3);
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
    }

    private void equilibrar(String SpreadSheet, String Sheet, String file, String factura, String tipo_lot, String key, HashMap<String, String> abajos3) throws IOException {//Este metodo revisa si se ha subido parte del tiquete a la nube.

        boolean flag = true;
        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput(facturas_diarias));
            //imprimir_archivo("facturas_online.txt");
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            while (linea != null) {
                String[] splito = linea.split("      ");
                String[] split = splito[3].split("_separador_");
                //                       0                           1                         2                        3                        4                       5                         6                       7                     8                       9                         10                     11                        12                          13                   14
                //String tempFile = jugador_act + "_separador_" + Loteria + "_separador_" + Horario + "_separador_" + fecha + "_separador_" + hoora + "_separador_" + miinuto + "_separador_" + factura + "_separador_" + dia + "_separador_" + mes + "_separador_" + tipo_lot + "_separador_" + Paga1 + "_separador_" + Paga2 + "_separador_" + monto_venta + "_separador_" + anio + "_separador_null.txt";
                int facturita = (Integer.parseInt(split[6]) * (-1)) ;
                Log.v("equilibrar_iterar_file", "...................\n\nfacturita: " + facturita + "\nfactura: " + factura + "\n\n.......................");
                if (facturita == Integer.parseInt(factura)) {//TODO: TODO LO QUE SIGUE ESTA MALO --> //Aplica para cualquier archivo, ya sea ..._equi.txt o ..._null.txt. Si cualquiera de estos aparece es que ya ha pasado por equilibrar.
                    if (split[14].equals("null.txt")) {      // ARCHIVOS ...equi.txt no se toman en cuenta.
                        flag = false;//Significa que ya se ha creado un archivo con el new_name.
                        break;
                    } else {
                        //Do nothing. Continue!
                    }
                } else {
                    //Do nothing. Continue!
                }
                linea = br.readLine();
            }
            br.close();
            archivo.close();
        } catch (IOException e) {}
        if (flag) {
            RequestQueue requestQueue;//Se llama a la SpreadSheet que contiene la loteria actual para verificar que no hay errores en la subida de datos. Usar: Method.get
            // Instantiate the cache
            Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

            // Set up the network to use HttpURLConnection as the HTTP client.
            Network network = new BasicNetwork(new HurlStack());

            // Instantiate the RequestQueue with the cache and network.
            requestQueue = new RequestQueue(cache, network);

            // Start the queue
            requestQueue.start();

            String readRowURL = "https://script.google.com/macros/s/AKfycbxJNCrEPYSw8CceTwPliCscUtggtQ2l_otieFmE/exec?spreadsheetId=" + SpreadSheet + "&sheet=" + Sheet;

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
                                    } else if (tipo_lot.equals("Parley")) {
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
                                    abajos3.remove(key);
                                    //cambiar_bandera(String.valueOf(factur), "equi");
                                    Log.v("Error9003_facturas", "\n\nTipo loteria: " + tipo_lot + "\nSpreadSheet: " + SpreadSheet + "\nSheet: " + Sheet + "\nFactura numero: " + factura + "file: " + new_name);
                                    try {
                                        subir_factura_resagadas(objeto_json, "equi", abajos3);
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
        } else {
            Log.v("Equilibrar_repetido", ".............................\n\nNo se genera ningun archivo new_name debido a que este ya ha sido creado!\n\n.............................");
        }
    }

    private void subir_factura_resagadas(JSONObject jsonObject, String tag, HashMap<String, String> abajos3) throws JSONException {
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
                                    abajiar(abajos3);
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
                    if (tag.equals("nada")) {
                        //Do nothing.
                    } else {
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
            crear_archivo("facturas_online.txt");
            //imprimir_archivo("facturas_online.txt");
            guardar(contenido, "facturas_online.txt");//Aqui se eliminan las lineas que corresponden a archivos que ya se han subido.
            Log.v("cambiar_band_result", "\n\nArchivo \"facturas_online.txt\":\n\n" + imprimir_archivo("facturas_online.txt"));
            //imprimir_archivo("facturas_online.txt");

        } catch (IOException e) {
        }
    }

    public void borrar_archivo(String file) {
        File archivo = new File(file);
        String empty_string = "";
        guardar(empty_string, file);
        archivo.delete();
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

                int largo_split = split.length;
                Log.v("Ver splites", ".\n\nVer splites: ");
                for (int i = 0; i < largo_split; i++) {
                    Log.v("split[" + i + "]", split[i]);
                }

                if (split[3].equals(file)) {
                    String information = split[0];
                    Log.v("Information", ".\n\nInformatin: " + information + "\n\n.");
                    if (information.equals("BORRADA")) {
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

    private void subir_facturas_resagadas() throws JSONException {
        boolean flag_internet = verificar_internet();
        //JSONObject objeto_Json_a_subir = null;
        if (flag_internet) {
            obtener_Json_otras_facturas();
        } else {
        }
    }

    public void cambiar_fecha(View view) {

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, i, i1, i2) -> {
            textView_fecha.setText(String.valueOf(i2) + "/" + String.valueOf(i1+1) + "/" + String.valueOf(i));
            mes_selected = i1+1;
            anio_selected = i;
            fecha_selected = i2;

            mes_selectedS = String.valueOf(mes_selected);
            anio_selectedS = String.valueOf(anio_selected);
            fecha_selectedS = String.valueOf(fecha_selected);

            flag_cad = true;
            cargar_facturas(fecha_selected, mes_selected, anio_selected);



        },anio_selected,mes_selected-1,fecha_selected);


        datePickerDialog.show();
        cargar_facturas(fecha_selected, mes_selected, anio_selected);

    }

    private void separar_fechaYhora(String ahora) {
        String[] split = ahora.split(" ");
        nombre_dia = split[0];
        mes = String.valueOf(meses.get(split[1]));
        anio = split[5];
        fecha = split[2];
        dia = split[2];
        hora = split[3];
        split = hora.split(":");
        minuto = split[1];
        hora = split[0];
    }

    /*Personalizacion de la navegacion hacia atras!!
    #################################################################################################*/
    @Override
    public void onBackPressed(){
        boton_atras();
    }

    private void boton_atras() {
        Intent Main = new Intent(this, ReportesActivity.class);
        startActivity(Main);
        finish();
        System.exit(0);
    }
    //#################################################################################################

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

    private void cargar_facturas(int diaf, int mesf, int aniof) {
        /*try {
            subir_facturas_resagadas();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        try {

            InputStreamReader archivo = new InputStreamReader(openFileInput("invoice.txt"));//Se abre archivo de facturas
            BufferedReader br = new BufferedReader(archivo);
            String linea_imprimir = "";//Aqui se lee el contenido del tiquete guardado.
            String linea = br.readLine();//Se lee archivo de facturacion
            String[] split_cont = linea.split(" ");
            contador = Integer.parseInt(split_cont[1]);
            //imprimir_archivo("invoice.txt");

            //Toast.makeText(this, "primera linea del archivo de facturas (Contador): " + linea + "\n\nContador = " + String.valueOf(contador), Toast.LENGTH_LONG).show();
            linea = br.readLine();//Se lee la siguiente linea que corresponde a la primera factura.
            numero_de_facturas.clear();
            while (linea != null) {
                if (linea.isEmpty()) {
                    //Toast.makeText(this, "Ultima linea que esta vacia. ", Toast.LENGTH_LONG).show();
                    //Do nothing.
                } else {
                    Log.v("rgar_facturas", ".\n\narchivo:\n\n" + imprimir_archivo("invoice.txt"));
                    String[] split = linea.split(" ");//Se separa el numero de factura del nombre del archivo.
                    String[] split2 = split[1].split("_separador_");//Se separan las partes del nombre del archivo.

//Nombre del archivo: jugador_act + "_separador_" + Loteria + "_separador_" + Horario + "_separador_" + fecha + "_separador_" + hora + "_separador_" + minuto + "_separador_" + consecutivo_str + "_separador_" + dia + "_separador_" + mes + "_separador_" + tipo_lot + "_separador_" + Paga1 + "_separador_" + Paga2 + "_separador_" + monto_venta + "_separador_" + anio + "_separador_null.txt";
//                      split2[0]                  split2[1]                  split2[2]                 split2[3]               split2[4]              split2[5]                    split2[6]                    split2[7]              split2[8]              split2[9]                split2[10]               split2[11]             split2[12]                    split2[13]

                    Loteria = split2[1];
                    Horario = split2[2];
                    dia = split2[3];
                    mes = String.valueOf(meses.get(split2[8]));
                    tipo_lot = split2[9];
                    Paga1 = split2[10];
                    Paga2 = split2[11];
                    anio = split2[13];

                    Log.v("car_facts_params", ".\n\nLoteria: " + Loteria + "\nHorario: " + Horario + "\nDia: " + dia + "\ndiaf: " + diaf + "\nmes: " + mes + "\nmesf: " + mesf + "\nanio: " + anio + "\naniof: " + aniof + "\n\n.");

                    if (Integer.parseInt(dia) == diaf) {
                        if (Integer.parseInt(mes) == mesf) {
                            if (Integer.parseInt(anio) == aniof) {
                                if (verificar_borrada(split[1])) {
                                    linea_imprimir = linea_imprimir + "factura # " + split[0] + "   BORRADA\n";
                                    Log.v("linea_imprimir if", ".\n\nLinea:\n\n" + linea_imprimir);
                                } else {
                                    String Cliente_adap = split2[0];
                                    Cliente_adap = Cliente_adap.replace("x_x"," ");
                                    linea_imprimir = linea_imprimir + "factura #  " + split[0] + "   " + split2[1] + " " + split2[2] + ".  Cliente: " + Cliente_adap + "\n";
                                    Log.v("linea_imprimir else", ".\n\nLinea:\n\n" + linea_imprimir);
                                }
                                numero_de_facturas.put(Integer.parseInt(split[0]), "true");
                            }
                        }
                    }
                }
                linea = br.readLine();
            }
            br.close();
            archivo.close();
            editText_listar_facturas.setText(linea_imprimir);
        }catch (IOException e) {
        }
    }

    private boolean verificar_borrada(String file) {
        boolean flag = false;
        String archivos[] = fileList();
        Log.v("verificar_borradas", ".\n\nFile: " + file + "\n\nContenido del archivo:\n\n" + imprimir_archivo(facturas_diarias) + "\n\n.");
        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput(facturas_diarias));
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();//Se lee archivo
            while (linea != null) {
                String[] split = linea.split("      ");
                if (split[3].equals(file)) {
                    if (split[0].equals("BORRADA")) {
                        flag = true;
                        Log.v("verif_borrad_BORRADA", ".\n\nFlag: " + flag + "\n\n.");
                    } else {
                        Log.v("verif_borrad_NO_borrada", ".\n\nFlag: " + flag + "\n\n.");
                    }
                } else {
                    //Do nothing.
                }
                linea = br.readLine();
            }

            br.close();
            archivo.close();
        } catch (IOException e) {}
        return flag;
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

                br.close();
                archivo.close();
            } catch (IOException e) {
            }
        }
        return contenido;
    }

    private boolean ArchivoExiste (String[] archivos, String Tiquet){
        for (int i = 0; i < archivos.length; i++) {

            if (Tiquet.equals(archivos[i])) {
                return true;
            }
        }
        return false;
    }

    public void guardar (String Tcompleto, String nombre){
        try {
            File file = new File(nombre);
            file.delete();
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre, Activity.MODE_PRIVATE));
            archivo.write("");
            archivo.write(Tcompleto);
            archivo.flush();
            //br.close();
            archivo.close();
        } catch (IOException e) {
        }
    }

    private void contar(String monto_act, String numero_act, String archivo_contable) {
        int valor = Integer.parseInt(monto_act);//Se parcea el monto del numero jugado.
        String archivos[] = fileList();
        if (ArchivoExiste(archivos, archivo_contable)) {//nombre del archivo CONTABle del dia
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(archivo_contable));//Se abre archivo contable
                BufferedReader br = new BufferedReader(archivo);
                String TiqueteContable = "";//Aqui se lee el contenido del tiquete guardado.
                String linea = br.readLine();//Se lee archivo contable
                while (linea != null) {
                    Log.v("contar", ".\n\nLinea:\n\n" + linea + "\n\n.");
                    String[] split = linea.split("      ");//Se separa el monto del numero guardado.
                    if (Integer.parseInt(split[2]) != Integer.parseInt(fecha)) {
                        linea = split[0] + "      0      " + fecha;
                        TiqueteContable = TiqueteContable + linea + "\n";
                    } else {
                        if (Integer.parseInt(split[0]) == Integer.parseInt(numero_act)) {
                            int monto_numero = Integer.parseInt(split[1]);
                            //monto_numero = (monto_numero * (-1));
                            monto_numero = monto_numero + valor;
                            linea = split[0] + "      " + String.valueOf(monto_numero) + "      " + fecha;
                        }
                        TiqueteContable = TiqueteContable + linea + "\n";
                    }
                    linea = br.readLine();
                }
                br.close();
                archivo.close();
                Log.v("contar_cont_file", ".\n\nArchivo contable antes de contar:\n\n" + imprimir_archivo(archivo_contable) + "\n\n.");
                borrar_archivo(archivo_contable);
                crear_archivo(archivo_contable);
                guardar(TiqueteContable, archivo_contable);
                Log.v("contar_cont_file", ".\n\nArchivo contable despues de contar:\n\n" + imprimir_archivo(archivo_contable) + "\n\n.");
            }catch (IOException e) {
            }
        }
        else {
            Toast.makeText(this, "ERROR!!!\nArchivo contable no existe!!!", Toast.LENGTH_LONG).show();
        }
    }

    private void contar_monazos(String monto_act, String numero_act, String archivo_contable, String desord_ord) {

        boolean flag_cont = true;//Bandera que ayuda a actualizar el archivo contable sin errores.

        int valor = Integer.parseInt(monto_act);     //Se parcea el monto del numero jugado.
        int num = Integer.parseInt(numero_act);

        //imprimir_archivo(Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt");
        String archivos[] = fileList();
        if (ArchivoExiste(archivos, archivo_contable)) {//nombre del archivo CONTABle del dia
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(archivo_contable));//Se abre archivo contable
                BufferedReader br = new BufferedReader(archivo);
                String TiqueteContable = "";//Aqui se lee el contenido del tiquete guardado.

                String linea = br.readLine();//Se lee archivo contable
                while (linea != null) {
                    Log.v("contar_mona", ".\n\nLinea:\n\n" + linea + "\n\n.");
                    String ord_desord_paga = "";
                    if (desord_ord.equals("Orden")) {
                        ord_desord_paga = Paga1;
                    } else if (desord_ord.equals("Desorden")) {
                        ord_desord_paga = Paga2;
                    } else {
                        Toast.makeText(this, "Debugueo: Nunca deberia estar aqui\n*\n*\n*", Toast.LENGTH_LONG).show();
                        //Do nothing. Se supone que nunca entraria aqui!!!
                    }
                    String[] split = linea.split("      ");//Se separa el monto del numero, del tipo de juego (orden o desorden) y el numero jugado.
                    //Log.v("borrando ERR 0", ".\n\nLinea:\n\n" + linea + "\nNumero a modificar: " + num + "\n\n.");
                    //Toast.makeText(this, "Debugueo (linea): " + linea + "\n\nSplit 1: " + split[0] + "\nSplit 2: " + split[1] + "\nSplit 3: " + split[2] + "\nSplit 4: " + split[3] + "\n\n", Toast.LENGTH_LONG).show();
                    if (Integer.parseInt(split[4]) != Integer.parseInt(fecha)) {
                        linea = split[0] + "      0      " + split[2] + "      " + split[3] + "      " + fecha;
                        TiqueteContable = TiqueteContable + linea + "\n";
                    } else {
                        if (Integer.parseInt(split[0]) == num) {
                            Log.v("Primera_coinsi borrar", ".\n\nsplit[0]: " + split[0] + "\nNumero: " + num + "\nsplit[1]: " + split[1] + "\ndesord_ord: " + desord_ord + "\n\n.");
                            //Toast.makeText(this, "Debugueo primera coinsidencia (linea): \n\n" + linea, Toast.LENGTH_LONG).show();
                            if (split[3].equals(desord_ord)) {
                                Log.v("Segunda_coinsi borrar", ".\n\nsplit[3]: " + split[3] + "\ndesord_ord: " + desord_ord + "\n\n.");
                                //Toast.makeText(this, "Debugueo segunda coinsidencia (linea): \n\n" + linea, Toast.LENGTH_LONG).show();
                                flag_cont = false;
                                int monto_numero = Integer.parseInt(split[1]);
                                monto_numero = monto_numero + valor;
                                linea = String.valueOf(num) + "      " + String.valueOf(monto_numero) + "      " + ord_desord_paga + "      " + desord_ord + "      " + fecha;
                                TiqueteContable = TiqueteContable + linea + "\n";
                            } else {
                                TiqueteContable = TiqueteContable + linea + "\n";
                            }
                        } else {
                            TiqueteContable = TiqueteContable + linea + "\n";
                        }
                    }
                    linea = br.readLine();
                    //Toast.makeText(this, "Debugueo leyendo la segunda linea (linea): \n\n" + linea, Toast.LENGTH_LONG).show();
                }
                br.close();
                archivo.close();
                Log.v("contar_mona_cont_file", ".\n\nArchivo contable antes de contar_monazos:\n\n" + imprimir_archivo(archivo_contable) + "\n\n.");
                borrar_archivo(archivo_contable);
                crear_archivo(archivo_contable);
                guardar(TiqueteContable, archivo_contable);
                Log.v("contar_mona_cont_file", ".\n\nArchivo contable despues de contar_monazos:\n\n" + imprimir_archivo(archivo_contable) + "\n\n.");
            } catch (IOException e) {
            }
        }

        if (ArchivoExiste(archivos, archivo_contable)) {//nombre del archivo CONTABle del dia
            if (flag_cont) {
                Log.v("MAL_FUNCIONAMIENTO", ".\n\n**ERROR** NUNCA DEBERIA ENTRAR AQUI!!! **ERROR**\n\n.");
                try {
                    InputStreamReader archivo = new InputStreamReader(openFileInput(archivo_contable));//Se abre archivo contable
                    BufferedReader br = new BufferedReader(archivo);
                    String TiqueteContable = "";//Aqui se lee el contenido del tiquete guardado.

                    String linea = br.readLine();//Se lee archivo contable
                    while (linea != null) {
                        String[] split = linea.split("      ");
                        if (Integer.parseInt(split[4]) != Integer.parseInt(fecha)) {
                            linea = split[0] + "      0      " + split[2] + "      " + split[3] + "      " + fecha;
                            TiqueteContable = TiqueteContable + linea + "\n";
                        } else {
                            TiqueteContable = TiqueteContable + linea + "\n";
                        }
                        linea = br.readLine();
                    }
                    br.close();
                    archivo.close();
                    String ord_desord_paga = "";
                    if (desord_ord.equals("Orden")) {
                        ord_desord_paga = Paga1;
                    } else if (desord_ord.equals("Desorden")) {
                        ord_desord_paga = Paga2;
                    } else {
                        //Do nothing. Se supone que nunca entraria aqui!!!
                    }
                    linea = String.valueOf(num) + "      " + String.valueOf(valor) + "      " + ord_desord_paga + "      " +  desord_ord + "      " + fecha;
                    TiqueteContable = TiqueteContable + linea + "\n";
                    Log.v("contar_mona_cont_file", ".\n\nArchivo contable antes de contar_monazos:\n\n" + imprimir_archivo(archivo_contable) + "\n\n.");
                    borrar_archivo(archivo_contable);
                    crear_archivo(archivo_contable);
                    guardar(TiqueteContable, archivo_contable);
                    Log.v("contar_mona_cont_file", ".\n\nArchivo contable despues de contar_monazos:\n\n" + imprimir_archivo(archivo_contable) + "\n\n.");
                } catch (IOException e) {
                }
            } else {
                Log.v("BUEN_FUNCIONAMIENTO", ".\n\nSe ha borrado MONAZOS!!!!\n\n.");
            }
        }
    }

    private void contar_parley(String monto_act, String numero1_act, String numero2_act, String archivo_contable) {

        boolean flag_cont = true;//Bandera que ayuda a actualizar el archivo contable sin errores.

        //Toast.makeText(this, "Parametros en contarParley:\n\nmonto_act: " + monto_act + "\nnumero1_act: " + numero1_act + "\nnumero2_act: " + numero2_act, Toast.LENGTH_LONG).show();

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
            return;
        }
        //imprimir_archivo(Loteria + "_" + Horario + "_" + dia + "_" + mes + ".txt");
        String archivos[] = fileList();
        if (ArchivoExiste(archivos, archivo_contable)) {//nombre del archivo CONTABle del dia
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(archivo_contable));//Se abre archivo contable
                BufferedReader br = new BufferedReader(archivo);
                String TiqueteContable = "";//Aqui se lee el contenido del tiquete guardado.
                String linea = br.readLine();//Se lee archivo contable
                while (linea != null) {
                    Log.v("contar_parley", ".\n\nLinea:\n\n" + linea + "\n\n.");
                    String[] split = linea.split("      ");//Se separa el monto de los numeros guardados.
                    //Toast.makeText(this, "Debugueo (linea): " + linea + "\n\nSplit 1: " + split[0] + "\nSplit 2: " + split[1] + "\nSplit 3: " + split[2] + "\n\n", Toast.LENGTH_LONG).show();
                    if (Integer.parseInt(split[3]) != Integer.parseInt(fecha)) {
                        linea = split[0] + "      " + split[1] + "      0      " + fecha;
                        TiqueteContable = TiqueteContable + linea + "\n";
                    } else {
                        if (Integer.parseInt(split[0]) == num1) {
                            Log.v("Debugueo_coinsidencia1", ".\n\nlinea: \n\n" + linea + "\n\n.");
                            if (Integer.parseInt(split[1]) == num2) {
                                Log.v("Debugueo_coinsidencia2", ".\n\nlinea: \n\n" + linea + "\n\n.");
                                flag_cont = false;
                                int monto_numero = Integer.parseInt(split[2]);
                                //monto_numero = (monto_numero * (-1));
                                monto_numero = monto_numero + valor;
                                linea = String.valueOf(num1) + "      " + String.valueOf(num2) + "      " + String.valueOf(monto_numero) + "      " + fecha;
                                TiqueteContable = TiqueteContable + linea + "\n";
                            } else {
                                TiqueteContable = TiqueteContable + linea + "\n";
                            }
                        } else {
                            TiqueteContable = TiqueteContable + linea + "\n";
                        }
                    }
                    linea = br.readLine();
                    //Toast.makeText(this, "Debugueo leyendo la segunda linea (linea): \n\n" + linea, Toast.LENGTH_LONG).show();
                }
                br.close();
                archivo.close();
                Log.v("contar_parley_cont_file", ".\n\nArchivo contable antes de contar_parley:\n\n" + imprimir_archivo(archivo_contable) + "\n\n.");
                borrar_archivo(archivo_contable);
                crear_archivo(archivo_contable);
                guardar(TiqueteContable, archivo_contable);
                Log.v("contar_parley_cont_file", ".\n\nArchivo contable despues de contar_parley:\n\n" + imprimir_archivo(archivo_contable) + "\n\n.");
            } catch (IOException e) {
            }
        }

        if (ArchivoExiste(archivos, archivo_contable)) {//nombre del archivo CONTABle del dia
            if (flag_cont) {
                Log.v("MAL_FUNCIONAMIENTO", ".\n\n**ERROR** NUNCA DEBERIA ENTRAR AQUI!!! **ERROR**\n\n.");
                try {
                    InputStreamReader archivo = new InputStreamReader(openFileInput(archivo_contable));//Se abre archivo contable
                    BufferedReader br = new BufferedReader(archivo);
                    String TiqueteContable = "";//Aqui se lee el contenido del tiquete guardado.

                    String linea = br.readLine();//Se lee archivo contable
                    while (linea != null) {
                        //flag_cont = true;
                        //Toast.makeText(this, "Debugueo guardar linea sin coinsidencias (linea): \n\n" + linea, Toast.LENGTH_LONG).show();
                        String[] split = linea.split("      ");//Se separa el monto de los numeros guardados.
                        if (Integer.parseInt(split[3]) != Integer.parseInt(fecha)) {
                            linea = split[0] + "      " + split[1] + "      0      " + fecha;
                            TiqueteContable = TiqueteContable + linea + "\n";
                        } else {
                            //linea = String.valueOf(num1) + "      " + String.valueOf(num2) + "      " + String.valueOf(valor) + "      " + fecha;
                            TiqueteContable = TiqueteContable + linea + "\n";
                        }
                        linea = br.readLine();
                    }
                    br.close();
                    archivo.close();
                    linea = String.valueOf(num1) + "      " + String.valueOf(num2) + "      " +  monto_act + "      " + fecha;
                    TiqueteContable = TiqueteContable + linea + "\n";
                    Log.v("contar_parley_cont_file", ".\n\nArchivo contable antes de contar_parley:\n\n" + imprimir_archivo(archivo_contable) + "\n\n.");
                    borrar_archivo(archivo_contable);
                    crear_archivo(archivo_contable);
                    guardar(TiqueteContable, archivo_contable);
                    Log.v("contar_parley_cont_file", ".\n\nArchivo contable despues de contar_parley:\n\n" + imprimir_archivo(archivo_contable) + "\n\n.");
                } catch (IOException e) {
                }
            } else {
                Log.v("BUEN_FUNCIONAMIENTO", ".\n\nSe ha borrado PARLEY!!!!\n\n.");
            }
        }
    }

    private void des_contar(String file) {//Funcion que acondiciona los datos que deben ser descontados del archivo contable del dia.
        try {
            InputStreamReader archivod = new InputStreamReader(openFileInput(facturas_diarias));
            BufferedReader brd = new  BufferedReader(archivod);
            String linead = brd.readLine();
            while (linead != null) {
                Log.v("des_contar", ".\n\nLinea:\n\n" + linead + "\n\n.");
                String[] splet = linead.split("      ");
                if (splet[3].equals(file)) {
                    int monto = 0;
                    String cont_file = Loteria + "_" + Horario + "_.txt";
                    //Log.v("des_contar", ".\n\nLinea:\n\n" + linead + "\n\n.");
                    if (splet[0].equals("BORRADA")) {
                        Toast.makeText(this, "Factura ya se ha borrado!!! Intente de nuevo.", Toast.LENGTH_LONG).show();
                    } else {
                        String[] splitdd = splet[3].split("_separador_");
                        tipo_lot = splitdd[9];
                        Paga1 = splitdd[10];
                        Paga2 = splitdd[11];
                        String[] splitd = splet[0].split("__");
                        //int numero = Integer.parseInt(splitd[0]);
                        int largo = splitd.length;

                        if (tipo_lot.equals("Monazos")) {
                            for (int i = 0; i < largo; i++) {
                                String[] splitA = splitd[i].split("_");
                                Log.v("descontar_monazos", ".\n\nLinea: " + linead + "\n\n.");
                                int montoi = Integer.parseInt(splitA[2]);
                                montoi = montoi * (-1);
                                contar_monazos(String.valueOf(montoi), splitA[0], cont_file, splitA[1]);
                                //break;
                            }

                        } if (tipo_lot.equals("Parley")) {
                            for (int i = 0; i < largo; i++) {
                                String[] splitA = splitd[i].split("_");
                                Log.v("descontar_parley", ".\n\nLinea: " + linead + "\n\n.");
                                int montoi = Integer.parseInt(splitA[2]);
                                montoi = (montoi * (-1));
                                contar_parley(String.valueOf(montoi), splitA[0], splitA[1], cont_file);
                                //break;
                            }

                        } else if (tipo_lot.equals("Regular") || tipo_lot.equals("Reventados")) {
                            for (int i = 0; i < largo; i++) {
                                String[] splitA = splitd[i].split("_");
                                Log.v("descontar_reg_o_rev", ".\n\nLinea: " + linead + "\n\n.");
                                int montoi = Integer.parseInt(splitA[1]);
                                montoi = montoi * (-1);
                                contar(String.valueOf(montoi), splitA[0], cont_file);
                                //break;
                            }

                            //temp_file = temp_file + linead + "\n";
                        } else {
                            //Do nothing.
                            Log.v("Debugg ERROR",".\n\nNUNCA DEBE IMPRIMIR ESTO!!!!!\n\n.");
                        }
                    }
                } else {
                    //Do nothing. Continue!
                }
                linead = brd.readLine();
            }
        } catch (IOException e) {
        }
    }

    public void borrar(View view) {//TODO: Verificar caducidad. Si la loteria ya caduco, entonces no se pueda borrar.

        if (verificar_internet()) {
            String numero_fact = edit_Text_numero_factura.getText().toString();
            if (numero_fact.isEmpty()) {
                Toast.makeText(this, "Debe indicar un numero de factura! ", Toast.LENGTH_LONG).show();
                edit_Text_numero_factura.setText("");
                return;
                //Do nothing.
            } else {
                int inv_number = Integer.parseInt(numero_fact);//Numero de factura introducido por el usuario
                if (numero_de_facturas.get(inv_number) == null) {
                    Toast.makeText(this, "Debe ingresar un numero de factura que se encuentre en la lista de arriba.", Toast.LENGTH_LONG).show();
                    edit_Text_numero_factura.setText("");
                    return;
                } else if (inv_number < 0) {
                    Toast.makeText(this, "Debe ingresar un numero de factura valido.", Toast.LENGTH_LONG).show();
                    edit_Text_numero_factura.setText("");
                    return;
                } else {

                    //

                    try {
                        InputStreamReader archivo = new InputStreamReader(openFileInput("invoice.txt"));
                        BufferedReader br = new BufferedReader(archivo);
                        String linea = br.readLine();
                        String linea_consecutivo = "";

                        linea_consecutivo = linea_consecutivo + linea + "\n";
                        linea = br.readLine();//Se lee la segunda linea del archivo
                        Log.v("Linea invoice", ".\n\n" + linea + "\n\n.");
                        while (linea != null) {
                            if (linea.isEmpty()) {
                                //Do nothing.
                            } else {
                                String[] split = linea.split(" ");
                                if (Integer.parseInt(split[0]) == inv_number) {

                                    String[] split_nombre_archivo = split[1].split("_separador_");
                                    String file_name = split[1];
                                    Log.v("split_nombre_archivo", ".\n\n" + split[1] + "\n\n.");

//Nombre del archivo: jugador_act + "_separador_" + Loteria + "_separador_" + Horario + "_separador_" + fecha + "_separador_" + hora + "_separador_" + minuto + "_separador_" + consecutivo_str + "_separador_" + dia + "_separador_" + mes + "_separador_" + tipo_lot + "_separador_" + Paga1 + "_separador_" + Paga2 + "_separador_" + monto_venta + "_separador_" + anio + "_separador_null.txt";
//                      split2[0]                  split2[1]                  split2[2]                 split2[3]               split2[4]              split2[5]                    split2[6]                    split2[7]              split2[8]              split2[9]                split2[10]               split2[11]             split2[12]                   split2[13]        split2[14]

                                    try {//Aqui cogemos el SPREADSHEET y el SHEET
                                        InputStreamReader archivod = new InputStreamReader(openFileInput(facturas_diarias));
                                        BufferedReader brr = new BufferedReader(archivod);
                                        String linearr = brr.readLine();
                                        while (linearr != null) {
                                            String lot_tipo = split_nombre_archivo[9];
                                            String[] splite = linearr.split("      ");
                                            if (splite[3].equals(file_name)) {
                                                Log.v("Error_borrar01", "Tipo lot: " + lot_tipo);
                                                SPREADSHEET_ID = splite[1];
                                                SHEET = splite[2];
                                                Log.v("Error borrando 1 ", ".\n\nSpreadSheet: " + SPREADSHEET_ID + "\nSheet: " + SHEET + "\n.");
                                            } else {
                                                //Do nothing, finding continue!!
                                            }
                                            linearr = brr.readLine();
                                        }
                                        brr.close();
                                        archivod.close();
                                    } catch (IOException e) {
                                        //Handle the error!
                                        Log.v("Error catch", "Error leyendo el SPREADSHEET_ID y/o el SHEET!");
                                    }

                                    //Se actualizan los parametros que se usaran para ir a los archivos correctos.
                                    Date now = Calendar.getInstance().getTime();
                                    String ahora = now.toString();
                                    //se separan los campos de la fecha y hora para verificar que si se pueda realizar la venta.
                                    separar_fechaYhora(ahora);
                                    int comp_hora_actual = Integer.parseInt(anio) - 2020;// queda solo el 22
                                    comp_hora_actual = comp_hora_actual * 100;
                                    comp_hora_actual = comp_hora_actual + Integer.parseInt(mes);
                                    comp_hora_actual = comp_hora_actual * 100;
                                    comp_hora_actual = comp_hora_actual + Integer.parseInt(dia);
                                    comp_hora_actual = comp_hora_actual * 100;
                                    comp_hora_actual = comp_hora_actual + Integer.parseInt(hora);
                                    comp_hora_actual = comp_hora_actual * 100;
                                    comp_hora_actual = comp_hora_actual + Integer.parseInt(minuto);
                                    Log.v("Debug_fecha_borrar", ".\nanio: " + anio + "\nmes: " + mes + "\ndia: " + dia + "\nhora: " + hora + "\nminuto: " + minuto);

                                    String file = split[1];
                                    Loteria = split_nombre_archivo[1];
                                    Horario = split_nombre_archivo[2];
                                    dia = split_nombre_archivo[3];
                                    mes = String.valueOf(meses.get(split_nombre_archivo[8]));
                                    tipo_lot = split_nombre_archivo[9];
                                    Paga1 = split_nombre_archivo[10];
                                    Paga2 = split_nombre_archivo[11];
                                    anio = split_nombre_archivo[13];
                                    String horita = split_nombre_archivo[4];
                                    String minutito = split_nombre_archivo[5];
                                    String coonsecut = split_nombre_archivo[6];

                                    int comp_hora_lote = Integer.parseInt(anio) - 2020;// queda solo el 22
                                    comp_hora_lote = comp_hora_lote * 100;
                                    comp_hora_lote = comp_hora_lote + Integer.parseInt(mes);
                                    comp_hora_lote = comp_hora_lote * 100;
                                    comp_hora_lote = comp_hora_lote + Integer.parseInt(dia);
                                    comp_hora_lote = comp_hora_lote * 100;
                                    comp_hora_lote = comp_hora_lote + Integer.parseInt(horita);
                                    comp_hora_lote = comp_hora_lote * 100;
                                    comp_hora_lote = comp_hora_lote + Integer.parseInt(minutito);

                                    Log.v("Debug_fecha_borrar", ".\nanio: " + anio + "\nmes: " + mes + "\ndia: " + dia + "\nhora: " + horita + "\nminuto: " + minutito);

                                    //Verificar caducidad de tiquete que se intenta borrar:

                                    Log.v("Error_caducidad", ".\nCom hora actual: " + comp_hora_actual + "\ncomp hora loteria: " + comp_hora_lote + "\n\n.");
                                    if (comp_hora_actual >= comp_hora_lote) {
                                        Toast.makeText(this, "ERROR!!!", Toast.LENGTH_SHORT);
                                        Toast.makeText(this, "Loteria ya ha jugado o se encuentra jugando!!", Toast.LENGTH_LONG);
                                        Toast.makeText(this, "Factura #" + coonsecut + " no se puede borrar!", Toast.LENGTH_SHORT);
                                    } else {
                                        borrar_de_la_nube(file, SPREADSHEET_ID, SHEET, tipo_lot);//Funcion que crea el anti-archivo.

                                        des_contar(file);


                                        //Se escribe la palabra "BORRADA" en el tiquete o factura guardada (file) despues de haber descontado en las cuentas o archivo contable del dia
                                        //cambiar_bandera(String.valueOf(inv_number), "BORRADA");
                                        //borrar_archivo(file);
                                        try {
                                            InputStreamReader archivod = new InputStreamReader(openFileInput(facturas_diarias));
                                            BufferedReader brr = new BufferedReader(archivod);
                                            String linearr = brr.readLine();
                                            String leer_escribir = "";
                                            while (linearr != null) {
                                                String lot_tipo = split_nombre_archivo[9];
                                                String[] splite = linearr.split("      ");
                                                if (splite[3].equals(file)) {
                                                    Log.v("Error_borrar1111", ".\n\nLinea:\n\n" + linea + "\n\nTipo lot: " + lot_tipo);
                                                    SPREADSHEET_ID = splite[1];
                                                    SHEET = splite[2];
                                                    String information = splite[0];
                                                    Log.v("Error borrando 1111 ", ".\n\nSpreadSheet: " + SPREADSHEET_ID + "\nSheet: " + SHEET + "\nsplit[0]: " + splite[0] + " (Debe ser la information)\n\n.");
                                                    linearr = linearr.replace(information, "BORRADA");
                                                    Log.v("Error borrando 1111 ", ".\n\nSpreadSheet: " + SPREADSHEET_ID + "\nSheet: " + SHEET + "\ninformation: " + information + " (Debe ser la information)\n\n.");
                                                    leer_escribir = leer_escribir + linearr + "\n";
                                                    Log.v("leer_escribir", ".\n\n" + leer_escribir + "\n\n.");
                                                } else {
                                                    leer_escribir = leer_escribir + linearr + "\n";
                                                }
                                                linearr = brr.readLine();
                                            }
                                            archivod.close();
                                            brr.close();
                                            borrar_archivo(facturas_diarias);
                                            crear_archivo(facturas_diarias);
                                            guardar(leer_escribir, facturas_diarias);
                                        } catch (IOException e) {
                                            //Handle the error!
                                            Log.v("Error catch", "Error leyendo el SPREADSHEET_ID y/o el SHEET!");
                                        }
                                        //guardar("BORRADA", file);
                                        //break;

                                        cargar_facturas(fecha_selected, mes_selected, anio_selected);
                                    }
                                }
                            }
                            linea_consecutivo = linea_consecutivo + linea + "\n";
                            linea = br.readLine();
                        }

                        br.close();
                        archivo.close();

                        guardar(linea_consecutivo, "invoice.txt");//Se actualiza el contador de consecutivos.
                        Log.v("debugg_invoice.txt", ".\n\ninvoice.txt:\n\n" + imprimir_archivo("invoice.txt") + "\n\n.");

                        //imprimir_archivo("invoice.txt");
                    } catch (IOException e) {
                    }

                    //*** La siguiente linea se debe colocar despues de generar el nombre del archivo tiquete factura.
                    //agregar_linea_archivo("invoice.txt", consecutivo_str + " " + tempFile);

                    //##########################################################################################


                }

            /*final Calendar c = Calendar.getInstance();
            mes_selected = (c.get(Calendar.MONTH)) + 1;
            //Toast.makeText(this, "mes selected: " + mes_selected, Toast.LENGTH_LONG).show();
            anio_selected = c.get(Calendar.YEAR);
            fecha_selected = c.get(Calendar.DAY_OF_MONTH);*/


                Log.v("Error5000", "Justantes de cargar facturas!!!");
                //numero_de_facturas.clear();
                //cargar_facturas(fecha_selected, mes_selected, anio_selected);
                edit_Text_numero_factura.setText("");

            }
            try {
                subir_facturas_resagadas();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "              ERROR!!!\nDebe estar conectado a internet", Toast.LENGTH_LONG).show();
            return;
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

    private void borrar_de_la_nube(String file, String spid, String sheet, String tip_lot) {

        //String estado_online = "emp";//TODO: No se puede consultar el estado online abajo, por fuerza hay que ver la nube.
        ocultar_todo();
        String estado_online = "emp";

        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("facturas_online.txt"));
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();

            while (linea != null) {
                Log.v("Borrar de la nube: ", "\n\nLinea: " + linea);
                //msg("- rr 00 -\n\nLinea: " + linea);
                String[] split = linea.split(" ");
                if (split[1].equals(file)) {
                    if (split[0].equals("abajo")) {
                        String[] spliteo = split[1].split("_separador_");
                        if (spliteo[14].equals("null.txt")) {
                            if (Integer.parseInt(spliteo[6]) < 0) {
                                //Do nothing.
                            } else {
                                estado_online = "abajo";
                                Log.v("Error99", "bandera estado_online: " + estado_online);
                            }
                        } else {
                            //Do nothing.
                        }
                    } else if (split[0].equals("arriba")) {
                        String[] spliteo = split[1].split("_separador_");
                        if (spliteo[14].equals("null.txt")) {
                            if (Integer.parseInt(spliteo[6]) < 0) {
                                // Do nothing.
                            } else {
                                estado_online = "arriba";
                                Log.v("Error99", "bandera estado_online: " + estado_online);
                            }
                        } else {
                            //Do nothing.
                        }
                    } else if (split[0].equals("BORRADA")) {
                        String[] spliteo = split[1].split("_separador_");
                        if (spliteo[14].equals("null.txt")) {
                            if (Integer.parseInt(spliteo[6]) < 0) {
                                //Do nothing.
                            } else {
                                estado_online = "BORRADA";
                                Log.v("Error99", "bandera estado_online: " + estado_online);
                            }
                        } else {
                            Log.v("Debugg borrar nube1", "No deberia estar aqui. \n\nSplit[0]: " + split[0] + " Deberia ser BOORADA");
                            //Do nothing.
                        }
                    } else {
                        Log.v("Debugg borrar nube2", "No deberia estar aqui. \n\nSplit[0]: " + split[0] + " Deberia ser BOORADA o abajo o arriba.\n\n.");
                        //Do nothing.
                    }
                } else {
                    //Do nothing.
                }
                linea = br.readLine();
            }

            br.close();
            archivo.close();

            } catch (IOException e){
            }
        if (estado_online.equals("abajo")) {//La loteria se pudo haber subido parcialmente

                    //Se analizan las loterias con etiqueta: "abajo


            String[] split_nombre_archivo = file.split("_separador_");
            String awLoteria = split_nombre_archivo[1];
            String awHorario = split_nombre_archivo[2];
            String awdia = split_nombre_archivo[7];
            String awmes = split_nombre_archivo[8];
            String awtipo_lot = split_nombre_archivo[9];
            String awPaga1 = split_nombre_archivo[10];
            String awPaga2 = split_nombre_archivo[11];
            String awnum_factura = String.valueOf(Integer.parseInt(split_nombre_archivo[6]));
            String awplayer = split_nombre_archivo[0];
            String awfecha = split_nombre_archivo[3];
            String awtotal = String.valueOf(Integer.parseInt(split_nombre_archivo[12]));
            String awanio = split_nombre_archivo[13];
            String anti_nombre = awplayer + "_separador_" + awLoteria + "_separador_" + awHorario + "_separador_" + awfecha + "_separador_" + split_nombre_archivo[4] + "_separador_" + split_nombre_archivo[5] + "_separador_" + awnum_factura + "_separador_" + awdia + "_separador_" + awmes + "_separador_" + awtipo_lot + "_separador_" + awPaga1 + "_separador_" + awPaga2 + "_separador_" + awtotal + "_separador_" + awanio + "_separador_equi.txt";
            Log.v("Error600_abajo", "Antinombre:\n" + anti_nombre);

            if (split_nombre_archivo[14].equals("equi.txt")) {
                Log.v("Prueba NULL", "Nunca va a llegar aqui!!!");
                //Do nothing.
            } else {
                //TODO: linea_agregar y cambiar crear_archivo por agregar_linea_archivo --> LISTO!!!

                //crear_archivo(anti_nombre);
                Log.v("Borrar de la nube_abajo", " Se borrara: " + anti_nombre);
                //try {
                //InputStreamReader archivo = new InputStreamReader(openFileInput(file));
                //BufferedReader br = new BufferedReader(archivo);
                //String linea = br.readLine();//Se lee la primera linea.
                String linea_escribir = "";
                String linea_inicial = "";
                Log.v("pre-equilibrar0_abajo: ", "Linea leida:\n\n" + "linea jiji");
                //while (linea != null) {
                //String[] split = linea.split("      ");
                Log.v("pre-equilibrar1_abajo: ", "Tipo_lot: " + awtipo_lot);
                if (awtipo_lot.equals("Regular")) {
                    //Log.v("Error700", "Si puede estar aqui!!!\n\nParametros:\n\nNumero1:" + split[0] + ", Numero2: " + split[1] + ", Antimonto: " + split[2]);
                    int numero = 0;
                    int anti_monto = 0;//Monto de cada numero
                    linea_inicial = linea_inicial + String.valueOf(numero) + "_" + String.valueOf(anti_monto) + "__";
                    //linea_escribir = linea_escribir + String.valueOf(numero) + "      " + String.valueOf(anti_monto) + "      " + spid + "      " + sheet + "\n";//TODO: Debe ser para cada tipo_lot
                    Log.v("pre-equilibrar2_abajo: ", "Linea_escribir:\n" + linea_escribir);
                } else if (awtipo_lot.equals("Reventados")) {
                    //Log.v("Error700", "Si puede estar aqui!!!\n\nParametros:\n\nNumero1:" + split[0] + ", Numero2: " + split[1] + ", Antimonto: " + split[2]);
                    int numero = 0;
                    int anti_monto = 0;//Monto de cada numero
                    linea_inicial = linea_inicial + String.valueOf(numero) + "_" + String.valueOf(anti_monto) + "__";
                    //linea_escribir = linea_escribir + String.valueOf(numero) + "      " + String.valueOf(anti_monto) + "      " + spid + "      " + sheet + "\n";//TODO: Debe ser para cada tipo_lot
                    Log.v("pre-equilibrar2_abajo: ", "Linea_escribir:\n" + linea_escribir);
                } else if (awtipo_lot.equals("Monazos")) {
                    //Log.v("Error700", "Si puede estar aqui!!!\n\nParametros:\n\nNumero1:" + split[0] + ", Numero2: " + split[1] + ", Antimonto: " + split[2]);
                    int numero = 0;
                    String ord_desord = "Orden";
                    int anti_monto = 0;//Monto de cada numero
                    linea_inicial = linea_inicial + String.valueOf(numero) + "_" + ord_desord + "_" + String.valueOf(anti_monto) + "__";
                    //linea_escribir = linea_escribir + String.valueOf(numero) + "      " + String.valueOf(anti_monto) + "      " + ord_desord + "      " + spid + "      " + sheet + "\n";//TODO: Debe ser para cada tipo_lot

                } else if (awtipo_lot.equals("Parley")) {
                    //Log.v("Error700", "Si puede estar aqui!!!\n\nParametros:\n\nNumero1:" + split[0] + ", Numero2: " + split[1] + ", Antimonto: " + split[2]);
                    int numero1 = 0;
                    int numero2 = 0;
                    int anti_monto = 0;//Monto de cada numero
                    linea_inicial = linea_inicial + String.valueOf(numero1) + "_" + String.valueOf(numero2) + "_" + String.valueOf(anti_monto) + "__";
                    //linea_escribir = linea_escribir + String.valueOf(numero1) + "      " + String.valueOf(numero2) + "      " + String.valueOf(anti_monto) + "      " + spid + "      " + sheet + "\n";//TODO: Debe ser para cada tipo_lot

                } else {
                    Log.v("pre-equilibrar3_abajo: ", "No deberia estar aqui!!!\n\nParametros:\n\nNumero1:" + "split[0]" + ", Numero2: " + "split[1]" + ", Antimonto: " + "split[2]" + "jiji");
                    //Do nothing. Nunca llega aqui!
                }//TODO: Para que hacer todo esto si esta abajo???
                //br.close();
                //archivo.close();
                //String fecha_invoice = anio + mes + fecha + "_" + nombre_dia;
                Log.v("Borrando 555", ".\n\nLinea inicial creada:\n\n" + linea_inicial + "\n\n.");
                linea_escribir = linea_inicial + "      " + spid + "      " + sheet + "      " + anti_nombre + "      " + fecha;
                //String linea_escribir2 = linea_inicial + "      " + spid + "      " + sheet + "      " + file + "      " + fecha_invoice;
                agregar_linea_archivo(facturas_diarias, linea_escribir);
                //agregar_linea_archivo(historial_facturas, linea_escribir2);

                //guardar(linea_escribir, anti_nombre);
                Log.v("pre-equilibrar4_abajo: ", "Nombre del archivo: " + anti_nombre + "\n\n" + "Archivo creado: " + "\n\n" + "(No se puede ver porque el archivo realmente no existe!!!)\n\n");
                Log.v("pre-equilibrar5_abajo: ", "Spid: " + spid + ", sheet: " + sheet + ", tip_lot: " + tip_lot + "\n\n");
                agregar_fact_online(anti_nombre, spid, sheet, tip_lot);
                //break;
                //}
                /*} catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
            Log.v("Error_camb_band_0_abajo", "Se va a cambiar la bandera de una loteria abajo");
            Log.v("Error_camb_band_1_abajo", "Factura numero: " + awnum_factura);
            cambiar_bandera(awnum_factura, "nada");

        } else if (estado_online.equals("arriba")) {

            /*Log.v("facturas_diarias(arri):", ".\n\n.");
            try {
                InputStreamReader archivow = new InputStreamReader(openFileInput(facturas_diarias));
                BufferedReader brw = new BufferedReader(archivow);
                String lineaw = brw.readLine();//Se lee la primera linea.
                int cont = 0;
                while (lineaw != null) {
                    cont++;
                    Log.v("linea " + cont, lineaw);
                    lineaw = brw.readLine();
                }
                brw.close();
                archivow.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            //Naming convention...

//Nombre del archivo: jugador_act + "_separador_" + Loteria + "_separador_" + Horario + "_separador_" + fecha + "_separador_" + hora + "_separador_" + minuto + "_separador_" + consecutivo_str + "_separador_" + dia + "_separador_" + mes + "_separador_" + tipo_lot + "_separador_" + Paga1 + "_separador_" + Paga2 + "_separador_" + monto_venta + "_separador_" + anio + "_separador_null.txt";
//                      split2[0]                  split2[1]                  split2[2]                 split2[3]               split2[4]              split2[5]                    split2[6]                    split2[7]              split2[8]              split2[9]                split2[10]               split2[11]             split2[12]                   split2[13]


            String[] split_nombre_archivo = file.split("_separador_");
            String awLoteria = split_nombre_archivo[1];
            String awHorario = split_nombre_archivo[2];
            String awdia = split_nombre_archivo[7];
            String awmes = split_nombre_archivo[8];
            String awtipo_lot = split_nombre_archivo[9];
            String awPaga1 = split_nombre_archivo[10];
            String awPaga2 = split_nombre_archivo[11];
            String awnum_factura = String.valueOf(Integer.parseInt(split_nombre_archivo[6]) * -1);
            String awplayer = split_nombre_archivo[0];
            String awfecha = split_nombre_archivo[3];
            String awtotal = String.valueOf(Integer.parseInt(split_nombre_archivo[12]) * -1);
            String awanio = split_nombre_archivo[13];
            String anti_nombre = awplayer + "_separador_" + awLoteria + "_separador_" + awHorario + "_separador_" + awfecha + "_separador_" + split_nombre_archivo[4] + "_separador_" + split_nombre_archivo[5] + "_separador_" + awnum_factura + "_separador_" + awdia + "_separador_" + awmes + "_separador_" + awtipo_lot + "_separador_" + awPaga1 + "_separador_" + awPaga2 + "_separador_" + awtotal + "_separador_" + awanio + "_separador_null.txt";
            Log.v("Error600", "Antinombre:\n" + anti_nombre);
            //crear_archivo(anti_nombre);

            try {
                //                     spllit[0]                    spllit[1]                spllit[2]            spllit[3]            spllit[4]
                //String liniesilla = sub_linea_final + "      " + SPREADSHEET_ID + "      " + SHEET + "      " + tempFile + "      " + fecha;
                InputStreamReader archivow = new InputStreamReader(openFileInput(facturas_diarias));
                BufferedReader brw = new BufferedReader(archivow);
                String lineaw = brw.readLine();//Se lee la primera linea.
                String lin_string = "";
                String linea_escribir = "";
                Log.v("Error601", "Linea leida:\n\n" + lineaw);
                //Log.v("ver facturas_diarias", ".\n\nFacturas diarias file:\n\n" + imprimir_archivo(facturas_diarias) + "\n\n.");
                //String[] spllit = lineaw.split("      ");
                while (lineaw != null) {
                    String[] spllit = lineaw.split("      ");
                    //Log.v("borrando lineaw", ".\n\nLineaw:\n\n" + lineaw + "\n\n.");
                    Log.v("spllit[3]", " " + spllit[3] + "\nfile: " + file + "\n");
                    if (spllit[3].equals(file)) {
                        String information = spllit[0];
                        Log.v("error1000", "Tipo_lot: " + awtipo_lot + "\n\nInformation:\n\n" + information + "\n\n.");

                        /*
                        //Log.v("Error700", "Si puede estar aqui!!!\n\nParametros:\n\nNumero1:" + split[0] + ", Numero2: " + split[1] + ", Antimonto: " + split[2]);
                            int numero1 = Integer.parseInt(splitw[0]);
                            int numero2 = Integer.parseInt(splitw[1]);
                            int anti_monto = Integer.parseInt(splitw[2]) * -1;//Monto de cada numero
                            linea_escribir = linea_escribir + String.valueOf(numero1) + "      " + String.valueOf(numero2) + "      " + String.valueOf(anti_monto) + "      " + spid + "      " + sheet + "\n";//TODO: Debe ser para cada tipo_lot

                         */

                        if (awtipo_lot.equals("Regular") | awtipo_lot.equals("Reventados")) {
                            Log.v("Regular_reventados", ".\n\nLinea:\n\n" + lineaw + "\n\n.");
                            String[] split2 = information.split("__");
                            for (int i = 0; i < split2.length; i++) {
                                String[] split3 = split2[i].split("_");
                                int numero1 = Integer.parseInt(split3[0]);
                                int anti_monto = Integer.parseInt(split3[1]) * -1;//Monto de cada numero
                                //                          #1              monto
                                lin_string = lin_string + numero1 + "_" + anti_monto + "__";
                            }

                        } else if (awtipo_lot.equals("Monazos")) {
                            String[] split2 = information.split("__");
                            Log.v("Se encuentra monazos", "\n\ninformation: " + information + "\n\n.");
                            for (int i = 0; i < split2.length; i++) {
                                String[] split3 = split2[i].split("_");
                                int numero1 = Integer.parseInt(split3[0]);
                                String ord_desord = split3[1];
                                int anti_monto = (Integer.parseInt(split3[2])) * -1;//Monto de cada numero
                                //                          #1            extra info           monto
                                lin_string = lin_string + numero1 + "_" + ord_desord + "_" + anti_monto + "__";
                            }
                        } else if (awtipo_lot.equals("Parley")) {
                            String[] split2 = information.split("__");
                            for (int i = 0; i < split2.length; i++) {
                                String[] split3 = split2[i].split("_");
                                int numero1 = Integer.parseInt(split3[0]);
                                int numero2 = Integer.parseInt(split3[1]);
                                int anti_monto = Integer.parseInt(split3[2]) * -1;//Monto de cada numero
                                //                          #1              #2               monto
                                lin_string = lin_string + numero1 + "_" + numero2 + "_" + anti_monto + "__";
                            }
                        } else {
                            Log.v("Error700", "No deberia estar aqui!!!\n\nParametros:\n\nNumero1:" + spllit[0] + ", Numero2: " + spllit[1] + ", Antimonto: " + spllit[2]);
                            //Do nothing. Nunca llega aqui!
                        }
                    }
                    lineaw = brw.readLine();
                }

                /*
                //                   split[0]                     split[1]                 split[2]           split[3]             split[4]
                String liniesilla = lin_string + "      " + SPREADSHEET_ID + "      " + SHEET + "      " + tempFile + "      " + fecha;
                String liniesilla_history = sub_linea_final + "      " + SPREADSHEET_ID + "      " + SHEET + "      " + tempFile + "      " + fecha_invoice;
                agregar_linea_archivo(facturas_diarias, liniesilla);
                agregar_linea_archivo(historial_facturas, liniesilla_history);//TODO: limpiar en facturasEditActivity
                 */

                String fecha_invoice = anio + mes + fecha + "_" + nombre_dia;
                Log.v("Lin_string", ".\n\nlin_string:\n\n" + lin_string + "\n\n.");
                linea_escribir = lin_string + "      " + spid + "      " + sheet + "      " + anti_nombre + "      " + fecha;
                String linea_escribir2 = lin_string + "      " + spid + "      " + sheet + "      " + anti_nombre + "      " + fecha_invoice;
                agregar_linea_archivo(facturas_diarias, linea_escribir);
                agregar_linea_archivo(historial_facturas, linea_escribir2);

                //guardar(linea_escribir, anti_nombre);
                Log.v("Error701", "Nombre del archivo: " + anti_nombre + "\n\n" + "Archivo creado: " + "\n\n" + "imprimir_archivo(anti_nombre) \nNo se puede leer el archivo porque ahora no existe!!!" + "\n\n");
                Log.v("Error702", "Spid: " + spid + ", sheet: " + sheet + ", tip_lot: " + tip_lot + "\n\n");
                agregar_fact_online(anti_nombre, spid, sheet, tip_lot);


                brw.close();
                archivow.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (estado_online.equals("BORRADA")) {
            Log.v("Estado online BORRADA", ".\n\nSe ha encontrado una loteria borrada!!!\n\n");
            //TODO: Pensar que hacer!!!
            // Nunca va a llegar aqui.
        } else if (estado_online.equals("emp")) {//Si entra aqui, significa que la factura ya se ha subido y se ha borrado del archivo facturas_online.txt.
            Log.v("borrar de la nube emp", "No encontro el archivo en la lista de facturas_online.txt. Estado online: " + estado_online + "\n");

            //Naming convention...

//Nombre del archivo: jugador_act + "_separador_" + Loteria + "_separador_" + Horario + "_separador_" + fecha + "_separador_" + hora + "_separador_" + minuto + "_separador_" + consecutivo_str + "_separador_" + dia + "_separador_" + mes + "_separador_" + tipo_lot + "_separador_" + Paga1 + "_separador_" + Paga2 + "_separador_" + monto_venta + "_separador_" + anio + "_separador_null.txt";
//                      split2[0]                  split2[1]                  split2[2]                 split2[3]               split2[4]              split2[5]                    split2[6]                    split2[7]              split2[8]              split2[9]                split2[10]               split2[11]             split2[12]                   split2[13]


            String[] split_nombre_archivo = file.split("_separador_");
            String awLoteria = split_nombre_archivo[1];
            String awHorario = split_nombre_archivo[2];
            String awdia = split_nombre_archivo[7];
            String awmes = split_nombre_archivo[8];
            String awtipo_lot = split_nombre_archivo[9];
            String awPaga1 = split_nombre_archivo[10];
            String awPaga2 = split_nombre_archivo[11];
            String awnum_factura = String.valueOf(Integer.parseInt(split_nombre_archivo[6]) * -1);
            String awplayer = split_nombre_archivo[0];
            String awfecha = split_nombre_archivo[3];
            String awtotal = String.valueOf(Integer.parseInt(split_nombre_archivo[12]) * -1);
            String awanio = split_nombre_archivo[13];
            String anti_nombre = awplayer + "_separador_" + awLoteria + "_separador_" + awHorario + "_separador_" + awfecha + "_separador_" + split_nombre_archivo[4] + "_separador_" + split_nombre_archivo[5] + "_separador_" + awnum_factura + "_separador_" + awdia + "_separador_" + awmes + "_separador_" + awtipo_lot + "_separador_" + awPaga1 + "_separador_" + awPaga2 + "_separador_" + awtotal + "_separador_" + awanio + "_separador_null.txt";
            Log.v("Error600_emp", "Antinombre:\n" + anti_nombre);

            /*Log.v("facturas_diarias (emp):", ".\n\n.");
            try {
                InputStreamReader archivow = new InputStreamReader(openFileInput(facturas_diarias));
                BufferedReader brw = new BufferedReader(archivow);
                String lineaw = brw.readLine();//Se lee la primera linea.
                int cont = 0;
                while (lineaw != null) {
                    cont++;
                    Log.v("linea " + cont, lineaw);
                    lineaw = brw.readLine();
                }
                brw.close();
                archivow.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            //crear_archivo(anti_nombre);

            try {
                //                     spllit[0]                    spllit[1]                spllit[2]            spllit[3]            spllit[4]
                //String liniesilla = sub_linea_final + "      " + SPREADSHEET_ID + "      " + SHEET + "      " + tempFile + "      " + fecha;
                InputStreamReader archivow = new InputStreamReader(openFileInput(facturas_diarias));
                BufferedReader brw = new BufferedReader(archivow);
                String lineaw = brw.readLine();//Se lee la primera linea.
                String lin_string = "";
                String linea_escribir = "";


                while (lineaw != null) {
                    String[] spllit = lineaw.split("      ");
                    Log.v("Error601", "Linea leida:\n\n" + lineaw);
                    if (spllit[3].equals(file)) {
                        String information = spllit[0];
                        Log.v("error1000", "Tipo_lot: " + awtipo_lot);

                        /*
                        //Log.v("Error700", "Si puede estar aqui!!!\n\nParametros:\n\nNumero1:" + split[0] + ", Numero2: " + split[1] + ", Antimonto: " + split[2]);
                            int numero1 = Integer.parseInt(splitw[0]);
                            int numero2 = Integer.parseInt(splitw[1]);
                            int anti_monto = Integer.parseInt(splitw[2]) * -1;//Monto de cada numero
                            linea_escribir = linea_escribir + String.valueOf(numero1) + "      " + String.valueOf(numero2) + "      " + String.valueOf(anti_monto) + "      " + spid + "      " + sheet + "\n";//TODO: Debe ser para cada tipo_lot

                         */

                        if (awtipo_lot.equals("Regular") | awtipo_lot.equals("Reventados")) {
                            Log.v("Regular_reventados", ".\n\nLinea:\n\n" + lineaw + "\n\n.");
                            String[] split2 = information.split("__");
                            for (int i = 0; i < split2.length; i++) {
                                String[] split3 = split2[i].split("_");
                                int numero1 = Integer.parseInt(split3[0]);
                                int anti_monto = Integer.parseInt(split3[1]) * -1;//Monto de cada numero
                                //                          #1              monto
                                lin_string = lin_string + numero1 + "_" + anti_monto + "__";
                            }

                        } else if (awtipo_lot.equals("Monazos")) {
                            String[] split2 = information.split("__");
                            for (int i = 0; i < split2.length; i++) {
                                String[] split3 = split2[i].split("_");
                                int numero1 = Integer.parseInt(split3[0]);
                                String ord_desord = split3[1];
                                int anti_monto = Integer.parseInt(split3[2]) * -1;//Monto de cada numero
                                //                          #1            extra info           monto
                                lin_string = lin_string + numero1 + "_" + ord_desord + "_" + anti_monto + "__";
                            }
                        } else if (awtipo_lot.equals("Parley")) {
                            String[] split2 = information.split("__");
                            for (int i = 0; i < split2.length; i++) {
                                String[] split3 = split2[i].split("_");
                                int numero1 = Integer.parseInt(split3[0]);
                                int numero2 = Integer.parseInt(split3[1]);
                                int anti_monto = Integer.parseInt(split3[2]) * -1;//Monto de cada numero
                                //                          #1              #2               monto
                                lin_string = lin_string + numero1 + "_" + numero2 + "_" + anti_monto + "__";
                            }
                        } else {
                            Log.v("Error700", "No deberia estar aqui!!!\n\nParametros:\n\nNumero1:" + spllit[0] + ", Numero2: " + spllit[1] + ", Antimonto: " + spllit[2]);
                            //Do nothing. Nunca llega aqui!
                        }
                    }
                    lineaw = brw.readLine();
                }

                /*
                //                   split[0]                     split[1]                 split[2]           split[3]             split[4]
                String liniesilla = lin_string + "      " + SPREADSHEET_ID + "      " + SHEET + "      " + tempFile + "      " + fecha;
                String liniesilla_history = sub_linea_final + "      " + SPREADSHEET_ID + "      " + SHEET + "      " + tempFile + "      " + fecha_invoice;
                agregar_linea_archivo(facturas_diarias, liniesilla);
                agregar_linea_archivo(historial_facturas, liniesilla_history);//TODO: limpiar en facturasEditActivity
                 */

                String fecha_invoice = anio + mes + fecha + "_" + nombre_dia;
                linea_escribir = lin_string + "      " + spid + "      " + sheet + "      " + anti_nombre + "      " + fecha;
                String linea_escribir2 = lin_string + "      " + spid + "      " + sheet + "      " + anti_nombre + "      " + fecha_invoice;
                agregar_linea_archivo(facturas_diarias, linea_escribir);
                agregar_linea_archivo(historial_facturas, linea_escribir2);

                brw.close();
                archivow.close();
                //guardar(linea_escribir, anti_nombre);
                Log.v("Error701", "Nombre del archivo: " + anti_nombre + "\n\n" + "Archivo creado: " + "\n\n" + imprimir_archivo(anti_nombre) + "\n\n");
                Log.v("Error702", "Spid: " + spid + ", sheet: " + sheet + ", tip_lot: " + tip_lot + "\n\n");
                agregar_fact_online(anti_nombre, spid, sheet, tip_lot);



            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            //Never come here!!!
            Log.v("borrar de la nube false", "Prueba de que aqui nunca llega.\n\nEstado online: " + estado_online + "\n");//Si llegase aqui, el estado online debe ser "emp"
        }
    }

    private void agregar_linea_archivo (String file_name, String new_line) {
        String archivos[] = fileList();
        String ArchivoCompleto = "";//Aqui se lee el contenido del archivo guardado.
        Log.v("Agreagar Linea", "\n\nArchivo al que se le agregara una linea: \n             " + file_name + "\n\nLinea a agregar: " + new_line + "\n\n");
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

    private void agregar_fact_online(String file, String spid, String sheet, String tip_lot) {
        String linea_agrgar = "abajo " + file + " " + spid + " " + sheet + " " + tip_lot;//agregar_linea_archivo("facturas_online.txt", "abajo " + file + " " + SPREADSHEET_ID + " " + SHEET + " " + tipo_lot);

        agregar_linea_archivo("facturas_online.txt", linea_agrgar);
        Log.v("Error800", ".\n\nAgregar a facturas_online.txt :\n\n" + imprimir_archivo(facturas_diarias) + "\n\n.");
        Log.v("Error111", ".\n\nSpreadSheet ID: " + spid + "\nSheet: " + sheet + "\nTipo lot: " + tip_lot + "\nFile name: " + file);
        //msg("Error111 SpreadSheet ID: " + spid + "\nSheet: " + sheet + "\nTipo lot: " + tip_lot + "\nFile name: " + file);
        //debug
        //imprimir_archivo(file);
        Log.v("Error110", "facturas_online.txt:\n\n" + imprimir_archivo("facturas_online.txt"));
        //msg("Error110\n" + imprimir_archivo(imprimir_archivo(file)));
    }

    public void reimprimir(View view) {
        String numero_fact = edit_Text_numero_factura.getText().toString();
        if (numero_fact.isEmpty()) {
            Toast.makeText(this, "Debe indicar un numero de factura! ", Toast.LENGTH_LONG).show();
        } else {
            int inv_number = Integer.parseInt(numero_fact);//Numero de factura introducido por el usuario
            if (numero_de_facturas.get(inv_number) == null) {
                Toast.makeText(this, "Debe ingresar un numero de factura que se encuentre en la lista de arriba.", Toast.LENGTH_LONG).show();
            } else if (inv_number < 0) {
                Toast.makeText(this, "Debe ingresar un numero de factura valido.", Toast.LENGTH_LONG).show();
            } else {
                String tiquet = "";
                try {
                    InputStreamReader archivo = new InputStreamReader(openFileInput("invoice.txt"));
                    BufferedReader br = new BufferedReader(archivo);
                    String linea = br.readLine();//Esta es la primera linea, no nos interesa por ahora.
                    linea = br.readLine();//se lee la segunda linea que si nos interesa!
                    while (linea != null) {
                        String[] split = linea.split(" ");

                        if (Integer.parseInt(split[0]) == inv_number) {

                            //Se actualizan los parametros que se usaran para ir a los archivos correctos.

//Nombre del archivo: jugador_act + "_separador_" + Loteria + "_separador_" + Horario + "_separador_" + fecha + "_separador_" + hora + "_separador_" + minuto + "_separador_" + consecutivo_str + "_separador_" + dia + "_separador_" + mes + "_separador_" + tipo_lot + "_separador_" + Paga1 + "_separador_" + Paga2 + "_separador_" + monto_venta + "_separador_" + anio + "_separador_null.txt";
//                      split2[0]                  split2[1]                  split2[2]                 split2[3]               split2[4]              split2[5]                    split2[6]                    split2[7]              split2[8]              split2[9]                split2[10]               split2[11]             split2[12]                   split2[13]

                            String[] split_nombre_archivo = split[1].split("_separador_");
                            Loteria = split_nombre_archivo[1];
                            Horario = split_nombre_archivo[2];
                            dia = split_nombre_archivo[3];
                            mes = String.valueOf(meses.get(split_nombre_archivo[8]));
                            tipo_lot = split_nombre_archivo[9];
                            Paga1 = split_nombre_archivo[10];
                            Paga2 = split_nombre_archivo[11];
                            num_factura = split_nombre_archivo[6];
                            player = split_nombre_archivo[0];
                            fecha = split_nombre_archivo[3];
                            total = split_nombre_archivo[12];
                            anio = split_nombre_archivo[13];
                            player = player.replace("\n", "");
                            player = player.replace("x_x", " ");
                            String pagar_print = "";
                            if (Integer.parseInt(Paga2) == 0) {
                                pagar_print = Paga1 + " veces";
                            } else {
                                pagar_print = Paga1 + " veces y " + Paga2 + " veces";
                            }
                            tiquet = "\nFactura # " + num_factura + "\n\nFecha: " + fecha + "/" + mes + "/" + anio +
                                    "\nLoteria: " + Loteria + " " + Horario + "\nCliente: " + player + "\nPagamos: " +
                                    pagar_print + "\n\n#######################\n";
                            String lineaa = "";
                            try {
                                InputStreamReader archivoo = new InputStreamReader(openFileInput(facturas_diarias));//split[1] es el file
                                BufferedReader bro = new BufferedReader(archivoo);
                                lineaa = bro.readLine();
                                while (lineaa != null) {
                                    String[] spliteito = lineaa.split("      ");
                                    if (spliteito[3].equals(split[1])) {//Linea contiene la factura buscada
                                        Log.v("verificar_archivo_reimp", ".\n\nLinea:\n\n" + lineaa + "\n\n.");
                                        String[] split_0 = spliteito[0].split("__");
                                        if (split_0[0].equals("BORRADA")) {
                                            lineaa = "Factura ha sido borrada!!!";
                                            total = "0";
                                            tiquet = tiquet + lineaa + "\n";
                                        } else {
                                            int largo = split_0.length;
                                            for (int i = 0; i < largo; i++) { //Se llena el tiquete que se va a imprimir con los datos que se requieren
                                                String[] spliti = split_0[i].split("_");
                                                if (tipo_lot.equals("Regular") | tipo_lot.equals("Reventados")) {
                                                    tiquet = tiquet + spliti[0] + "  --->  " + spliti[1] + "\n";
                                                    //tiquet = tiquet + lineaa + "\n";
                                                } else if (tipo_lot.equals("Monazos")) {
                                                    tiquet = tiquet + spliti[0] + "  " + spliti[1] + "  --->  " + spliti[2] + "\n";
                                                    //tiquet = tiquet + lineaa + "\n";
                                                } else if (tipo_lot.equals("Parley")) {
                                                    tiquet = tiquet + spliti[0] + "  " + spliti[1] + "  --->  " + spliti[2] + "\n";
                                                    //tiquet = tiquet + lineaa + "\n";
                                                } else {
                                                    tiquet = tiquet + lineaa + "\n";
                                                }
                                            }
                                        }
                                    }
                                    lineaa = bro.readLine();
                                }
                            } catch (IOException e) {
                            }
                            tiquet = tiquet + "#######################\nTotal: " + total + "\n#######################\n\n\n\n";
                        }
                        linea = br.readLine();
                    }
                    br.close();
                    archivo.close();
                    printIt(tiquet);
                } catch (IOException e) {
                }
            }
            numero_de_facturas.clear();
            cargar_facturas(fecha_selected, mes_selected, anio_selected);
            edit_Text_numero_factura.setText("");
        }
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
            //System.exit(0);

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

}
