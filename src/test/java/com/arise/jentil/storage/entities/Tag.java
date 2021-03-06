package com.arise.jentil.storage.entities;

import java.util.Set;

/**
 * Autogenerated at Thu Nov 21 16:58:22 EET 2019
 */public class Tag {
    private Set<FileTag> fileTags;
    private Set<FolderTag> folderTags;
    private Long id;
    private String value;

    public Long getId() {
         return id;
    }
    public String getValue() {
         return value;
    }
    public Set<FileTag> getFileTags() {
         return fileTags;
    }
    public Set<FolderTag> getFolderTags() {
         return folderTags;
    } 
    public Tag setId(Long id) {
          this.id = id;
          return this;
    }
    public Tag setValue(String value) {
          this.value = value;
          return this;
    }
    public Tag setFileTags(Set<FileTag> fileTags) {
          this.fileTags = fileTags;
          return this;
    }
    public Tag setFolderTags(Set<FolderTag> folderTags) {
          this.folderTags = folderTags;
          return this;
    }
}
