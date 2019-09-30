package com.arise.jentil.storage.entities;

import java.util.Map;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "ent_files")
public class FileEntity {

    @Column(name = "deleted")
    private boolean deleted;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="file_seq_gen")
    @SequenceGenerator(name = "file_seq_gen", sequenceName="file_sequence", allocationSize=1)
    @Column(name = "id")
    private Long id;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "name")
    private String name;

    @Column(name = "notes")
    private String notes;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "path")
    private String path;

    @Transient
    private transient Map<String, String> props;

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "size")
    private Long size;

    @Column(name = "status")
    private String status;

    @Transient
    private transient Set<String> tags;


    public boolean getDeleted() {
        return this.deleted;
    }

    public FileEntity setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public Long getId() {
        return this.id;
    }

    public FileEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public FileEntity setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public FileEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getNotes() {
        return this.notes;
    }

    public FileEntity setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public Long getParentId() {
        return this.parentId;
    }

    public FileEntity setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getPath() {
        return this.path;
    }

    public FileEntity setPath(String path) {
        this.path = path;
        return this;
    }

    @Transient
    public Map<String, String> getProps() {
        return this.props;
    }

    @Transient
    public FileEntity setProps(Map<String, String> props) {
        this.props = props;
        return this;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public FileEntity setServiceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public Long getSize() {
        return this.size;
    }

    public FileEntity setSize(Long size) {
        this.size = size;
        return this;
    }

    public String getStatus() {
        return this.status;
    }

    public FileEntity setStatus(String status) {
        this.status = status;
        return this;
    }

    @Transient
    public Set<String> getTags() {
        return this.tags;
    }

    @Transient
    public FileEntity setTags(Set<String> tags) {
        this.tags = tags;
        return this;
    }


}
