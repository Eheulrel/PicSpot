package net.codestorage.picspot.storage;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Storage {
    public static StorageReference storageRef = FirebaseStorage.getInstance().getReference();
}
