package net.codestorage.picspot.database.userinfo;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

public interface FavoriteReadSuccessInterface {
    public void readSuccessMethod(DocumentSnapshot result);
}