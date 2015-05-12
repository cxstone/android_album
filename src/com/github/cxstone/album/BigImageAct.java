package com.github.cxstone.album;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class BigImageAct extends Activity{
	
	private ImageView mImg;
	private Bitmap bmpDefaultPic = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.big_image);
		
		mImg = (ImageView) findViewById(R.id.img);
		
		bmpDefaultPic = BitmapFactory.decodeFile(getIntent().getExtras().getString("path"), null);
		mImg.setImageBitmap(bmpDefaultPic);
	
	}
}
