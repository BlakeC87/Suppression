package firestormsoftwarestudio.suppression;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Blake on 4/19/2016.
 */
public class Menu extends Activity implements OnClickListener {
    public Button quickStartBtn;
    public Button customGameBtn;
    public Button statsBtn;
    public Button creditsBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        quickStartBtn = (Button) findViewById(R.id.quickStartBtn);
        customGameBtn = (Button) findViewById(R.id.customGameBtn);
        statsBtn = (Button) findViewById(R.id.statsBtn);
        creditsBtn = (Button) findViewById(R.id.creditsBtn);

        quickStartBtn.setOnClickListener(this);
        customGameBtn.setOnClickListener(this);
        statsBtn.setOnClickListener(this);
        creditsBtn.setOnClickListener(this);

        SharedPreferences settings = getSharedPreferences(SuppressionToDo.PREFS_NAME, 0);
        if (settings.getString("save", "").length() == 65) {
            quickStartBtn.setText("Continue");
        }
        else {
            quickStartBtn.setText("Quick Start");
        }
    }

    public void onClick(View v) {
        //Intent i;
        switch (v.getId()) {
            case R.id.quickStartBtn:

                SharedPreferences settings = getSharedPreferences(SuppressionToDo.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();

                Intent i = new Intent(this, SuppressionToDo.class);
                // reset settings
                editor.putInt("AISetting", 1);
                editor.putInt("StartingPiecesSetting", 1);
                editor.commit();

                startActivity(i);
                break;
            case R.id.customGameBtn:
                Intent j = new Intent(this, Settings.class);
                startActivity(j);
                break;
            case R.id.statsBtn:
                Intent k = new Intent(this, Stats.class);
                startActivity(k);
                break;
            case R.id.creditsBtn:
                Intent l = new Intent(this, Credits.class);
                startActivity(l);
                break;
        }
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
}
