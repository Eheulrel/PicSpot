package net.codestorage.picspot;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import net.codestorage.picspot.notimsg.NotiMsg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ShareImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_image);

        gotoSpotInfo();
    }

    private void gotoSpotInfo() {

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    //이미지랑 결과값 넘겨줘야함
                    Bitmap bitmap = null;

                    try {
                        if (Build.VERSION.SDK_INT >= 29) {
                            ImageDecoder.Source src =
                                    ImageDecoder.createSource(getContentResolver(), imageUri);
                            bitmap = ImageDecoder.decodeBitmap(src);
                        } else {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        }
                    } catch (IOException ioe) {
                        NotiMsg.notiMsg(this, "Failed to read Image");
                        Log.e(TAG, "Failed to read Image", ioe);
                    }

                    if (bitmap != null) {
                        Intent intentNew = new Intent(this,SpotinfoActivity.class);
                        intentNew.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);

                        Bitmap finalBitmap = bitmap;

                        //갤러리에서 선택한 사진 Intent
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                        byte[] byteArray = stream.toByteArray();
                        intentNew.putExtra("bitmap", byteArray);

                        startActivity(intentNew);
                        finish();
                    }
                }
            }
        }
    }
}