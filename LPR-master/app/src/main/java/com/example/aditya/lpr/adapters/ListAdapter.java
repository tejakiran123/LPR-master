package com.example.aditya.lpr.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aditya.lpr.R;

import java.util.ArrayList;

/**
 * Created by aditya on 13/10/16.
 */
public class ListAdapter extends ArrayAdapter<String> {

    private static Context mContext;
    private ArrayList<String> mVehicleImages; // List of file paths
    private LayoutInflater mLayoutInflater;

    @Override
    public int getCount() {
        return mVehicleImages.size();
    }

    private static final String LOG_TAG = ListAdapter.class.getSimpleName();

    public ListAdapter(Context context, int resource, ArrayList<String> vehicle_images) {
        super(context, resource, vehicle_images);
        mContext = context;
        mVehicleImages = vehicle_images;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get an instance of ViewHolder class
        ViewHolder viewHolder;

        if(convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.list_view_item, null);

            // initialize our views
            viewHolder.licensePlateTextView = (TextView) convertView.findViewById(R.id.license_plate_number);
            viewHolder.licensePlateImageView = (ImageView) convertView.findViewById(R.id.vehicle_image);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        // Assign data to the views

        // temporarily set license plate number to "processing" string
        viewHolder.licensePlateTextView.setText("Processing...");

        // Set image
        Log.v(LOG_TAG,"Setting the image!");

        int reqWidth = dpToPx(100);
        int reqHeight = dpToPx(100);

        Bitmap myBitmap = decodeSampledBitmapFromResource(mVehicleImages.get(position),reqWidth,reqHeight);
        viewHolder.licensePlateImageView.setImageBitmap(myBitmap);

        return convertView;
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static int dpToPx(int dp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static Bitmap decodeSampledBitmapFromResource(String path,int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path,options);
    }

    public class ViewHolder {
        TextView licensePlateTextView;
        ImageView licensePlateImageView;
    }
}