package com.gtp.hunter.wms.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class ViewTaskStub extends BaseModel implements Comparable<ViewTaskStub> {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    @Expose
    @SerializedName("id")
    private UUID id;

    @Expose
    @SerializedName("docname")
    private String docname;

    @Expose
    @SerializedName("doccode")
    private String doccode;

    @Expose
    @SerializedName("contents")
    private String contents;

    @Expose
    @SerializedName("cancel")
    private boolean cancel;

    @Expose
    @SerializedName("actions")
    private Action[] actions;

    @Expose
    @SerializedName("created_at")
    private String createdAtSQL;

    @Expose
    @SerializedName("created_at2")
    private String createdAtBR;

    @Expose
    @SerializedName("cancel_task")
    private boolean cancel_task;

    @Expose
    @SerializedName("priority")
    private short priority;

    public ViewTaskStub(UUID id, String docname, String contents, boolean cancel, Action[] actions, String createdAtSQL, String createdAtBR, boolean cancel_task, short priority) {
        this.id = id;
        this.docname = docname;
        this.contents = contents;
        this.cancel = cancel;
        this.actions = actions;
        this.createdAtSQL = createdAtSQL;
        this.createdAtBR = createdAtBR;
        this.cancel_task = cancel_task;
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof ViewTaskStub)) {
            return false;
        }
        ViewTaskStub other = (ViewTaskStub) o;

        return this.id.equals(other.id) &&
                cancel == other.cancel &&
                cancel_task == other.cancel_task;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, docname, contents, actions, createdAtSQL, createdAtBR, cancel, cancel_task, priority);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDocname() {
        return docname;
    }

    public void setDocname(String docname) {
        this.docname = docname;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public Action[] getActions() {
        return actions;
    }

    public void setActions(Action[] actions) {
        this.actions = actions;
    }

    public String getCreatedAtSQL() {
        return createdAtSQL;
    }

    public void setCreatedAtSQL(String createdAtSQL) {
        this.createdAtSQL = createdAtSQL;
    }

    public String getCreatedAtBR() {
        return createdAtBR;
    }

    public void setCreatedAtBR(String createdAtBR) {
        this.createdAtBR = createdAtBR;
    }

    public boolean isCancel_task() {
        return cancel_task;
    }

    public void setCancel_task(boolean cancel_task) {
        this.cancel_task = cancel_task;
    }

    public String getDoccode() {
        return doccode;
    }

    public void setDoccode(String doccode) {
        this.doccode = doccode;
    }

    @Override
    public int compareTo(@NonNull ViewTaskStub o) {
        if (this.equals(o))
            return 0;
        else {
            if (this.priority == o.priority) {
                try {
                    if (this.createdAtSQL != null && o.createdAtSQL != null) {
                        int ret = Objects.requireNonNull(SDF.parse(this.createdAtSQL)).compareTo(SDF.parse(o.createdAtSQL));
                        return ret == 0 ? this.getDoccode().compareTo(o.getDoccode()) : ret;
                    }
                } catch (ParseException ignore) {
                }
                return this.hashCode() > o.hashCode() ? -1 : 1;
            }
            return this.priority - o.priority;
        }
    }
}
