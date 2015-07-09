package com.example.manumaheshwari.myapplication;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends ListActivity {

    private ProgressDialog pDialog;

    // URL to get pending rides JSON
    private static String url = "http://128.199.206.145/vigo/v1/pendingtrips/1";

    // JSON Node names
    private static final String TAG_SOURCE = "source";
    private static final String TAG_DESTINATION = "destination";
    private static final String TAG_DATE = "date";
    private static final String TAG_NAME = "name";

    // pending rides JSONArray
    JSONArray pendingRides = null;
    ListView lv;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> pendingRidesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        pendingRidesList = new ArrayList<HashMap<String, String>>();

        lv = getListView();

        // Calling async task to get json
        new GetRides().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class GetRides extends AsyncTask<Void, Void, Void> {




        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }
        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();




            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.d("Response: "  , "> " + jsonStr);

            if (jsonStr != null) {

                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    pendingRides = jsonObj.getJSONArray("trip");

                    // looping through All Contacts
                    for (int i = 0; i < pendingRides.length(); i++) {
                        JSONObject c = pendingRides.getJSONObject(i);

                        String source = c.getString(TAG_SOURCE);
                        String destination = c.getString(TAG_DESTINATION);
                        String date = c.getString(TAG_DATE);

                        Log.d("here", source + destination + date);

                        // tmp hashmap for single contact
                        HashMap<String, String> pendingRide = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        pendingRide.put(TAG_SOURCE, source);
                        pendingRide.put(TAG_DESTINATION, destination);
                        pendingRide.put(TAG_DATE, date);


                        // adding pending ride to pending ride list
                        pendingRidesList.add(pendingRide);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, pendingRidesList,
                    R.layout.list_view, new String[] { TAG_SOURCE, TAG_DESTINATION, TAG_DATE}, new int[] { R.id.source,R.id.destination, R.id.date});

            setListAdapter(adapter);
            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                    Toast.makeText(getApplicationContext(),
                            "Click ListItem Number " + position + "  " + pendingRidesList.get(position).get("source") +"to" + pendingRidesList.get(position).get(TAG_DESTINATION), Toast.LENGTH_LONG)
                            .show();

                    Intent i = new Intent(MainActivity.this, FreeDriversActivity.class);
                    i.putExtra(TAG_SOURCE, pendingRidesList.get(position).get(TAG_SOURCE));
                    i.putExtra(TAG_DESTINATION, pendingRidesList.get(position).get(TAG_DESTINATION));
                    i.putExtra("postition", position);
                    startActivity(i);


                }
            });



        }

    }
}
