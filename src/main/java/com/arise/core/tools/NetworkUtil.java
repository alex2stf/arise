package com.arise.core.tools;

import com.arise.astox.net.clients.JHttpClient;
import com.arise.astox.net.models.Peer;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.core.exceptions.CommunicationException;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.arise.core.tools.Util.close;

public class NetworkUtil {
    private static final Mole log = Mole.getInstance(NetworkUtil.class);

    public static void pingUrl(final String u, final Handler<URLConnection> suk, final Handler<Object> err) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(u).openConnection();
            connection.setConnectTimeout(10000);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (200 <= responseCode && responseCode <= 399) {
                suk.handle(connection);
            } else {
                if(responseCode == 403){
                    log.error("AI LUAT BAN de la " + u);
                } else {
                    log.error("Ping " + u + " returned " + responseCode);
                }
                err.handle(responseCode);
            }
            connection.disconnect();
        } catch (Exception e) {
            log.error("Ping " + u + "throws ", e);
            err.handle(e);
        }

    }

    public static List<NetworkInterface> networkInterfaces(){
        try {
            if(NetworkInterface.getNetworkInterfaces() != null){
                return Collections.list(NetworkInterface.getNetworkInterfaces());
            }
            return Collections.EMPTY_LIST;
        } catch (SocketException e) {
            return Collections.EMPTY_LIST;
        }
    }

    private static AddressIterator newIPV4Iterator(final IPIterator iter){
        final Set<String> ips = new HashSet<>();
        return new AddressIterator() {
             @Override
             public void onIterate(InetAddress inetAddress, NetworkInterface networkInterface) {
                 boolean isIPv4 = inetAddress.getHostAddress().indexOf(':') < 0; // InetAddressUtils.isIPv4Address(sAddr);
                 String sAddr = inetAddress.getHostAddress();
                 if (isIPv4 && !"127.0.0.1".equals(sAddr)){
                     iter.onFound(sAddr);
                     ips.add(sAddr);
                 }
             }

             @Override
             public void onComplete(Iterable<InetAddress> inetAddresses) {
                 String[] x = new String[ips.size()];
                 x = ips.toArray(x);
                 iter.onComplete(x);
             }
         };
    }

    public static void scanIPV4(final IPIterator ipIterator){
        inetAddresses(newIPV4Iterator(ipIterator));
    }


    public static String getCurrentIPV4AddressSync(){
        String ga = "127.0.0.1";
        try {
            ga = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ga = "127.0.0.1";
        }
        
        final String fga = ga;
        final String[] act = new String[]{ ga };
        inetAdressesSync(newIPV4Iterator(new IPIterator() {
            @Override
            public void onFound(String ip) {
                if (fga.equals(ip)){
                   act[0] = ip;
                }
            }

            @Override
            public void onComplete(String[] ips) {
                if (act[0].indexOf("127.0") == 0 && ips.length == 1){
                    act[0] = ips[0];
                }
            }
        }));

        return act[0];
    }


    public static void inetAdressesSync(final AddressIterator c){
        final List<InetAddress> rsp = new ArrayList<>();
        List<NetworkInterface> intfs = networkInterfaces();
        for (NetworkInterface ni : intfs) {
            List<InetAddress> ads = Collections.list(ni.getInetAddresses());
            for (InetAddress a: ads){
                rsp.add(a);
                if (c != null){
                    c.onIterate(a, ni);
                }
            }
        }
        if (c != null){
            c.onComplete(rsp);
        }
    }


    public static void inetAddresses(final AddressIterator c){
        ThreadUtil.startDaemon(new Runnable() {
            @Override
            public void run() {
                inetAdressesSync(c);
            }
        }, "NetworkUtil#inetAddresses-" + UUID.randomUUID().toString());
    }





    public static String extractHost(URI uri)
    {
        // Extract the host part from the URI.
        String host = uri.getHost();

        if (host != null)
        {
            return host;
        }

        // According to Issue#74, URI.getHost() method returns null in
        // the following environment when the host part of the URI is
        // a host name.
        //
        //   - Samsung Galaxy S3 + Android API 18
        //   - Samsung Galaxy S4 + Android API 21
        //
        // The following is a workaround for the issue.

        // Extract the host part from the authority part of the URI.
        host = extractHostFromAuthorityPart(uri.getRawAuthority());

        if (host != null)
        {
            return host;
        }

        // Extract the host part from the entire URI.
        return extractHostFromEntireUri(uri.toString());
    }


    static String extractHostFromAuthorityPart(String authority)
    {
        // If the authority part is not available.
        if (authority == null)
        {
            // Hmm... This should not happen.
            return null;
        }

        // Parse the authority part. The expected format is "[id:password@]host[:port]".
        Matcher matcher = Pattern.compile("^(.*@)?([^:]+)(:\\d+)?$").matcher(authority);

        // If the authority part does not match the expected format.
        if (matcher == null || matcher.matches() == false)
        {
            // Hmm... This should not happen.
            return null;
        }

        // Return the host part.
        return matcher.group(2);
    }


    static String extractHostFromEntireUri(String uri)
    {
        if (uri == null)
        {
            // Hmm... This should not happen.
            return null;
        }

        // Parse the URI. The expected format is "scheme://[id:password@]host[:port][...]".
        Matcher matcher = Pattern.compile("^\\w+://([^@/]*@)?([^:/]+)(:\\d+)?(/.*)?$").matcher(uri);

        // If the URI does not match the expected format.
        if (matcher == null || matcher.matches() == false) {
            // Hmm... This should not happen.
            return null;
        }

        // Return the host part.
        return matcher.group(2);
    }

    public static String[] findFriendsForIp(String ip, int depth) {
        Set<String> response = new HashSet<>();
        String[] parts = ip.split("\\.");
        if (parts.length == 4){
            for (int i = 0; i < 256; i++){
                String addr = parts[0] + "." + parts[1] + "." + parts[2] + "." + i;
                if (!ip.equals(addr)){
                    response.add(addr);
                }
            }
        }
        return response.toArray(new String[response.size()]);
    }

    public static void downloadImage(String search, File output) throws Exception {
        InputStream inputStream = null;

        // This will read the data from the server;
        OutputStream outputStream = null;


        URL url = new URL(search);
        String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
        URLConnection con = url.openConnection();;

        // Setting the user agent
        con.setRequestProperty("User-Agent", USER_AGENT);

        // Requesting input data from server
        inputStream = con.getInputStream();

        // Open local file writer
        outputStream = new FileOutputStream(output);

        // Limiting byte written to file per loop
        byte[] buffer = new byte[2048];

        // Increments file size
        int length;

        // Looping until server finishes
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();
        log.info("SUCCESS download " + search +  " into " + output.getAbsolutePath());
   }


    @Deprecated
    public interface AddressIterator {
        void onIterate(InetAddress inetAddress, NetworkInterface networkInterface);
        void onComplete(Iterable<InetAddress> inetAddresses);
    }

    public static abstract class IPIterator {
        public void onFound(String ip){};
        public void onComplete(String[] ips){};
    }


}
