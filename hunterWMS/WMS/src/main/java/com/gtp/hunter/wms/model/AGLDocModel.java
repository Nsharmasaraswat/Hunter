package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AGLDocModel extends BaseModel implements Serializable {

    @Expose
    @SerializedName("attrib")
    String attrib;

    @Expose
    @SerializedName("name")
    String name;

    @Expose
    @SerializedName("type")
    String type;

    @Expose
    @SerializedName("options")
    List<ActionFormOption> options = new ArrayList<>();

    @Expose
    @SerializedName("value")
    String value;

    @Expose
    @SerializedName("ordem")
    Integer ordem;

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAttrib() {
        return attrib;
    }

    public void setAttrib(String attrib) {
        this.attrib = attrib;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ActionFormOption> getOptions() {
        return options;
    }

    public void setOptions(List<ActionFormOption> options) {
        this.options = options;
    }
}