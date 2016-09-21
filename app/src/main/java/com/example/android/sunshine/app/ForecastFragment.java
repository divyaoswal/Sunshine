package com.example.android.sunshine.app;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import java.text.SimpleDateFormat;
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
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather(){
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        fetchWeatherTask.execute(location);
    }

    @Override
    public void onStart(){
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ArrayList<String> weekForecast = new ArrayList<String>();




        adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekForecast);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);



        //if you want to use toast in android
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//        @Override
//        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//            String forecast = adapter.getItem(position);
//            Toast.makeText(getActivity(),forecast,Toast.LENGTH_SHORT).show();
//            }
//        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String forecast = adapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }

        });

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


        private String getReadableDateString(long time){

            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        private String formatHighLows(double high, double low){
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = sharedPrefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_metric));

            if(unitType.equals(getString(R.string.pref_units_imperial))) {
                high = (high * 1.8) + 32;
                low = (low * 1.8) + 32;
            }
            else if(!unitType.equals(getString(R.string.pref_units_metric))){
                Log.d("ForecastFragment", "unit type not found:" +unitType);

            }

            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);
            String highlowStr = roundedHigh + "/" + roundedLow;
            return highlowStr;
        }

        private String[] getWeatherDataFromJson(String JsonStr, int numDays) throws  JSONException{
            double maxTemp = 0;
            double minTemp = 0;
            String description = null;
            String day;
            String[] resultArray = new String[numDays];

            Time dayTime = new Time();
            dayTime.setToNow();

            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            dayTime = new Time();

            JSONObject jsonObject = new JSONObject(JsonStr);
            JSONArray arr = jsonObject.getJSONArray("list");

            for(int i=0; i<arr.length(); i++){

                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                maxTemp = arr.getJSONObject(i).getJSONObject("temp").getDouble("max");
                minTemp = arr.getJSONObject(i).getJSONObject("temp").getDouble("min");

                description = arr.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("description");
                resultArray[i] = day +" "+ formatHighLows(maxTemp,minTemp) +" "+description ;
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
