package com.example.spgold.Util;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.spgold.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


public class FuncionesComunesUtil extends AppCompatActivity {


    private static void post_crear_archivo(String nombre_archivo) throws IOException {

        if (archivo_existe(nombre_archivo)) {
            Log.v("crear_archivo existe", ".\n\nEl archivo " + nombre_archivo + " existe. No es necesario crearlo!\n\n.");
        } else {
            File ruta_files = Environment.getExternalStorageDirectory();
            File file = new File(ruta_files.getPath(), nombre_archivo);
            //File file = new File(nombre_archivo);

            try {
                if (file.createNewFile()) {
                    Log.v("post_crear_archivo", ".\n\nArchivo creado: " + nombre_archivo + "\n\n.");
                } else {
                    Log.v("post_crear_archivo", ".\n\nError al crear archivo: " + nombre_archivo + "\n\n.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String check_device() {
        String dispositivo = "";
        String file_name = "device.txt";
        if (archivo_existe(file_name)) {
            File archivo = null;
            FileReader fr = null;
            BufferedReader br = null;
            try {
                File ruta_files = Environment.getExternalStorageDirectory();
                archivo = new File(ruta_files.getPath(), file_name);
                fr = new FileReader(archivo);
                br = new BufferedReader(fr);

                //lectura del fichero:
                String linea = br.readLine();
                if (linea.equals("Celular")) {
                    dispositivo = "Celular";
                } else if (linea.equals("Maquina")) {
                    dispositivo = "Maquina";
                }
                br.close();
                fr.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != fr) {
                        fr.close();
                        br.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return dispositivo;
    }

    public static void crear_archivo(String nombre_archivo) throws IOException {
        post_crear_archivo(nombre_archivo);
    }

    public static void agregar_linea_archivo(String file_name, String new_line) throws IOException {

        if (archivo_existe(file_name)) {
            File archivo = null;
            FileReader fr = null;
            BufferedReader br = null;
            String ArchivoCompleto = "";//Aqui se lee el contenido del archivo guardado.
            try {
                File ruta_files = Environment.getExternalStorageDirectory();
                archivo = new File(ruta_files.getPath(), file_name);
                fr = new FileReader(archivo);
                br = new BufferedReader(fr);

                //lectura del fichero:
                String linea = br.readLine();
                while (linea != null || !linea.equals("")) {
                    ArchivoCompleto = ArchivoCompleto + linea + "\n";
                    linea = br.readLine();
                }
                br.close();
                fr.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != fr) {
                        fr.close();
                        br.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            FileWriter fichero = null;
            PrintWriter pw = null;
            try {
                File ruta_files = Environment.getExternalStorageDirectory();
                File ruta = new File(ruta_files.getPath(), file_name);
                fichero = new FileWriter(ruta);
                pw = new PrintWriter(fichero);
                pw.println(ArchivoCompleto + new_line);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != fichero) {
                        fichero.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        } else {
            crear_archivo(file_name);
            agregar_linea_archivo2(file_name, new_line);
        }
    }

    public static void agregar_linea_archivo2(String file_name, String new_line) throws IOException {


            File archivo = null;
            FileReader fr = null;
            BufferedReader br = null;
            String ArchivoCompleto = "";//Aqui se lee el contenido del archivo guardado.
            try {
                File ruta_files = Environment.getExternalStorageDirectory();
                archivo = new File(ruta_files.getPath(), file_name);
                fr = new FileReader(archivo);
                br = new BufferedReader(fr);

                //lectura del fichero:
                String linea = br.readLine();
                while (linea != null || !linea.equals("")) {
                    ArchivoCompleto = ArchivoCompleto + linea + "\n";
                    linea = br.readLine();
                }
                br.close();
                fr.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != fr) {
                        fr.close();
                        br.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            FileWriter fichero = null;
            PrintWriter pw = null;
            try {
                File ruta_files = Environment.getExternalStorageDirectory();
                File ruta = new File(ruta_files.getPath(), file_name);
                fichero = new FileWriter(ruta);
                pw = new PrintWriter(fichero);
                pw.println(ArchivoCompleto + new_line);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != fichero) {
                        fichero.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
    }

    public static boolean archivo_existe (String file_name){
        boolean flag = false;
        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;
        Log.v("archivo existe", ".\n\nArchivo creado: " + file_name + "\n\n.");
        try {
            File ruta_files = Environment.getExternalStorageDirectory();
            archivo = new File(ruta_files.getPath(), file_name);
            fr = new FileReader(archivo);
            br = new BufferedReader(fr);

            //lectura del fichero:
            String linea = br.readLine();
            while (linea != null) {
                flag = true;
                linea = br.readLine();
            }
            br.close();
            fr.close();

        } catch(Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (null != fr) {
                    fr.close();
                    br.close();
                } else {
                    //Do nothing.
                }
            } catch (Exception e2){
                e2.printStackTrace();
            }
        }
        return flag;
    }

    public static void borrar_archivo(String file) throws IOException {
        File archivo = new File(file);
        String empty_string = "";
        guardar(empty_string, file);
        archivo.delete();
    }

    public static void guardar (String contenido, String file_name) throws IOException {
        if (archivo_existe(file_name)) {
            borrar_archivo(file_name);
            FileWriter fichero = null;
            PrintWriter pw = null;
            try {
                File ruta_files = Environment.getExternalStorageDirectory();
                File ruta = new File(ruta_files.getPath(), file_name);

                Log.v("guardar", ".\n\nPath de los archivos: " + ruta_files.getPath() + "\n\n.");
                fichero = new FileWriter(ruta);
                pw = new PrintWriter(fichero);
                pw.println(contenido);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != fichero) {
                        fichero.close();

                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        } else {
            crear_archivo(file_name);
            guardar(contenido, file_name);
        }
    }

    public static String imprimir_archivo(String file_name){

        String ArchivoCompleto = "";//Aqui se lee el contenido del archivo guardado.
        if (archivo_existe(file_name)) {
            File archivo = null;
            FileReader fr = null;
            BufferedReader br = null;
            try {
                File ruta_files = Environment.getExternalStorageDirectory();
                archivo = new File(ruta_files.getPath(), file_name);
                fr = new FileReader(archivo);
                br = new BufferedReader(fr);

                //lectura del fichero:
                String linea = br.readLine();
                while (linea != null || !linea.equals("")) {
                    ArchivoCompleto = ArchivoCompleto + linea + "\n";
                    linea = br.readLine();
                }
                br.close();
                fr.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != fr) {
                        fr.close();
                        br.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        } else {
            Log.v("Imp_archivo no existe", ".\n\nArchivo " + file_name + ", no existe!\n\n.");
        }
        return ArchivoCompleto;
    }

    public static String get_impresora() {
        String impresora = "00:11:22:33:44:55";
        String file_name = "vent_active.txt";
        if (archivo_existe(file_name)) {
            File archivo = null;
            FileReader fr = null;
            BufferedReader br = null;
            try {
                File ruta_files = Environment.getExternalStorageDirectory();
                archivo = new File(ruta_files.getPath(), file_name);
                //archivo = new File(file_name);
                fr = new FileReader(archivo);
                br = new BufferedReader(fr);

                //lectura del fichero:
                String linea = br.readLine();
                impresora = linea;
                br.close();
                fr.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != fr) {
                        fr.close();
                        br.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        } else {
            Log.v("Imp_archivo no existe", ".\n\nArchivo " + file_name + ", no existe!\n\n.");
        }
        return impresora;
    }

}
