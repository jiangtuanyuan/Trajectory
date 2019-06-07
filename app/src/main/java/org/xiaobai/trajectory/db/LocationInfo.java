package org.xiaobai.trajectory.db;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;


public class LocationInfo extends LitePalSupport {
    @Column
    private int id; //不可构造set方法 自增ID
    @Column
    private String lat;//维度
    @Column
    private String lng;//经度
    @Column
    private String address;//定位时候所在的详细地址
    @Column
    private String time;//时间

    public int getId() {
        return id;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
