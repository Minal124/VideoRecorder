package com.minal.hp.video;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements VideoAdapter.OnItemClickListener {

    private Uri fileUri;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    public String path;
    public Uri selectedImageUri;
    DatabaseManager dbm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton buttonRecording = (FloatingActionButton) findViewById(R.id.record_fab);

        loadRecordings();

        buttonRecording.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                fileUri = getOutputMediaFileUri();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    private void loadRecordings() {
        RecyclerView rv = (RecyclerView) findViewById(R.id.recording_rv);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);

        DatabaseManager db = new DatabaseManager(this);

        ArrayList<RecordItem> recordingList = db.getData();
        VideoAdapter my = new VideoAdapter(this, recordingList, this);
        rv.setAdapter(my);
    }

    @Override
    public void onItemClick(RecordItem item) {
        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra("video_path", item.path);
        startActivity(intent);
    }

    private Uri getOutputMediaFileUri() {
        File outputFile = getOutputMediaFile();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", outputFile);
        } else {
            return Uri.fromFile(outputFile);
        }
    }

    private File getOutputMediaFile() {

        File mediaStorageDir =
            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "MyCameraVideo");
        if (!mediaStorageDir.exists()) {

            if (!mediaStorageDir.mkdirs()) {

                Toast.makeText(this, "Failed to create directory MyCameraVideo.", Toast.LENGTH_LONG).show();
                return null;
            }
        }

        java.util.Date date = new java.util.Date();
        String timeStamp = SimpleDateFormat.getDateTimeInstance().format(date.getTime());
        return new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Video saved to: " + data.getData(), Toast.LENGTH_LONG).show();
                selectedImageUri = data.getData();
                path = selectedImageUri.toString();
                dbm = new DatabaseManager(this);
                boolean isInserted = dbm.insertToTable(path);
                if (isInserted) {
                    Toast.makeText(this, "Recording Saved", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Failed to save recording", Toast.LENGTH_LONG).show();
                }
                loadRecordings();
            }
        }
    }
}