package de.tum.in.tumcampusapp.component.ui.smartmic;

//client port 50008

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

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

public class SmartMicSend extends Activity{
    private Button startButton,stopButton,getIpButton, sendRequestButton, sosButton;
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

    AudioRecord recorder;

    private int sampleRate = 16000 ; // 44100 for music
    //    private int sampleRate = 48000 ; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    //    private int audioFormat = AudioFormat.ENCODING_PCM_FLOAT;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private boolean status = true;
    String ipstring;
    JoinRoomScanActivity joinRoomScanActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,sampleRate,channelConfig,audioFormat,minBufSize);
        startButton = (Button) findViewById (R.id.start_button);
        stopButton = (Button) findViewById (R.id.stop_button);
        getIpButton = (Button) findViewById(R.id.get_ip_button);
        sendRequestButton = (Button) findViewById(R.id.send_request_button);
        sosButton = (Button) findViewById(R.id.sos);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        startButton.setOnClickListener (startListener);
        stopButton.setOnClickListener (stopListener);
        getIpButton.setOnClickListener(getIPListener);
        sendRequestButton.setOnClickListener(sendRequestListener);
        sosButton.setOnClickListener(sosListener);
        joinRoomScanActivity = new JoinRoomScanActivity();
    }

    private final View.OnClickListener stopListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            status = false;
            recorder.release();
            Log.d("VS","Recorder released");
            startButton.setEnabled(false);
            stopButton.setEnabled(false);
        }

    };

    private final View.OnClickListener sendRequestListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            sendRequest(1);
        }
    };

    private final View.OnClickListener startListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            status = true;
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

    public void startStreaming() {


        Thread streamThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    int dataSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                    Log.d("VS", "data size: " + dataSize);
                    DatagramSocket socket = new DatagramSocket();
                    Log.d("VS", "Socket Created");

                    byte[] buffer = new byte[minBufSize];

                    Log.d("VS","Buffer created of size " + minBufSize);
                    DatagramPacket packet;

                    final InetAddress destination = InetAddress.getByName(ipstring);
                    Log.d("VS", "Address retrieved");

                    ActivityCompat.requestPermissions(SmartMicSend.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

//                    recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,sampleRate,AudioFormat.CHANNEL_CONFIGURATION_MONO,AudioFormat.ENCODING_PCM_16BIT,minBufSize*10);
                    Log.d("VS", "Recorder initialized");

                    AcousticEchoCanceler canceler = AcousticEchoCanceler.create(recorder.getAudioSessionId());
                    NoiseSuppressor suppressor = NoiseSuppressor.create(recorder.getAudioSessionId());
                    canceler.setEnabled(true);
                    suppressor.setEnabled(true);
                    recorder.startRecording();


                    while(status == true) {


                        //reading data from MIC into buffer
                        minBufSize = recorder.read(buffer, 0, buffer.length);

                        //putting buffer in the packet
                        packet = new DatagramPacket (buffer,buffer.length,destination,port);

                        socket.send(packet);
                        System.out.println("MinBufferSize: " +minBufSize);


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
                        message = clientName.getBytes("UTF-8");
                    }
                    else{
                        message = "SOS:".getBytes("UTF-8");
                    }
                    dPacket = new DatagramPacket (message,message.length,destination,request_port_send);
                    dSocket.send(dPacket);
                    receiveQueue();

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
                while (true) {
                    byte[] lMsg = new byte[4096];
                    DatagramSocket dSocket = null;
                    try {
                        dSocket = new DatagramSocket(request_port_listen);
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
//                    dSocket.close();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        receiveThread.start();
    }
}