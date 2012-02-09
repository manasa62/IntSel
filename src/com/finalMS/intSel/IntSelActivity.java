package com.finalMS.intSel;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class IntSelActivity extends Activity implements OnClickListener {
	private static final String TAG = "Interface Selection Activity";
	Button buttonStart, buttonStop;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		buttonStart = (Button) findViewById(R.id.buttonStart);
		buttonStop = (Button) findViewById(R.id.buttonStop);

		buttonStart.setOnClickListener(this);
		buttonStop.setOnClickListener(this);
	}

	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.buttonStart:
			Log.i(TAG, "onClick: starting srvice");
			startService(new Intent(this, NetworkFinder.class));
			break;
		case R.id.buttonStop:
			Log.i(TAG, "onClick: stopping srvice");
			stopService(new Intent(this, NetworkFinder.class));
			break;
		}
	}


}
