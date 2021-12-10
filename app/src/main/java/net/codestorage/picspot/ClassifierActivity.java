package net.codestorage.picspot;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.codestorage.picspot.notimsg.NotiMsg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ClassifierActivity extends AppCompatActivity {

    private View classifierLayout;
    private ImageButton imageButton;

    boolean isExpand = false;
    boolean isLogin = false;
    private FloatingActionButton floating_login, floating_logout, floating_profile, floating_plus;
    private ActivityResultLauncher<Intent> classifierResultLauncher;

    GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 123;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classifier);

        classifierLayout = findViewById(R.id.classifierActivityView);
        imageButton = findViewById(R.id.imageButton);
        floating_login = findViewById(R.id.login);
        floating_profile = findViewById(R.id.profile);
        floating_logout = findViewById(R.id.logout);
        floating_plus = findViewById(R.id.floating4);

        Animation layout_fadein = AnimationUtils.loadAnimation(this, R.anim.classifieractivity_fadein);
        Animation floating_fadein = AnimationUtils.loadAnimation(this, R.anim.floating_fadein);

        classifierLayout.startAnimation(layout_fadein);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 기존에 로그인 했던 계정을 확인한다.
        GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(this);

        // 로그인 되있는 경우 (토큰으로 로그인 처리)
        if (gsa != null && gsa.getId() != null) {
            //액티비티 진입하면 userID에 로그인한 ID 넣어줌, 안해주면 초기값(1) 들어감
            Variables.userID = gsa.getId();
            isLogin = true;
        }else{
            isLogin = false;
        }

        //login, logout 버튼 클릭
        floating_logout.setOnClickListener(this::onClick);
        floating_login.setOnClickListener(this::onClick);
        //profile 버튼 눌러서 profile 로 이동
        floating_profile.setOnClickListener(this::onClick);

        //floating 버튼 눌러서 메뉴 표시
        floating_plus.setOnClickListener(view -> {
            if (!isExpand) {
                isExpand = true;
                if(isLogin){
                    floating_logout.startAnimation(floating_fadein);
                    floating_profile.startAnimation(floating_fadein);
                    floating_profile.setVisibility(View.VISIBLE);
                    floating_logout.setVisibility(View.VISIBLE);
                }else{
                    floating_login.startAnimation(floating_fadein);
                    floating_login.setVisibility(View.VISIBLE);
                }
            } else {
                isExpand = false;
                floating_logout.setVisibility(View.GONE);
                floating_profile.setVisibility(View.GONE);
                floating_login.setVisibility(View.GONE);
            }
        });

        //버튼 눌러서 받아왔을 때 처리
        imageButton.setOnClickListener(view -> getImageFromGallery());

        //이미지 받아서 다음 액티비티 호출까지
        classifierResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }

                        Uri selectedImage = data.getData();
                        Bitmap bitmap = null;

                        try{
                            if(Build.VERSION.SDK_INT >= 29){
                                ImageDecoder.Source src =
                                        ImageDecoder.createSource(getContentResolver(),selectedImage);
                                bitmap = ImageDecoder.decodeBitmap(src);
                            }else{
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                            }
                        }catch (IOException ioe){
                            NotiMsg.notiMsg(ClassifierActivity.this, "Failed to read Image");
                            Log.e(TAG, "Failed to read Image", ioe);
                        }

                        //갤러리에서 사진을 선택했을때
                        if(bitmap != null){
                            Intent intent = new Intent(ClassifierActivity.this,SpotinfoActivity.class);
                            intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);

                            Bitmap finalBitmap = bitmap;

                            //갤러리에서 선택한 사진 Intent
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                            byte[] byteArray = stream.toByteArray();
                            intent.putExtra("bitmap", byteArray);

                            startActivity(intent);
                            finish();
                        }
                    }
                }
        });
    }

    private void getImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");

        classifierResultLauncher.launch(intent);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                signIn();
                isLogin = true;
                isExpand = false;
                floating_logout.setVisibility(View.GONE);
                floating_profile.setVisibility(View.GONE);
                floating_login.setVisibility(View.GONE);
                break;
            case R.id.logout:
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(this, task -> {
                            Log.d(TAG, "onClick:logout success ");
                            mGoogleSignInClient.revokeAccess()
                                    .addOnCompleteListener(this, task1 -> Log.d(TAG, "onClick:revokeAccess success "));
                        });
                Toast.makeText(this,"로그아웃 되었습니다.",Toast.LENGTH_SHORT).show();
                isLogin = false;
                isExpand = false;
                Variables.userID = "1";
                floating_logout.setVisibility(View.GONE);
                floating_profile.setVisibility(View.GONE);
                floating_login.setVisibility(View.GONE);
                break;
            case R.id.profile:
                Intent intent = new Intent(ClassifierActivity.this,BookmarkActivity.class);
                intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);

                startActivity(intent);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acct = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            if (acct != null) {
                String personName = acct.getDisplayName();
                String personGivenName = acct.getGivenName();
                String personFamilyName = acct.getFamilyName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();

                Variables.userID = personId;

                Log.d(TAG, "handleSignInResult:personName "+personName);
                Log.d(TAG, "handleSignInResult:personGivenName "+personGivenName);
                Log.d(TAG, "handleSignInResult:personEmail "+personEmail);
                Log.d(TAG, "handleSignInResult:personId "+personId);
                Log.d(TAG, "handleSignInResult:personFamilyName "+personFamilyName);
                Log.d(TAG, "handleSignInResult:personPhoto "+personPhoto);
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    boolean isBackPressed = false;
    @Override
    public void onBackPressed() {
        if (isBackPressed) {
            System.exit(0);
        } else {
            isBackPressed = true;
            Toast.makeText(this, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();

            Handler handler = new Handler();
            handler.postDelayed(() -> isBackPressed = false, R.integer.closeDoubleTouchTimeout);
        }
    }
}