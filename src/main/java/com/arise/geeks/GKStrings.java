package com.arise.geeks;

public class GKStrings {
  //https://app.codesignal.com/challenge/Fucm34ZryB8EBKbJP
  public static boolean istPalindrom(char[] word){
    int i1 = 0;
    int i2 = word.length - 1;
    while (i2 > i1) {
      if (word[i1] != word[i2]) {
        return false;
      }
      ++i1;
      --i2;
    }
    return true;
  }


  public static String buildPalindrome(String st) {
    String copy = st;
    int initLength = st.length();
    int cnt = 0;
    while(!istPalindrom(copy.toCharArray())){
      copy = copy.substring(0, initLength) + st.charAt(cnt) + copy.substring(initLength, copy.length());
      cnt++;
      System.out.println(copy);
    }
    return copy;
  }


  //https://app.codesignal.com/challenge/wBBHdf8aSNd4q9ECZ
  public static  String lineEncoding(String s) {
    StringBuilder resp = new StringBuilder();

    char prev = s.charAt(0);
    int cnt  = 1;
    int visitedIndex = 0;
    char c = '\0';
    int i;

    for ( i = 1; i < s.length(); i++){
      c = s.charAt(i);
      if (c != prev){
        if (cnt > 1){
          resp.append(cnt);
        }
        resp.append(prev);
        cnt = 1;
        visitedIndex = i;
      } else {
        cnt++;
      }
      prev = c;
    }

    if (visitedIndex != i){
      if (cnt > 1){
        resp.append(cnt);
      }
      resp.append(c);
    }


    return resp.toString();
  }




}
