package no.uio.tmdetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Upload the local DB to the Firebase storage
 */
public class UploadDb extends AppCompatActivity {
    EditText dbName;
    Button uploadDb;
    static int dbVersion=0;
    private String TAG = "DBUploadActivity";
    Uri dbFilePath;
    private StorageReference storageReference;
    String userDBName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_db);

        dbName = (EditText) findViewById(R.id.dbName);
        uploadDb = (Button) findViewById(R.id.uploadYourdb);
        storageReference = FirebaseStorage.getInstance().getReference();


        //get intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        dbFilePath = Uri.parse(extras.getString("db-Uri"));


        uploadDBFile();

    }
    public void uploadDBFile(){
        uploadDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String now = getTime();
                dbVersion++;
                userDBName = dbName.getText().toString();
                if (dbName.getText().length() > 0) {
                    Log.d(TAG, "onClick: Uri of db" + dbFilePath.toString());
                    if (dbFilePath != null) {
                        ProgressDialog progressDialog = new ProgressDialog(UploadDb.this);
                        progressDialog.setTitle("Uploading "+userDBName+ "-"+ now +".db");
                        progressDialog.show();
                        //upload db file to firebase storage
                        StorageReference riversRef = storageReference.child("files/"+userDBName+ "-" + now + ".db");

                        riversRef.putFile(dbFilePath)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), userDBName + "-"+ now +  ".db" +"  Uploaded", Toast.LENGTH_LONG).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle unsuccessful uploads
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage((int) progress + "% Uploaded...");

                            }
                        });
                    } else {
                        //display an error toast
                        Toast.makeText(UploadDb.this, "Error in uploading!", Toast.LENGTH_SHORT).show();

                    }
                }
                else{
                    //if user forgot to enter a name for db
                    Toast.makeText(getApplicationContext(), "you must enter your name", Toast.LENGTH_SHORT).show();

                }


            }
        });
    }
    //convert timestamp to a readable format date-time for user
    public String getTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
}