package com.example.android.sunshine.app;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public  class ForecastFragment extends Fragment{

    private ArrayAdapter<String> adapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_refresh) {
            new FetchWeatherTask().execute("94108,us");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ArrayList<String> weekForecast = new ArrayList<String>();
        weekForecast.add("Today-Sunny-88/93");
        weekForecast.add("Weds-Sunny-88/93");
        weekForecast.add("Thurs-Sunny-88/93");




        adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekForecast);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);


        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{
        @Override
        protected void onPostExecute(String[] strings) {

           if(strings != null){
               adapter.clear();
               for(int i=0; i<strings.length; i++){
                   adapter.add(strings[i]);

               }
           }
        }

        private String[] getWeatherDataFromJson(String JsonStr, int numDays) throws  JSONException{
            double maxTemp = 0;
            double minTemp = 0;
            String description = null;
            String[] resultArray = new String[numDays];


            JSONObject jsonObject = new JSONObject(JsonStr);
            JSONArray arr = jsonObject.getJSONArray("list");

            for(int i=0; i<arr.length(); i++){

                maxTemp = arr.getJSONObject(i).getJSONObject("temp").getDouble("max");
                minTemp = arr.getJSONObject(i).getJSONObject("temp").getDouble("min");
                description = arr.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("description");
                resultArray[i] = Integer.toString(i)+" "+Double.toString(maxTemp) +" " + Double.toString(minTemp) +" "+description ;
            }
            return  resultArray;

        }

        @Override
        protected String[] doInBackground(String... strings) {
            if(strings.length == 0){
                return  null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String result = null;

            int numDays = 7;
            try {
               // String urlString = "http://api.openweathermap.org/data/2.5/forecast/daily?zip="+strings[0]+"&appid=df173cb983cc4f39dd5386c449b9e9f8&units=metric&cnt=7";
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendPath("daily")
                        .appendQueryParameter("zip", strings[0])
                        .appendQueryParameter("appid", "df173cb983cc4f39dd5386c449b9e9f8")
                        .appendQueryParameter("units","metric")
                        .appendQueryParameter("cnt", Integer.toString(numDays));

                String urlString = builder.build().toString();
                Log.d("sunshine", urlString);
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if(inputStream == null){
                    result = null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuffer stringBuffer = new StringBuffer();
                String line;

                while ((line = reader.readLine()) != null){
                    stringBuffer.append(line + "\n");
                }

                if(stringBuffer.length() == 0){
                    result = null;
                }
                result = stringBuffer.toString();
                Log.d("ForecastFragment", result);
            }
            catch(IOException e){
                Log.e("Sunshine", e.toString());
                result = null;
            }
            finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("ForecastFragment", "Error closing stream");


                    }
                }
            }
           try{
               return getWeatherDataFromJson(result, numDays);

           }
           catch (JSONException e){
               Log.e("Forecast Fragment", e.getMessage());
               e.printStackTrace();
           }

            return new String[] {result};
        }


    }
}
