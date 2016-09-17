package scripts.api.web;

import org.tribot.api.General;
import scripts.api.data.Bag;
import scripts.api.files.FileHelper;
import scripts.api.patterns.BaseScript;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.tribot.api.General.println;

public class AccountCreator {
    private static String[] words;
    public static BaseScript script;
    private static Bag bag = new Bag();

    /**
     * CreationResult is an enum that contains the possible results when attempting to create an account.
     * SUCCESS - Account has been successfully created and is ready for use.
     * TIMEOUT - The script was unable to successfully request one of the pages used.
     * EMAIL_IN_USE - The email provided has already been registered with an account.
     * BAN - The current IP is banned from creating accounts. (15 minutes)
     */
    public enum CreationResult {
        SUCCESS, TIMEOUT, EMAIL_IN_USE, BAN
    }

    /**
     * Returns an array of Strings that contain separate words retrieved from a file.
     * Used to create randomized usernames.
     * @return the array of words from the word list.
     */

    public static String[] getWords() {
        if (words != null) return words;

        try {
            Path path = Paths.get(org.tribot.util.Util.getWorkingDirectory().getAbsolutePath() + "/DeluxeTutorial/rswords.txt");

            if (Files.exists(path)) {
                String result = FileHelper.readFile(path);
                if (result.length() == 0) {
                    result = Internet.requestUrl("http://45.55.150.83/rswords.txt");
                    FileHelper.writeFile(path, result, true);
                }
                words = result.split(" ");
            } else if (FileHelper.createFile(path)) {
                String result = Internet.requestUrl("http://45.55.150.83/rswords.txt");
                words = result.split(" ");
                FileHelper.writeFile(path, result, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return words;
    }

    /**
     * Registers the default information that will be used to create accounts with.
     * @param hashMap HashMap which is the arguments provided to the script
     */
    public static void setup(HashMap<String, String> hashMap) {
        hashMap = getFinalizedArguments(hashMap);
        for (String key : hashMap.keySet()) {
            bag.addOrUpdate(key, hashMap.get(key));
        }
    }

    /**
     * Returns an integer from a HashMap based on the key given.
     * @param args HashMap of String to String key value pairs.
     * @param key Key to search within the provided hashmap.
     * @return Integer value from the HashpMap if it exists, otherwise 0.
     */
    public static int getInteger(HashMap<String, String> args, String key) {
        return args.containsKey(key) ? Integer.valueOf(args.get(key)) : 0;
    }

    /**
     * Returns a boolean from a HashMap based on the key given.
     * @param args HashMap of String to String key value pairs.
     * @param key Key to search within the provided hashmap.
     * @return boolean value from the HashpMap if it exists, otherwise false.
     */
    public static boolean getBoolean(HashMap<String, String> args, String key) {
        return args.containsKey(key) ? Boolean.valueOf(args.get(key)) : false;
    }

    /**
     * Returns a String from a HashMap based on the key given.
     * @param args HashMap of String to String key value pairs.
     * @param key Key to search within the provided hashmap.
     * @return String value from the HashpMap if it exists, otherwise an empty string.
     */
    public static String getString(HashMap<String, String> args, String key) {
        return args.containsKey(key) ? args.get(key) : "";
    }

    /**
     * Returns a HashMap containing the script arguments.
     * @param input Script arguments provided from the passArguments method opon running the script.
     * @return HashMap containing a String to String key value pair.
     */
    public static HashMap<String, String> getFinalizedArguments(HashMap<String, String> input) {
        return input.containsKey("custom_input") ? getStringArguments(input.get("custom_input")) : getStringArguments(input.get("autostart"));
    }

    /**
     * Returns a HashMap containing the script arguments.
     * @param str String containing the script arguments contained inside the custom_input key.
     * @return HashpMap containing a String to String key value pair.
     */
    public static HashMap<String, String> getStringArguments(String str) {
        HashMap<String, String> args = new HashMap<>();
        if (str != null) {
            //str = str.replaceAll("\\s","");
            List<String> argList = Arrays.asList(str.split(";"));
            for(String arg : argList) {
                if (!arg.contains(":")) {
                    continue;
                }
                String[] tmp = arg.split(":");
                args.put(tmp[0], tmp[1]);
            }
        }

        return args;
    }

    /**
     * Attempts to create a new account with the details given on setup.
     * @return the CreationResult from attempting to create an account.
     * @see CreationResult
     */
    public static CreationResult createAccount() {
        if (getWords() == null) {
            return CreationResult.TIMEOUT;
        }

        String displayName = randomizeString(bag.get("displayNameBase"));
        String password = randomizeString(bag.get("password"));
        String email = bag.get("emailBase", "").replace("#", bag.get("accountIndex"));

        println("-- Generating account with details --");
        println("Display Name: " + displayName);
        println("Password: " + password);
        println("Email: " + email);
        println("Age: " + bag.get("age"));
        println("-- ------------------------------- --");

        String response = Internet.requestUrl("http://2captcha.com/in.php?key=" + bag.get("apiKey") + "&method=userrecaptcha&googlekey=6LccFA0TAAAAAHEwUJx_c1TfTBWMTAOIphwTtd1b&pageurl=https://secure.runescape.com/m=account-creation/g=oldscape/create_account");
        println("Requesting Captcha: " + response);
        if (response.substring(0, 2).equals("OK")) {
            String captchaid = response.substring(3);
            println("Captcha id: " + captchaid);
            String googleCode = "";
            while (googleCode.equals("") || googleCode.equals("CAPCHA_NOT_READY")) {
                General.sleep(10000);
                googleCode = Internet.requestUrl("http://2captcha.com/res.php?key=" + bag.get("apiKey") + "&action=get&id=" + captchaid);
                println("Request gcode: " + googleCode);
            }

            if (googleCode.substring(0, 2).equals("OK")) {
                long responseTime = System.currentTimeMillis();
                long codeTimeout = 150000; //recaptcha expires after 180s, make it alittle shorter

                Map<String,Object> params = new LinkedHashMap<>();
                params.put("trialactive", true);
                params.put("trialactive", true);
                params.put("onlyOneEmail", 1);
                params.put("displayname_present", true);
                params.put("age", Integer.parseInt(bag.get("age")));
                params.put("displayname", displayName);
                params.put("email1", email);
                params.put("password1", password);
                params.put("password2", password);
                params.put("agree_email", "on");
                params.put("submit", "Join Now");
                params.put("g-recaptcha-response", googleCode.substring(3));

                String createResponse = "";
                while (createResponse.equals("") && System.currentTimeMillis() - responseTime < codeTimeout) {
                    try {
                        createResponse = Internet.postUrl("https://secure.runescape.com/m=account-creation/g=oldscape/create_account", params);//No delay maybe? We have 30s timeout so this should be fine.
                    } catch (FileNotFoundException e) {
                        return CreationResult.BAN;
                    } catch (IOException e) {
                        
                    }
                }

                if (createResponse.equals(""))
                    return CreationResult.TIMEOUT;

                if (createResponse.contains("If your confirmation email has not arrived please check your spam filter.")) {
                    println("Account successfully created.");
                    Path path = Paths.get(org.tribot.util.Util.getWorkingDirectory().getAbsolutePath() + "/DeluxeTutorial/GeneratedAccounts.ini");
                    if (!Files.exists(path))
                        FileHelper.createFile(path);
                    FileHelper.writeFile(path, email + ":" + password + "" + System.getProperty( "line.separator" ), true);

                    bag.addOrUpdate("email", email);
                    bag.addOrUpdate("password", password);
                    bag.addOrUpdate("accountIndex", String.valueOf(Integer.parseInt(bag.get("accountIndex", "0")) + 1));
                    bag.addOrUpdate("accountsToMake", String.valueOf(Integer.parseInt(bag.get("accountsToMake", "0")) - 1));
                    return CreationResult.SUCCESS;
                } else if (createResponse.contains("Your email address has already been taken.")) {
                    println("Email is in use.");
                    bag.addOrUpdate("accountIndex", String.valueOf(Integer.parseInt(bag.get("accountIndex", "0")) + 1));
                    bag.addOrUpdate("email", email);
                    bag.addOrUpdate("password", password);
                    return CreationResult.EMAIL_IN_USE;
                }
            } else {
                return CreationResult.TIMEOUT;
            }
        } else {
            return CreationResult.TIMEOUT;
        }

        return CreationResult.BAN;
    }

    /**
     * Returns a randomized string based on the String provided.
     * @param base String template to base randomization on.
     * @return the randomized String
     */
    public static String randomizeString(String base) {
        String returnWord = "";
        String[] wordParts = base.split("\\|");
        for (int i = 0; i < wordParts.length; i++) {
            String part = wordParts[i];
            if (part.charAt(0) == '&') {
                String word = "";
                while (word.length() != part.length()) {
                    int index = (int) Math.floor(Math.random() * (words.length - 1));
                    word = words[index];
                }

                returnWord += word;
            } else if (part.charAt(0) == '#') {
                for (int j = 0; j < part.length(); j++)
                    returnWord += "" + General.random(0, 9);
            } else if (part.charAt(0) == '%') {
                for (int j = 0; j < part.length(); j++) {
                    boolean caps = General.random(0, 1) == 1;
                    returnWord += (char) (General.random(0, 26) + (caps ? 65 : 97));
                }
            } else {
                returnWord += part;
            }
        }

        return returnWord;
    }

    public static Bag getBag() {
        return bag;
    }
}

