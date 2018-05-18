package app.test.myassignment.ui_handling.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import app.test.myassignment.CommonFunctions;
import app.test.myassignment.R;
import app.test.myassignment.api_handling.pojo.Value;


/**
 * A fragment for displaying an image.
 */
public class ImageFragment extends Fragment {

    private static final String KEY_IMAGE_RES = "app.test.myassignment.key.imageRes";
    private static final String KEY_IMAGE_BYTE = "app.test.myassignment.key.imageByte";

    public static ImageFragment newInstance(Value drawableRes, int viewFrom) {
        ImageFragment fragment = new ImageFragment();
        Bundle argument = new Bundle();
        argument.putString(KEY_IMAGE_RES, drawableRes.getThumbnailUrl());
        argument.putByteArray(KEY_IMAGE_BYTE, drawableRes.getImageByteArray());
        argument.putInt(CommonFunctions.viewFrom, viewFrom);
        fragment.setArguments(argument);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_image, container, false);

        Bundle arguments = getArguments();
        String imageRes = arguments.getString(KEY_IMAGE_RES);
        int viewFrom = arguments.getInt(CommonFunctions.viewFrom);

        // Just like we do when binding views at the grid, we set the transition name to be the string
        // value of the image res.
        view.findViewById(R.id.image).setTransitionName(imageRes);
        if (viewFrom == CommonFunctions.Online) {
            //Set Image from URL
            Picasso.with(getContext())
                    .load(imageRes)
                    .into(view.findViewById(R.id.image), new Callback() {
                        @Override
                        public void onSuccess() {
                            getParentFragment().startPostponedEnterTransition();
                        }

                        @Override
                        public void onError() {
                            getParentFragment().startPostponedEnterTransition();
                        }
                    });
        } else {
            //Set image from local DB
            byte[] byteArray = arguments.getByteArray(KEY_IMAGE_BYTE);
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            Drawable bitmapDrawable = new BitmapDrawable(getContext().getResources(), bmp);
            ((ImageView) view.findViewById(R.id.image)).setImageDrawable(bitmapDrawable);
            getParentFragment().startPostponedEnterTransition();
        }
        return view;
    }
}
