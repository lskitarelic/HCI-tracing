package com.example.pressuresignature;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    CaptureSignatureView mSig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout mContent = findViewById(R.id.linearLayout);
        mSig = new CaptureSignatureView(this, null);
        mContent.addView(mSig, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        SharedPreferences prefs = this.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String name = prefs.getString("name", "");
        String input_type = prefs.getString("input", "");
        String task;
        switch(mSig.count) {
            case 1:
                task = "_e1";
                break;
            case 2:
                task = "_e2";
                break;
            case 3:
                task = "_e3";
                break;
            case 4:
                task = "_m1";
                break;
            case 5:
                task = "_m2";
                break;
            case 6:
                task = "_m3";
                break;
            case 7:
                task = "_h1";
                break;
            case 8:
                task = "_h2";
                break;
            case 9:
                task = "_h3";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + mSig.count);
        }
        switch (item.getItemId()) {
            case R.id.settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return  true;
            case R.id.save:
                mSig.updateCount();
                intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_TITLE, name + '_' + input_type + task + ".txt");

                startActivityForResult(intent, 1);
                mSig.ClearCanvas();
                return true;
            case R.id.clear:
                mSig.ClearCanvas();
                mSig.resetData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 1) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                try {
                    ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(uri, "w");
                    FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                    SharedPreferences prefs = this.getSharedPreferences("settings", Context.MODE_PRIVATE);
                    String line = prefs.getString("input", "") + "\n";
                    fileOutputStream.write(line.getBytes());
                    line = mSig.getTime() + "\n";
                    fileOutputStream.write(line.getBytes());
                    for (SignaturePointModel point : mSig._data.dataPoints) {
                        line = "" + point.x + " " + point.y + "\n";
                        fileOutputStream.write(line.getBytes());
                    }
                    fileOutputStream.close();
                    pfd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mSig.resetData();
                }
            }
        }
    }
}

