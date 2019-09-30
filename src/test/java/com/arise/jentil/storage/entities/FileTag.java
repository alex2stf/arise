package com.arise.jentil.storage.entities;

import com.arise.jentil.embeddables.InfoID;
import java.util.Date;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "f_tags")
public class FileTag {

    @Column(name = "created_on")
    private Date createdOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("entityId")
    private FileEntity file;

    @EmbeddedId
    @AttributeOverrides(value = {
        @AttributeOverride(name = "infoId", column = @Column(name = "tag_id")),
        @AttributeOverride(name = "entityId", column = @Column(name = "file_id"))
    })
    private InfoID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("infoId")
    private Tag tag;


    public Date getCreatedOn() {
        return this.createdOn;
    }

    public FileTag setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public FileEntity getFile() {
        return this.file;
    }

    public FileTag setFile(FileEntity file) {
        this.file = file;
        return this;
    }

    public InfoID getId() {
        return this.id;
    }

    public FileTag setId(InfoID id) {
        this.id = id;
        return this;
    }

    public Tag getTag() {
        return this.tag;
    }

    public FileTag setTag(Tag tag) {
        this.tag = tag;
        return this;
    }


}
