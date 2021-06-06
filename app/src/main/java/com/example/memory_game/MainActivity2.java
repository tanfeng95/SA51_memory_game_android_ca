package com.example.memory_game;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;


public class MainActivity2 extends AppCompatActivity {

    int prev_i = -1;
    int pair_counter = 0;
    ArrayList<String> sel_pics;
    int numberSelected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Intent intent = getIntent();
        sel_pics = intent.getStringArrayListExtra("sel_pics");

        playMemoryGame(sel_pics);
    }

    protected void playMemoryGame(ArrayList<String> sel_pics) {
        int N_pairs = sel_pics.size();
        System.out.println(N_pairs);
        ArrayList<String> picseq = new ArrayList<>();

        for (int i = 0; i < N_pairs*2; i++)
            picseq.add(sel_pics.get(i % N_pairs));

        Collections.shuffle(picseq);

        long totalSeconds = 30;
        long intervalSeconds = 1;
        TextView timer_box = findViewById(R.id.timer);

        CountDownTimer2 timer = new CountDownTimer2(totalSeconds * 1000, intervalSeconds * 1000, timer_box);
        timer.start();

        for (int i = 0; i < N_pairs*2; i++) {
//            Bitmap bitmap = BitmapFactory.decodeFile(picseq.get(i));
////            System.out.println(bitmap);
//
//            if (bitmap != null) {
//                int id = getResources().getIdentifier("imageView" + i,
//                        "id", getPackageName());
////                System.out.println(id);
////                System.out.println(R.id.imageView0);
//                  ImageView imgView = findViewById(id);
////                int[] arr = {bitmap.getWidth(), bitmap.getHeight(), imgView.getWidth(), imgView.getHeight()};
////                int dim = Arrays.stream(arr).min().getAsInt();
////                System.out.println(dim);
//                Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0, 133, 133);
//                imgView.setImageBitmap(resized);
            ImageView imgView = findViewById( getResources().getIdentifier("imageView" + i,"id", getPackageName()));
            imgView.setOnClickListener(view -> {
                int min_ImageView_id = (int) findViewById(R.id.imageView0).getUniqueDrawingId();
                int curr_i = (int) view.getUniqueDrawingId() - min_ImageView_id;
                System.out.println(curr_i);
                System.out.println(prev_i);
                updateMatchCounter(curr_i, N_pairs);
                if (curr_i == 2)
                    timer.stop();
                if (curr_i != prev_i){
                    Bitmap bitmap = BitmapFactory.decodeFile(picseq.get(curr_i));
                    if (bitmap != null) {
                        int id = getResources().getIdentifier("imageView" + curr_i,
                                "id", getPackageName());
                        ImageView curr_imgView = findViewById(id);
                        //hardcode size here, to be improved
                        int[] arr = {bitmap.getWidth(), bitmap.getHeight()};
                        int dim = Arrays.stream(arr).filter((int x)->x != 0).min().getAsInt();
                        Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0, dim, dim);
                        curr_imgView.setImageBitmap(resized);
                        //numberSelected += 1;
//                        try {
//                            TimeUnit.SECONDS.sleep(1);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        //if (numberSelected % 2 == 0){
                        if (prev_i > -1) {
                            if (picseq.get(curr_i) == picseq.get(prev_i)){
                                pair_counter+=1;
                                updateMatchCounter(pair_counter, N_pairs);
                                //if(pair_counter==sel_pics.size()){
                                if (pair_counter == N_pairs) {
                                    System.out.println("all matches found");
                                }else{
                                    System.out.println("match");
                                }
                            }else{
                                try {
                                    TimeUnit.SECONDS.sleep(1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                ImageView prev_imgView = findViewById(getResources().getIdentifier("imageView" + prev_i,
                                        "id", getPackageName()));
                                //numberSelected -= 2;
                                curr_imgView.setImageResource(this.getResources().getIdentifier("question","drawable",this.getPackageName()));
                                prev_imgView.setImageResource(this.getResources().getIdentifier("question","drawable",this.getPackageName()));
                            }
                            //}
                            //prev_i = curr_i;
                        }
                        prev_i = curr_i;
                    }
                }
            });
        };
    }

    protected void updateMatchCounter(int pair_counter, int N_pairs) {
        TextView matchCounter = findViewById(R.id.matchCounter);

        matchCounter.setText(pair_counter + " / " + N_pairs + " matches");
    }

}

class CountDownTimer2 extends CountDownTimer {

    private TextView timer_box;
    private boolean is_running = true;

    public CountDownTimer2(long millisInFuture, long countDownInterval, TextView timer_box) {
        super(millisInFuture, countDownInterval);
        this.timer_box = timer_box;
    }

    int countUpSeconds = 0; // acts as a running sum
    // ticks every second
    public void onTick(long millisUntilFinished) {
        if (is_running) { countUpSeconds++; }
        timer_box.setText(this.getCountUpTime());
    }

    public String getCountUpTime() {
        int hours = countUpSeconds / 3600;
        int minutes = (countUpSeconds % 3600) / 60;
        int seconds = countUpSeconds % 60;

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        return timeString;
    }

    @Override
    public void onFinish() {

    }

    public void stop() {
        is_running = false;
    }
}
