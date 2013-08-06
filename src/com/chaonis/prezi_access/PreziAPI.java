package com.chaonis.prezi_access;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HttpsURLConnection;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;
import android.util.Pair;

public class PreziAPI {
	
	public static CookieManager cookieManager;
	public static String sessionId;
	public static File cacheDir;
	
	public static boolean login(String username, String password) {
		boolean success = false;
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
			
			Log.d("return", String.format("response code %d", urlConnection.getResponseCode()));
			Log.d("return", String.format("cipher suite %s", urlConnection.getCipherSuite()));
			   
			BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

			String input;
						
			while ((input = br.readLine()) != null) {
				//Yeah this is ugly
				if (input.contains("<success>1</success>")) {
					success = true;
				}
				Log.d("return", input);
			}
			
			br.close();
							
			for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
				if (cookie.getName().compareTo("sessionid") == 0) {
					sessionId = cookie.getValue();
					Log.d("return", "sessionid = " + cookie.getValue());
				}					
			}
			
			urlConnection.disconnect();
			
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

		return success;		
	} //login
	
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
		
			int total_count = 0;
			int dummyIdCount = 1;

			JsonReader reader = new JsonReader(new InputStreamReader(urlConnection.getInputStream()));
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("list")) {
					reader.beginArray();
					while (reader.hasNext()) {
						reader.beginObject();
						PreziItem item = new PreziItem();
						while (reader.hasNext()) {							
							String n = reader.nextName();
							if (n.equals("thumb_url")) {
								item.thumb_url = reader.nextString();
							} else if (n.equals("preview_url")) {
								item.preview_url = reader.nextString();
							} else if (n.equals("title")) {
								item.title = reader.nextString();
							} else if (n.equals("oid")) {
								item.oid = reader.nextString();
							} else if (n.equals("id")) {
								item.id = reader.nextInt();
							} else if (n.equals("size")) {
								item.size = reader.nextInt();
							} else if (n.equals("landing_url")) {
								item.landing_url = reader.nextString();
							} else if (n.equals("owner_profile_url")) {
								item.owner_profile_url = reader.nextString();
							} else {
								reader.skipValue();
							}
						}
						item.dummyId = String.valueOf(dummyIdCount);
						++dummyIdCount;
						resp.add(item);
						reader.endObject();
					}
					reader.endArray();
				} else if (name.equals("total_count")) {
					total_count = reader.nextInt();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
			
			Log.d("return", String.format("response code %d", urlConnection.getResponseCode()));
			
			reader.close();
			urlConnection.disconnect();
			
			Log.d("return", String.format("total_count : %d", total_count));
			Log.d("return", String.format("list_count : %d", resp.size()));
							
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
			
			Log.d("return", String.format("response code %d", urlConnection.getResponseCode()));
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
			
			boolean success = false;
			String smethod = "";
			String sjobUrl = "";
			String sheader = "";
			String sjob_uuid = "";
			
			JsonReader reader = new JsonReader(new InputStreamReader(urlConnection.getInputStream()));
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("conversion_status_token")) {
					reader.beginObject();
					while (reader.hasNext()) {
						String n = reader.nextName();
						if (n.equals("url")) {
							sjobUrl = reader.nextString();
						} else if (n.equals("header")) {
							sheader = reader.nextString();
						} else if (n.equals("method")) {
							smethod = reader.nextString();
						} else {
							reader.skipValue();
						}						
					}
					reader.endObject();
				} else if (name.equals("success")) {
					success = reader.nextBoolean();
				} else if (name.equals("job_uuid")) {
					sjob_uuid = reader.nextString();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
			
			Log.d("return", String.format("response code %d", urlConnection.getResponseCode()));
			reader.close();
			
			urlConnection.disconnect();
			
			if (success) {
				boolean go = true;
				int tries = 0;
				while (tries < 5 && go) {
					++tries;
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						Log.d("return", "interrupted conversion");
					}
					Pair<String, Boolean> ret = callStatusJob(sjobUrl, sheader, sjob_uuid, smethod);
					if ((Boolean)ret.second) {
						resp = (String) ret.first;
						go = false;
					}
				}				
			}
			
		} catch (MalformedURLException e) {
			Log.d("return", "malformed url");
		} catch (IOException e) {
			Log.d("return", e.toString());
		}
		
		return resp;
	}

	private static Pair<String, Boolean> callStatusJob(String jobUrl, String header, String job_uuid, String method) {
		String resp = "";
		boolean success = false;
		
		try {
			URL url = new URL(jobUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoOutput(true);
			
			urlConnection.setRequestProperty("Authorization", header);
			
			boolean succ = false;
			int status = 0; //5 is finished, 1 is working
			//These two are sometimes ints sometimes nulls
			int progress = 0; // seen 50 
			int remaining = 0; // seen 30
			String pezUrl = ""; // url is present before finish?
						
			JsonReader reader = new JsonReader(new InputStreamReader(urlConnection.getInputStream()));
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("status")) {
					status = reader.nextInt();
				} else if (name.equals("success")) {
					succ = reader.nextBoolean();
				} else if (name.equals("url")) {
					pezUrl = reader.nextString();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
			reader.close();			
			
			Log.d("return", String.format("response code %d", urlConnection.getResponseCode()));
			urlConnection.disconnect();

			if (succ && status >=5 && !pezUrl.isEmpty()) {
				success = true;
				resp = pezUrl;
			}
		} catch (MalformedURLException e) {
			Log.d("return", "malformed url");
		} catch (IOException e) {
			Log.d("return", e.toString());
		}
		
		return Pair.create(resp, success);
	}

	public static String getContentsXML(String pezUrl) {
		String pez = "";
		
		String httpUrl = pezUrl.replace("https://", "http://");
		try {
			URL url = new URL(httpUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
			//Download file
			//Didn't work without downloading
			//See for why at http://commons.apache.org/proper/commons-compress/zip.html
			
			File outputFile = File.createTempFile("pez", "tmp", cacheDir);			
			
			BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
			FileOutputStream fout = new FileOutputStream(outputFile);
			
			byte[] buffer = new byte[1024];
			int count;			
			while ((count = in.read(buffer)) != -1) {
				fout.write(buffer, 0, count);
			}
			fout.close();
			in.close();
			conn.disconnect();
			
			ZipFile zip = new ZipFile(outputFile);
			ZipEntry ze = zip.getEntry("prezi/content.xml");
			if (ze == null) {
				Log.d("return", "can't find content.xml");
				return pez;
			}
			
			outputFile.delete();
			
			BufferedInputStream zis = new BufferedInputStream(zip.getInputStream(ze));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			while ((count = zis.read(buffer)) != -1) {
				baos.write(buffer, 0, count);
			}
			
			zis.close();
			baos.flush();
			pez = baos.toString();
			baos.close();
			
		} catch (MalformedURLException e) {
			Log.d("return", "malformed url");
		} catch (IOException e) {
			Log.d("return", e.toString());
		}
		
		return pez;
	}

	
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
	
}
