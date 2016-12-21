package cn.edu.gdmec.s07150724.myweather;

import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;


import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MainActivity extends AppCompatActivity {
    private MainActivity seft;
    //保存省份和城市，key为省份，value为其下级市
    private Map<String,List<String>> cityMap;
    //省份列表
    private Spinner province_spinner;
    //城市列表
    private Spinner city_spinner;
    //选择城市的dialog
    private AlertDialog choose_dialog;
    //选择城市窗口的内容布局
    private LinearLayout choose_layout;
    //设置按钮
    private ImageButton settingBtn;
    //刷新按钮
    private ImageButton refreshBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seft = this;
        setContentView(R.layout.activity_main);
        //初始化省，市map
        initProvinces();
        //初始化选择城市窗口
        initChooseDialog();
        settingBtn = (ImageButton) this.findViewById(R.id.setting);
        refreshBtn = (ImageButton) this.findViewById(R.id.refresh);
        settingBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                choose_dialog.show();
            }
        });
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获得当前城市
                String cityName = ((TextView)seft.findViewById(R.id.city)).getText().toString();
                //请求天气信息
                new GetWeatherInfoTask(seft).execute(cityName);
            }
        });
        new GetWeatherInfoTask(this).execute("广州");
    }
    //初始化选择城市窗口
    private void initChooseDialog(){
        //创建一个警告对话框
        choose_dialog = new AlertDialog.Builder(seft).setTitle("选择城市")
                .setPositiveButton("确定",new ChooseCityListenner())
                .setNegativeButton("取消", null).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        //初始化布局
        choose_layout = (LinearLayout)inflater.inflate(R.layout.choose,null);
        //初始化省分spinner
        province_spinner = (Spinner)choose_layout.findViewById(R.id.province_spinner);
        //将所有省份取出放进选择省份spinner
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<String>(this,R.layout.simple_list_item,new ArrayList<String >(
                cityMap.keySet()
        ));
        province_spinner.setAdapter(provinceAdapter);
        //初始化城市spinner
        city_spinner = (Spinner)choose_layout.findViewById(R.id.city_spinner);
        //省份选择二级级联事件 当选中一个省份时city_spinner自动出现该省份的所有下级市
        province_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //获得选中省份的名称
                String province_name = province_spinner.getSelectedItem().toString();
                //获得选中省份的所有下级市，并放进城市选择spinner
                ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                        seft,R.layout.simple_list_item,cityMap.get(province_name)
                );
                city_spinner.setAdapter(cityAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //设置窗口的内容
        choose_dialog.setView(choose_layout);
    }
    //选择城市对话框的确定单击事件
    private class ChooseCityListenner implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //获得选中的城市
            String city_name = city_spinner.getSelectedItem().toString();
            TextView cityName = (TextView)seft.findViewById(R.id.city);
            //设置当前城市名称
            cityName.setText(city_name);
            //链接网络获取选中城市的天气
            new GetWeatherInfoTask(seft).execute(city_name);
        }
    }
    //解析XML文件，初始化省份和城市
    private void initProvinces(){
        AssetManager assetManager = getAssets();
        SaxHandler handler = new SaxHandler();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open("City.xml");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(inputStream,handler);
            //解析xml得到省市列表
            cityMap = handler.getCityMap();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }finally {
            if (inputStream !=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
