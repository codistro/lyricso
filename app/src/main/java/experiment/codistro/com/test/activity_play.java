package experiment.codistro.com.test;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class activity_play extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{

    private SeekBar seekbar;
    private Cursor cursor;
    private static MediaPlayer player;
    private String path;
    private Handler handler = new Handler();
    private ImageView album_image;
    private Bitmap album_art;
    private String baseUrl = "https://api.musixmatch.com/ws/1.1/matcher.lyrics.get?format=json&callback=callback&q_track=";
    private String api = "&apikey=ac72a1636955000a9b3d39b964633dd6";
    private String another = "&q_artist=";
    private String title;
    private String artist;
    private TextView lyricsView;
    private String finalUrl;
    private int id;
    public static final String LOG_TAG = activity_play.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        Intent mIntent = getIntent();
        id = mIntent.getIntExtra("_ID",0);
        getCursor(id);
        path = getCursor(id);
        play(path);

        //album_image = (ImageView)findViewById(R.id.album_art);
        seekbar = (SeekBar)findViewById(R.id.seek);
        lyricsView = (TextView)findViewById(R.id.lyrics);
        seekbar.setOnSeekBarChangeListener(this);
        seekUpdation();

        downloadLyrics();
    }



    public void downloadLyrics(){
        title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim();
        artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)).trim();
        title = convert(title);
        artist = convert(artist);
        finalUrl = baseUrl+title+another+artist+api;
        lyricsView.setText("");
        downloadLyricsAsyncTask download = new downloadLyricsAsyncTask();
        download.execute();

    }


    public static String convert(String title){
        int temp = 0;
        int id = title.indexOf(" ");
        if(id == -1){
            return title;
        }
        StringBuilder s = new StringBuilder();
        int beg = 0;
        while(id!=-1){
            s.append(title.substring(beg,id));
            beg = id+1;
            s.append("%20");
            temp = id;
            id = title.indexOf(" ",temp+1);
        }
        s.append(title.substring(temp+1));
        return s.toString();
    }

    private class downloadLyricsAsyncTask extends AsyncTask<URL, Void, String> {
        ProgressDialog pd = new ProgressDialog(activity_play.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("loading");
            pd.show();
        }

        @Override
        protected String doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl(finalUrl);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                e.getStackTrace();
            }

            // Extract relevant fields from the JSON response and create an {@link Event} object
            String lyrics = extractFeatureFromJson(jsonResponse);

            // Return the {@link Event} object as the result fo the {@link TsunamiAsyncTask}
            return lyrics;
        }

        @Override
        protected void onPostExecute(String lyrics) {
            if (lyrics == null) {
                pd.hide();
                Toast.makeText(activity_play.this,"NO LYRICS FOUND",Toast.LENGTH_SHORT).show();
                lyricsView.setText("NO LYRICS FOUND");
                return;
            }
            pd.hide();
            updateUi(lyrics);
        }
    }

    private URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();
            if(urlConnection.getResponseCode()==200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
        } catch (IOException e) {
            e.getStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private String extractFeatureFromJson(String lyricsJSON) {

            if(TextUtils.isEmpty(lyricsJSON))
                return null;
            try {
                JSONObject baseJsonResponse = new JSONObject(lyricsJSON);
                JSONObject message = baseJsonResponse.getJSONObject("message");
                JSONObject body = message.getJSONObject("body");
                JSONObject lyrics = body.getJSONObject("lyrics");
                String finalLyrics = lyrics.getString("lyrics_body");

               return finalLyrics;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the lyrics JSON results", e);
            }
            return null;
    }

    private void updateUi(String lyrics) {
            lyricsView.setText(lyrics);
    }

   /* private void setImage(){
        Long album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        album_art = getArtistImage(album_id.toString());
        if(album_art != null)
            album_image.setImageBitmap(album_art);
        else{
            Drawable myDrawable = getResources().getDrawable(R.drawable.music_ph);
            album_image.setImageDrawable(myDrawable);
        }
    }*/


    public String getCursor(int id){
        String[] projection = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID};
        cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null);
        cursor.moveToPosition(id);
        String s = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        return s;
    }



    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(player!=null) {
            player.seekTo(seekBar.getProgress());
        }
    }

    public Bitmap getArtistImage(String albumid) {
        Bitmap artwork = null;
        try {
            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri,
                    Long.valueOf(albumid));
            ContentResolver res = getApplicationContext().getContentResolver();
            InputStream in = res.openInputStream(uri);
            artwork = BitmapFactory.decodeStream(in);

        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
        return artwork;
    }



    public void play(String s){
        if(player!=null)
            stop();
        player = new MediaPlayer();
        try {
            player.setDataSource(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.prepareAsync();

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                play();

            }
        });

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                next();
            }
        });


    }

    public void play() {
        player.start();
        seekbar.setMax(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
        //setImage();
        downloadLyrics();
    }

    public void stop(View view) {
        if(player!=null) {
            /*player.stop();
            player.release();
            player=null;*/
            player.pause();
        }
    }

    public void stop(){
        if(player!=null) {
            /*player.stop();
            player.release();
            player=null;*/
            player.pause();
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };


    public void seekUpdation(){
        if(player!=null)
            seekbar.setProgress(player.getCurrentPosition());
        handler.postDelayed(runnable,1000);
    }



    public void next(View view) {
        if(cursor!=null){
            if(cursor.isLast())
                cursor.moveToFirst();
            cursor.moveToNext();

            String s = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            play(s);
            //setImage();
            downloadLyrics();
        }
    }

    public void next() {
        if(cursor!=null){
            if(cursor.isLast())
                cursor.moveToFirst();
            cursor.moveToNext();

            String s = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            play(s);
           // setImage();
            downloadLyrics();
        }
    }

    public void prev(View view) {
        if(cursor!=null){
            if(cursor.isFirst())
                cursor.moveToLast();
            cursor.moveToPrevious();
            String s = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            play(s);
           // setImage();
            downloadLyrics();
        }
    }

    public void play(View view) {
        play();
    }
}
