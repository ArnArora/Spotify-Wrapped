package com.example.spotifywrapped;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class SliderAdapter extends PagerAdapter {
    Context context;
    LayoutInflater inflater;


    //list of images
    public int[] images = {
            R.drawable.burna,
            R.drawable.drake,
            R.drawable.khalid,
            R.drawable.mac_miller,
            R.drawable.uzi
    };
    //list of artist titles
    public String [] names= {
            "Burna Boy",
            "Drake",
            "Khalid",
            "Mac Miller",
            "Lil Uzi Vert"
    };


    public SliderAdapter(Context context){
        this.context = context;
    }
    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view==(LinearLayout)object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.artist_slider,container,false);
        LinearLayout layoutslide = (LinearLayout) view.findViewById(R.id.slidelinearlayout);
        ImageView imgslide = (ImageView) view.findViewById(R.id.slideimg);
        TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        imgslide.setImageResource(images[position]);
        txtTitle.setText(names[position]);
        container.addView(view);
        return view;
    }
}
