package com.arise.core.tools;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkUtil {



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

    private static AddressIterator newIPV4Iterator(IPIterator iter){
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
        
        String fga = ga;
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
        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
                inetAdressesSync(c);
            }
        });
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
