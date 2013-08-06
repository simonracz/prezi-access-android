package com.chaonis.prezi_access;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * An activity representing a list of Prezis. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link PreziDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link PreziListFragment} and the item details (if present) is a
 * {@link PreziDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link PreziListFragment.Callbacks} interface to listen for item selections.
 */
public class PreziListActivity extends FragmentActivity implements
		PreziListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prezi_list);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case R.id.action_logout:
            logout();
            return true;
        default:
            return super.onOptionsItemSelected(item);
		}
	}
	
	private void logout() {
		SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.clear();
	    editor.commit();
	    			    
		//go to login activity
	    
	    Intent intent = new Intent(this, LoginActivity.class);
	    startActivity(intent);
	    
		finish();
	}
	
	/**
	 * Callback method from {@link PreziListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		//do nothing
		Log.d("item", String.format("item selected : %s", id));
	}
}
