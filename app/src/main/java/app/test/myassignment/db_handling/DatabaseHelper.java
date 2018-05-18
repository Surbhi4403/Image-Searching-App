package app.test.myassignment.db_handling;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import app.test.myassignment.api_handling.pojo.Value;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private final String TAG = "DatabaseHelperClass";
    private static final int databaseVersion = 1;
    private static final String databaseName = "ImageDB";
    private static final String TABLE_IMAGE = "ImageTable";

    // Image Table Columns names
    private static final String COL_ID = "col_id";
    private static final String IMAGE_ID = "image_id";
    private static final String IMAGE_TAG = "image_tag";
    private static final String IMAGE_BITMAP = "image_bitmap";
    private static final String IMAGE_THUMBNAIL = "image_thumbnail";

    public DatabaseHelper(Context context) {
        super(context, databaseName, null, databaseVersion);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_IMAGE_TABLE = "CREATE TABLE " + TABLE_IMAGE + "("
                + COL_ID + " INTEGER PRIMARY KEY ,"
                + IMAGE_ID + " TEXT,"
                + IMAGE_BITMAP + " TEXT, "
                + IMAGE_TAG + " TEXT, "
                + IMAGE_THUMBNAIL + " TEXT )";
        sqLiteDatabase.execSQL(CREATE_IMAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGE);
        onCreate(sqLiteDatabase);
    }

    public void insetImage(Drawable dbDrawable, Value value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(IMAGE_ID, value.getImageId());
        Bitmap bitmap = ((BitmapDrawable) dbDrawable).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        values.put(IMAGE_BITMAP, stream.toByteArray());
        values.put(IMAGE_TAG, value.getSearchTerm());
        values.put(IMAGE_THUMBNAIL, value.getThumbnailUrl());
        db.insert(TABLE_IMAGE, null, values);
        db.close();
    }

    public boolean isRecordExist(String imageId) {
        boolean result = false;
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "Select * from " + TABLE_IMAGE + " where " + IMAGE_ID
                + "='" + imageId + "'";
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor != null && cursor.getCount() > 0) {
            return true;
        }
        cursor.close();
        db.close();
        return result;
    }

    public List<Value> getImages(String imageTag) {
        List<Value> imageHelpers = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + TABLE_IMAGE + " WHERE " + IMAGE_TAG + "='" + imageTag + "'";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Value imageHelper = new Value();
                imageHelper.setImageId(cursor.getString(1));
                imageHelper.setImageByteArray(cursor.getBlob(2));
                imageHelper.setSearchTerm(cursor.getString(3));
                imageHelper.setThumbnailUrl(cursor.getString(4));
                imageHelpers.add(imageHelper);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return imageHelpers;
    }

}
