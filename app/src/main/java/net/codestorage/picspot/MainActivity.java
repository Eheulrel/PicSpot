package net.codestorage.picspot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private TextView textView1, textView2, textView3;
    private View mainLayout;

    public static FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //db를 만들자
        db = FirebaseFirestore.getInstance();

        mainLayout = findViewById(R.id.mainActivityView);
        fab = findViewById(R.id.fab);
        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);

        Animation slide_up;
        slide_up = AnimationUtils.loadAnimation(this, R.anim.slideup);

        Animation fade_in;
        fade_in = AnimationUtils.loadAnimation(this, R.anim.fadein);

        Animation fade_out;
        fade_out = AnimationUtils.loadAnimation(this, R.anim.fadeout);

        Animation mainActivity_fade_out;
        mainActivity_fade_out = AnimationUtils.loadAnimation(
                this, R.anim.mainactivity_fadeout);

        mainLayout.startAnimation(fade_in);

        Handler handler = new Handler();
        Intent intent = new Intent(this, ClassifierActivity.class);
        intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);

        fab.setOnClickListener(view -> {
            textView1.startAnimation(slide_up);
            textView2.startAnimation(slide_up);
            textView3.startAnimation(slide_up);
            fab.startAnimation(fade_out);

            handler.postDelayed(() -> {
                fab.hide();
                mainLayout.startAnimation(mainActivity_fade_out);

            }, fade_out.getDuration()-50);

            handler.postDelayed(() -> {
                startActivity(intent);
                finish();
            }, fade_out.getDuration()+mainActivity_fade_out.getDuration());

        });

    }

}