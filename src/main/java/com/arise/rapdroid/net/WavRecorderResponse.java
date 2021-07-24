package com.arise.rapdroid.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.arise.astox.net.models.SingletonHttpResponse;
import com.arise.astox.net.models.ServerRequest;
import com.arise.core.tools.Mole;


public class WavRecorderResponse extends SingletonHttpResponse {

    private static final int RECORDER_BPP = 16;
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    short[] audioData;

    private volatile AudioRecord recorder = null;
    private int bufferSize = 200;
    private volatile boolean recording = false;
    private Mole log = Mole.getInstance(WavRecorderResponse.class);

    private Thread worker;


    public WavRecorderResponse() {
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) * 3;

        audioData = new short[bufferSize]; // short array that pcm data is put


       this.addHeader("Connection", "close")
                .addHeader("Server", "Test1234")
                .addHeader("Cache-Control", "no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0")
                .addHeader("Pragma", "no-cache")
                .addHeader("Expires", "-1")
                .addHeader("Content-Type", "audio/x-wav");
    }

    @Override
    public boolean isSelfManageable() {
        return true;
    }


    @Override
    public void onTransporterAccepted(ServerRequest request, Object... args) {
        super.onTransporterAccepted(request, args);
        sendWAVHeader();
    }


    private void sendWAVHeader(){
        byte[] headBytes = (this.headerLine().getBytes(Charset.forName("UTF-8")));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(headBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        sendSync(headBytes);


        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = ((RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO) ? 1 : 2);
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

        try {
            WriteWaveFileHeader(byteArrayOutputStream, Integer.MAX_VALUE, Integer.MAX_VALUE, longSampleRate, channels, byteRate);
        } catch (IOException e) {
            e.printStackTrace();
        }


        sendHeader(byteArrayOutputStream.toByteArray());
    }


    public void startRecord() {

        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                if (recorder == null) {
                    stopRecording();
                    recorder = null;
                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
                    int i = recorder.getState();
                    if (i == 1) {
                        recorder.startRecording();
                    } else {
                        throw new RuntimeException("ILLEGAL RECORDING STATE");

                    }
                }

                recording = true;
                byte data[] = new byte[bufferSize];
//                while (recordr.read(data, 0, bufferSize) > -1) {
                while (recorder.read(data, 0, bufferSize) != AudioRecord.ERROR_INVALID_OPERATION) {
                    try {
                        send(data);
                    }catch (Exception e){
                        log.e("FAILED TO SEND SOUND");
                    }
                }
            }
        });
        worker.start();
    }





    public synchronized void stopRecording() {
        try {
            if (worker != null){
                worker.interrupt();
                worker = null;
            }
        } catch (Exception ex){

        }
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
            } catch (Exception e) {

            }
        }
        recording = false;
    }



    @Override
    public void onServerError(IOException e) {
        log.e("SERVER Error" + e.getMessage());
    }

    public void resume() {
        startRecord();
    }

    private void WriteWaveFileHeader(OutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (((RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO) ? 1
                : 2) * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

//        out.write(header, 0, 44);

        out.write(header);
    }

    public boolean isRecording() {
        return recording;
    }
}
