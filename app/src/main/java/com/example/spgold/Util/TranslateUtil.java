package com.example.spgold.Util;

//convierte archivos (ficheros) a formato Json para poder enviarlos a Google Sheets

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TranslateUtil {

    public static JSONObject string_to_Json(String s, String spreadSheetId, String sheet, String factura) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("spreadsheet_id", spreadSheetId);
        jsonObject.put("sheet", sheet);
        JSONArray rowsArray = new JSONArray();

        String[] split = s.split("_l_");// la letra "l" representa la linea.

        int cont = 0;
        for (int i = 0; i < split.length; i++) {
            String id = "";
            id = factura + String.valueOf(cont);//Primer venta genera la primera fila, cuyo ID debe ser 10. Segunda venta: ID = 20
            String[] split2 = split[i].split("_n_");// la letra "n" representa los numeros de cada linea
            JSONArray row = new JSONArray();
            row.put(split2[0]);
            row.put(split2[1]);
            row.put(split2[2]);
            row.put(split2[3]);
            row.put(split2[4]);
            row.put(id);
            rowsArray.put(row);
            cont++;
        }

        jsonObject.put("rows", rowsArray);

        return jsonObject;
    }

    public static JSONObject premiados_to_Json_subir(String numero1, String numero2, String numero3, String extra, String spreadSheetId, String sheet, String ID, String Loteria, String Horario) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("spreadsheet_id", spreadSheetId);
        jsonObject.put("sheet", sheet);
        JSONArray rowsArray = new JSONArray();
        String id = "";
        id = ID;
        JSONArray row = new JSONArray();
        row.put(Loteria);
        row.put(Horario);
        row.put(numero1);
        row.put(numero2);
        row.put(numero3);
        row.put(extra);
        row.put(id);
        rowsArray.put(row);
        jsonObject.put("rows", rowsArray);

        return jsonObject;
    }

}
