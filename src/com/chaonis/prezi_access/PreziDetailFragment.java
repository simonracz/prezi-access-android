package com.chaonis.prezi_access;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chaonis.prezi_access.LoginActivity.UserLoginTask;
import com.chaonis.prezi_access.dummy.DummyContent;

/**
 * A fragment representing a single Prezi detail screen. This fragment is either
 * contained in a {@link PreziListActivity} in two-pane mode (on tablets) or a
 * {@link PreziDetailActivity} on handsets.
 */
public class PreziDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private PreziItem mItem;
	
	private String content = null;
	
	private ContentTask mTask = null;
	
	public View rootView = null;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public PreziDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = DummyContent.ITEM_MAP.get(getArguments().getString(
					ARG_ITEM_ID));
			if (mTask == null) {
				Log.d("detail", "starting Task");
				mTask = new ContentTask();
				mTask.execute((Void) null);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_prezi_detail,
				container, false);

		// Show the dummy content as text in a TextView.
		if ((mItem != null) && (content != null)) {
			((TextView) rootView.findViewById(R.id.prezi_detail))
					.setText(content);
		} else if (mItem != null) {
			((TextView) rootView.findViewById(R.id.prezi_detail))
			.setText("Loading " + mItem.oid + "...");
		}

		return rootView;
	}
	
	public class ContentTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			Log.d("detail", "doInBg()");
			String pezUrl = PreziAPI.requestPEZ(mItem.oid);
			boolean success = false;
			if (!pezUrl.isEmpty()) {				
				String pezXML = PreziAPI.getContentsXML(pezUrl);
				if (!pezXML.isEmpty()) {
					Log.d("detail", "content is set");
					content = pezXML;
					success = true;
				}
			}			
			return success;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			Log.d("detail", "onPostExecute");
			mTask = null;
			if (rootView != null) {
				if (!success) {					
					content = "Couldn't load " + mItem.oid;					
				}
				((TextView) rootView.findViewById(R.id.prezi_detail))
				.setText(content);
			}
		}

		@Override
		protected void onCancelled() {
			Log.d("detail", "onCancelled()");
			mTask = null;
			if (rootView != null) {
				((TextView) rootView.findViewById(R.id.prezi_detail))
				.setText("Loading " + mItem.oid + "has been cancelled");
			}
			
		}
	}
	
}
