package iit.cnr.it.gatheringapp.dbutils;

import android.content.Context;
import android.util.Log;
import iit.cnr.it.gatheringapp.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by giacomo on 17/10/18.
 */

public class DbManage {

    public DbManage(){
    }



    public boolean write_from_file(Context context) throws IOException {
        URL url = null;
        HttpURLConnection httpConn = null;
        Utils utils = new Utils();
        String databaseIp = utils.getConfigValue(context, "databaseIp");
        String databasePort = utils.getConfigValue(context, "databasePort");
        String databaseName = utils.getConfigValue(context, "databaseName");
        String databaseUsername = utils.getConfigValue(context, "databaseUsername");
        String databasePassword = utils.getConfigValue(context, "databasePassword");
        String measurement = utils.getConfigValue(context, "measurement");

        try {
            url = new URL("http", databaseIp, Integer.parseInt(databasePort), "/write?db="+ databaseName + "&u=" + databaseUsername +"&p=" + databasePassword );
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setDoOutput(true);

            File directory = context.getFilesDir();
            File file = new File(directory, "data.txt");
            System.out.println(file.getAbsolutePath());
            String s = file.getAbsolutePath();
            Log.d("Influx", s);
            FileInputStream fin = new FileInputStream(s);
            copy(fin, httpConn.getOutputStream());
            fin.close();
            httpConn.getOutputStream().flush();
            httpConn.getOutputStream().close();

            // output HTTP response
            if (httpConn.getResponseCode() >= 400) {
                copy(httpConn.getErrorStream(), System.out);
                httpConn.getInputStream().close();
                httpConn.disconnect();
                Log.e("InfluxDb", "disconnected");

            } else {
                copy(httpConn.getInputStream(), System.out);
                httpConn.getInputStream().close();
                httpConn.disconnect();
                Log.e("InfluxDb", "disconnected");


            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {

            httpConn.disconnect();
            Log.e("InfluxDb", "disconnected");

        }

        return true;
    }

    public boolean write_string(Context context, String data) throws IOException {
        URL url = null;
        HttpURLConnection httpConn = null;
        Utils utils = new Utils();
        String databaseIp = utils.getConfigValue(context, "databaseIp");
        String databasePort = utils.getConfigValue(context, "databasePort");
        String databaseName = utils.getConfigValue(context, "databaseName");
        String databaseUsername = utils.getConfigValue(context, "databaseUsername");
        String databasePassword = utils.getConfigValue(context, "databasePassword");
        String measurement = utils.getConfigValue(context, "measurement");

        try {
            url = new URL("http", databaseIp, Integer.parseInt(databasePort), "/write?db="+ databaseName + "&u=" + databaseUsername +"&p=" + databasePassword );
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setDoOutput(true);
            httpConn.getOutputStream().write( data.getBytes() );

            httpConn.getOutputStream().flush();
            httpConn.getOutputStream().close();

            // output HTTP response
            if (httpConn.getResponseCode() >= 400) {
                copy(httpConn.getErrorStream(), System.out);
                httpConn.getInputStream().close();
                httpConn.disconnect();
                Log.e("InfluxDb", "disconnected");

            } else {
                copy(httpConn.getInputStream(), System.out);
                httpConn.getInputStream().close();
                httpConn.disconnect();
                Log.e("InfluxDb", "disconnected");


            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {

            httpConn.disconnect();
            Log.e("InfluxDb", "disconnected");

        }

        return true;
    }

    public static void copy(InputStream in, OutputStream out)
            throws IOException {
        synchronized (in) {
            synchronized (out) {
                byte[] buffer = new byte[256];
                while (true) {
                    int bytesRead = in.read(buffer);
                    if (bytesRead == -1)
                        break;
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }



}
