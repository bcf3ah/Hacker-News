package tech.bfitzsimmons.hackernewsv3;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> urls = new ArrayList<>();
    ArrayList<String> content = new ArrayList<>();
    ArrayAdapter<String> adapter;
//    SQLiteDatabase articleDB;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get listView
        listView = (ListView) findViewById(R.id.listView);

        //initialize adapter and listView adapter
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(adapter);

        //set up networking queue
        queue = Volley.newRequestQueue(this);

        //do click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                intent.putExtra("url", urls.get(i));
                startActivity(intent);
            }
        });

        //perform HTTP request
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty", null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if(response != null && response.length() > 0){
                    Type type = new TypeToken<ArrayList<String>>(){}.getType();
                    ArrayList<String> ids = new Gson().fromJson(response.toString(), type);
                    getContentFromIds(ids);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        queue.add(jsonArrayRequest);
    }

    public void getContentFromIds(ArrayList<String> ids){
        int num = 20;
        if(ids.size() < 20)
            num = ids.size();

        for (int i = 0; i < num; i++) {
            String articleId = ids.get(i);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://hacker-news.firebaseio.com/v0/item/" + articleId + ".json?print=pretty", null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    getTitlesAndUrls(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            queue.add(jsonObjectRequest);
        }
    }

    public void getTitlesAndUrls(JSONObject object){
        if(!object.isNull("title") && !object.isNull("url")){
            try {
                titles.add(object.getString("title"));
                urls.add(object.getString("url"));
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
