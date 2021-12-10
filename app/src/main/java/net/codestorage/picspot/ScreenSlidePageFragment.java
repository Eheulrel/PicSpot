package net.codestorage.picspot;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import net.codestorage.picspot.notimsg.NotiMsg;
import net.codestorage.picspot.storage.Storage;

public class ScreenSlidePageFragment extends Fragment {

    private int position;
    private String placeName;

    public ScreenSlidePageFragment() {
        // Required empty public constructor
    }

    public ScreenSlidePageFragment(int position, String placeName) {
        super();
        this.position = position;
        this.placeName = placeName;
    }

    public static ScreenSlidePageFragment newInstance() {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen_slide_page, container, false);

        ImageView iv = view.findViewById(R.id.fragmentImageView);

        //이미지를 받자
        StorageReference[] placeImage = {
                Storage.storageRef.child(placeName + "/0.jpg"),
                Storage.storageRef.child(placeName + "/1.jpg"),
                Storage.storageRef.child(placeName + "/2.jpg")
        };

        placeImage[position].getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageStr = uri.toString();
                Glide.with(getContext()).load(imageStr).into(iv);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                NotiMsg.notiMsg(getContext(), e.toString());
            }
        });

        return view;
    }
}