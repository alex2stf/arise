package com.arise.corona.impl;

import com.arise.corona.dto.ContentInfo;

import java.io.File;

public class ContentInfoDecoder {

   public ContentInfo decode(File file){
//       System.out.println(file.getAbsolutePath());
       return new ContentInfo(file);
   }


}
