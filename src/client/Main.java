package client;

import common.util.Log;

/**
 * Initial point of execution.
 * Do not change this class.
 */
public class Main {

    private static final String[] argNames = {"AICHostIP", "AICHostPort", "AICToken", "AICRetryDelay"};
    private static final String[] argDefaults = {"127.0.0.1", "7099", "00000000000000000000000000000000", "1000"};

    public static void main(String[] args) {
        try {
            run(getArgs());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void run(String[] args) {
        try {
            Controller c = new Controller(args[0], Integer.parseInt(args[1]), args[2], Long.parseLong(args[3]));
            c.start();
        } catch (Exception e) {
            System.err.println("Client terminated with error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String[] getArgs() {
        String[] args = new String[argNames.length];
        for (int i = 0; i < argNames.length; i++) {
            args[i] = System.getenv(argNames[i]);
            if (args[i] == null)
                args[i] = argDefaults[i];
            Log.i("PARAM", argNames[i] + "=" + args[i]);
        }
        return args;
    }

}