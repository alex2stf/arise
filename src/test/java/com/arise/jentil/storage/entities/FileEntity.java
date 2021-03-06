package com.arise.jentil.storage.entities;

import java.util.Map;
import java.util.Set;

/**
 * Autogenerated at Thu Nov 21 16:58:22 EET 2019
 */public class FileEntity {
    private boolean deleted;
    private Long id;
    private String mimeType;
    private String name;
    private String notes;
    private Long parentId;
    private String path;
    private Map<String, String> props;
    private String serviceId;
    private Long size;
    private String status;
    private Set<String> tags;

    public Long getId() {
         return id;
    }
    public String getName() {
         return name;
    }
    public Long getSize() {
         return size;
    }
    public String getMimeType() {
         return mimeType;
    }
    public String getPath() {
         return path;
    }
    public String getServiceId() {
         return serviceId;
    }
    public boolean getDeleted() {
         return deleted;
    }
    public String getNotes() {
         return notes;
    }
    public String getStatus() {
         return status;
    }
    public Long getParentId() {
         return parentId;
    }
    public Set<String> getTags() {
         return tags;
    }
    public Map<String, String> getProps() {
         return props;
    } 
    public FileEntity setId(Long id) {
          this.id = id;
          return this;
    }
    public FileEntity setName(String name) {
          this.name = name;
          return this;
    }
    public FileEntity setSize(Long size) {
          this.size = size;
          return this;
    }
    public FileEntity setMimeType(String mimeType) {
          this.mimeType = mimeType;
          return this;
    }
    public FileEntity setPath(String path) {
          this.path = path;
          return this;
    }
    public FileEntity setServiceId(String serviceId) {
          this.serviceId = serviceId;
          return this;
    }
    public FileEntity setDeleted(boolean deleted) {
          this.deleted = deleted;
          return this;
    }
    public FileEntity setNotes(String notes) {
          this.notes = notes;
          return this;
    }
    public FileEntity setStatus(String status) {
          this.status = status;
          return this;
    }
    public FileEntity setParentId(Long parentId) {
          this.parentId = parentId;
          return this;
    }
    public FileEntity setTags(Set<String> tags) {
          this.tags = tags;
          return this;
    }
    public FileEntity setProps(Map<String, String> props) {
          this.props = props;
          return this;
    }
}
