package com.example.memory_game;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    int max_pics = 20;
    int max_sel = 6;

    Thread bkgdThread;
    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.fetch).setOnClickListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction("download_ok");
        filter.addAction("pic_selected");
        registerReceiver(receiver, filter);
    }

    protected String[] getUrls(String webpage_url, int max_pics) {
        Document document = null;

        try {
            document = Jsoup.connect(webpage_url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Elements es = document.select("img[src~=https.*jpg]");

        List<String> urls = new ArrayList<String>();

        for (Element e : es)
            urls.add(e.attr("src"));

        if (urls.size() > max_pics)
            urls = urls.subList(0, max_pics);

        String[] out = urls.toArray(new String[urls.size()]);

        return out;
    }

    protected String[] makeFileNames(File dir, String[] urls) {
        String[] out = new String[urls.length];

        for (int i = 0; i < urls.length; i++)
            out[i] = new File(dir + "/" + new File(urls[i]).getName()).toString();

        return out;
    }

    protected BroadcastReceiver receiver = new BroadcastReceiver() {

        int pos = -1;
        int nsel = 0;
        ArrayList<String> sel_pics = new ArrayList<>();
        ArrayList<String> filenames = new ArrayList<>();

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("download_ok")) {
                pos++;
                String filename = intent.getStringExtra("filename");

                imageToImageView(filename, pos);
                updateProgressBar(pos, max_pics);
                showToastMsg(pos, max_pics);
                filenames.add(filename);
            }

            if (pos == max_pics)
                stopService(new Intent(MainActivity.this, DownloadService.class));


            if (action.equals("pic_selected")) {
                int index = Integer.parseInt(intent.getStringExtra("index"));
                System.out.println("filename: " + filenames.get(index));
                sel_pics.add(filenames.get(index));
                nsel++;
                System.out.println("nsel: " + nsel);

                if (nsel == max_sel) {
                    if(pos == max_pics -1){
                        stopService(new Intent(MainActivity.this, DownloadService.class));
                        play_game(sel_pics);}
                    else{
                        nsel = 0;
                    }
                }
            }
        }
    };

    protected void play_game(ArrayList<String> sel_pics) {
        Intent play_game = new Intent(this, MainActivity2.class); // go to part 2
        play_game.putStringArrayListExtra("sel_pics", sel_pics);
        startActivity(play_game);
    }

    @Override
    public void onClick(View v) {
        running = !running;     // change from false to true

        if (! running) {    // change from true to false (stop running)
            bkgdThread.interrupt();
            return;
        }
        bkgdThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String webpage_url = ((EditText) findViewById(R.id.webpage_url)).getText().toString();

                String[] urls = getUrls(webpage_url, max_pics);
                File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                String[] filenames = makeFileNames(dir, urls);
                startDownloadService(urls, filenames);

                if (Thread.interrupted()) {
                    stopService(new Intent(MainActivity.this, DownloadService.class));
                    return;     // downloading aborted
                }


                running = false;    // computation done
            }
        });

        bkgdThread.start();
    }

    protected void startDownloadService(String[] urls, String[] filenames) {
        for (int i = 0; i < filenames.length; i++) {
            Intent intent = new Intent(this, DownloadService.class);
            intent.setAction("download");
            intent.putExtra("filename", filenames[i]);
            intent.putExtra("where", urls[i]);
            startService(intent);

//            try {
//                TimeUnit.SECONDS.sleep(3);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    }

    protected void imageToImageView(String filename, int pos) {
        Bitmap bitmap = BitmapFactory.decodeFile(filename);

        if (bitmap != null) {
            int id = getResources().getIdentifier("imageView" + pos,
                    "id", getPackageName());
            ImageView imgView = findViewById(id);
            int[] arr = {bitmap.getWidth(), bitmap.getHeight()};
            int dim = Arrays.stream(arr).filter((int x)->x != 0).min().getAsInt();
            Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0, dim, dim);
            imgView.setImageBitmap(resized);

            imgView.setOnClickListener(view -> {
                long min_ImageView_id = findViewById(R.id.imageView0).getUniqueDrawingId();
                long index = view.getUniqueDrawingId() - min_ImageView_id;
                System.out.println(index);

                Intent intent = new Intent();
                intent.setAction("pic_selected");
                intent.putExtra("index", String.valueOf(index));
                sendBroadcast(intent);
                });
        }
    }

    protected void updateProgressBar(int pos, int max_pics) {
        ProgressBar bar = findViewById(R.id.progressBar);

        bar.setProgress(0);
        bar.setMax(max_pics);
        bar.setProgress(pos + 1);
    }

    protected void showToastMsg(int pos, int max_pics) {
        Toast.makeText(this, "Downloaded " + (pos + 1) + " out of " + max_pics + " images",
                Toast.LENGTH_SHORT).show();
    }
}