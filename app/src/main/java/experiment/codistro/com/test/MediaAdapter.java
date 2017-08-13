package experiment.codistro.com.test;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;



public class MediaAdapter extends CursorAdapter {

    Context context;

    public MediaAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView text_title = (TextView)view.findViewById(R.id.song_title);
        TextView text_artist = (TextView)view.findViewById(R.id.song_artist);
        ImageView album_image = (ImageView)view.findViewById(R.id.image_list);

        Long album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        Bitmap album_art = getArtistImage(album_id.toString());
        if(album_art != null)
            album_image.setImageBitmap(album_art);
        else{
            Drawable myDrawable = context.getResources().getDrawable(R.drawable.music_ph);
            album_image.setImageDrawable(myDrawable);
        }

        int title_index = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artist_index = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

        String title = cursor.getString(title_index);
        String artist = cursor.getString(artist_index);


        text_title.setText(title);
        text_artist.setText(artist);
    }


    public Bitmap getArtistImage(String albumid) {
        Bitmap artwork = null;
        try {
            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri,
                    Long.valueOf(albumid));
            ContentResolver res = context.getContentResolver();
            InputStream in = res.openInputStream(uri);
            artwork = BitmapFactory.decodeStream(in);

        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
        return artwork;
    }
}
