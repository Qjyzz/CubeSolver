package com.isvsa;

import com.isvsa.jni.ImageUtilEngine;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CameraEngineActivity extends AppCompatActivity {
	private TextView colorText1,colorText2,colorText3,
			colorText4,colorText5,colorText6,
			colorText7,colorText8,colorText9;
	private CameraView mSelfView;
	private Button TakePicButton;
	public static String NCube = "Cube";
	static ImageUtilEngine imageEngine;
	char []yanse=new char[9];//存放单面颜色状态字符串
	char []copy=new char[54];//存放6面颜色状态字符串，复制送入还原算法
	StringBuffer aa=new StringBuffer();
	String s;
	int i=0;
	int c=1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.camera_color_panel);
		imageEngine = new ImageUtilEngine();
		mSelfView = (CameraView) findViewById(R.id.self_view);
		colorText1 = (TextView)findViewById(R.id.color1);
		colorText2 = (TextView)findViewById(R.id.color2);
		colorText3 = (TextView)findViewById(R.id.color3);
		colorText4 = (TextView)findViewById(R.id.color4);
		colorText5 = (TextView)findViewById(R.id.color5);
		colorText6 = (TextView)findViewById(R.id.color6);
		colorText7 = (TextView)findViewById(R.id.color7);
		colorText8 = (TextView)findViewById(R.id.color8);
		colorText9 = (TextView)findViewById(R.id.color9);
		TakePicButton=(Button)findViewById(R.id.takepic);
		TakePicButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				for(int j=0;j<9;j++)
				{
					char a;//声明字符类型a，存放6面颜色状态字符
					copy[i]=yanse[j];
					a=copy[i];
					aa.append(a);//在aa里添加a的内容
					i++;
				}
				switch (c)//Toast信息显示
				{
					case 1:Toast.makeText(getApplicationContext(),R.string.pic1,Toast.LENGTH_SHORT).show();break;
					case 2:Toast.makeText(getApplicationContext(),R.string.pic2,Toast.LENGTH_SHORT).show();break;
					case 3:Toast.makeText(getApplicationContext(),R.string.pic3,Toast.LENGTH_SHORT).show();break;
					case 4:Toast.makeText(getApplicationContext(),R.string.pic4,Toast.LENGTH_SHORT).show();break;
					case 5:Toast.makeText(getApplicationContext(),R.string.pic5,Toast.LENGTH_SHORT).show();break;
					case 6:Toast.makeText(getApplicationContext(),R.string.pic6,Toast.LENGTH_SHORT).show();break;
					default:break;
				}
				if(i==54)
				{
					s=aa.toString();//aa转换为字符串
					Intent i = new Intent();
					i.putExtra(NCube,s);//颜色状态字符串传入还原算法（传入参数）
					setResult(Activity.RESULT_OK, i);
					finish();
				}
				i=i%54;
				c++;
			}
		});


		mSelfView.setOnColorStatusChange(new OnColorStatusChange() {

			@Override
			public void onColorChange(int[] color) {
				// TODO Auto-generated method stub
				int grey = 150;
				int White=0xFFFFFFFF;
				int Red=0xFFC9547C;
				int Blue=0xFF5FAAC8;
				int Green=0xFF96C752;
				int Orange=0xFFFF684B;
				int Yellow=0xFFFFFF4F;
				int []coo=new int[9];//9位数组，存放颜色字符
				float[] HSV = new float[3];//3位数组，存放H、S、V对应数值
				int []HHH=new int[9];//9位数组，存放9个色块H值
				for(int i=0;i<9;i++){
				Color.colorToHSV(color[i], HSV);//RGB转HSV
					HHH[i]=(int)HSV[0];//取HSV[]中第一个值，即H
				if (Color.red(color[i]) > grey && Color.green(color[i]) > grey && Color.blue(color[i]) > grey)
				{
					yanse[i] = 'W';//遍历9个色块，R、G、B大于150判断为白色
					coo[i]=White;//WHITE 255 255 255 白色无法通过H值确定
				}
				else {
					float R, O, Y, G, B, MIN;//用角度度量，取值范围为0°～360°，红色开始按逆时针方向计算，红色为0°，绿色为120°,蓝色为240°，黄色为60°
					R = Math.min(HSV[0], 360 - HSV[0]);//红色在0°和360°附近均有取值
					O = Math.abs(HSV[0] - 25);//orange 根据转换公式存在负值，结果取绝对值，采样值与标准值相减得到差值，差值越小则越接近
					Y = Math.abs(HSV[0] - 60);//yellow
					G = Math.abs(HSV[0] - 120);//green
					B = Math.abs(HSV[0] - 240);//blue
					MIN = Math.min(R, O);
					MIN = Math.min(MIN, Y);
					MIN = Math.min(MIN, G);
					MIN = Math.min(MIN, B);//分别比较取最小
					if (MIN == R)//RED
					{
						yanse[i] = 'H';
						coo[i]=Red;
					} else if (MIN == O)//ORANGE
					{
						yanse[i] = 'O';
						coo[i]=Orange;
					} else if (MIN == Y)//YELLOW
					{
						yanse[i] = 'Y';
						coo[i]=Yellow;
						if(HHH[i]>70)
						{
							yanse[i] = 'G';
							coo[i]=Green;
						}
					} else if (MIN == G)//GREEN
					{
						yanse[i] = 'G';
						coo[i]=Green;
					} else if (MIN == B)//BLUE
					{
						yanse[i] = 'I';
						coo[i]=Blue;
					}
				}
				}
				colorText1.setBackgroundColor(coo[0]);//动态设置背景颜色
				colorText2.setBackgroundColor(coo[1]);
				colorText3.setBackgroundColor(coo[2]);
				colorText4.setBackgroundColor(coo[3]);
				colorText5.setBackgroundColor(coo[4]);
				colorText6.setBackgroundColor(coo[5]);
				colorText7.setBackgroundColor(coo[6]);
				colorText8.setBackgroundColor(coo[7]);
				colorText9.setBackgroundColor(coo[8]);
				colorText1.setText(String.valueOf(HHH[0]));//将colorText1上的内容设为当前颜色的H值
				colorText2.setText(String.valueOf(HHH[1]));
				colorText3.setText(String.valueOf(HHH[2]));
				colorText4.setText(String.valueOf(HHH[3]));
				colorText5.setText(String.valueOf(HHH[4]));
				colorText6.setText(String.valueOf(HHH[5]));
				colorText7.setText(String.valueOf(HHH[6]));
				colorText8.setText(String.valueOf(HHH[7]));
				colorText9.setText(String.valueOf(HHH[8]));
			}
		});
	}

	public static ImageUtilEngine getImageEngine() {
		return imageEngine;
	}

}