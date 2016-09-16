package scripts.api.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import static org.tribot.api.General.println;

public class Internet {
    private static Proxy proxy = null;
    private static int timeout = 30000;

    /**
     * Initializes proxy settings to use for the following web requests.
     * @param args HashMap containing proxy data
     */
    public static void proxySetup(HashMap<String, String> args) {
        if (args.containsKey("socks") && args.get("socks").equals("true")) {
            proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(args.get("proxyip"), Integer.parseInt(args.get("proxyport"))));
            if (args.containsKey("proxyusername") && args.containsKey("proxypassword") && !args.get("proxyusername").equals("") && !args.get("proxypassword").equals(""))
                Authenticator.setDefault(new ProxyAuth(args.get("proxyusername"), args.get("proxypassword")));
        } else {
            if (args.containsKey("proxyip")) {
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(args.get("proxyip"), Integer.parseInt(args.get("proxyport"))));

                if (args.containsKey("proxyusername") && args.containsKey("proxypassword") && !args.get("proxyusername").equals("") && !args.get("proxypassword").equals(""))
                    Authenticator.setDefault(new ProxyAuth(args.get("proxyusername"), args.get("proxypassword")));
            }
        }
    }

    /**
     * Returns a String that contains the data response to the requested page
     * @param urlString Web URL
     * @return a String with the data response.
     */
    public static String requestUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection connection = proxy == null ? url.openConnection() : url.openConnection(proxy);
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder output = new StringBuilder();

            while ((inputLine = in.readLine()) != null)
                output.append(inputLine);
            in.close();

            return output.toString();
        } catch (IOException e) {
            println("IOExcept Req: " + e.toString());
        }
        return "";
    }

    /**
     * Returns a String that contains the data response to the requested page
     * @param urlString Web URL
     * @param params Post params
     * @return a String with the data response.
     */
    public static String postUrl(String urlString, Map<String, Object> params) throws IOException {
        URL url = new URL(urlString);
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }

        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
        URLConnection connection = proxy == null ? url.openConnection() : url.openConnection(proxy);

        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);

        connection.getOutputStream().write(postDataBytes);


        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        return response.toString();
    }
}
