package com.whispertflite.asr;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.konovalov.vad.webrtc.Vad;
import com.konovalov.vad.webrtc.VadWebRTC;
import com.konovalov.vad.webrtc.config.FrameSize;
import com.konovalov.vad.webrtc.config.Mode;
import com.konovalov.vad.webrtc.config.SampleRate;
import com.whispertflite.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Recorder {

    public interface RecorderListener {
        void onUpdateReceived(String message,byte[] samples);
    }

    private static final String TAG = "Recorder";
    public static final String ACTION_STOP = "Stop";
    public static final String ACTION_RECORD = "Record";
    public static final String MSG_RECORDING = "Recording...";
    public static final String MSG_RECORDING_DONE = "Recording done...!";
    public static final String MSG_RECORDING_ERROR = "Recording error...";

    private final Context mContext;
    private final AtomicBoolean mInProgress = new AtomicBoolean(false);

    private RecorderListener mListener;
    private final Lock lock = new ReentrantLock();
    private final Condition hasTask = lock.newCondition();

    private final Lock processLock = new ReentrantLock();
    private final Condition processTask = processLock.newCondition();

    private final AtomicBoolean mInProcess = new AtomicBoolean(false);
    private final Object fileSavedLock = new Object(); // Lock object for wait/notify

    private volatile boolean shouldStartRecording = false;
    private boolean useVAD = false;
    private VadWebRTC vad = null;
    private static final int VAD_FRAME_SIZE = 480;

    private final Thread workerThread;

    public Recorder(Context context) {
        this.mContext = context;

        initVad();

        // Initialize and start the worker thread
        workerThread = new Thread(this::recordLoop);
        workerThread.start();
    }

    public void setListener(RecorderListener listener) {
        this.mListener = listener;
    }


    public void start() {
        if (!mInProgress.compareAndSet(false, true)) {
            Log.d(TAG, "Recording is already in progress...");
            return;
        }
        lock.lock();
        try {
            Log.d(TAG, "Recording starts now");
            shouldStartRecording = true;
            hasTask.signal();
        } finally {
            lock.unlock();
        }
    }

    public void initVad(){
        vad = Vad.builder()
                .setSampleRate(SampleRate.SAMPLE_RATE_16K)
                .setFrameSize(FrameSize.FRAME_SIZE_480)
                .setMode(Mode.VERY_AGGRESSIVE)
                .setSilenceDurationMs(800)
                .setSpeechDurationMs(200)
                .build();

//        vad = Vad.builder()
//                .setSampleRate(SampleRate.SAMPLE_RATE_16K)
//                .setFrameSize(FrameSize.FRAME_SIZE_320)
//                .setMode(Mode.NORMAL)
//                .setSilenceDurationMs(800)
//                .setSpeechDurationMs(500)
//                .build();
        useVAD = true;
        Log.d(TAG, "VAD initialized");
    }


    public void stop() {
        Log.d(TAG, "Recording stopped");
        mInProgress.set(false);

        // Wait for the recording thread to finish
//        synchronized (fileSavedLock) {
//            try {
//                fileSavedLock.wait(); // Wait until notified by the recording thread
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt(); // Restore interrupted status
//            }
//        }
    }

    public boolean isInProgress() {
        return mInProgress.get();
    }

    private void sendUpdate(String message,byte[] samples) {
        if (mListener != null)
            mListener.onUpdateReceived(message,samples);
    }

    int n = 0;
    Thread processThread;

    private void processAudio() {



        int channels = 1;
        int bytesPerSample = 2;
        int sampleRateInHz = 16000;



        int bytesForFiveSeconds = sampleRateInHz * bytesPerSample * channels * 3;
        //byte[] sample = new byte[bytesForFiveSeconds];
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sample);

//      processThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                int totalSamples = 0;
//
//                boolean isProcessing = true;
//
//                while (mInProgress.get()) {
//
//                    byte[] totalBytes = outputBuffer.toByteArray();
//
//                    int available = totalBytes.length - n;
//
//                    if (available>bytesForFiveSeconds) {
//
//                        //System.arraycopy(totalBytes,n,sample,0,bytesForFiveSeconds);
//
//                        byte[] chunk = Arrays.copyOfRange(totalBytes,n,n+bytesForFiveSeconds);
//                        n+= chunk.length;
//
//                        whisperExecutor.execute(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                RecordBuffer.setOutputBuffer(chunk);
//                                sendUpdate(MSG_RECORDING_DONE);
//                            }
//                        });
//
////                        if (n >= outputBuffer.toByteArray().length) {
////
////                            isProcessing = false;
////                            mInProcess.compareAndSet(true,false);
////                            //outputBuffer.reset();
////
////
////                        }
//
//
//                    }
//
////                    else {
////
////                        isProcessing = false;
////
//////                        byte[] chunk = Arrays.copyOfRange(totalBytes,n,totalBytes.length);
//////                        //n+= bytesForFiveSeconds;
//////
//////                        whisperExecutor.execute(new Runnable() {
//////                            @Override
//////                            public void run() {
//////
//////                                RecordBuffer.setOutputBuffer(chunk);
//////                                sendUpdate(MSG_RECORDING_DONE);
//////                            }
//////                        });
//////
//////                        isProcessing = false;
//////                        mInProcess.compareAndSet(true,false);
////
////
////                    }
//
//                }
//
////                byte[] chunk = Arrays.copyOfRange(outputBuffer.toByteArray(),n,outputBuffer.toByteArray().length);
//////                        //n+= bytesForFiveSeconds;
//////
////                        whisperExecutor.execute(new Runnable() {
////                            @Override
////                            public void run() {
////
////                                RecordBuffer.setOutputBuffer(chunk);
////                                sendUpdate(MSG_RECORDING_DONE);
////                            }
////                        });
//
//
//
//            }
//        });
//
//      processThread.start();

        processThread = new Thread(() -> {
            try {
                while (mInProgress.get() || !audioQueue.isEmpty()) {
                    byte[] chunk = audioQueue.poll(500, TimeUnit.MILLISECONDS);
                    if (chunk != null) {
                        whisperExecutor.execute(() -> {
                            RecordBuffer.setOutputBuffer(chunk);
                            //sendUpdate(MSG_RECORDING_DONE);
                        });
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                mInProcess.set(false); // allow restart next time
            }
        });
        processThread.start();







    }

    private void processAudio2() {



        int channels = 1;
        int bytesPerSample = 2;
        int sampleRateInHz = 16000;

//    int bytesForFiveSeconds = sampleRateInHz * bytesPerSample * channels * 3;
//
//    Deque<Double> rmsWindow = new ArrayDeque<>();
//    int windowSize = 10; // sliding window of 10 frames (~200 ms)
//    double threshold = 0.02; // tune this for your environment


        processThread = new Thread(() -> {
            try {
                while (mInProgress.get() || !audioQueue.isEmpty()) {



                    //int frameSize = sampleRate / 50; // 20 ms frame at 16 kHz = 320 samples



                    // Fetch one frame (20 ms) from blocking queue
                    byte[] frame = audioQueue.poll(500,TimeUnit.MILLISECONDS);

                    if (frame!=null) {

                        whisperExecutor.execute(() -> {
                            RecordBuffer.setOutputBuffer(frame);
                            //sendUpdate(MSG_RECORDING_DONE);
                        });


                    }



//                    if (frame!=null) {
//
//                        double rms = calculateRMS(frame);
//
//                        rmsWindow.addLast(rms);
//                        if (rmsWindow.size() > windowSize) {
//                            rmsWindow.removeFirst();
//                        }
//
//                        // Compute average RMS across window
//                        double avgRms = rmsWindow.stream()
//                                .mapToDouble(Double::doubleValue)
//                                .average()
//                                .orElse(0.0);
//
//                        // Decision
//                        if (avgRms < threshold) {
//
//                            Log.d(TAG, "Silence detected → stop mic");
//                            System.out.println("Silence detected → stop mic");
//                            // stopMic(); // your stop logic here
//                        } else {
//
//                            whisperExecutor.execute(() -> {
//                                RecordBuffer.setOutputBuffer(frame);
//                                sendUpdate(MSG_RECORDING_DONE);
//                            });
//
//
//
//                            Log.d(TAG, "Speech active → send to queue");
//                            System.out.println("Speech active → send to queue");
//                            // processingQueue.add(frame);
//                        }
//
//
//                    }













                    // Compute RMS for this frame
//                    double sumSquares = 0.0;
//                    for (float sample : frame) {
//                        sumSquares += sample * sample;
//                    }
//                    double rms = Math.sqrt(sumSquares / frame.length);

                    // Maintain sliding window







                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            } finally {
                mInProcess.set(false); // allow restart next time
            }
        });
        processThread.start();

    }


    private void recordLoop() {
        while (true) {
            lock.lock();
            try {
                while (!shouldStartRecording) {
                    hasTask.await();
                }
                shouldStartRecording = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                lock.unlock();
            }

            // Start recording process
            try {

                recordAudio3();

                //recordAudio();

                //recordingLoop();
                //recordAudio2();
            } catch (Exception e) {
                Log.e(TAG, "Recording error...", e);
                //sendUpdate(e.getMessage());
            } finally {
                mInProgress.set(false);
            }
        }
    }

    private ExecutorService whisperExecutor = Executors.newSingleThreadExecutor();

//    private void recordAudio() {
//
//        useVAD = true;
//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            //Log.d(TAG, "AudioRecord permission is not granted");
//            sendUpdate(mContext.getString(R.string.need_record_audio_permission));
//            return;
//        }
//
//        Log.d(TAG, "AudioRecord permission is not granted");
//
//        int channels = 1;
//        int bytesPerSample = 2;
//        int sampleRateInHz = 16000;
//        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
//        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
//        int audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
//        int totalBytesRead = 0;
//        int lastEmitted = 0;
//
//        int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
//        if (bufferSize < VAD_FRAME_SIZE * 2) bufferSize = VAD_FRAME_SIZE * 2;
//
//        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//        audioManager.startBluetoothSco();
//        audioManager.setBluetoothScoOn(true);
//
//        AudioRecord.Builder builder = new AudioRecord.Builder()
//                .setAudioSource(audioSource)
//                .setAudioFormat(new AudioFormat.Builder()
//                        .setChannelMask(channelConfig)
//                        .setEncoding(audioFormat)
//                        .setSampleRate(sampleRateInHz)
//                        .build())
//                .setBufferSizeInBytes(bufferSize);
//
//        AudioRecord audioRecord = builder.build();
//        audioRecord.startRecording();
//
//        // Calculate maximum byte counts for 30 seconds (for saving)
//        int bytesForThirtySeconds = sampleRateInHz * bytesPerSample * channels * 30;
//        int bytesForFiveSeconds = sampleRateInHz * bytesPerSample * channels * 3;
//        int n1 = 0;
//        int n2 = 0;
//
//        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(); // Buffer for saving data RecordBuffer
//
//        byte[] audioData = new byte[bufferSize];
//
//
//        boolean isSpeech;
//        //boolean isRecording = false;
//        byte[] vadAudioBuffer = new byte[VAD_FRAME_SIZE * 2];  //VAD needs 16 bit
//
//        Log.d(TAG, "recordAudio: " + mInProgress.get() + " " + useVAD);
//
//        Log.d(TAG, "recordAudio: " + mInProgress.get() + " useVAD=" + useVAD);
//
//        while (mInProgress.get()) {
//
//            int bytesRead = audioRecord.read(audioData, 0, audioData.length);
//
////            short[] rmsShorts = new short[audioData.length/2];
////            double rms = 0;
////
////            for (int i = 0; i < rmsShorts.length; i++) {
////                int low  =  audioData[2*i]     &  0xFF;
////                int high = (audioData[2*i + 1] & 0xFF) << 8;
////                rmsShorts[i] = (short)(low | high);
////            }
////
////// compute RMS
////            for (short s : rmsShorts) {
////                rms += s * s;
////            }
////
////            rms = Math.sqrt(rms / rmsShorts.length);
//
//
//
//                if (vad.isSpeech(audioData)) {
//
//                    Log.d(TAG, "recordAudioTrue: ");
//                    outputBuffer.write(audioData,0,bytesRead);
//
//                    byte[] fullData = outputBuffer.toByteArray();
//
//                    int available = fullData.length - lastEmitted;
//
//                    if (available >= bytesForFiveSeconds) {
//
//
//
//                        // --------- SAFETY CHECK: prevent overflow ---------
////                    if (fullData.length < lastEmitted + bytesForFiveSeconds) {
////                        return;  // Wait for more data
////                    }
//
//                        // --------- EXACT 5-second chunk ----------
////                    byte[] chunk = Arrays.copyOfRange(
////                            fullData,
////                            lastEmitted,
////                            lastEmitted + bytesForFiveSeconds
////                    );
//
//                        // Move pointer ALWAYS (critical)
//                        // lastEmitted += bytesForFiveSeconds;
//
//                        outputBuffer.reset();
//                        //lastEmitted = 0;
//
//                        whisperExecutor.execute(() -> {
//                            RecordBuffer.setOutputBuffer(fullData);
//                            sendUpdate(MSG_RECORDING_DONE);
//                        });
//
//
////
////                    // --------- VAD on tail ----------
////                    int vadBytes = VAD_FRAME_SIZE * 2;
////
////                    if (chunk.length >= vadBytes) {
////
////                        byte[] vadBuffer = Arrays.copyOfRange(
////                                chunk,
////                                chunk.length - vadBytes,
////                                chunk.length
////                        );
////
////                        Log.d(TAG, "VAD Size: "+vadBuffer.length);
////
////                        if (vad.isSpeech(vadBuffer)) {
////
////                            Log.d(TAG, "Is Speech");
////
////                            whisperExecutor.execute(() -> {
////                                RecordBuffer.setOutputBuffer(chunk);
////                                sendUpdate(MSG_RECORDING_DONE);
////                            });
////                        }
////                    }
////                }
////            }
//
//
//
//                    }
//
//
//
//                } else {
//
//                    Log.d(TAG, "recordAudioFalse: ");
//
//
//                }
//
//
//
//
//
//
//
//
//
//
//
////            if (bytesRead > 0) {
////
////                outputBuffer.write(audioData, 0, bytesRead);
////                totalBytesRead += bytesRead;
////
////                int available = totalBytesRead - lastEmitted;
////
////                // Enough accumulated to cut 5 seconds?
////                if (available >= bytesForFiveSeconds && available>=VAD_FRAME_SIZE) {
////
////                    byte[] fullData = outputBuffer.toByteArray();
////
////                    // --------- SAFETY CHECK: prevent overflow ---------
////                    if (fullData.length < lastEmitted + bytesForFiveSeconds) {
////                        return;  // Wait for more data
////                    }
////
////                    // --------- EXACT 5-second chunk ----------
////                    byte[] chunk = Arrays.copyOfRange(
////                            fullData,
////                            lastEmitted,
////                            lastEmitted + bytesForFiveSeconds
////                    );
////
////                    // Move pointer ALWAYS (critical)
////                    lastEmitted += bytesForFiveSeconds;
//
//////                    whisperExecutor.execute(() -> {
//////                        RecordBuffer.setOutputBuffer(chunk);
//////                        sendUpdate(MSG_RECORDING_DONE);
//////                    });
////
////                    // --------- VAD on tail ----------
////                    int vadBytes = VAD_FRAME_SIZE * 2;
////
////                    if (chunk.length >= vadBytes) {
////
////                        byte[] vadBuffer = Arrays.copyOfRange(
////                                chunk,
////                                chunk.length - vadBytes,
////                                chunk.length
////                        );
////
////                        Log.d(TAG, "VAD Size: "+vadBuffer.length);
////
////                        if (vad.isSpeech(vadBuffer)) {
////
////                            Log.d(TAG, "Is Speech");
////
////                            whisperExecutor.execute(() -> {
////                                RecordBuffer.setOutputBuffer(chunk);
////                                sendUpdate(MSG_RECORDING_DONE);
////                            });
////                        }
////                    }
////                }
////            }
//
//
//
//        }
//
//
//
//        // Only emit if there's at least one VAD frame available at tail to check
////            if (remaining.length >= VAD_FRAME_SIZE*2) {
////                // Check last VAD frame inside remaining
////                int startOfLastFrame = remaining.length - VAD_FRAME_SIZE*2;
////                byte[] lastFrame = Arrays.copyOfRange(remaining, startOfLastFrame, remaining.length);
////
////                if (vad.isSpeech(lastFrame)) {
////                    // send remaining (it may be shorter than 5s)
////                    final byte[] toSend = remaining;
////                    whisperExecutor.execute(() -> {
////                        RecordBuffer.setOutputBuffer(toSend);
////                        sendUpdate(MSG_RECORDING_DONE);
////                    });
////                } else {
////                    Log.d(TAG, "Tail VAD false — not emitting remaining " + remaining.length + " bytes");
////                }
////            } else {
////                Log.d(TAG, "Not enough tail bytes to run VAD: remaining=" + remaining.length);
////            }
//        // }
//
//        if (useVAD) {
//            //useVAD = false;
//            //vad.close();
//            //vad = null;
//            Log.d(TAG, "Closing VAD");
//        }
//        audioRecord.stop();
//        audioRecord.release();
//        audioManager.stopBluetoothSco();
//        audioManager.setBluetoothScoOn(false);
//
//        byte[] fullData = outputBuffer.toByteArray();
//        // if (lastEmitted < fullData.length) {
//        // remaining chunk: [lastEmitted, fullData.length)
//        //byte[] remaining = Arrays.copyOfRange(fullData, lastEmitted, fullData.length);
//
//        whisperExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//
//                RecordBuffer.setOutputBuffer(fullData);
//                sendUpdate(MSG_RECORDING_DONE);
//            }
//        });
//
    ////        synchronized (fileSavedLock) {
    ////            fileSavedLock.notify(); // Notify that recording is finished
    ////        }
//    }

    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(); // Buffer for saving data RecordBuffer
    BlockingQueue<byte[]> audioQueue = new LinkedBlockingQueue<>();

    private void recordAudio() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "AudioRecord permission is not granted");
            //sendUpdate(mContext.getString(R.string.need_record_audio_permission));
            return;
        }

        int channels = 1;
        int bytesPerSample = 2;
        int sampleRateInHz = 16000;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;

        int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (bufferSize < VAD_FRAME_SIZE * 2) bufferSize = VAD_FRAME_SIZE * 2;

        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.startBluetoothSco();
        audioManager.setBluetoothScoOn(true);

        AudioRecord.Builder builder = new AudioRecord.Builder()
                .setAudioSource(audioSource)
                .setAudioFormat(new AudioFormat.Builder()
                        .setChannelMask(channelConfig)
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRateInHz)
                        .build())
                .setBufferSizeInBytes(bufferSize);

        AudioRecord audioRecord = builder.build();
        audioRecord.startRecording();

        // Calculate maximum byte counts for 30 seconds (for saving)
        int bytesForThirtySeconds = sampleRateInHz * bytesPerSample * channels * 30;
        int bytesForTwoSeconds = sampleRateInHz * bytesPerSample * channels * 2;

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(); // Buffer for saving data RecordBuffer
        ByteArrayOutputStream silenceBuffer = new ByteArrayOutputStream();

        byte[] audioData = new byte[bufferSize];
        int totalBytesRead = 0;

        boolean isSpeech;
        boolean isRecording = false;
        byte[] vadAudioBuffer = new byte[VAD_FRAME_SIZE * 2];  //VAD needs 16 bit

        while (mInProgress.get() && totalBytesRead < bytesForThirtySeconds) {
            int bytesRead = audioRecord.read(audioData, 0, VAD_FRAME_SIZE * 2);
            if (bytesRead > 0) {

                outputBuffer.write(audioData, 0, bytesRead);// Save all bytes read up to 30 seconds

                silenceBuffer.write(audioData,0,bytesRead);

                if (silenceBuffer.size()>bytesForTwoSeconds) {

                    double rms = calculateRMS(silenceBuffer.toByteArray());

                    if (rms < 0.04) {
                        Log.d(TAG, "Silence detected, stopping mic");
                        //stopMic();
                        mInProgress.compareAndSet(true,false);
                        silenceBuffer.reset();
                        break;
                    }

                    // Optionally keep sliding window instead of full reset
                    silenceBuffer.reset();

                }
                totalBytesRead += bytesRead;
            } else {
                Log.d(TAG, "AudioRecord error, bytes read: " + bytesRead);
                break;
            }

//            if (useVAD){
//                byte[] outputBufferByteArray = outputBuffer.toByteArray();
//                if (outputBufferByteArray.length >= VAD_FRAME_SIZE * 2) {
//                    // Always use the last VAD_FRAME_SIZE * 2 bytes (16 bit) from outputBuffer for VAD
//                    System.arraycopy(outputBufferByteArray, outputBufferByteArray.length - VAD_FRAME_SIZE * 2, vadAudioBuffer, 0, VAD_FRAME_SIZE * 2);
//
//                    isSpeech = vad.isSpeech(vadAudioBuffer);
//                    if (isSpeech) {
//
//                        Log.d(TAG, "Voice Record ");
//                        if (!isRecording) {
//                            Log.d(TAG, "VAD Speech detected: recording starts");
//                            sendUpdate(MSG_RECORDING);
//                        }
//                        isRecording = true;
//                    } else {
//
//                        Log.d(TAG, "Silence ");
//                        if (isRecording) {
//                            isRecording = false;
//                            mInProgress.set(false);
//                        }
//                    }
//                }
//            } else {
//                if (!isRecording) sendUpdate(MSG_RECORDING);
//                isRecording = true;
//            }
        }
        Log.d(TAG, "Total bytes recorded: " + totalBytesRead);

        if (useVAD){
            useVAD = false;
            vad.close();
            vad = null;
            Log.d(TAG, "Closing VAD");
        }
        audioRecord.stop();
        audioRecord.release();
        audioManager.stopBluetoothSco();
        audioManager.setBluetoothScoOn(false);

        // Save recorded audio data to BufferStore (up to 30 seconds)
        RecordBuffer.setOutputBuffer(outputBuffer.toByteArray());
        if (totalBytesRead > 6400){  //min 0.2s
            //sendUpdate(MSG_RECORDING_DONE);
        } else {
            //sendUpdate(MSG_RECORDING_ERROR);
        }

        // Notify the waiting thread that recording is complete
//        synchronized (fileSavedLock) {
//            fileSavedLock.notify(); // Notify that recording is finished
//        }

    }

    private void recordAudio3() {

//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "AudioRecord permission is not granted");
//            sendUpdate(mContext.getString(R.string.need_record_audio_permission));
//            return;
//        }

        int channels = 1;
        int bytesPerSample = 2;
        int sampleRateInHz = 16000;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;

        int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (bufferSize < VAD_FRAME_SIZE * 2) bufferSize = VAD_FRAME_SIZE * 2;

        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.startBluetoothSco();
        audioManager.setBluetoothScoOn(true);

        AudioRecord.Builder builder = new AudioRecord.Builder()
                .setAudioSource(audioSource)
                .setAudioFormat(new AudioFormat.Builder()
                        .setChannelMask(channelConfig)
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRateInHz)
                        .build())
                .setBufferSizeInBytes(bufferSize);

        AudioRecord audioRecord = builder.build();
        audioRecord.startRecording();

        // Calculate maximum byte counts for 30 seconds (for saving)
        int bytesForThirtySeconds = sampleRateInHz * bytesPerSample * channels * 30;
        int bytesForTwoSeconds = sampleRateInHz * bytesPerSample * channels * 2;
        //int bytesForTwoSeconds = sampleRateInHz * bytesPerSample * channels * 2;

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(); // Buffer for saving data RecordBuffer
        ByteArrayOutputStream silenceBuffer = new ByteArrayOutputStream();
        Queue<Double> silenceQueue = new ArrayDeque<>();

        byte[] audioData = new byte[bufferSize];
        int totalBytesRead = 0;

        short[] pcm = new short[VAD_FRAME_SIZE];

        final int SILENCE_FRAMES_REQUIRED = 33;     // ~2 sec (20 * 100ms)

        int silenceFrameCount = 0;

        boolean isSpeech;
        boolean isRecording = false;
        byte[] vadAudioBuffer = new byte[VAD_FRAME_SIZE * 2];  //VAD needs 16 bit

        while (mInProgress.get() && totalBytesRead < bytesForThirtySeconds) {
            int bytesRead = audioRecord.read(audioData, 0, VAD_FRAME_SIZE * 2);
            if (bytesRead > 0) {

                outputBuffer.write(audioData, 0, bytesRead);// Save all bytes read up to 30 seconds
                totalBytesRead += bytesRead;

//                ByteBuffer.wrap(audioData,0,bytesRead)
//                        .order(ByteOrder.LITTLE_ENDIAN)
//                        .asShortBuffer()
//                        .get(pcm, 0, bytesRead / 2);

                double rms = calculateRMS(Arrays.copyOfRange(audioData,0,bytesRead));


                if (silenceFrameCount>=67) {



                    silenceQueue.poll();
                    silenceQueue.add(rms);

                    silenceFrameCount = silenceQueue.size();

                    Log.d(TAG, "FrameCount:"+silenceFrameCount);

                    double avgRms = silenceQueue.stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);

                    Log.d(TAG, "AvgRMS: "+avgRms);

                    if (avgRms < 0.01f) {   // realistic threshold
                        //silenceFrameCount++;

                        Log.d(TAG, "Silence detected, stopping mic");
                        mInProgress.compareAndSet(true,false);
                        break;

//                        if (silenceFrameCount >= SILENCE_FRAMES_REQUIRED) {
//                            Log.d(TAG, "Silence detected, stopping mic");
//                            mInProgress.compareAndSet(true,false);
//                            break;
//                        }
                    } else {
                        //silenceFrameCount = 0; // reset on speech
                    }


                    //calculate avg RMS


                } else {

                    silenceFrameCount++;

                    silenceQueue.add(rms);
                }

                // Compute RMS on THIS frame only
                //float rms = calculateNormalizedRMS(pcm, bytesRead / 2);

//                if (rms < 0.04f) {   // realistic threshold
//                    silenceFrameCount++;
//
//                    if (silenceFrameCount >= SILENCE_FRAMES_REQUIRED) {
//                        Log.d(TAG, "Silence detected, stopping mic");
//                        mInProgress.compareAndSet(true,false);
//                        break;
//                    }
//                } else {
//                    silenceFrameCount = 0; // reset on speech
//                }

            } else {
                Log.d(TAG, "AudioRecord error, bytes read: " + bytesRead);
                mInProgress.compareAndSet(true,false);
                break;
            }

//            if (useVAD){
//                byte[] outputBufferByteArray = outputBuffer.toByteArray();
//                if (outputBufferByteArray.length >= VAD_FRAME_SIZE * 2) {
//                    // Always use the last VAD_FRAME_SIZE * 2 bytes (16 bit) from outputBuffer for VAD
//                    System.arraycopy(outputBufferByteArray, outputBufferByteArray.length - VAD_FRAME_SIZE * 2, vadAudioBuffer, 0, VAD_FRAME_SIZE * 2);
//
//                    isSpeech = vad.isSpeech(vadAudioBuffer);
//                    if (isSpeech) {
//
//                        Log.d(TAG, "Voice Record ");
//                        if (!isRecording) {
//                            Log.d(TAG, "VAD Speech detected: recording starts");
//                            sendUpdate(MSG_RECORDING);
//                        }
//                        isRecording = true;
//                    } else {
//
//                        Log.d(TAG, "Silence ");
//                        if (isRecording) {
//                            isRecording = false;
//                            mInProgress.set(false);
//                        }
//                    }
//                }
//            } else {
//                if (!isRecording) sendUpdate(MSG_RECORDING);
//                isRecording = true;
//            }
        }
        Log.d(TAG, "Total bytes recorded: " + totalBytesRead);

        if (useVAD){
            useVAD = false;
            vad.close();
            vad = null;
            Log.d(TAG, "Closing VAD");
        }
        audioRecord.stop();
        audioRecord.release();
        audioManager.stopBluetoothSco();
        audioManager.setBluetoothScoOn(false);

        // Save recorded audio data to BufferStore (up to 30 seconds)
        //RecordBuffer.setOutputBuffer(outputBuffer.toByteArray());
        if (totalBytesRead > 6400){  //min 0.2s
            sendUpdate(MSG_RECORDING_DONE,outputBuffer.toByteArray());
        } else {
            sendUpdate(MSG_RECORDING_ERROR,null);
        }

        // Notify the waiting thread that recording is complete
//        synchronized (fileSavedLock) {
//            fileSavedLock.notify(); // Notify that recording is finished
//        }

    }

    private float calculateNormalizedRMS(short[] samples, int len) {
        double sum = 0;
        for (int i = 0; i < len; i++) {
            float s = samples[i] / 32768f;
            float scaledSample = s*0.80f;
            sum += scaledSample * scaledSample;
        }
        return (float) Math.sqrt(sum / len);
    }




//    private void recordAudio() {
//
//        audioQueue.clear();
//
//        outputBuffer.reset();
//        n = 0;
//
//        //outputBuffer.reset();
//
//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "AudioRecord permission is not granted");
//            sendUpdate(mContext.getString(R.string.need_record_audio_permission));
//            return;
//        }
//
//        int channels = 1;
//        int bytesPerSample = 2;
//        int sampleRateInHz = 16000;
//        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
//        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
//        int audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
//
//        int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
//        if (bufferSize < VAD_FRAME_SIZE * 2) bufferSize = VAD_FRAME_SIZE * 2;
//
//        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//        audioManager.startBluetoothSco();
//        audioManager.setBluetoothScoOn(true);
//
//        AudioRecord.Builder builder = new AudioRecord.Builder()
//                .setAudioSource(audioSource)
//                .setAudioFormat(new AudioFormat.Builder()
//                        .setChannelMask(channelConfig)
//                        .setEncoding(audioFormat)
//                        .setSampleRate(sampleRateInHz)
//                        .build())
//                .setBufferSizeInBytes(bufferSize);
//
//        AudioRecord audioRecord = builder.build();
//        audioRecord.startRecording();
//
//        // Calculate maximum byte counts for 30 seconds (for saving)
//        int bytesForThirtySeconds = sampleRateInHz * bytesPerSample * channels * 30;
//        int bytesForFiveSeconds = sampleRateInHz * bytesPerSample * channels * 5;
//        int bytesForThreeSeconds = sampleRateInHz * bytesPerSample * channels * 3;
//
//        int silenceFrames = 0;
//        int silenceThresholdFrames = (sampleRateInHz / VAD_FRAME_SIZE) * 2; // 2 seconds silence
//
//        byte[] audioData = new byte[VAD_FRAME_SIZE*2];
//        int totalBytesRead = 0;
//
//        //boolean isSpeech;
//        boolean isRecording = false;
//        byte[] vadAudioBuffer = new byte[VAD_FRAME_SIZE * 2];  //VAD needs 16 bit
//
//        while (mInProgress.get() && totalBytesRead < bytesForThirtySeconds) {
////            int bytesRead = audioRecord.read(audioData, 0, VAD_FRAME_SIZE * 2);
//            int bytesRead = audioRecord.read(audioData, 0, VAD_FRAME_SIZE*2);
//
//
//
//            boolean isSpeech = vad.isSpeech(audioData);
//
//            if (isSpeech) {
//
//                Log.d(TAG, "Voice ");
//
//                outputBuffer.write(audioData, 0, bytesRead);  // Save all bytes read up to 30 seconds
//                totalBytesRead += bytesRead;
//
//            } else {
//
////                silenceFrames++;
////                Log.d(TAG, "Silence: ");
////                if (silenceFrames >= silenceThresholdFrames) {
////                    Log.d(TAG, "Complete silence detected, stopping recording");
////                    mInProgress.set(false);
////                    break;
////                }
//
//            }
//
//            if (outputBuffer.size()>=bytesForFiveSeconds) {
//
//
//                byte[] chunk = outputBuffer.toByteArray();
//
//                //byte[] silenceChunk = Arrays.copyOfRange(chunk,chunk.length-bytesForThreeSeconds , chunk.length);
//
//                //double rms = calculateRMS(silenceChunk);
//
////                if (rms<threshold) {
////
////                    //stop mic
////
////
////                } else  {
////
////                    //Send to queue for processing
////                }
//                // take only the first 5s worth
//                byte[] slice = Arrays.copyOfRange(chunk, 0, bytesForFiveSeconds);
//
//                // enqueue for processing
//                audioQueue.offer(slice);
//
//                // reset buffer for next chunk
//                outputBuffer.reset();
//
//
//                if (!mInProcess.get()) {
//
//                    mInProcess.compareAndSet(false,true);
//
//                    processAudio();
//
//
//                }
//
//
//            }
////            if (bytesRead > 0) {
////                outputBuffer.write(audioData, 0, bytesRead);  // Save all bytes read up to 30 seconds
////                totalBytesRead += bytesRead;
////            } else {
////                Log.d(TAG, "AudioRecord error, bytes read: " + bytesRead);
////                break;
////            }
////
////            if (useVAD){
////                byte[] outputBufferByteArray = outputBuffer.toByteArray();
////                if (outputBufferByteArray.length >= VAD_FRAME_SIZE * 2) {
////                    // Always use the last VAD_FRAME_SIZE * 2 bytes (16 bit) from outputBuffer for VAD
////                    System.arraycopy(outputBufferByteArray, outputBufferByteArray.length - VAD_FRAME_SIZE * 2, vadAudioBuffer, 0, VAD_FRAME_SIZE * 2);
////
////                    isSpeech = vad.isSpeech(vadAudioBuffer);
////                    if (isSpeech) {
////                        if (!isRecording) {
////                            Log.d(TAG, "VAD Speech detected: recording starts");
////                            //sendUpdate(MSG_RECORDING);
////
//////                            s.execute(()->{
//////
//////
//////
//////                                RecordBuffer.setOutputBuffer(outputBufferByteArray);
//////                                sendUpdate(MSG_RECORDING_DONE);
//////
//////
//////                            });
////
////                            //outputBuffer.reset();
////                        }
////                        isRecording = true;
////                    } else {
////                        if (isRecording) {
////                            isRecording = false;
////                            mInProgress.set(false);
////                        }
////                    }
////                }
////            } else {
////                if (!isRecording) sendUpdate(MSG_RECORDING);
////                isRecording = true;
////            }
//        }
//        Log.d(TAG, "Total bytes recorded: " + totalBytesRead);
//
////        if (useVAD){
////            useVAD = false;
////            vad.close();
////            vad = null;
////            Log.d(TAG, "Closing VAD");
////        }
//        audioRecord.stop();
//        audioRecord.release();
//        audioManager.stopBluetoothSco();
//        audioManager.setBluetoothScoOn(false);
//
//        if (processThread!=null) {
//
//            if (processThread.isAlive()) {
//
//                try {
//
//                    processThread.join();
//
//                } catch (InterruptedException ex) {
//
//
//                }
//
//
//            }
//
//
//        }
//
//        if (outputBuffer.size() > 0) {
//
//            try {
//
//                outputBuffer.flush();
//
//            }catch (Exception e) {
//
//
//            }
//
//
//            byte[] leftover = outputBuffer.toByteArray();
//            audioQueue.offer(leftover);   // enqueue partial chunk
//            outputBuffer.reset();
//
//            whisperExecutor.execute(new Runnable() {
//                @Override
//                public void run() {
//
//                    try {
//
//                        RecordBuffer.setOutputBuffer(audioQueue.poll(500,TimeUnit.MILLISECONDS));
//                        sendUpdate(MSG_RECORDING_DONE);
//
//                    } catch (InterruptedException ex) {
//
//
//                    }
//
//
//                }
//            });
//        }
//
//
//
//
////        byte[] chunk = Arrays.copyOfRange(outputBuffer.toByteArray(),n,outputBuffer.toByteArray().length);
////        //n+= chunk.length;
////
////        whisperExecutor.execute(new Runnable() {
////            @Override
////            public void run() {
////
////                RecordBuffer.setOutputBuffer(chunk);
////                sendUpdate(MSG_RECORDING_DONE);
////            }
////        });
//
//        mInProcess.compareAndSet(true,false);
//
//
//        // Save recorded audio data to BufferStore (up to 30 seconds)
////        RecordBuffer.setOutputBuffer(outputBuffer.toByteArray());
////        if (totalBytesRead > 6400){  //min 0.2s
////            sendUpdate(MSG_RECORDING_DONE);
////        } else {
////            sendUpdate(MSG_RECORDING_ERROR);
////        }
//
//        // Notify the waiting thread that recording is complete
    ////        synchronized (fileSavedLock) {
    ////            fileSavedLock.notify(); // Notify that recording is finished
    ////        }
//
//    }

    private void recordingLoop() {

        //outputBuffer.reset();
        audioQueue.clear();
        n = 0;


        //outputBuffer.reset();

//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "AudioRecord permission is not granted");
//            sendUpdate(mContext.getString(R.string.need_record_audio_permission));
//            return;
//        }

        int channels = 1;
        int bytesPerSample = 2;
        int sampleRateInHz = 16000;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;

        int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (bufferSize < VAD_FRAME_SIZE * 2) bufferSize = VAD_FRAME_SIZE * 2;

        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.startBluetoothSco();
        audioManager.setBluetoothScoOn(true);

        AudioRecord.Builder builder = new AudioRecord.Builder()
                .setAudioSource(audioSource)
                .setAudioFormat(new AudioFormat.Builder()
                        .setChannelMask(channelConfig)
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRateInHz)
                        .build())
                .setBufferSizeInBytes(bufferSize);

        AudioRecord audioRecord = builder.build();
        audioRecord.startRecording();

        // Calculate maximum byte counts for 30 seconds (for saving)
        int bytesForThirtySeconds = sampleRateInHz * bytesPerSample * channels * 30;
        int bytesForFiveSeconds = sampleRateInHz * bytesPerSample * channels * 2;
        int bytesForThreeSeconds = (sampleRateInHz * bytesPerSample * channels * 2);

        ByteArrayOutputStream silenceBuffer = new ByteArrayOutputStream();

        int silenceFrames = 0;
        int silenceThresholdFrames = (sampleRateInHz / VAD_FRAME_SIZE) * 2; // 2 seconds silence

        byte[] audioData = new byte[bufferSize];
        int totalBytesRead = 0;
        int read = 0;

        //boolean isSpeech;
        boolean isRecording = false;
        byte[] vadAudioBuffer = new byte[VAD_FRAME_SIZE * 2];

        final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        int bufferBytesCount = 0; // current size of outputBuffer
        int totalBytesReadSinceStart = 0; // optional cap for max read (used against bytesForThirtySeconds)

        try {
            audioRecord.startRecording();
            // If you use bluetooth SCO, ensure it's enabled out of this method where appropriate
            // audioManager.startBluetoothSco();
            // audioManager.setBluetoothScoOn(true);

            while (mInProgress.get() && (totalBytesReadSinceStart < bytesForThirtySeconds)) {
                read = audioRecord.read(audioData, 0, VAD_FRAME_SIZE*2);
//                if (read <= 0) {
//                    // read==0 or negative means no data or error - continue or break depending on your strategy
//                    continue;
//                }

                totalBytesReadSinceStart += read;
                // append read bytes to outputBuffer
                silenceBuffer.write(audioData, 0, read);
                outputBuffer.write(audioData, 0, read);
                bufferBytesCount += read;

//                if (silenceBuffer.size() >= bytesForThreeSeconds) {
//
//                    // Calculate RMS over the *last* sizeof=bytesForThreeSeconds
//                    byte[] buf = silenceBuffer.toByteArray();
//
//                    // Take only the most recent 3 seconds window
//                    byte[] window = Arrays.copyOfRange(
//                            buf,
//                            buf.length - bytesForThreeSeconds,
//                            buf.length
//                    );
//
//                    double rms = calculateRMS(window);
//
//                    if (rms < 0.04) {
//                        Log.d(TAG, "Silence detected (rms=" + rms + "), stopping mic");
//
//                        // Stop the recording loop
//                        mInProgress.set(false);
//
//                        // Allow buffer to flush cleanly after loop
//                        break;
//                    }
//
//                    // Keep only the last 3 seconds (slide the window)
//                    silenceBuffer.reset();
//                    silenceBuffer.write(window, 0, window.length);
//                }

                // When our buffer exceeds the five-second threshold, take one chunk for processing
                while (bufferBytesCount >= bytesForThreeSeconds) {
                    // Convert buffer to array once (we'll slice it)
                    byte[] full = outputBuffer.toByteArray();

                    // take exactly bytesForFiveSeconds bytes as the chunk to process
                    int take = bytesForThreeSeconds;
                    byte[] chunkToProcess = Arrays.copyOfRange(full, 0, take);

                    // Offer chunk to queue (non-blocking). If queue is bounded in your app, handle offer return accordingly.
                    audioQueue.offer(chunkToProcess);

                    // If processor is idle, kick it off
                    if (!mInProcess.get()) {
                        if (mInProcess.compareAndSet(false, true)) {
                            // Start processing (your implementation)
                            processAudio2();
                        }
                    }

                    // Calculate remainder and reset buffer to only contain remainder
                    int remainder = full.length - take;
                    outputBuffer.reset();
                    bufferBytesCount = 0;
                    //totalBytesReadSinceStart = 0; // we consumed up to five seconds, reset the total count if you use cap per loop

                    if (remainder > 0) {
                        // write the remainder bytes back to buffer
                        outputBuffer.write(full, take, remainder);
                        bufferBytesCount = remainder;
                        // totalBytesReadSinceStart should reflect the remainder count for the next loop
                        //totalBytesReadSinceStart = remainder;
                    }
                }

                // Optional: VAD sliding-window logic can go here (using a separate silenceBuffer)
            } // end while recording

        } catch (Exception e) {
            Log.e(TAG, "Exception in recording loop", e);
        } finally {
            // Stop & release recorders
            try {
                audioRecord.stop();
            } catch (Exception ex) {
                Log.w(TAG, "audioRecord.stop() threw", ex);
            }

            try {
                audioRecord.release();
            } catch (Exception ex) {
                Log.w(TAG, "audioRecord.release() threw", ex);
            }

            // If you used Bluetooth SCO, stop it:
            try {
                audioManager.stopBluetoothSco();
                audioManager.setBluetoothScoOn(false);
            } catch (Exception ex) {
                // ignore if audioManager wasn't using bluetooth
            }

            // Wait for processing thread to finish if present
            if (processThread != null) {
                try {
                    if (processThread.isAlive()) {
                        processThread.join();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt(); // restore
                }
            }

            // If there's leftover data in outputBuffer, hand it off (non-empty)
            if (outputBuffer.size() > 0) {
                final byte[] leftover = outputBuffer.toByteArray();
                outputBuffer.reset();

                // Use actual leftover length
                final byte[] slice = Arrays.copyOfRange(leftover, 0, bufferBytesCount);

                // Use your existing executor to hand off the final block to the consumer
                whisperExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Example: hand over the final buffer & notify finished
                            RecordBuffer.setOutputBuffer(slice);
                            //sendUpdate(MSG_RECORDING_DONE);
                        } catch (Exception ex) {
                            Log.e(TAG, "Error handing final buffer to RecordBuffer", ex);
                        }
                    }
                });
            }

            // Mark processing flag false (we're done recording)
            mInProcess.set(false);
            mInProgress.set(false);
        }
    }









    private void recordAudio2() {

        outputBuffer.reset();
        audioQueue.clear();
        n = 0;


        //outputBuffer.reset();

//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "AudioRecord permission is not granted");
//            sendUpdate(mContext.getString(R.string.need_record_audio_permission));
//            return;
//        }

        int channels = 1;
        int bytesPerSample = 2;
        int sampleRateInHz = 16000;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;

        int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        //if (bufferSize < VAD_FRAME_SIZE * 2) bufferSize = VAD_FRAME_SIZE * 2;

        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.startBluetoothSco();
        audioManager.setBluetoothScoOn(true);

        AudioRecord.Builder builder = new AudioRecord.Builder()
                .setAudioSource(audioSource)
                .setAudioFormat(new AudioFormat.Builder()
                        .setChannelMask(channelConfig)
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRateInHz)
                        .build())
                .setBufferSizeInBytes(bufferSize);

        AudioRecord audioRecord = builder.build();
        audioRecord.startRecording();

        // Calculate maximum byte counts for 30 seconds (for saving)
        int bytesForThirtySeconds = sampleRateInHz * bytesPerSample * channels * 30;
        int bytesForFiveSeconds = sampleRateInHz * bytesPerSample * channels * 3;
        int bytesForThreeSeconds =(int) (sampleRateInHz * bytesPerSample * channels * 0.5);

        ByteArrayOutputStream silenceBuffer = new ByteArrayOutputStream();

        int silenceFrames = 0;
        int silenceThresholdFrames = (sampleRateInHz / VAD_FRAME_SIZE) * 2; // 2 seconds silence

        byte[] audioData = new byte[bufferSize];
        //Arrays.fill(audioData,(byte) 0);
        int totalBytesRead = 0;
        int read = 0;

        //boolean isSpeech;
        boolean isRecording = false;
        byte[] vadAudioBuffer = new byte[VAD_FRAME_SIZE * 2];  //VAD needs 16 bit

        while (mInProgress.get() && totalBytesRead<bytesForThirtySeconds) {


            read = audioRecord.read(audioData, 0, audioData.length);
            if (read > 0) {

                totalBytesRead+=read;

                outputBuffer.write(audioData,0,read);

//            silenceBuffer.write(audioData,0,read);
//
//            if (silenceBuffer.size()>bytesForThreeSeconds) {
//
//                double rms = calculateRMS(silenceBuffer.toByteArray());
//
//                if (rms < 0.04) {
//                    Log.d(TAG, "Silence detected, stopping mic");
//                    //stopMic();
//                    mInProgress.compareAndSet(true,false);
//                    silenceBuffer.reset();
//                    break;
//                }
//
//                // Optionally keep sliding window instead of full reset
//                silenceBuffer.reset();
//
//            }

                if (outputBuffer.size()>bytesForFiveSeconds) {

                    //byte[] totalBytes = new byte[outputBuffer.size()];
                    //Arrays.fill(totalBytes,(byte) 0);

                    byte[] chunk = outputBuffer.toByteArray();

                    //System.arraycopy(chunk,0,totalBytes,0,totalBytesRead);

                    byte[] slice = Arrays.copyOfRange(chunk, 0,totalBytesRead);
//                float[] frame = new float[read];
//                for (int i = 0; i < read; i++) {
//                    frame[i] = buffer[i] / 32768.0f; // normalize to [-1,1]
//                }

                    audioQueue.offer(slice);// push into queue


                    if (!mInProcess.get()) {

                        mInProcess.compareAndSet(false, true);

                        processAudio2();


                    }

                    outputBuffer.reset();
                    totalBytesRead = 0;


                }





            }


//

        }

        audioRecord.stop();
        audioRecord.release();
        audioManager.stopBluetoothSco();
        audioManager.setBluetoothScoOn(false);

        if (processThread != null) {

            if (processThread.isAlive()) {

                try {

                    processThread.join();

                } catch (InterruptedException ex) {


                }


            }


        }
//
//    if (outputBuffer.size() > 0) {
//
//        try {
//
//            outputBuffer.flush();
//
//            byte[] leftover = outputBuffer.toByteArray();
//            outputBuffer.reset();
//
//            byte[] slice = Arrays.copyOfRange(leftover,0,totalBytesRead);
//            //audioQueue.offer(slice);
//            //audioQueue.offer(leftover);   // enqueue partial chunk
//
//            whisperExecutor.execute(new Runnable() {
//                @Override
//                public void run() {
//
//                    try {
//
//                        RecordBuffer.setOutputBuffer(slice);
//                        sendUpdate(MSG_RECORDING_DONE);
//
//                    } catch (Exception ex) {
//
//
//                    }
//
//
//                }
//            });
//
//        } catch (Exception e) {
//
//
//        }
//
//    }

        mInProcess.compareAndSet(true,false);
    }

    private double calculateRMS(byte[] audioData) {
        double sum = 0;
        int numSamples = audioData.length / 2; // 2 bytes per sample

        float[] samples = new float[numSamples];

        ByteBuffer bb = ByteBuffer.wrap(audioData).order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < numSamples; i++) {

            samples[i] = (float) (bb.getShort() / 32768.0);
            samples[i] *= 0.80f;
//

            //float sample = (float) (bb.getShort()/32768.0);

            sum += samples[i] * samples[i];
        }



//        for (int i = 0; i < audioData.length; i += 2) {
//            // Little-endian: low byte first, high byte second
//            int low = audioData[i] & 0xFF;
//            int high = audioData[i + 1]; // signed
//            short sample = (short) ((high << 8) | low);
//
//            sum += sample * sample;
//        }
//
//
//
//        for (int i = 0; i < audioData.length; i += 2) {
//            // Little-endian: low byte first, high byte second
//            int low = audioData[i] & 0xFF;
//            int high = audioData[i + 1]; // signed
//            short sample = (short) ((high << 8) | low);
//
//            sum += sample * sample;
//        }

        double mean =  sum / numSamples;
        return Math.sqrt(mean);
    }


}
