package com.chaonis.prezi_access;

public class PreziItem {

	public String thumb_url;
	public String preview_url;
	public String title;
	public String oid;
	public int id;
	public int size;
	public String landing_url;
	public String owner_profile_url;
	
	public String dummyId;

	@Override
	public String toString() {
		return title;
	}
}
