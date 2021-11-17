package com.arise.astox.net.models.http;

import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.StringUtil;

import java.util.*;

public class Multipart {

    private static boolean isMatch(byte[] pattern, byte[] input, int pos) {
        for(int i=0; i< pattern.length; i++) {
            if(pattern[i] != input[pos+i]) {
                return false;
            }
        }
        return true;
    }

    /**
     *         byte[] boundary;
     *         try {
     *             boundary = boundaryStr.getBytes("ISO-8859-1");
     *         } catch (UnsupportedEncodingException e) {
     *             boundary = boundaryStr.getBytes(); // Intentionally falls back to default charset
     *         }
     * @param input
     * @param boundary
     * @return
     */

    public static List<byte[]> splitBytesByBoundary(byte[] input, String boundary) {
        List<byte[]> l = new LinkedList<byte[]>();
        int blockStart = 0;
        byte[] pattern1 = ("--" + boundary + "\r\n").getBytes();
        byte[] pattern2 = ("--" + boundary + " \n").getBytes();
        byte[] patternEnd = ("--" + boundary + "--").getBytes();
        int matches = 0;
        for(int i=0; i<input.length; i++) {
            if(isMatch(pattern2,input,i)) {
                if(matches > 0) {
                    byte[] copy = Arrays.copyOfRange(input, blockStart, i);
                    l.add(copy);

                }
                blockStart = i + pattern2.length;
                i = blockStart;
                matches++;
            }
            else if(isMatch(pattern1,input,i)) {
                if (matches > 0) {
                    byte[] copy = Arrays.copyOfRange(input, blockStart, i);
                    l.add(copy);

                }
                blockStart = i + pattern1.length;
                i = blockStart;
                matches++;
            }
            else if (isMatch(patternEnd, input, i)){
                byte[] copy = Arrays.copyOfRange(input, blockStart, i);
                l.add(copy);
                return l;
            }
        }
        l.add(Arrays.copyOfRange(input, blockStart, input.length ));
        return l;
    }


    public static FormData getFormData(byte[] bytes, String boundary){

        FormData formData = new FormData();
        if (bytes.length < 5){
            formData.bytes = bytes;
            formData.headers = new HashMap<>();
            return formData;
        }

        int index = 0;
        int i = 0;
        for (i = 0; i < bytes.length - 4 ; i++){
            byte n1 = bytes[i];
            byte n2 = bytes[i+1];
            byte n3 = bytes[i+2];
            byte n4 = bytes[i+3];
            if ( (n1 == '\r' && n2 == '\n' && n3 == '\r' && n4 == '\n') ){
                index = i + 3;
                break;
            }
            if (n1 == '\n' && n2 == '\n'){
                index = i + 1;
                break;
            }
        }

        if (index > 0) {


            int end = bytes.length;
            int start = index + 1;
            String bound = boundary + "--";
            if (StringUtil.bytesEndWithString(bytes, bound)){
                end = end - bound.length();
            }

            if (end > start) {
                byte[] fileBytes = Arrays.copyOfRange(bytes, start, end);
                formData.bytes = fileBytes;
            }
            byte[] headerBytes = Arrays.copyOfRange( bytes, 0, i);
            StringBuilder sb = new StringBuilder();
            List<String> lines = new ArrayList<>();
            for (int j = 0; j < headerBytes.length; j++){
                char c = (char) headerBytes[j];
                if (c == '\n'){
                    lines.add(sb.toString().trim());
                    sb = new StringBuilder();
                }
                else {
                    sb.append(c);
                }
            }
            lines.add(sb.toString().trim());

            Map<String, String> headers = new HashMap<>();
            for (String line: lines){
                String args[] = StringUtil.splitByFirstOccurrence(line, ":");
                headers.put(args[0], args[1]);
            }
            formData.headers = headers;
        }
        else {
            formData.bytes = bytes;
            formData.headers = new HashMap<>();
        }




        return formData;
    }




    public static class FormData {
        byte[] bytes;
        Map<String, String> headers;

        public byte[] getBytes() {
            return bytes;
        }

        public String getFileName(){
            if (CollectionUtil.isEmpty(headers)){
                return null;
            }
            String cdisp = headers.get("Content-Disposition");
            if (!StringUtil.hasText(cdisp)){
                return null;
            }
            String args1[] = cdisp.split(";");
            for (String arg: args1){
                String cp = arg.trim();
                if (cp.startsWith("filename")){
                  return StringUtil.splitByFirstOccurrence(cp, "=")[1].replaceAll("\"", "");
                }
            }
            return null;
        }

        public ContentType getContentType(){
            if (CollectionUtil.isEmpty(headers)){
                return ContentType.TEXT_PLAIN;
            }
            String ctype = headers.get("Content-Type");
            if (!StringUtil.hasText(ctype)){
                ctype = headers.get("content-type");
            }
            if (StringUtil.hasText(ctype)){
                return ContentType.search(ctype.trim());
            }

            return ContentType.TEXT_PLAIN;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public boolean hasHeader(String header) {
            return !CollectionUtil.isEmpty(headers) && headers.containsKey(header);
        }
    }
}
