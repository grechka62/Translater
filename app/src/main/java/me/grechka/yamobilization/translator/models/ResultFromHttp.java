package me.grechka.yamobilization.translator.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResultFromHttp {
    @SerializedName("code")
    @Expose
    private int code;
    @SerializedName("text")
    @Expose
    private String[] result;
    @SerializedName("lang")
    @Expose
    private String langs;

    public int getCode() {
        return code;
    }

    public String[] getResult() {
        return result;
    }

}
