package com.finalMS.intSel;

import java.util.Iterator;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class MonitorNetwork extends BroadcastReceiver {

	private static final String TAG = "NetworkMonitor";

	// public FindGoodNetwork findNetworkService = new FindGoodNetwork();

	public void onReceive(Context context, Intent intent) {

		final String action = intent.getAction();

		if (action.equals(Constants.CUSTOM_INTENT)) {

			if (intent.getIntExtra(Constants.MSG_FROM_NETWORKFINDER_TO_BR,
					Constants.MSG_DEFAULT) == Constants.MSG_FIND_AVAILABLE_NETWORKS_DONE) {
				Log.i(TAG, "Find network done message");
				
				/*
				 * Intent msgIntent = new Intent(context, NetworkFinder.class);
				 * msgIntent.putExtra(Constants.MSG_FROM_BR_TO_NETWORKFINDER,
				 * Constants.MSG_INITIALIZE); context.startService(msgIntent);
				 */

			}
		}

		/*
		 * else { action = intent.getAction();
		 * 
		 * if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
		 * Log.i(TAG, "Scan Results available");
		 * 
		 * Intent msgIntent = new Intent(context, NetworkFinder.class);
		 * msgIntent.putExtra(Constants.MSG_FROM_BR_TO_NETWORKFINDER,
		 * Constants.MSG_SCAN_RESULTS_AVAILABLE_ACTION);
		 * context.startService(msgIntent);
		 * 
		 * // findNetworkService.findAvailableNetworks();
		 * 
		 * } }
		 */

	}
}