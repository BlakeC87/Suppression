package firestormsoftwarestudio.suppression;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Blake on 4/19/2016.
 */
public class Stats extends Activity implements OnClickListener {

    public Button resetStatsBtn;
    public Button leaveStatsBtn;
    public TextView suppressionsText;
    public TextView winsText;
    public TextView lossesText;
    public TextView drawsText;

    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);

        prefs = getSharedPreferences(SuppressionToDo.PREFS_NAME, Context.MODE_PRIVATE);


        resetStatsBtn = (Button) findViewById(R.id.resetStatsBtn);
        leaveStatsBtn = (Button) findViewById(R.id.leaveStatsBtn);

        resetStatsBtn.setOnClickListener(this);
        leaveStatsBtn.setOnClickListener(this);

        suppressionsText = (TextView) findViewById(R.id.suppressionsText);
        winsText = (TextView) findViewById(R.id.winsText);
        lossesText = (TextView) findViewById(R.id.lossesText);
        drawsText = (TextView) findViewById(R.id.drawsText);

        refreshTextViews();
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.resetStatsBtn:
                // reset all stats to 0
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("wins", 0);
                editor.putInt("losses", 0);
                editor.putInt("ties", 0);
                editor.putInt("suppressions", 0);

                editor.commit();

                // refresh text views
                refreshTextViews();
                break;
            case R.id.leaveStatsBtn:
                Intent i = new Intent(this, Menu.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                break;
        }
    }

    private void refreshTextViews() {
        suppressionsText.setText(Integer.toString(prefs.getInt("suppressions", 0)));
        winsText.setText(Integer.toString(prefs.getInt("wins", 0)));
        lossesText.setText(Integer.toString(prefs.getInt("losses", 0)));
        drawsText.setText(Integer.toString(prefs.getInt("ties", 0)));
    }


    @Override
    protected void onPause() {
        super.onPause();
        Music.stop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Music.play(this, R.raw.timefluxmenu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent i = new Intent(this, Menu.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
