//package de.tum.`in`.tumcampusapp.component.ui.smartmic
//
//import de.tum.`in`.tumcampusapp.R
//import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
//
//class SendSmartMics : BaseActivity(R.layout.activity_send){
//    private val startButton,stopButton,getIpButton, sendRequestButton, sosButton;
//    private final val REQUEST_RECORD_AUDIO_PERMISSION = 200;
//    private val permissions = {Manifest.permission.RECORD_AUDIO};
//    private val address = "";
//    public val buffer = 0;
//    public val socket = Null;
//    private int port = 50005;
//    private int request_port_listen = 50008;
//    private int request_port_send = 50009;
//    private String clientName = "Rami";
//    private boolean requestServerActive = false;
//    private boolean recorderActive = false;
//
//    AudioRecord recorder;
//
//    private int sampleRate = 16000 ; // 44100 for music
//    //    private int sampleRate = 48000 ; // 44100 for music
//    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
//    //    private int audioFormat = AudioFormat.ENCODING_PCM_FLOAT;
//    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
//    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
//    private boolean status = true;
//    String ipstring;
//
//}