package com.creativeminds.remotedroid;

public class SMSModel {

    private int id;
    private String address;
    private String date;
    private String body;

    public SMSModel(){

    }

    public SMSModel(int id, String address,String body, String date){
        this.id = id;
        this.address = address;
        this.date = date;
        this.body = body;
    }

    public int getID(){
        return this.id;
    }

    public void setID(int id){
        this.id = id;
    }

    public String getAddress(){
        return this.address;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public String getDate(){
        return this.date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public String getBody(){
        return this.body;
    }

    public void setBody(String body){
        this.body = body;
    }

}
