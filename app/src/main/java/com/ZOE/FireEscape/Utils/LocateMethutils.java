package com.ZOE.FireEscape.Utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;

import com.ZOE.FireEscape.Utils.database.DBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhou on 2017/3/3.
 */

public class LocateMethutils {
    private DBHelper db;
    private SQLiteDatabase sd;
    List list=new ArrayList();
    private double value;
    private List saverssi=new ArrayList();
    public LocateMethutils(Context context){
        db = new DBHelper(context);
        sd=db.getReadableDatabase();
}
public  int Comper(List<ScanResult> results){
    int mix=0;
    Cursor c = sd.rawQuery("select * from mac", null);
    Cursor currssi = sd.rawQuery("select * from rssi", null);
    value=1000;
    int j,k;
    double m;
    int id=0;
    for( j=0;j<currssi.getCount();j++){
            currssi.moveToPosition(j);
        for (ScanResult result : results)
        {
            for(int i=0;i<c.getCount();i++) {
                c.moveToPosition(i);
                 id=c.getInt(c.getColumnIndex("id"));
                int ss=currssi.getInt(currssi.getColumnIndex("rssi"+id));
                if ((result.BSSID).equals(c.getString(c.getColumnIndex("mac"))) && ss!=0) {
                    list.add((double)result.level);
                    saverssi.add((currssi.getDouble(currssi.getColumnIndex("rssi"+id))));
                    break;
                }
            }
        }
        if(list.size()<=results.size()/2) {
            list.clear();
            saverssi.clear();
            continue;
        }
        double end=Locate(list,saverssi);
        if(end<value){
        value=end;
            mix=currssi.getInt(currssi.getColumnIndex("id"));
        }
        list.clear();
        saverssi.clear();
    }
    return  mix;
}
    public static double GetA(double input, List<Double> scans) {
        double temp = 1.0 / input;
        double tempre = 0;
        for (int i = 0; i < scans.size(); i++) {
            tempre += 1.0 / scans.get(i);
        }
        return temp / tempre;
    }


    public static double Locate(List<Double>  rssi, List<Double> scans) {
        double temp = 0.0, res = 0.0;
        for (int i = 0; i < scans.size(); i++) {
            temp = GetA(scans.get(i), scans) * Math.pow((rssi.get(i) - scans.get(i)), 2);
            res += temp;
        }
        return Math.sqrt(res);
    }
}
