package com.finalMS.intSel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class NetworkFinder extends IntentService {

	private static final String TAG = "Network Finder Logs";
	List<InfoElement> RTTList;
	List<InfoElement> ThrputList;

	public NetworkFinder() {
		super("FindNetworkService");
		Log.i(TAG, "Starting Network Finder Service");
		RTTList = new ArrayList<InfoElement>();
		ThrputList = new ArrayList<InfoElement>();
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

	/*
	 * public void onCreate() { Toast.makeText(this,
	 * "Network Monitor Service Created", Toast.LENGTH_SHORT).show(); Log.i(TAG,
	 * "onCreate"); //Constants.wifiMan = (WifiManager)
	 * getSystemService(Context.WIFI_SERVICE); }
	 * 
	 * public void onStart(Intent intent, int startid) { Toast.makeText(this,
	 * "Network Monitor Service Started", Toast.LENGTH_SHORT).show(); Log.i(TAG,
	 * "onStart"); //scanNetwork(); }
	 * 
	 * public void onDestroy() { Toast.makeText(this,
	 * "Network Monitor Service Stopped", Toast.LENGTH_SHORT).show(); Log.i(TAG,
	 * "onDestroy"); //Constants.wifiMan = null; }
	 */

	private boolean initializeConnectionsManager() {
		Constants.wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		Constants.connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (Constants.wifiMan != null && Constants.connManager != null)
			return true;
		else
			return false;
	}

	/*
	 * public void scanNetwork() { boolean isWifiAvailable =
	 * Constants.wifiMan.startScan(); if (isWifiAvailable) { Log.i(TAG,
	 * "Scanning wifi Networks...."); findAvailableNetworks(); }
	 * 
	 * else Log.i(TAG, "Scan initiation failed"); }
	 */

	public void findAvailableNetworks() {

		List<WifiConfiguration> networksFound = null;
		NetworkInfo currentNetInfo = Constants.connManager
				.getActiveNetworkInfo();
		Log.i(TAG, "Initial Network Info: " + currentNetInfo.toString());
		if (currentNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			measure_performance();
			Constants.wifiMan.setWifiEnabled(true);
			Constants.wifiMan.reconnect();
			waitForActivation();
			networksFound = findWifiNetworks(false);
		} else if (currentNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			networksFound = findWifiNetworks(true);
			Constants.wifiMan.setWifiEnabled(false);
			Constants.wifiMan.disconnect();
			waitForActivation();
			measure_performance();
		}

		//Logging Info
		Log.i(TAG, "------ RTT Table--------");
		Iterator<InfoElement> itr = RTTList.iterator();
		while (itr.hasNext()) {
			InfoElement thisElem = itr.next();
			Log.i(TAG, thisElem.toString());
		}
		Log.i(TAG, "------ Throughput Table--------");
		itr = ThrputList.iterator();
		while (itr.hasNext()) {
			InfoElement thisElem = itr.next();
			Log.i(TAG, thisElem.toString());
		}

		InfoElement switchTo = RTTList.get(Constants.MAX_THRPUT);
		Log.i(TAG,"Final Switch to network : "+switchTo.toString());
		if (switchTo.getNetType() == "MOBILE") {
			Constants.wifiMan.disconnect();
			Constants.wifiMan.setWifiEnabled(false);
			waitForActivation();
		} else {
			Constants.wifiMan.setWifiEnabled(true);
			Constants.wifiMan.disconnect();
			Constants.wifiMan.enableNetwork(switchTo.getNetID(), true);
			Constants.wifiMan.reconnect();
			waitForActivation();
			enableAllNetworks(networksFound);
		}

		NetworkInfo finalNetInfo = Constants.connManager.getActiveNetworkInfo();
		Log.i(TAG, "Final Network Info: " + finalNetInfo.toString());

	}

	private void waitForActivation() {
		try {
			Thread.sleep(12000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private List<WifiConfiguration> findWifiNetworks(boolean isCurrentNetworkWifi) {
		int thisWifiNetID = Constants.NO_NET_ID;
		List<WifiConfiguration> availableNetworks = Constants.wifiMan
				.getConfiguredNetworks();
		if (availableNetworks.isEmpty())
			Log.i(TAG, "No configured networks found");

		else {
			Iterator<WifiConfiguration> itr = availableNetworks.iterator();
			if(isCurrentNetworkWifi){
				WifiInfo thisWifi = Constants.wifiMan.getConnectionInfo();
				thisWifiNetID = thisWifi.getNetworkId();
				Log.i(TAG, "Current Network SSID: " + thisWifiNetID);
				measure_performance();
			}
			while (itr.hasNext()) {
				WifiConfiguration element = (WifiConfiguration) itr.next();
				
				if(element.networkId != thisWifiNetID) {
				Log.i(TAG, "Switching to " + element.SSID + " wit network ID"
						+ element.networkId);

				Constants.wifiMan.disconnect();
				if (Constants.wifiMan.enableNetwork(element.networkId, true)) {
					Constants.wifiMan.setWifiEnabled(true);
					Constants.wifiMan.reconnect();

					WifiInfo wifiInfo = Constants.wifiMan.getConnectionInfo();
					SupplicantState spstate = wifiInfo.getSupplicantState();

					while (spstate != SupplicantState.COMPLETED) {
						wifiInfo = Constants.wifiMan.getConnectionInfo();
						spstate = wifiInfo.getSupplicantState();
					}
				}

				measure_performance();

				Log.i(TAG, "Switching to " + element.SSID + " completed");

			}
			enableAllNetworks(availableNetworks);
			}
		}

		Intent findNetworkDone = new Intent(this, MonitorNetwork.class);
		findNetworkDone.setAction(Constants.CUSTOM_INTENT);
		findNetworkDone.putExtra(Constants.MSG_FROM_NETWORKFINDER_TO_BR,
				Constants.MSG_FIND_AVAILABLE_NETWORKS_DONE);
		sendBroadcast(findNetworkDone);
		
		return availableNetworks;

	}

	private void enableAllNetworks(List<WifiConfiguration> availableNetworks) {
		Iterator<WifiConfiguration> itr = availableNetworks.iterator();

		while (itr.hasNext()) {
			WifiConfiguration element = (WifiConfiguration) itr.next();
			Constants.wifiMan.enableNetwork(element.networkId, false);
		}

	}

	protected void onHandleIntent(Intent intent) {

		Log.i(TAG, "On handle event");

		int recvdMessage = intent.getIntExtra(
				Constants.MSG_FROM_BR_TO_NETWORKFINDER, Constants.MSG_DEFAULT);

		int i;
		switch (recvdMessage) {

		case Constants.MSG_INITIALIZE:
			Log.i(TAG, "Message intialize");

		case Constants.MSG_DEFAULT:
			Log.i(TAG, "Default message");
			if (initializeConnectionsManager()) {
				int cnt = 0;
				while(cnt < 2){
				findAvailableNetworks();
				cnt++;
				clearLists();
				try {
					Thread.sleep(300000);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
				}
			}
				
			else {
				Log.i(TAG, "Connection Manager Init failed");
				break;
			}
		}
		

	}

	private void clearLists() {
		ThrputList.clear();
		RTTList.clear();
	}

	void ping_to_umass() {

		String subtype = new String();
		int netID = Constants.NO_NET_ID;
		waitForActivation();
		NetworkInfo currentNetInfo = Constants.connManager
				.getActiveNetworkInfo();
		if (currentNetInfo != null) {
			Log.i(TAG, "Current Network Info: " + currentNetInfo.toString());
			subtype = currentNetInfo.getSubtypeName();

		}
		if (currentNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			Log.i(TAG, "Current dhcp info :"
					+ Constants.wifiMan.getDhcpInfo().toString());
			subtype = Constants.wifiMan.getConnectionInfo().getSSID();
			netID = Constants.wifiMan.getConnectionInfo().getNetworkId();
		}

		InetAddress server;
		try {
			server = InetAddress.getByName("www.cs.umass.edu");
			Log.i(TAG, server.toString());
			long beforeTime = System.currentTimeMillis();

			if (server.isReachable(Constants.TIMEOUT)) {
				long afterTime = System.currentTimeMillis();
				long rtt = afterTime - beforeTime;
				Log.i(TAG, server.getHostName() + " is reachable. RTT = " + rtt);

				createRTTListEntry(subtype, currentNetInfo.getTypeName(),
						netID, rtt);
			}

			else
				Log.i(TAG, " Server is not reachable ");

		} catch (UnknownHostException e) {
			Log.i(TAG, " Unknown host ");
			e.printStackTrace();
		} catch (IOException e) {
			Log.i(TAG, " IO Exception in getting hostname ");
			e.printStackTrace();
		}

	}

	private void createRTTListEntry(String subtype, String type, int netID,
			long rtt) {
		int cnt;
		InfoElement elem = new InfoElement();
		elem.setNetType(type);
		elem.setSsid(subtype);
		elem.setValue(rtt);
		elem.setNetID(netID);
		Iterator<InfoElement> it = RTTList.iterator();

		for (cnt = -1; it.hasNext(); cnt++) {
			InfoElement ie = (InfoElement) it.next();
			if (elem.getValue() < ie.getValue())
				break;

		}

		RTTList.add(cnt + 1, elem);
	}

	private void measureThroughput() {
		String subtype = new String();
		int netID = Constants.NO_NET_ID;
		waitForActivation();
		NetworkInfo currentNetInfo = Constants.connManager
				.getActiveNetworkInfo();
		if (currentNetInfo != null) {
			Log.i(TAG, "Current Network Info: " + currentNetInfo.toString());
			subtype = currentNetInfo.getSubtypeName();

		}
		if (currentNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			Log.i(TAG, "Current dhcp info :"
					+ Constants.wifiMan.getDhcpInfo().toString());
			subtype = Constants.wifiMan.getConnectionInfo().getSSID();
			netID = Constants.wifiMan.getConnectionInfo().getNetworkId();
		}

		Log.i(TAG, "Measuring thrput");
		URL url = null;
		try {
			url = new URL("http://www.cs.umass.edu/~brian/plHUGE.txt");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		Log.i(TAG, "SDCARD status " + Environment.getExternalStorageState());
		File filename = new File(Environment.getExternalStorageDirectory(),
				"download.txt");

		InputStream reader = null;
		try {
			url.openConnection();
			reader = url.openStream();

			FileOutputStream writer = new FileOutputStream(filename);

			byte[] buffer = new byte[4096];
			int bytesRead = 0, total_bytes = 0;
			Log.i(TAG, "Starting to read");
			long beforeTime = System.currentTimeMillis();

			while ((bytesRead = reader.read(buffer)) > 0) {
				writer.write(buffer, 0, bytesRead);
				total_bytes += bytesRead;
			}
			long afterTime = System.currentTimeMillis();
			long thrput = total_bytes / (afterTime - beforeTime);
			Log.i(TAG, "Total Bytes read: " + total_bytes);
			Log.i(TAG, "Time taken: " + (afterTime - beforeTime));
			Log.i(TAG, "Throughput seen: " + thrput);
			createThrputListEntry(subtype, currentNetInfo.getTypeName(), netID,
					thrput);
			writer.close();
			reader.close();
			filename.delete();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void createThrputListEntry(String subtype, String type, int netID,
			long thrput) {
		int cnt;
		InfoElement elem = new InfoElement();
		elem.setNetType(type);
		elem.setSsid(subtype);
		elem.setValue(thrput);
		elem.setNetID(netID);
		Iterator<InfoElement> it = ThrputList.iterator();

		for (cnt = -1; it.hasNext(); cnt++) {
			InfoElement ie = (InfoElement) it.next();
			if (elem.getValue() > ie.getValue())
				break;

		}

		ThrputList.add(cnt + 1, elem);
	}

	private void measure_performance() {
		ping_to_umass();
		measureThroughput();
	}
}
