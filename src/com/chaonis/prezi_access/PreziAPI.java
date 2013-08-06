package com.chaonis.prezi_access;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import android.util.JsonReader;
import android.util.Log;

public class PreziAPI {
	
	public static CookieManager cookieManager;
	public static String sessionId;
	
	public static boolean login(String username, String password) {
		int success = 0;
		try {
			URL url = new URL("https://prezi.com/api/desktop/login/");

			cookieManager = new CookieManager();
			CookieHandler.setDefault(cookieManager);
			HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
			urlConnection.setDoOutput(true);				
			String urlParams = "username=" + URLEncoder.encode(username,"UTF-8") + "&password=" + URLEncoder.encode(password,"UTF-8");
			
			urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
	        out.print(urlParams);		        
			out.close();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			
			Log.d("return", String.format("response code %d", urlConnection.getResponseCode()));
			Log.d("return", String.format("cipher suite %s", urlConnection.getCipherSuite()));
			   
			String input;
			
			while ((input = br.readLine()) != null) {
				//Yeah this is ugly
				if (input.contains("<success>1</success>")) {
					success = 1;
				}
				Log.d("return", input);
			}
							
			for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
				if (cookie.getName().compareTo("sessionid") == 0) {
					sessionId = cookie.getValue();
					Log.d("return", cookie.getValue());
				}					
			}
			
			br.close();
			urlConnection.disconnect();
			
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

		if (success == 1) {
			return true;
		} else {
			return false;
		}		
	} //login
	
	public static String license() {
		String resp = "";
		try {
			URL url = new URL("http://prezi.com/auth/refresh/?next=" + URLEncoder.encode("http://prezi.com/api/desktop/license/json/", "UTF-8"));
						
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();			
			
			Log.d("return", String.format("response code %d", urlConnection.getResponseCode()));
			
			if (urlConnection.getResponseCode() == 302) {
				String loc = urlConnection.getHeaderField("Location");
				Log.d("return", loc);
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			   
			String input;
			StringBuilder sb = new StringBuilder();
			while ((input = br.readLine()) != null) {
				sb.append(input);
				Log.d("return", input);
			}
			resp = sb.toString();
			
			br.close();
			urlConnection.disconnect();
							
		} catch (MalformedURLException e) {
			
			
		} catch (IOException e) {
			
		}
		return resp;
	}
	
	public static ArrayList<PreziItem> preziList() {
		ArrayList<PreziItem> resp = new ArrayList<PreziItem>();
		
		try {
			URL url = new URL("http://prezi.com/auth/refresh/?next=" + URLEncoder.encode("http://prezi.com/my/prezi/list/", "UTF-8"));
						
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();			
			
			Log.d("return", String.format("response code %d", urlConnection.getResponseCode()));
			
			if (urlConnection.getResponseCode() == 302) {
				String loc = urlConnection.getHeaderField("Location");
				Log.d("return", loc);
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			   
			String input;
			StringBuilder sb = new StringBuilder();
			while ((input = br.readLine()) != null) {
				sb.append(input);
				Log.d("return", input);
			}
			
			//parse input
			//create PreziItem
			//add to resp
			
			br.close();
			urlConnection.disconnect();
							
		} catch (MalformedURLException e) {
			
			
		} catch (IOException e) {
			
		}
				
		return resp;
	}
	
	public static String requestPEZ(String oid) {
		String resp = "";
		try {
			URL url = new URL("http://prezi.com/backend/export/" + oid + "/pez/");

			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoOutput(true);				
			
			boolean success = false;
			String method = "";
			String jobUrl = "";
			String header = "";
			
			JsonReader reader = new JsonReader(new InputStreamReader(urlConnection.getInputStream()));
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("conversion_token")) {
					reader.beginObject();
					while (reader.hasNext()) {
						String n = reader.nextName();
						if (n.equals("url")) {
							jobUrl = reader.nextString();
						} else if (n.equals("header")) {
							header = reader.nextString();
						} else if (n.equals("method")) {
							method = reader.nextString();
						} else {
							reader.skipValue();
						}						
					}
					reader.endObject();
				} else if (name.equals("success")) {
					success = reader.nextBoolean();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
			
			reader.close();
			urlConnection.disconnect();
			
			if (success) {
				resp = callJob(jobUrl, header, method);
			}

		} catch (MalformedURLException e) {
			Log.d("return", "malformed url");
		} catch (IOException e) {
			Log.d("return", e.toString());
		}
	
		return resp;	
	}

	private static String callJob(String jobUrl, String header, String method) {
		String resp = "";
		
		try {
			URL url = new URL(jobUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoOutput(true);
			
			urlConnection.setRequestProperty("Authorization", header);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			   
			String input;
			StringBuilder sb = new StringBuilder();
			while ((input = br.readLine()) != null) {
				sb.append(input);
				Log.d("return", input);
			}
			resp = sb.toString();
			
			Log.d("return", String.format("response code %d", urlConnection.getResponseCode()));
			
			br.close();
			urlConnection.disconnect();			
			
		} catch (MalformedURLException e) {
			Log.d("return", "malformed url");
		} catch (IOException e) {
			Log.d("return", e.toString());
		}

		
		
		
		return resp;
	}

}
