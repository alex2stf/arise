package com.arise.core.tools;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.*;

/**
 * Created by alex on 19/10/2017.
 */
public class StringEncoder {

    private final static String basechars = "qorghdmspta";
    private final static String base64chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    private static final Mole logger = Mole.getInstance(StringEncoder.class);
    private static final List<SecureRandom> secureRandoms = new ArrayList<SecureRandom>();

     static {
         secureRandoms.add(new SecureRandom());

        String [][] algorithms = new String[][]{
                {"SHA1PRNG", "SUN"},
                {"NativePRNG", "SUN"},
                {"SHA1PRNG", "SUN"},
                {"NativePRNGNonBlocking", "SUN"},
                {"Windows-PRNG", "SunMSCAPI"},
        };
        for (String s[]: algorithms){
            SecureRandom sr = null;
            try {
                 sr = SecureRandom.getInstance(s[0], s[1]);
            } catch (NoSuchAlgorithmException e) {
                logger.warn("NoSuchAlgorithmException on [" + s[0] + " " + s[1] + "]");
                sr = null;
            } catch (NoSuchProviderException e) {
                logger.warn("NoSuchProviderException on [" + s[0] + " " + s[1] + "]");
                sr = null;
            } finally {
                if (sr != null){
                    logger.trace("SecureRandom of [" + s[0] + " " + s[1] + "]");
                    secureRandoms.add(sr);
                }
            }
        }

    }
    public static String unique(){
        int index = (int) Math.round(Math.random() * secureRandoms.size());
        SecureRandom sr = secureRandoms.get(index);
        String response = UUID.randomUUID().toString().replaceAll("-", "");
        response+= "|T" + System.currentTimeMillis();
        response+="|sr" + sr.getAlgorithm() + sr.getProvider().getName() + sr.nextDouble();
        return response;
    }

    public static String encodePassword(String val){
        int index = StringEncoder.getIndex(val.hashCode()+val.length(), StringEncoder.MAX_CASES - 1);
        return StringEncoder.encode(val, index);
    }

    public static String itos(Object obj){
        String in = String.valueOf(obj);
        StringBuilder sb = new StringBuilder();
        for (int i =0; i < in.length(); i++){
            try {
                int c = Integer.parseInt(String.valueOf(in.charAt(i)));
                sb.append(basechars.charAt(c));
            }catch (NumberFormatException ex){
                sb.append(in.charAt(i));
            }
        }
        return sb.toString();
    }

    public static String failsafe(String ... inputs){
        StringBuilder sb = new StringBuilder();
        for (String s: inputs){
            sb.append(s.hashCode());
        }
        return itos(sb.reverse().toString());
    }

    public static String opMessage(String message, String key, String method) {
        try {
            if (message == null || key == null) return null;

            char[] keys = key.toCharArray();
            char[] mesg = message.toCharArray();

            int ml = mesg.length;
            int kl = keys.length;
            char[] newmsg = new char[ml];

            for (int i = 0; i < ml; i++) {
                if ("+".equals(method)){
                    newmsg[i] = (char)(mesg[i] + keys[i % kl]);
                }
                else if ("-".equals(method)){
                    newmsg[i] = (char)(mesg[i] + keys[i % kl]);
                }
                else if ("^".equals(method)){
                    newmsg[i] = (char)(mesg[i] ^ keys[i % kl]);
                }
                else if ("%".equals(method)){
                    newmsg[i] = (char)(mesg[i] % keys[i % kl]);
                }
                else if ("*".equals(method)){
                    newmsg[i] = (char)(mesg[i] * keys[i % kl]);
                }
                else {
                    newmsg[i] = (char)(mesg[i] + keys[i % kl]);
                }

            }//for i

            return new String(newmsg);
        } catch (Exception e) {
            return null;
        }
    }//xorMessage

    public static final String reverse(String input){
        char[] in = input.toCharArray();
        int begin=0;
        int end=in.length-1;
        char temp;
        while(end>begin){
            temp = in[begin];
            in[begin]=in[end];
            in[end] = temp;
            end--;
            begin++;
        }
        return new String(in);
    }

    public static final Algorithm MESSAGE_DIGEST  = new Algorithm() {
        public String encode(String input, String algo) {
            MessageDigest mDigest = null;
            try {
                mDigest = MessageDigest.getInstance(algo);
            } catch (NoSuchAlgorithmException e) {
                List<String> avail = getAlgorithmNames(MessageDigest.class);
                int index = getIndex(algo.hashCode(), avail.size());
                String alg = avail.get(index);
                try {
                    mDigest = MessageDigest.getInstance(avail.get(index));
                } catch (Exception e1) {
                    mDigest = null;
                }
            }


            if (mDigest != null){
                byte[] result = mDigest.digest(input.getBytes());
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < result.length; i++) {
                    sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
                }
                return sb.toString();
            }

            return failsafe(input, algo);
        }

        public String encode(String input) {
           return encode(input, "SHA-1");
        }
    };



    public static final Algorithm BASE64 = new Algorithm() {

        public String encode(String s, String algo) {
            // the result/encoded string, the padding string, and the pad count
            String r = "", p = "";
            int c = s.length() % 3;

            // add a right zero pad to make this string a multiple of 3 characters
            if (c > 0) {
                for (; c < 3; c++) {
                    p += "=";
                    s += "\0";
                }
            }

            // increment over the length of the string, three characters at a time
            for (c = 0; c < s.length(); c += 3) {

                // we add newlines after every 76 output characters, according to
                // the MIME specs
                if (c > 0 && (c / 3 * 4) % 76 == 0)
                    r += "\r\n";

                // these three 8-bit (ASCII) characters become one 24-bit number
                int n = (s.charAt(c) << 16) + (s.charAt(c + 1) << 8)
                        + (s.charAt(c + 2));

                // this 24-bit number gets separated into four 6-bit numbers
                int n1 = (n >> 18) & 63, n2 = (n >> 12) & 63, n3 = (n >> 6) & 63, n4 = n & 63;

                // those four 6-bit numbers are used as indices into the base64
                // character list
                r += "" + base64chars.charAt(n1) + base64chars.charAt(n2)
                        + base64chars.charAt(n3) + base64chars.charAt(n4);
            }

            return r.substring(0, r.length() - p.length()) + p;
        }

        public String encode(String input) {
            return encode(input, null);
        }
    };


    public static final Algorithm BASE32 = new Algorithm() {
        private final String base32Chars =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        private final int[] base32Lookup =
                { 0xFF,0xFF,0x1A,0x1B,0x1C,0x1D,0x1E,0x1F,
                        0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,
                        0xFF,0x00,0x01,0x02,0x03,0x04,0x05,0x06,
                        0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,
                        0x0F,0x10,0x11,0x12,0x13,0x14,0x15,0x16,
                        0x17,0x18,0x19,0xFF,0xFF,0xFF,0xFF,0xFF,
                        0xFF,0x00,0x01,0x02,0x03,0x04,0x05,0x06,
                        0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,
                        0x0F,0x10,0x11,0x12,0x13,0x14,0x15,0x16,
                        0x17,0x18,0x19,0xFF,0xFF,0xFF,0xFF,0xFF
                };

        public String encode(String input, String algo) {
            byte[] bytes = input.getBytes();
            int i = 0, index = 0, digit = 0;
            int currByte, nextByte;
            StringBuffer base32
                    = new StringBuffer((bytes.length + 7) * 8 / 5);

            while (i < bytes.length) {
                currByte = (bytes[i] >= 0) ? bytes[i] : (bytes[i] + 256);

            /* Is the current digit going to span a byte boundary? */
                if (index > 3) {
                    if ((i + 1) < bytes.length) {
                        nextByte = (bytes[i + 1] >= 0)
                                ? bytes[i + 1] : (bytes[i + 1] + 256);
                    } else {
                        nextByte = 0;
                    }

                    digit = currByte & (0xFF >> index);
                    index = (index + 5) % 8;
                    digit <<= index;
                    digit |= nextByte >> (8 - index);
                    i++;
                } else {
                    digit = (currByte >> (8 - (index + 5))) & 0x1F;
                    index = (index + 5) % 8;
                    if (index == 0)
                        i++;
                }
                base32.append(base32Chars.charAt(digit));
            }

            return base32.toString();
        }

        public String encode(String input) {
            return encode(input, null);
        }
    };



    public static final Algorithm CYPHER = new Algorithm() {

        public String encode(String input, String algo_key) {
            if (algo_key == null){
                algo_key = "AES_" + reverse(input);
            }

            String key = null;
            String p[] = algo_key.split("_");
            String algo = p[0]; //might be invalid
            if (p.length <= 1){
                key = reverse(input);
            } else {
                key = p[1];
            }


            while (key.length() < 16){
                key = key+key.charAt(0);
            }
            while (key.length() > 16){
                key = key.substring(1);
            }
            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance(algo);
            } catch (Exception e) {
                try {
                    cipher = Cipher.getInstance("AES");
                } catch (NoSuchAlgorithmException e1) {
                    return failsafe(input, algo_key+"NoSuchAlgorithmException");
                } catch (NoSuchPaddingException e1) {
                    return failsafe(input, algo_key + "NoSuchPaddingException");
                }

            } finally {
                if (cipher != null){
                    String alg = "AES";
                    if ("Rijndael".equals(algo)){
                        alg = algo;
                    }
                    Key aesKey = new SecretKeySpec(key.getBytes(), alg); // sau Rijndael
                    try {
                        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                        StringBuilder sb = new StringBuilder();
                        for (byte b : cipher.doFinal(input.getBytes())) {
                            sb.append(String.format("%02X", b));
                        }
                        return sb.toString();
                    } catch (InvalidKeyException e) {
                        return failsafe(input, algo_key + "InvalidKeyException");
                    } catch (BadPaddingException e) {
                        return failsafe(input, algo_key + "BadPaddingException");
                    } catch (IllegalBlockSizeException e) {
                        return failsafe(input, algo_key + "IllegalBlockSizeException");
                    }
                }
            }

            return failsafe(input, algo_key);
        }


        public String encode(String input) {
            return encode(input, "AES_" + input);
        }
    };


    private static Algorithm[]encoders = new Algorithm[]{
            MESSAGE_DIGEST, BASE64, BASE32, CYPHER,


            new Algorithm() {
                public String encode(String input, String algo) {
                    return MESSAGE_DIGEST.encode(input, algo) + BASE64.encode(input) + BASE32.encode(algo);
                }

                public String encode(String input) {
                    return CYPHER.encode(input) + BASE64.encode(reverse(input));
                }
            }
    };


    public static final List<String> getAlgorithmNames(java.security.Provider prov, Class<?> typeClass) {
        List<String> r = new ArrayList<String>();
        String type = typeClass.getSimpleName();

        List<java.security.Provider.Service> algos = new ArrayList<java.security.Provider.Service>();

        Set<java.security.Provider.Service> services = prov.getServices();
        for (java.security.Provider.Service service : services) {
            if (service.getType().equalsIgnoreCase(type)) {
                algos.add(service);
            }
        }

        if (!algos.isEmpty()) {
//            System.out.printf(" --- Provider %s, version %.2f --- %n", prov.getLibName(), prov.getVersion());
            for (java.security.Provider.Service service : algos) {
                String algo = service.getAlgorithm();
                r.add(algo);
//                System.out.printf("Algorithm name: \"%s\"%n", algo);
            }
        }

        // --- find aliases (inefficiently)
//        Set<Object> keys = prov.keySet();
//        for (Object key : keys) {
//            final String prefix = "Alg.Alias." + _t + ".";
//            if (key.strDump().startsWith(prefix)) {
//                String value = prov.get(key.strDump()).strDump();
//                System.out.printf("Alias: \"%s\" -> \"%s\"%n",
//                        key.strDump().substring(prefix.length()),
//                        value);
//            }
//        }
        Collections.sort(r);
        return r;
    }


    public static final List<String> getAlgorithmNames(Class<?> typeClass){
        java.security.Provider[] providers = Security.getProviders();
        List<String> r = new ArrayList<String>();
        for (java.security.Provider p : providers) {
            for (String s : getAlgorithmNames(p, typeClass)){
                r.add(s);
            }
        }

        Collections.sort(r);
        return r;
    }

    public static int getIndex(int val, int max){
        val = Math.abs(val);
        while(val > max){
            val = (val % max);
        }
        return Math.abs(val);
    }

    public static String encode(String input, int index){
        return encode(input, String.valueOf(input.hashCode()), index);
    }




    public static String key(String input, int index){
        String tmpst = itos(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replaceAll("-", "-" + String.valueOf(index) + "-");
        String response = uuid + encode(input, String.valueOf(input.hashCode()), index);
        response = response.replaceAll("-", String.valueOf(index * 2));
        int mid = response.length() / 2;
        return response.substring(0, mid) + tmpst + response.substring(mid);
    }


    public static final int MAX_CASES = 19;

    public static String encode(String input, String key, int i){
        switch (i){
            case 0:
                return BASE32.encode(input);
            case 1:
                return MESSAGE_DIGEST.encode(input, key);
            case 2:
                return BASE64.encode(input);
            case 3:
                return CYPHER.encode(input, "AES_"+key);
            case 4:
                return BASE64.encode(input + key);
            case 5:
                return BASE32.encode(input + key);
            case 6:
                return MESSAGE_DIGEST.encode(input + key, "MD2");
            case 7:
                return MESSAGE_DIGEST.encode(input + key, "MD5");
            case 8:
                return MESSAGE_DIGEST.encode(input + key, "SHA");
            case 9:
                return MESSAGE_DIGEST.encode(input + key, "SHA-224");
            case 10:
                return MESSAGE_DIGEST.encode(input + key, "SHA-256");
            case 11:
                return MESSAGE_DIGEST.encode(
                        MESSAGE_DIGEST.encode(input + key, "SHA-384"),
                        "MD5"
                );
            case 12:
                return MESSAGE_DIGEST.encode(
                        MESSAGE_DIGEST.encode(input + key, "SHA-512"),
                        "MD5"
                );
            case 13:
                return MESSAGE_DIGEST.encode(
                        BASE32.encode(MESSAGE_DIGEST.encode(input + key, "SHA-512")),
                        "MD5"
                );
            case 14: return encodeShiftSHA(input, key);
            case 15: return encodeShiftSHA(input, key, key.hashCode() + i);
            case 16: return encodeShiftSHA(input, key, input.hashCode() + i);
            case 17: return encodeShiftSHA(input, key, (input+key).hashCode() + i);
            default: return failsafe(input, key);

            //https://gist.github.com/dorneanu/b8e53b034d89f6be383c
        }
    }


    public static final String encodeShiftSHA(String input){
        if (input == null){
            return "null";
        }
        return encodeShiftSHA(input, String.valueOf(input.hashCode()));
    }


    public static final String encodeShiftSHA(String input, String key){
        return encodeShiftSHA(input, key, key.hashCode());
    }

    public static final String encodeShiftSHA(String input, String key, int index){
        return encodeShift(input, key, index, MESSAGE_DIGEST);
    }

    public static final String encodeShift(String input, String key, int index, Algorithm algorithm){
        String val;
        char[] toEncode = (input+""+key).toCharArray();
        for (int i = 0; i < toEncode.length; i++) {
            if (Character.isLetter(toEncode[i])) {
                toEncode[i] += index;
            }
        }
        val = String.valueOf(toEncode);
        return algorithm.encode(val, key);
    }


    public interface Algorithm {
        String encode(String input, String algo);
        String encode(String input);
    }





}
