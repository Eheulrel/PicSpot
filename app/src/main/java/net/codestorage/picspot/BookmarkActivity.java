package net.codestorage.picspot;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class BookmarkActivity extends AppCompatActivity {

    private TextView username, email;

    private String personEmail;
    private String personName;

    private ListView listView;
    private SingleAdapter adapter;

    private String TAG = "BOOKMARK";

    private ArrayList<locate> arrayList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        //커스텀 리스트뷰 시작
        listView = findViewById(R.id.listView);
        arrayList = new ArrayList<>(); //그룹객체를 담을 리스트
        //db 불러오기
        db = MainActivity.db;

        //db 경로지정
        CollectionReference docRef =db.collection(Variables.userInfo)
                .document(Variables.userID)
                .collection(Variables.favorites);

        //저장된 데이터들 불러오기
        docRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                arrayList.clear(); // 기존 배열리스트 초기화
                //memo -> name 으로 변경해야함, 테스트용 memo
                //value값만큼 반복
                for (QueryDocumentSnapshot doc : value) {
                    if (doc.get("name") != null) {
                        //locate(string, string) 에 name, photo 데이터 저장
                        locate plist = new locate(doc.getString("name"),doc.getString("photo"));
                        //arrayList에 plist에 저장된 값 저장
                        arrayList.add(plist);
                    }
                }
//                Log.d(TAG, "Current cites in CA: " + arrayList.get(0).getTitle());
                //어댑터로 데이터 전송, Listener 밖에서 선언하면 null값 전송
                adapter = new SingleAdapter(arrayList, BookmarkActivity.this);
                listView.setAdapter(adapter);
            }
        });

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            personName = acct.getDisplayName();
            personEmail = acct.getEmail();
        }

        username = findViewById(R.id.Name);
        email = findViewById(R.id.Email);

        if(personName != null) {
            username.setText(personName);
            email.setText(personEmail);
        }else{
            username.setText(personEmail);
            email.setText(personEmail);
        }

    }
}

