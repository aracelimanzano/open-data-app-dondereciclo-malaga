package es.uma.aracelimanzano.dondereciclomlg;

/**
 * Created by aracelimanzano on 8/11/17.
 */

public class RecPoint {

    private String _type = "Non specified type";
    private int _fid = -1;
    private int _v = -1;
    private int _q = -1;
    private double _latitude = 0.0;
    private double _longitude = 0.0;
    private String _avUpdate = "Non available update";

    public RecPoint(int fid, String type, int v, int q){
        _fid = fid;
        _type = type;
        _v = v;
        _q = q;
    }

    public void setLatLng (double latitude, double longitude){
        _latitude = latitude;
        _longitude = longitude;
    }

    public void setAvailableUpdate (String avUpdate){
        _avUpdate = avUpdate;
    }

    public String getIDString() { return ""+_fid; }

    public String getType(){
        return _type;
    }

    public int getVolume(){
        return _v;
    }

    public int getQuantity(){
        return _q;
    }

    public Double getLatitude(){
        return _latitude;
    }

    public Double getLongitude(){
        return _longitude;
    }

    public String getAvailableUpdate(){
        return _avUpdate;
    }


}
