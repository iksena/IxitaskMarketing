package com.ixitask.ixitask.activities;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.ixitask.ixitask.R;
import com.ixitask.ixitask.utils.Constants;
import com.squareup.picasso.Picasso;

public class PhotoActivity extends AppCompatActivity {

    private static final String TAG = PhotoActivity.class.getSimpleName();
    private String imageUrl;

    @BindView(R.id.photo_view)
    PhotoView photoView;
    @BindView(R.id.btn_back)
    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_photo);
        ButterKnife.bind(this);

        if (getIntent()!=null){
            if (getIntent().getExtras()!=null) {
                imageUrl = getIntent().getStringExtra(Constants.ARG_IMAGE_URL);
            }
        }

        if (!TextUtils.isEmpty(imageUrl))
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(getResources().getDrawable(R.drawable.picture_placeholder))
                    .error(getResources().getDrawable(R.drawable.picture_placeholder))
                    .into(photoView);
        else {
            Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBack.setOnClickListener(v->finish());
    }
}
