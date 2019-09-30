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
@Table(name = "d_tags")
public class FolderTag {

    @Column(name = "created_on")
    private Date createdOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("entityId")
    private FolderEntity folder;

    @EmbeddedId
    @AttributeOverrides(value = {
        @AttributeOverride(name = "infoId", column = @Column(name = "tag_id")),
        @AttributeOverride(name = "entityId", column = @Column(name = "document_id"))
    })
    private InfoID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("infoId")
    private Tag tag;


    public Date getCreatedOn() {
        return this.createdOn;
    }

    public FolderTag setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public FolderEntity getFolder() {
        return this.folder;
    }

    public FolderTag setFolder(FolderEntity folder) {
        this.folder = folder;
        return this;
    }

    public InfoID getId() {
        return this.id;
    }

    public FolderTag setId(InfoID id) {
        this.id = id;
        return this;
    }

    public Tag getTag() {
        return this.tag;
    }

    public FolderTag setTag(Tag tag) {
        this.tag = tag;
        return this;
    }


}
