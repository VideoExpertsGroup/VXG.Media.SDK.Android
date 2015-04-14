/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */

package veg.mediaplayer.sdk.teststreamcontrol;

import java.util.List;

import veg.mediaplayer.sdk.M3U8;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
 
public class StreamsAdapter extends ArrayAdapter<M3U8.HLSStream> {
 
 
	public StreamsAdapter(Context context, int textViewResourceId, List<M3U8.HLSStream> objects) 
	{
		super(context, textViewResourceId, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View curView = convertView;
        if (curView == null) 
        {
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            curView = vi.inflate(R.layout.streams_item, null);
        }    
        
        M3U8.HLSStream cp = getItem(position);
        TextView title = (TextView) curView.findViewById (R.id.textview_streams_item_name);
        TextView info1 = (TextView) curView.findViewById (R.id.textview_streams_item_info1);
        TextView info2 = (TextView) curView.findViewById (R.id.textview_streams_item_info2);
        TextView info3 = (TextView) curView.findViewById (R.id.textview_streams_item_info3);
        TextView info4 = (TextView) curView.findViewById (R.id.textview_streams_item_info4);
 
        
        title.setText(cp.URL);
        info1.setText(cp.BANDWIDTH);
        info2.setText(cp.CODECS);
        info3.setText(cp.RESOLUTION);
        info4.setText("");
        
        return curView;
        
	}
	
}
