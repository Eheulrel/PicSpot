package net.codestorage.picspot.database.userinfo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import net.codestorage.picspot.MainActivity;
import net.codestorage.picspot.Variables;

import java.util.HashMap;
import java.util.Map;

public class FirestoreUserInfo {
    FirebaseFirestore db;
    Context context;

    //Context 받아와서 초기화
    public FirestoreUserInfo(Context context) {
        db = MainActivity.db;
        this.context = context;
    }

    public void set(String placeName, String name, String photo, FavoriteSetSuccessInterface ssif) {
        Map<String, Object> data = new HashMap<>();
        data.put(Variables.date, Timestamp.now());
        data.put(Variables.name, name);
        data.put(Variables.photo, photo);

        db.collection(Variables.userInfo)
                .document(Variables.userID)
                .collection(Variables.favorites)
                .document(placeName)

                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ssif.setSuccessMethod();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        notiMsg("에러가 발생했습니다. : " + e.toString());
                    }
                });
    }
    public void read(String placeName, FavoriteReadSuccessInterface rsif) {
        DocumentReference docRef =db.collection(Variables.userInfo)
                .document(Variables.userID)
                .collection(Variables.favorites)
                .document(placeName);


        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("",  document.getId()+ " => " + document.getData().toString());
                    } else {
                        Log.d("",  "No Data");
                    }
                    rsif.readSuccessMethod(document);
                } else {
                    Log.d("", "Error getting documents: ", task.getException());
                }
            }
        });

    }
    public void delete(String placeName, FavoriteDeleteSuccessInterface dsif){
        db.collection(Variables.userInfo)
                .document(Variables.userID)
                .collection(Variables.favorites)
                .document(placeName)

                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dsif.deleteSuccessMethod();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        notiMsg("에러가 발생하였습니다 : " + e.toString());
                    }
                });
    }
    public void notiMsg(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                });
        builder.create();
        builder.show();

    }

    private boolean dialogMsg(String msg) {

        return false;
    }
}