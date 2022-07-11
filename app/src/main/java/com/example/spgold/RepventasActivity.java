package com.example.spgold;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spgold.Util.BluetoothUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    }

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
                            linea_tempo = linea_tempo + key2 + separacion_str + treeMap2.get(key2) + "| ";
                            treeMap2.remove(key2);
                            for (String key3 : treeMap3.keySet()) {
                                linea_tempo = linea_tempo + key3 + separacion_str + treeMap3.get(key3);
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
