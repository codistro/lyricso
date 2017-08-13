package experiment.codistro.com.test;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


public class MainActivity extends Activity  {


    private ListView list;
    private Cursor cursor;
    private MediaAdapter mediaAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

                return;
            }}
        list = (ListView)findViewById(R.id.songs_list);

        getData();
        click(list);

    }

    public void getData(){
        String[] projection = {MediaStore.Audio.Media._ID,
                                MediaStore.Audio.Media.TITLE,
                                MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM_ID};

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                    projection,
                                                    null,
                                                    null,
                                                    null);

        mediaAdapter = new MediaAdapter(this,cursor);
        list.setAdapter(mediaAdapter);

    }


    public void click(ListView list){
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cursor = (Cursor)parent.getAdapter().getItem(position);
                Intent in = new Intent(getApplicationContext(),activity_play.class);
                in.putExtra("_ID",position);
                startActivity(in);
            }
        });
    }



}
