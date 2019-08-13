package de.tum.in.tumcampusapp.component.ui.smartmic;

//client port 50008

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.CommonStatusCodes;
//import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;



import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.chat.activity.JoinRoomScanActivity;

public class SmartMicSend extends AppCompatActivity {
    private Button startButton,stopButton,getIpButton, sendRequestButton, sosButton;
    private EditText nameEditText;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private String address = "";
    public byte[] buffer;
    public static DatagramSocket socket;
    private int port = 50005;
    private int request_port_listen = 50008;
    private int request_port_send = 50009;
    private String clientName = "Rami";
    private boolean requestServerActive = false;
    private boolean recorderActive = false;
    private int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private int MY_PERMISSIONS_REQUEST_CAMERA = 2;

    AudioRecord recorder;

    private int sampleRate = 16000 ; // 44100 for music
    //    private int sampleRate = 48000 ; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    //    private int audioFormat = AudioFormat.ENCODING_PCM_FLOAT;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    final int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private boolean status = true;
    String ipstring;
    JoinRoomScanActivity joinRoomScanActivity;
    private DatagramSocket socketStream;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
//        Toolbar toolbar = new Toolbar(findViewById(R.id.main_toolbar));
        setSupportActionBar(findViewById(R.id.main_toolbar));
        getSupportActionBar().setTitle("Smart Mic");
        recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,sampleRate,channelConfig,audioFormat,minBufSize);
        startButton = (Button) findViewById (R.id.start_button);
        stopButton = (Button) findViewById (R.id.stop_button);
        getIpButton = (Button) findViewById(R.id.get_ip_button);
        sendRequestButton = (Button) findViewById(R.id.send_request_button);
        sosButton = (Button) findViewById(R.id.sos);
        nameEditText = (EditText) findViewById(R.id.name_text_view);

        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        startButton.setOnClickListener (startListener);
        stopButton.setOnClickListener (stopListener);
        getIpButton.setOnClickListener(getIPListener);
        sendRequestButton.setOnClickListener(sendRequestListener);
        sosButton.setOnClickListener(sosListener);
        joinRoomScanActivity = new JoinRoomScanActivity();
        receiveQueue();
    }

    private final View.OnClickListener stopListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            status = false;
            recorder.release();
            Log.d("VS","Recorder released");

            sendRequestButton.setText("Request");
            socketStream.close();

            startButton.setText("Start");

            startButton.setEnabled(false);

            stopButton.setEnabled(false);

        }

    };

    private final View.OnClickListener sendRequestListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            if (sendRequestButton.getText().toString().equals("Request")){
                sendRequest(1);
            }
            else if (sendRequestButton.getText().toString().equals("Repeal Request")){
                sendRequest(3);
            }

        }
    };

    private final View.OnClickListener startListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            status = true;
            startButton.setText("Streaming");
            sendRequestButton.setText("Request");
            startStreaming();
        }

    };

    private final View.OnClickListener getIPListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getIP();
        }
    };

    private final View.OnClickListener sosListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendRequest(2);
        }
    };

    public void getIP(){

        Intent intent = new Intent(this, JoinRoomScanActivity.class);
        startActivityForResult(intent, 0);
//        String IP = thisCode.rawValue;
//
//        return IP;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0){
            if(resultCode == RESULT_OK){
                if (data != null){
                    ipstring = data.getStringExtra("name");
                    Log.d("sumant", ipstring);
                }
                else
                {
//                    ipstring = "192.168.0.102";
                    Log.d("Sumant", ipstring);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
        super.onStart();
    }

    public void startStreaming(){


        Thread streamThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    int dataSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                    Log.d("VS", "data size: " + dataSize);
                    socketStream = new DatagramSocket();
                    Log.d("VS", "Socket Created");

                    byte[] buffer = new byte[minBufSize];

                    Log.d("VS","Buffer created of size " + minBufSize);
                    DatagramPacket packet;

                    final InetAddress destination = InetAddress.getByName(ipstring);
                    Log.d("VS", "Address retrieved");

                    SmartMicSend.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ActivityCompat.requestPermissions(SmartMicSend.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
                        }
                    });
                    if(recorder == null){
                        Log.d("VS", "Recorder initialized");
                    }
                    recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,sampleRate,channelConfig,audioFormat,minBufSize);
                    AcousticEchoCanceler canceler = AcousticEchoCanceler.create(recorder.getAudioSessionId());
                    NoiseSuppressor suppressor = NoiseSuppressor.create(recorder.getAudioSessionId());
                    canceler.setEnabled(true);
                    suppressor.setEnabled(true);
                    recorder.startRecording();



                    while(status == true) {


                        //reading data from MIC into buffer
                        int bufSize = recorder.read(buffer, 0, buffer.length);

                        //putting buffer in the packet
                        packet = new DatagramPacket (buffer,buffer.length,destination,port);

                        socketStream.send(packet);
                        System.out.println("MinBufferSize: " +bufSize);


                    }



                } catch(UnknownHostException e) {
                    Log.e("VS", "UnknownHostException");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("VS", "IOException" + e.getLocalizedMessage());
                }
            }

        });
        streamThread.start();
    }

    public void sendRequest(int flag){
        Thread requestThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    DatagramSocket dSocket = new DatagramSocket();
                    DatagramPacket dPacket;
                    final InetAddress destination = InetAddress.getByName(ipstring);
                    byte[] message = new byte[minBufSize];
                    if(flag == 1){
                        message = nameEditText.getText().toString().getBytes("UTF-8");
                        SmartMicSend.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendRequestButton.setText("Repeal Request");
                            }
                        });
                    }
                    else if(flag == 2){
                        message = "SOS:".getBytes("UTF-8");
                    }
                    else if (flag == 3){
                        message = "PULLOUT:".getBytes("UTF-8");
                        SmartMicSend.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendRequestButton.setText("Request");
                            }
                        });
                    }

                    dPacket = new DatagramPacket(message,message.length,destination,request_port_send);
                    dSocket.send(dPacket);


                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        requestThread.start();
    }

    public void receiveQueue(){
        Thread receiveThread = new Thread(new Runnable(){
            @Override
            public void run() {
                DatagramSocket dSocket = null;
                try {
                    dSocket = new DatagramSocket(request_port_listen);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                while (true) {
                    byte[] lMsg = new byte[4096];

                    try {

                        DatagramPacket dPacket = new DatagramPacket(lMsg, lMsg.length);
                        final InetAddress destination = InetAddress.getByName(ipstring);
                        byte[] message = new byte[minBufSize];
                        dSocket.receive(dPacket);
                        String data = new String(dPacket.getData(), 0,
                                dPacket.getLength());

                        SmartMicSend.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("Sumant", data);
                                Toast.makeText(SmartMicSend.this, data, Toast.LENGTH_LONG).show();
                                if (data.equals("ACK")) {
                                    startButton.setEnabled(true);
                                    stopButton.setEnabled(true);
                                }
                            }
                        });
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                       //
                    }
                }
              //  dSocket.close();
            }
        });
        receiveThread.start();
    }
}

