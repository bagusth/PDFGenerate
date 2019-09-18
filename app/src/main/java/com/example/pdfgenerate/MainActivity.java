package com.example.pdfgenerate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PdfCreaorActivity";
    private EditText mContentEditText;
    private Button mButtoncreate;
    private File pdfFile;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContentEditText = findViewById(R.id.edit_text_content);
        mButtoncreate = findViewById(R.id.buttoncreate);
        mButtoncreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContentEditText.getText().toString().isEmpty()){
                    mContentEditText.setError("Fill this blank is required !!");
                    mContentEditText.requestFocus();
                    return;
                }
                try {
                    createPdfWrapper();

                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }catch (DocumentException d){
                    d.printStackTrace();
                }
            }
        });
    }



    private void createPdfWrapper() throws  FileNotFoundException, DocumentException {
        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)){
                    showMessageOKCancel("U need to allow access to Storage", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new  String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
                            }

                        }
                    });
                    return;
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        }else {
            createPDF();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        createPdfWrapper();
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }

                }else {
                    Toast.makeText(this, "WRITE_EXTERNAL Permission Denied", Toast.LENGTH_LONG).show();
                }
                break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener onClickListener) {
        new  AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("Ok",onClickListener)
                .setNegativeButton("Exit", null)
                .create()
                .show();
    }


    private void createPDF() throws FileNotFoundException, DocumentException{
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
        if (!docsFolder.exists()){
            docsFolder.mkdir();
            Log.i(TAG, "Buat Folder PDF Baru");
        }

        pdfFile = new File(docsFolder.getAbsolutePath(), "PDFGenerate.pdf");
        OutputStream outputStream = new FileOutputStream(pdfFile);
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        document.add(new Paragraph(mContentEditText.getText().toString()));
        document.close();
        previewPDF();
    }

    private void previewPDF() {
        PackageManager packageManager = getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);

        if (list.size() > 0){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(pdfFile);
            intent.setDataAndType(uri, "application.pdf");

            startActivity(intent);
        }else {
            Toast.makeText(this, "Download PDF Viewer Apps for the Result", Toast.LENGTH_LONG).show();
        }
    }


}