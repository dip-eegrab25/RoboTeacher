//package com.ai.roboteacher.activities;
//
//import android.media.AudioFormat;
//import android.media.AudioRecord;
//import android.media.MediaRecorder;
//import android.os.Bundle;
//import android.os.Environment;
//import android.util.Log;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import org.mozilla.deepspeech.libdeepspeech.DeepSpeechModel;
//import org.mozilla.deepspeech.libdeepspeech.DeepSpeechStreamingState;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//
//public class DeepSpeechActivity extends AppCompatActivity {
//
//    private DeepSpeechModel dsModel;
//    private AudioRecord audioRecord;
//    private boolean isRecording = false;
//    private int bufferSize;
//    private Thread recordingThread;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Load model
//        try {
//            File modelFile = copyAssetToFile("deepspeech-0.9.3-models.pbmm");
//            File scorerFile = copyAssetToFile("deepspeech-0.9.3-models.scorer");
//            dsModel = new DeepSpeechModel(modelFile.getAbsolutePath());
//            dsModel.enableExternalScorer(scorerFile.getAbsolutePath());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        bufferSize = AudioRecord.getMinBufferSize(
//                16000,
//                AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT);
//
//        audioRecord = new AudioRecord(
//                MediaRecorder.AudioSource.MIC,
//                16000,
//                AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT,
//                bufferSize);
//
//        // Start Recording and Transcribing
//        startRecording();
//    }
//
//    private void startRecording() {
//        isRecording = true;
//        audioRecord.startRecording();
//
//        recordingThread = new Thread(() -> {
//            DeepSpeechStreamingState stream = dsModel.createStream();
//
//            byte[] audioBuffer = new byte[bufferSize];
//
//            while (isRecording) {
//                int read = audioRecord.read(audioBuffer, 0, audioBuffer.length);
//                if (read > 0) {
//                    short[] shorts = new short[read / 2];
//                    ByteBuffer.wrap(audioBuffer).order(ByteOrder.LITTLE_ENDIAN)
//                            .asShortBuffer().get(shorts);
//                    dsModel.feedAudioContent(stream, shorts, shorts.length);
//
//                    String partial = dsModel.intermediateDecode(stream);
//                    Log.d("PARTIAL", partial);
//                }
//            }
//
//            // Final result
//            String finalResult = dsModel.finishStream(stream);
//            Log.d("FINAL", finalResult);
//
//            audioRecord.stop();
//            audioRecord.release();
//        });
//
//        recordingThread.start();
//    }
//
//    private File copyAssetToFile(String assetName) throws IOException {
//        File outFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), assetName);
//        if (!outFile.exists()) {
//            try (InputStream in = getAssets().open(assetName);
//                 FileOutputStream out = new FileOutputStream(outFile)) {
//                byte[] buffer = new byte[1024];
//                int read;
//                while ((read = in.read(buffer)) != -1) {
//                    out.write(buffer, 0, read);
//                }
//            }
//        }
//        return outFile;
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        isRecording = false;
//        if (recordingThread != null) {
//            recordingThread.interrupt();
//        }
//        if (dsModel != null) {
//            dsModel.freeModel();
//        }
//    }
//}
