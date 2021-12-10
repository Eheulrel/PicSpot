package net.codestorage.picspot.database.spotinfo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import net.codestorage.picspot.MainActivity;
import net.codestorage.picspot.Variables;
import net.codestorage.picspot.notimsg.NotiMsg;

public class FirestoreSpotInfo {
    FirebaseFirestore db;
    Context context;

    //Context 받아와서 초기화
    public FirestoreSpotInfo(Context context) {
        db = MainActivity.db;
        this.context = context;
    }

    public void read(String placeName, SpotInfoReadSuccessInterface sirsif) {
        DocumentReference docRef =db.collection(Variables.spotInfo)
                .document(placeName);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        sirsif.readSuccessMethod(document.getData());
                    } else {
                        NotiMsg.notiMsg(context,"데이터가 존재하지 않습니다.");
                    }
                } else {
                    NotiMsg.notiMsg(context,"에러가 발생하였습니다 : " + task.getException().toString());
                }
            }
        });

    }
}