package es.uma.aracelimanzano.dondereciclomlg;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.style.BulletSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private String solidsURL = "http://datosabiertos.malaga.eu/recursos/ambiente/limasa/cntrsu-4326.csv";
    private String plasticsURL = "http://datosabiertos.malaga.eu/recursos/ambiente/limasa/cntenvases-4326.csv";
    private String paperURL = "http://datosabiertos.malaga.eu/recursos/ambiente/limasa/cntpapelCarton-4326.csv";
    private String industrialWasteURL = "http://datosabiertos.malaga.eu/recursos/ambiente/limasa/cntindustria-4326.csv";
    private String date;

    public static ArrayList<RecPoint> recyclePointsSolids = new ArrayList<RecPoint>();
    public static ArrayList<RecPoint> recyclePointsPlastics = new ArrayList<RecPoint>();
    public static ArrayList<RecPoint> recyclePointsPaper = new ArrayList<RecPoint>();
    public static ArrayList<RecPoint> recyclePointsIndustrialWaste = new ArrayList<RecPoint>();

    TextView update;
    ImageButton buttonPlastics;
    ImageButton buttonSolids;
    ImageButton buttonPaper;
    ImageButton buttonIndustrialWaste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        update = (TextView) findViewById(R.id.textViewUpdate);
        buttonPlastics = (ImageButton) findViewById(R.id.buttonPlastics);
        buttonSolids = (ImageButton) findViewById(R.id.buttonSolids);
        buttonPaper = (ImageButton) findViewById(R.id.buttonPaper);
        buttonIndustrialWaste = (ImageButton) findViewById(R.id.buttonIndustrialWaste);

    }

    public void onClick (View v) throws InterruptedException {

        buttonSolids.setEnabled(false);
        buttonPlastics.setEnabled(false);
        buttonPaper.setEnabled(false);

        switch(v.getId()){

            case R.id.buttonSolids:
                new DownloadCSVTask().execute(solidsURL,"s");
                Thread.sleep(3000);
                if (recyclePointsSolids.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Error de conexión", Toast.LENGTH_LONG).show();
                }
                else {
                    Intent solidsMap = new Intent(MainActivity.this, SolidsMapActivity.class);
                    startActivity(solidsMap);}
                break;

            case R.id.buttonPlastics:
                new DownloadCSVTask().execute(plasticsURL,"pl");
                Thread.sleep(3000);
                if (recyclePointsPlastics.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Error de conexión", Toast.LENGTH_LONG).show();
                }
                else {
                    Intent plasticsMap = new Intent(MainActivity.this, PlasticsMapActivity.class);
                    startActivity(plasticsMap);}
                break;

            case R.id.buttonPaper:
                new DownloadCSVTask().execute(paperURL,"p");
                Thread.sleep(3000);
                if (recyclePointsPaper.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Error de conexión", Toast.LENGTH_LONG).show();
                }
                else {
                    Intent paperMap = new Intent(MainActivity.this, PaperMapActivity.class);
                    startActivity(paperMap);
                }
                break;

            case R.id.buttonIndustrialWaste:
                new DownloadCSVTask().execute(industrialWasteURL,"ind");
                Thread.sleep(3000);
                if (recyclePointsIndustrialWaste.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "", Toast.LENGTH_LONG).show();
                }
                else {
                    Intent industrialWasteMap = new Intent(MainActivity.this, IndustrialWasteMapActivity.class);
                    startActivity(industrialWasteMap);
                }
                break;

            case R.id.imageWebLimasa:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.limasa3.es/")));
                break;
        }
    }


    private class DownloadCSVTask extends AsyncTask<String,Void,Long>{

        @Override
        protected Long doInBackground(String... strings){

            long res = 0;

            try{
                URL stockURL = new URL(strings[0]);
                BufferedReader in = new BufferedReader(new InputStreamReader(stockURL.openStream()));
                CSVReader reader = new CSVReader(in);

                String[] nextLine;
                boolean header = true;
                RecPoint recyclePoint;
                String POINT;
                String geos [];
                int id = 0;

                while((nextLine = reader.readNext()) != null){

                    if (header){
                        header = false;
                    }

                    else{
                        recyclePoint = new RecPoint(id,nextLine[2],Integer.parseInt(nextLine[3]),Integer.parseInt(nextLine[4]));

                        POINT = nextLine[7];
                        POINT=POINT.replace('(',' ');
                        POINT=POINT.replace(')',' ');
                        geos = POINT.split(" ");
                        recyclePoint.setLatLng(Double.parseDouble(geos[3]), Double.parseDouble(geos[2]));

                        recyclePoint.setAvailableUpdate(nextLine[5]);

                        if (strings[1].equals("s")) {
                            recyclePointsSolids.add(recyclePoint);
                        }
                        else if (strings[1].equals("pl")){
                            recyclePointsPlastics.add(recyclePoint);
                        }
                        else if (strings[1].equals("p")){
                            recyclePointsPaper.add(recyclePoint);
                        }
                        else if (strings[1].equals("ind")){
                            recyclePointsIndustrialWaste.add(recyclePoint);
                        }
                        else{
                            res = -1;
                            throw new RecPointException();
                        }

                        id++;
                    }
                }

                res = 1;
            }

            catch (MalformedURLException mue){
                res = -1;
                throw new RecPointException();
            }

            catch (IOException ioe){
                res = -1;
                throw new RecPointException();
            }

            return res;
        }

        @Override
        protected void onPostExecute(Long res){

            buttonSolids.setEnabled(true);
            buttonPlastics.setEnabled(true);
            buttonPaper.setEnabled(true);

            if (res==1){

                Calendar rightNow = Calendar.getInstance();
                date = rightNow.getTime().toString();
                update.setText("Última actualización: "+ date);
            }

            else {
                update.setText("Error de conexión");
            }
        }
    }

}
