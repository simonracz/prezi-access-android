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
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

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
	
	public static List<PreziItem> preziList() {
		List<PreziItem> list = new ArrayList<PreziItem>();
		
		
		
		return list;		
	}

}
