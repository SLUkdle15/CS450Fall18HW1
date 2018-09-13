package edu.stlawu.stopwatch;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // Define variable for our views
    private TextView tv_count = null;
    private Button bt_start = null;
    private Button bt_stop =null;
    private Timer t = null;
    private Counter ctr = null;  // TimerTask

    public AudioAttributes  aa = null;
    private SoundPool soundPool = null;
    private int bloopSound = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize views
        this.tv_count = findViewById(R.id.tv_count);
        this.bt_start = findViewById(R.id.bt_start);
        this.bt_stop = findViewById(R.id.bt_stop);

        //initialize start button
        this.bt_start.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 //Toggle the start and reset function on the Start button
                 if(bt_start.getText().equals("Start")){
                     t.scheduleAtFixedRate(ctr, 0, 100);
                     bt_start.setText("Reset");
                     bt_stop.setEnabled(true);
                     bt_stop.setBackgroundColor(Color.RED);
                 }else {
                     bt_start.setText("Start");
                     bt_stop.setText("Stop");
                     bt_stop.setBackgroundColor(Color.GRAY);
                     bt_stop.setEnabled(false);
                     bt_start.setEnabled(true);
                     //restart the timer and Timer Task
                     restart_timer(0,0,0);
                 }
             }
            });

        this.aa = new AudioAttributes
                .Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        this.soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(aa)
                .build();
        this.bloopSound = this.soundPool.load(
                this, R.raw.bloop, 1);

        this.bt_stop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //toggle the Stop and Resume function on the Stop button
                if (bt_stop.getText().equals("Stop")){
                    if(t != null){
                        t.cancel();
                        t = null;
                    }
                    bt_stop.setText("Resume");
                    bt_stop.setBackgroundColor(Color.BLUE);
                    bt_start.setText("Reset");
                }else{
                    bt_stop.setText("Stop");
                    bt_stop.setBackgroundColor(Color.RED);
                    bt_start.setText("Reset");
                    bt_start.setEnabled(true);
                   ////restart the timer and Timer Task
                    int minute = ctr.getMm();
                    int second = ctr.getSs();
                    int tenth_second = ctr.getTenth_second();
                    restart_timer(minute,second,tenth_second);
                    t.scheduleAtFixedRate(ctr, 0, 100);
                }
            }
        });

        this.tv_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.play(bloopSound, 1f,
                        1f, 1, 0, 1f);
                Animator anim = AnimatorInflater
                        .loadAnimator(MainActivity.this,
                                       R.animator.counter);
                anim.setTarget(tv_count);
                anim.start();
            }
        });
        //load the saved data
        int tenth_second = getPreferences(MODE_PRIVATE)
                .getInt("TENTH_SECOND", 0);
        int ss = getPreferences(MODE_PRIVATE).getInt("SECOND", 0);
        int mm = getPreferences(MODE_PRIVATE).getInt("MINUTE",0);
        this.ctr = new Counter(mm,ss,tenth_second);
        this.t = new Timer();
        this.tv_count.setText(ctr.toTheString());

        bt_stop.setBackgroundColor(Color.GRAY);

        if(!this.tv_count.getText().equals("00:00.0")){
            bt_start.setText("Reset");
            bt_stop.setText("Resume");
            bt_stop.setBackgroundColor(Color.BLUE);
        }else{
            this.bt_stop.setEnabled(false);
        }
        this.bt_start.setBackgroundColor(Color.GREEN);
    }

    private void restart_timer(int minute,int second,int tenth_second){
        if(t!= null){
            t.cancel();
            t = null;
        }
        this.t = new Timer();
        this.ctr = new Counter(minute, second,tenth_second);
        MainActivity.this.tv_count.setText(ctr.toTheString());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(this, "Stopwatch is started",
                        Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        //There are situations where the system will simply kill
        // the activity's hosting process without calling onDestroy so we need to
        // do the process of saving data here.
        getPreferences(MODE_PRIVATE)
                .edit()
                .putInt("MINUTE", ctr.getMm())
                .apply();

        getPreferences(MODE_PRIVATE).edit().putInt("SECOND", ctr.getSs()).apply();
        getPreferences(MODE_PRIVATE).edit().putInt("TENTH_SECOND", ctr.getTenth_second()).apply();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPreferences(MODE_PRIVATE)
                .edit()
                .putInt("MINUTE", ctr.getMm())
                .apply();

        getPreferences(MODE_PRIVATE).edit().putInt("SECOND", ctr.getSs()).apply();
        getPreferences(MODE_PRIVATE).edit().putInt("TENTH_SECOND", ctr.getTenth_second()).apply();
    }

    class Counter extends TimerTask {
        private int mm = 0;
        private int ss = 0;
        private int tenth_second = 0;

        public int getMm() {
            return mm;
        }

        public int getSs() {
            return ss;
        }

        public int getTenth_second() {
            return tenth_second;
        }

        public void setMm(int mm) {
            this.mm = mm;
        }

        public void setSs(int ss) {
            this.ss = ss;
        }

        public void setTenth_second(int tenth_second) {
            this.tenth_second = tenth_second;
        }

        Counter(int mm, int ss, int tenth_second){
            this.mm = mm;
            this.ss = ss;
            this.tenth_second = tenth_second;
        }

        public String toTheString(){
            StringBuilder sb = new StringBuilder();
            if(this.mm < 10){
                sb.append("0");
                sb.append(this.mm);
            }
            else{
                sb.append(this.mm);
            }
            sb.append(":");
            if(this.ss < 10){
                sb.append("0");
                sb.append(this.ss);
            }else{
                sb.append(this.ss);
            }
            sb.append(".");
            sb.append(this.tenth_second);

            return sb.toString();
        }


        @Override
        public void run() {
            MainActivity.this.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.tv_count.setText(
                                    MainActivity.this.ctr.toTheString()
                            );
                            ctr.setTenth_second(ctr.getTenth_second() + 1);
                            if(ctr.getTenth_second() % 10 == 0){
                                ctr.setTenth_second(0);
                                ctr.setSs(ctr.getSs() + 1);
                                if(ctr.getSs() % 60 == 0){
                                    ctr.setSs(0);
                                    ctr.setMm(ctr.getMm() + 1);
                                }
                            }
                        }
                    }
            );
        }
    }
}
