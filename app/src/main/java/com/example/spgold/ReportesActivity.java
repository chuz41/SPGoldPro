package com.example.spgold;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ReportesActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);
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


    public void rep_ventas(View view) {
        Intent rep_sales = new Intent(this, RepventasActivity.class);
        startActivity(rep_sales);
        finish();
        System.exit(0);
    }

    public void rep_winners(View view) {
        Intent winners = new Intent(this, WinnersActivity.class);
        startActivity(winners);
        finish();
        System.exit(0);
    }

    public void invoice_admin(View view) {
        Intent facturas = new Intent(this, FacturaseditActivity.class);
        startActivity(facturas);
        finish();
        System.exit(0);
    }


}