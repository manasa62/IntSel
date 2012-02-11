package com.finalMS.intSel;

import java.io.InputStream;

import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

public class Constants {

	public static WifiManager wifiMan;
	public static ConnectivityManager connManager;
	public static final String MSG_FROM_BR_TO_NETWORKFINDER = "Message from broadcast receiver to Network finder";
	public static final int MSG_DEFAULT = 0;
	public static final int MSG_INITIALIZE = 1;
	public static final int MSG_SCAN_RESULTS_AVAILABLE_ACTION = 100;
	
	public static final String MSG_FROM_NETWORKFINDER_TO_BR = "Message from Network finder to broadcast receiver";
	public static final int MSG_FIND_AVAILABLE_NETWORKS_DONE = 200;
	public static final int TIMEOUT=1000;
	public static final String CUSTOM_INTENT="com.finalMS.intSel.customIntent";
	public static final int NO_NET_ID = -1;
	public static final int LEAST_RTT = 0;
	public static final int MAX_THRPUT = 0;
	
	
	
}
