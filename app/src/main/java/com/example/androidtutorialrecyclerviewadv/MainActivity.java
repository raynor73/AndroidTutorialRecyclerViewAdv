package com.example.androidtutorialrecyclerviewadv;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private ProgressBar mProgressBar;

	private ListAdapter mAdapter;
	private RecyclerView mRecyclerView;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mProgressBar = (ProgressBar) findViewById(R.id.view_progress_bar);
		mRecyclerView = (RecyclerView) findViewById(R.id.view_list);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

		mAdapter = new ListAdapter(this);
		mRecyclerView.setAdapter(mAdapter);

		mProgressBar.setVisibility(View.VISIBLE);
		mRecyclerView.setVisibility(View.GONE);

		new LoadGoogleReposTask().execute();
	}

	private class LoadGoogleReposTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(final Void... params) {
			final List<String> names = getGoogleReposNames();

			return names.toArray(new String[names.size()]);
		}

		@Override
		protected void onPostExecute(final String[] strings) {
			if (!isDestroyed()) {
				mProgressBar.setVisibility(View.GONE);
				mRecyclerView.setVisibility(View.VISIBLE);

				mAdapter.setData(strings);
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	private List<String> getGoogleReposNames() {
		final List<String> names = new ArrayList<>();

		final URL url;
		try {
			url = new URL("https://api.github.com/users/google/repos");
		} catch (final MalformedURLException e) {
			throw new RuntimeException(e);
		}

		final HttpURLConnection urlConnection;
		final StringBuilder sb = new StringBuilder();
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
			final BufferedReader reader =
					new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String line;
			do {
				line = reader.readLine();
				sb.append(line);
			} while (line != null);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		urlConnection.disconnect();

		try {
			final JSONArray reposJsonArray = new JSONArray(sb.toString());
			for (int i = 0; i < reposJsonArray.length(); i++) {
				names.add(reposJsonArray.getJSONObject(i).getString("name"));
			}
		} catch (final JSONException e) {
			throw new RuntimeException(e);
		}
		return names;
	}

	private static class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

		private String[] mData;
		private final LayoutInflater mInflater;

		public ListAdapter(final Context context) {
			mInflater = LayoutInflater.from(context);
		}

		public void setData(final String[] data) {
			mData = data;
		}

		@Override
		public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
			return new ViewHolder(mInflater.inflate(android.R.layout.simple_list_item_1, parent, false));
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, final int position) {
			holder.textView.setText(mData[position]);
		}

		@Override
		public int getItemCount() {
			return mData == null ? 0 : mData.length;
		}

		public class ViewHolder extends RecyclerView.ViewHolder {

			public TextView textView;

			public ViewHolder(final View itemView) {
				super(itemView);

				textView = (TextView) itemView.findViewById(android.R.id.text1);
			}
		}
	}
}
