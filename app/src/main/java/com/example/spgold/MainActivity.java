package com.example.spgold;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
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
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private TextView inicio;
    private String dispositivo;
    private EditText passET;
    private String addRowURL = "https://script.google.com/macros/s/AKfycbweyYb-DHVgyEdCWpKoTmvOxDGXleawjAN8Uw9AeJYbZ24t9arB/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicio = (TextView)findViewById(R.id.tv_inicio);
        passET = (EditText) findViewById(R.id.et_password);

        passET.setFocusableInTouchMode(false);

        crear_archivos_config();

        dispositivo = check_device();

        String archivos[] = fileList();
        boolean crear_lot = true;
        for (int i = 0; i < archivos.length; i++){
            Pattern pattern = Pattern.compile("loteria_sfile", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound){
                borrar_archivo(archivos[i]);
                //crear_lot = false;
                //break;
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

        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput(file));
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            String json_string = "";
            while (linea != null) {
                String[] split = linea.split("      ");

                if (linea.equals("BORRADA")) {
                    Log.v("ErrorBorrada", "Factura esta borrada, no hacer nada!!!");
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
            Log.v("Error56", "Json String: " + json_string);
            if (json_string.isEmpty()) {
                Log.v("Error57", "Linea esta vacia.");
            } else if (json_string.equals(null)) {
                Log.v("Error57", "Linea es nula.");
            } else {
                Log.v("Error57", "Linea no esta vacia ni nula.");
                jsonObject = TranslateUtil.string_to_Json(json_string, SPREEADSHEET_ID, SSHHEETT, factura);
            }

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
                    String tipo_lote = split_name[9];
                    String SSHHEETT = split[3];
                    String SSPPRREEAADDSSHHEETT = split[2];
                    objeto_json = generar_Json_resagadas(split[1], factura, SSHHEETT, SSPPRREEAADDSSHHEETT, tipo_lote);
                    subir_factura_resagadas(objeto_json);
                } else if (split[0].equals("BORRADA")) {
                    //TODO: Pensar que hacer!!! Somthing like this: Nothin!!!
                } else if (split[0].equals("arriba")) {
                    //TODO: Pensar que hacer!!!
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
                                msg("factura #" + factura_num + " se ha subido correctamente!");
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

    private void msg(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    private void cambiar_bandera (String Consecutivo) {
        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("facturas_online.txt"));
            //imprimir_archivo("facturas_online.txt");
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            String contenido = "";
            while (linea != null) {
                String[] split = linea.split(" ");
                if (split[0].equals("abajo")) {
                    String[] split_name = split[1].split("_separador_");
                    String factura = split_name[6];// split_name[6] contiene el numero de la factura que se desea subir.
                    if (factura.equals(Consecutivo)) {
                        linea = linea.replace("abajo", "arriba");
                        contenido = contenido + linea;
                    } else {
                        contenido = contenido + linea;
                    }

                } else if (split[0].equals("BORRADA")) {
                    //TODO: Pensar que hacer!!!
                } else if (split[0].equals("arriba")) {
                    //TODO: Pensar que hacer!!!
                } else {
                    //Do nothing. No deberia llegar aqui.
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

    public void guardar (String Tcompleto, String nombre){
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre, Activity.MODE_PRIVATE));
            archivo.write(Tcompleto);
            archivo.flush();
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

    private void crear_archivos_config() {

        String files[] = fileList();

        ///////////////Se crea el archivo password.txt//////////
        String password = "password.txt";
        if (ArchivoExiste(files, password)) {
            //Do nothing
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
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_juego_D  " + "12:55");
        agregar_linea_archivo("loteria_sfile" + lot_demo + "_sfile.txt", "Hora_lista_D  " + "2359");
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
