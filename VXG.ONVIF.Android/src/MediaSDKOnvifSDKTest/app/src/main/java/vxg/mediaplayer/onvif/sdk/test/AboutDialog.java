/*
 *
 * Copyright (c) 2011-2017 VXG Inc.
 *
 */


package vxg.mediaplayer.onvif.sdk.test;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;



//import vxg.mediaplayer.onvif.sdk.test.R;
public class AboutDialog extends Dialog
{
	private static Context mContext = null;
	public AboutDialog(Context context)
	{
		super(context);
		mContext = context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setContentView(R
				.layout.about);
		setTitle(R.string.app_about_title); 

		TextView tv = (TextView)findViewById(R.id.about_opensource_text);
		tv.setText(Html.fromHtml(readRawTextFile(R.raw.opensource)));
		tv.setLinkTextColor(Color.WHITE);
		Linkify.addLinks(tv, Linkify.ALL);

		tv = (TextView)findViewById(R.id.about_info_text);
		tv.setText(Html.fromHtml(readRawTextFile(R.raw.info)));
		tv.setLinkTextColor(Color.WHITE);
		Linkify.addLinks(tv, Linkify.ALL);
	}

	public static String readRawTextFile(int id)
	{
		InputStream inputStream = mContext.getResources().openRawResource(id);
		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader buf = new BufferedReader(in);
		String line;
		StringBuilder text = new StringBuilder();
		try 
		{
			while (( line = buf.readLine()) != null) text.append(line);
		} 
		catch (IOException e)
		{
			return null;
		}

		return text.toString();
	}
}