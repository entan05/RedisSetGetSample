package jp.team.e_works.redissetgetsample;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

import redis.clients.jedis.Jedis;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 6379;

    private static final int MESSAGE_GET_RESULT = 666;
    private static final int MESSAGE_KEYS_RESULT = 777;

    private Handler mHandler;

    private String mHost;
    private int mPort;

    private ListView mKeyListView;
    private ArrayList<ListItem> mKeyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_GET_RESULT:
                        JSONObject json = (JSONObject) msg.obj;
                        try {
                            ListItem item = new ListItem(json.getString("key"), json.getString("value"));
                            mKeyList.add(item);
                            ListAdapter adapter = new ListAdapter(MainActivity.this, R.layout.list_item, mKeyList);
                            mKeyListView.setAdapter(adapter);
                        } catch (Exception e) {
                            Log.e(TAG, exception2String(e));
                        }
                        break;
                    case MESSAGE_KEYS_RESULT:
                        Set<String> keys = (Set<String>) msg.obj;
                        for(String key : keys) {
                            get(mHost, mPort, key);
                        }
                        break;
                    default:
                        // pass
                        break;
                }
            }
        };

        mKeyListView = (ListView) findViewById(R.id.list_keys);
        mKeyList = new ArrayList<>();

        final EditText textHost = (EditText) findViewById(R.id.text_host);
        final EditText textPort = (EditText) findViewById(R.id.text_port);

        final EditText textKey = (EditText) findViewById(R.id.text_key);
        final EditText textValue = (EditText) findViewById(R.id.text_value);

        Button btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = textKey.getText().toString();
                if(TextUtils.isEmpty(key)) {
                    return;
                }
                String value = textValue.getText().toString();

                String host = textHost.getText().toString();
                if(TextUtils.isEmpty(host)) {
                    host = DEFAULT_HOST;
                }
                int port = DEFAULT_PORT;
                try {
                    port = Integer.parseInt(textPort.getText().toString());
                } catch (Exception e) {
                    // pass
                }

                set(host, port, key, value);
                textKey.setText("");
                textValue.setText("");
            }
        });

        Button btnReload = (Button) findViewById(R.id.btn_reload);
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String host = textHost.getText().toString();
                if(TextUtils.isEmpty(host)) {
                    host = DEFAULT_HOST;
                }
                int port = DEFAULT_PORT;
                try {
                    port = Integer.parseInt(textPort.getText().toString());
                } catch (Exception e) {
                    // pass
                }
                mHost = host;
                mPort = port;
                mKeyList.clear();
                keys(host, port, "*");
            }
        });
    }

    private void set(final String host, final int port, final String key, final String value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Jedis jedis = new Jedis(host, port);
                    jedis.set(key, value);
                    jedis.quit();
                } catch (Exception e) {
                    Log.e(TAG, exception2String(e));
                }
            }
        }).start();
    }

    private void get(final String host, final int port, final String key) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Jedis jedis = new Jedis(host, port);
                    String value = jedis.get(key);
                    jedis.quit();

                    // 結果をJSONに詰める
                    JSONObject json = new JSONObject();
                    json.put("key", key);
                    json.put("value", value);

                    Message message = Message.obtain();
                    message.what = MESSAGE_GET_RESULT;
                    message.obj = json;
                    mHandler.sendMessage(message);
                } catch (Exception e) {
                    Log.e(TAG, exception2String(e));
                }
            }
        }).start();
    }

    private void keys(final String host, final int port, final String pattern) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Jedis jedis = new Jedis(host, port);
                    Set<String> keys = jedis.keys(pattern);
                    jedis.quit();

                    Message message = Message.obtain();
                    message.what = MESSAGE_KEYS_RESULT;
                    message.obj = keys;
                    mHandler.sendMessage(message);
                } catch (Exception e) {
                    Log.e(TAG, exception2String(e));
                }
            }
        }).start();
    }

    private String exception2String(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString());
        sb.append("\n");
        for(StackTraceElement ste : e.getStackTrace()) {
            sb.append("    ");
            sb.append(ste.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
