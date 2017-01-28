package edu.rosehulman.keinslc.rhithmu;

import android.os.AsyncTask;

/**
 * Created by keinslc on 1/28/2017.
 */

public class ScheduleDownloadTask extends AsyncTask<String, Void, String> {

    private ScheduleConsumer mScheduleConsumer;
    private String mUsername;
    private String mPassword;

    public ScheduleDownloadTask(String username, String password, ScheduleConsumer consumer) {
        mUsername = username;
        mPassword = password;
        mScheduleConsumer = consumer;
    }

    @Override
    protected String doInBackground(String... params) {
        String schedule = "";
//        String urlstring = "https://" + mUsername.trim() +
//                ":" + mPassword.trim() +
//                "@prodweb.rose-hulman.edu/regweb-cgi/reg-sched.pl?type=Ucal&termcode=201730&view=tgrid&id=keinslc";
//        try {
//            InputStream input = null;
//            URL url = new URL(
////            input = url.openStream();
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
//            StringBuilder out = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                out.append(line);
//            }
//            reader.close();
//            schedule = out.toString();
//        } catch (Exception e) {
//            System.out.println("EXCEPTION FOUND " + e.toString());
//        }
        return schedule;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mScheduleConsumer.onScheduleLoaded(s);
    }

    public interface ScheduleConsumer {
        void onScheduleLoaded(String schedule);
    }
}
