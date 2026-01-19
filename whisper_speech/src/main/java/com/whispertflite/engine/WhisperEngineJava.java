package com.whispertflite.engine;

import android.content.Context;
import android.util.Log;

import com.whispertflite.asr.RecordBuffer;
import com.whispertflite.asr.Whisper;
import com.whispertflite.asr.WhisperResult;
import com.whispertflite.utils.InputLang;
import com.whispertflite.utils.WhisperUtil;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhisperEngineJava implements WhisperEngine {
    private final String TAG = "WhisperEngineJava";
    private final WhisperUtil mWhisperUtil = new WhisperUtil();

    private final Context mContext;
    private boolean mIsInitialized = false;
    private Interpreter mInterpreter = null;

    public WhisperEngineJava(Context context) {
        mContext = context;
    }

    @Override
    public boolean isInitialized() {
        return mIsInitialized;
    }

    @Override
    public void initialize(String modelPath, String vocabPath, boolean multilingual) throws IOException {
        // Load model
        loadModel(modelPath);
        Log.d(TAG, "Model is loaded..." + modelPath);

        // Load filters and vocab
        boolean ret = mWhisperUtil.loadFiltersAndVocab(multilingual, vocabPath);
        if (ret) {
            mIsInitialized = true;
            Log.d(TAG, "Filters and Vocab are loaded..." + vocabPath);
        } else {
            mIsInitialized = false;
            Log.d(TAG, "Failed to load Filters and Vocab...");
        }

    }

    // Unload the model by closing the interpreter
    @Override
    public void deinitialize() {
        if (mInterpreter != null) {
            mInterpreter.setCancelled(true);
            mInterpreter.close();
            mInterpreter = null; // Optional: Set to null to avoid accidental reuse
        }
    }

    @Override
    public WhisperResult processRecordBuffer(Whisper.Action mAction, int mLangToken, byte[] samples) {
        // Calculate Mel spectrogram
        Log.d(TAG, "Calculating Mel spectrogram...");
        float[] melSpectrogram = getMelSpectrogram(samples);
        Log.d(TAG, "Mel spectrogram is calculated...!");

        // Perform inference
        //WhisperResult whisperResult = runInference(melSpectrogram, mAction, mLangToken);
        WhisperResult whisperResult = runInference(melSpectrogram);
        Log.d(TAG, "Inference is executed...!");

        return whisperResult;
    }


    // Load TFLite model
    private void loadModel(String modelPath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(modelPath);
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = 0;
        long declaredLength = fileChannel.size();
        ByteBuffer tfliteModel = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

        // Set the number of threads for inference
        Interpreter.Options options = new Interpreter.Options();
        options.setUseXNNPACK(false);  //cannot be used due to dynamic tensors
        options.setNumThreads(Runtime.getRuntime().availableProcessors());
        options.setCancellable(true);

        mInterpreter = new Interpreter(tfliteModel, options);
    }

    private float[] getMelSpectrogram(byte[] sampleBytes) {
        // Get samples in PCM_FLOAT format
        float[] samples = getSamples(sampleBytes);

        int fixedInputSize = WhisperUtil.WHISPER_SAMPLE_RATE * WhisperUtil.WHISPER_CHUNK_SIZE;
        float[] inputSamples = new float[fixedInputSize];
        int copyLength = Math.min(samples.length, fixedInputSize);
        System.arraycopy(samples, 0, inputSamples, 0, copyLength);

        int cores = Runtime.getRuntime().availableProcessors();
        return mWhisperUtil.getMelSpectrogram(inputSamples, inputSamples.length, copyLength, cores);
    }

    public float[] getSamples(byte[] sampleBytes) {

        float attenuation = 0.5f;
        double sumSquares = 0.0;

        int numSamples = sampleBytes.length / 2;
        ByteBuffer byteBuffer = ByteBuffer.wrap(sampleBytes);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Convert audio data to PCM_FLOAT format
        float[] samples = new float[numSamples];
        float maxAbsValue = 0.0f;

        for (int i = 0; i < numSamples; i++) {
            samples[i] = (float) (byteBuffer.getShort() / 32768.0);
//            samples[i] *= attenuation;
//
//            sumSquares += samples[i] * samples[i];
            // Track the maximum absolute value
            if (Math.abs(samples[i]) > maxAbsValue) {
                maxAbsValue = Math.abs(samples[i]);
            }
        }

//        // RMS calculation
//        double rms = Math.sqrt(sumSquares / numSamples);


        // Normalize the samples
        if (maxAbsValue > 0.0f) {
            for (int i = 0; i < numSamples; i++) {
                samples[i] /= maxAbsValue;
            }
        }

        return samples;

    }

//    private WhisperResult runInference(float[] inputData, Whisper.Action mAction, int mLangToken) {
//
//        //Log.d(TAG, "Language: "+mLangToken);
//        Log.d("Whisper","Signatures "+ Arrays.toString(mInterpreter.getSignatureKeys()));
//
//        // Create input tensor
//        Tensor inputTensor = mInterpreter.getInputTensor(0);
//
//
//        // Create output tensor
//        Tensor outputTensor = mInterpreter.getOutputTensor(0);
//        //TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputTensor.shape(), DataType.INT32);
//
//        // Load input data
//        int inputSize = inputTensor.shape()[0] * inputTensor.shape()[1] * inputTensor.shape()[2] * Float.BYTES;
//        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(inputSize);
//        inputBuffer.order(ByteOrder.nativeOrder());
//        for (float input : inputData) {
//            inputBuffer.putFloat(input);
//        }
//        inputBuffer.rewind();
//
//        String signature_key = "serving_default";
////        if (mAction == Whisper.Action.TRANSLATE) {
////            if (Arrays.asList(mInterpreter.getSignatureKeys()).contains("serving_translate")) signature_key = "serving_translate";
////        } else if (mAction == Whisper.ACTION_TRANSCRIBE) {
////            if (Arrays.asList(mInterpreter.getSignatureKeys()).contains("serving_transcribe_lang") && mLangToken != -1) signature_key = "serving_transcribe_lang";
////            else if (Arrays.asList(mInterpreter.getSignatureKeys()).contains("serving_transcribe")) signature_key = "serving_transcribe";
////        }
//
//
//
//        Map<String, Object> inputsMap = new HashMap<>();
//        String[] inputs = mInterpreter.getSignatureInputs(signature_key);
//        for (String c:inputs) {
//
//            Log.d(TAG, "InputSig: "+c);
//        }
//        Log.d(TAG, "Signatures: "+inputs.toString());
//        inputsMap.put(inputs[0], inputBuffer);
//
////        IntBuffer dec = IntBuffer.wrap(new int[]{
////                50258,  // <|startoftranscript|>
////                50303,  // <|bn|> correct Bengali token
////                50360   // <|transcribe|>
////        });
////        inputsMap.put("decoder_input", dec);
////        if (signature_key.equals("serving_transcribe_lang")) {
////            Log.d(TAG,"Serving_transcribe_lang " + mLangToken);
////            IntBuffer langTokenBuffer = IntBuffer.allocate(1);
////            langTokenBuffer.put(mLangToken);
////            langTokenBuffer.rewind();
////            inputsMap.put(inputs[1], langTokenBuffer);
////        }
//
////        IntBuffer langTokenBuffer = IntBuffer.allocate(1);
////        langTokenBuffer.put(mLangToken);
////        langTokenBuffer.rewind();
////
////        inputsMap.put(inputs[1], langTokenBuffer);
//
////        inputsMap.put("decoder_input", IntBuffer.wrap(new int[]{
////                50258,    // <|startoftranscript|>
////                mLangToken,    // bn
////                50360     // transcribe
////        }));
//
//        Map<String, Object> outputsMap = new HashMap<>();
//        String[] outputs = mInterpreter.getSignatureOutputs(signature_key);
//
//        for (String c:outputs) {
//
//            Log.d(TAG, "InputSig: "+c);
//        }
//
//
//
//        //Tensor outputTensor = mInterpreter.getOutputTensor(0);
//        int[] outShape = outputTensor.shape();
//        Log.d(TAG, "OutputShape: "+Arrays.toString(outShape));
//
//// total number of int tokens
//        int outputSize = 1;
//        for (int s : outShape) outputSize *= s;
//
//        ByteBuffer outputBuffer = ByteBuffer
//                .allocateDirect(outputSize*4).order(ByteOrder.nativeOrder());
//        //outputBuffer.rewind();
//
//// TFLite OUTPUT MUST BE ByteBuffer
//        outputsMap.put("sequences", outputBuffer);
//
//
//// Allocate correct buffer
////        IntBuffer outputTokens = IntBuffer.allocate(outputSize);
////        outputsMap.put("sequences", outputTokens);
//
////        ByteBuffer outputBuffer = ByteBuffer.allocateDirect(outputSize * 4)
////                .order(ByteOrder.nativeOrder());
////
////        outputBuffer.rewind();
////
////        IntBuffer outputTokens = outputBuffer.asIntBuffer();
//
//        //outputsMap.put("sequences", outputTokens);
//
//        // Run inference
//        try {
//
//            mInterpreter.runSignature(inputsMap, outputsMap,signature_key);
//        } catch (Exception e) {
//
//            Log.d(TAG, "runInference: "+e.getMessage());
//            return new WhisperResult("", "", mAction);
//        }
//
//        outputBuffer.rewind();
//
//        //Log.d(TAG, "runInference: "+outputBuffer.array().length);
//
//        // Retrieve the results
//        ArrayList<InputLang> inputLangList = InputLang.getLangList();
//        String language = "";
//        Whisper.Action task = null;
//
//
//
//        IntBuffer intBuffer = outputBuffer.asIntBuffer();
//        intBuffer.rewind();
//
//        int[] tokens = new int[intBuffer.remaining()];
//        intBuffer.get(tokens);
//
//        int outputLen = tokens.length;
//
//        Log.d(TAG, "output_len: " + outputLen);
//        List<byte[]> resultArray = new ArrayList<>();
//        for (int i = 0; i < outputLen; i++) {

    /// /            int token = outputBuffer.getBuffer().getInt();
//
//            int token = tokens[i];
//            if (token == mWhisperUtil.getTokenEOT())
//                break;
//
//            // Get word for token and Skip additional token
//            if (token < mWhisperUtil.getTokenEOT()) {
//                byte[] wordBytes = mWhisperUtil.getWordFromToken(token);
//                resultArray.add(wordBytes);
//            } else {
//                if (token == mWhisperUtil.getTokenTranscribe()){
//                    Log.d(TAG, "It is Transcription...");
//                    task = Whisper.Action.TRANSCRIBE;
//                }
//
//                if (token == mWhisperUtil.getTokenTranslate()){
//                    Log.d(TAG, "It is Translation...");
//                    task = Whisper.Action.TRANSLATE;
//                }
//
//                if (token >= 50259 && token <= 50357){
//                    language = InputLang.getLanguageCodeById(inputLangList, token);
//                    Log.d(TAG, "Detected language code: "+ language);
//                }
//                byte[] wordBytes = mWhisperUtil.getWordFromToken(token);
//                Log.d(TAG, "Skipping token: " + token + ", word: " + new String(wordBytes, StandardCharsets.UTF_8));
//            }
//        }
//
//        // Calculate the total length of the combined byte array
//        int totalLength = 0;
//        for (byte[] byteArray : resultArray) {
//            totalLength += byteArray.length;
//        }
//
//        // Combine the byte arrays into a single byte array
//        byte[] combinedBytes = new byte[totalLength];
//        int offset = 0;
//        for (byte[] byteArray : resultArray) {
//            System.arraycopy(byteArray, 0, combinedBytes, offset, byteArray.length);
//            offset += byteArray.length;
//        }
//
//        return new WhisperResult(new String(combinedBytes, StandardCharsets.UTF_8), language, task);
//    }
    private WhisperResult runInference(float[] inputData, Whisper.Action mAction, int mLangToken) {
        Log.d("Whisper", "Signatures " + Arrays.toString(mInterpreter.getSignatureKeys()));

        // Create input tensor
        Tensor inputTensor = mInterpreter.getInputTensor(0);

        // Create output tensor
        Tensor outputTensor = mInterpreter.getOutputTensor(0);
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputTensor.shape(), DataType.FLOAT32);

        // Load input data
        int inputSize = inputTensor.shape()[0] * inputTensor.shape()[1] * inputTensor.shape()[2] * Float.BYTES;
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(inputSize);
        inputBuffer.order(ByteOrder.nativeOrder());
        for (float input : inputData) {
            inputBuffer.putFloat(input);
        }

        String signature_key = "serving_en";
        if (mAction == Whisper.Action.TRANSLATE) {
            if (Arrays.asList(mInterpreter.getSignatureKeys()).contains("serving_translate"))
                signature_key = "serving_translate";
        } else if (mAction == Whisper.ACTION_TRANSCRIBE) {
            if (Arrays.asList(mInterpreter.getSignatureKeys()).contains("serving_transcribe_lang") && mLangToken != -1)
                signature_key = "serving_transcribe_lang";
            else if (Arrays.asList(mInterpreter.getSignatureKeys()).contains("serving_transcribe"))
                signature_key = "serving_transcribe";
        }

        Map<String, Object> inputsMap = new HashMap<>();
        String[] inputs = mInterpreter.getSignatureInputs(signature_key);
        inputsMap.put(inputs[0], inputBuffer);
        if (signature_key.equals("serving_transcribe_lang")) {
            Log.d(TAG, "Serving_transcribe_lang " + mLangToken);
            IntBuffer langTokenBuffer = IntBuffer.allocate(1);
            langTokenBuffer.put(mLangToken);
            langTokenBuffer.rewind();
            inputsMap.put(inputs[1], langTokenBuffer);
        }

        Map<String, Object> outputsMap = new HashMap<>();
        String[] outputs = mInterpreter.getSignatureOutputs(signature_key);
        outputsMap.put(outputs[0], outputBuffer.getBuffer());

        // Run inference
        try {
            mInterpreter.runSignature(inputsMap, outputsMap, signature_key);
        } catch (Exception e) {
            return new WhisperResult("", "", mAction);
        }

        // Retrieve the results
        ArrayList<InputLang> inputLangList = InputLang.getLangList();
        String language = "";
        Whisper.Action task = null;
        int outputLen = outputBuffer.getIntArray().length;
        Log.d(TAG, "output_len: " + outputLen);
        List<byte[]> resultArray = new ArrayList<>();
        for (int i = 0; i < outputLen; i++) {
            int token = outputBuffer.getBuffer().getInt();
            if (token == mWhisperUtil.getTokenEOT())
                break;

            // Get word for token and Skip additional token
            if (token < mWhisperUtil.getTokenEOT()) {
                byte[] wordBytes = mWhisperUtil.getWordFromToken(token);
                resultArray.add(wordBytes);
            } else {
                if (token == mWhisperUtil.getTokenTranscribe()) {
                    Log.d(TAG, "It is Transcription...");
                    task = Whisper.Action.TRANSCRIBE;
                }

                if (token == mWhisperUtil.getTokenTranslate()) {
                    Log.d(TAG, "It is Translation...");
                    task = Whisper.Action.TRANSLATE;
                }

                if (token >= 50259 && token <= 50357) {
                    language = InputLang.getLanguageCodeById(inputLangList, token);
                    Log.d(TAG, "Detected language code: " + language);
                }
                byte[] wordBytes = mWhisperUtil.getWordFromToken(token);
                Log.d(TAG, "Skipping token: " + token + ", word: " + new String(wordBytes, StandardCharsets.UTF_8));
            }
        }

        // Calculate the total length of the combined byte array
        int totalLength = 0;
        for (byte[] byteArray : resultArray) {
            totalLength += byteArray.length;
        }

        // Combine the byte arrays into a single byte array
        byte[] combinedBytes = new byte[totalLength];
        int offset = 0;
        for (byte[] byteArray : resultArray) {
            System.arraycopy(byteArray, 0, combinedBytes, offset, byteArray.length);
            offset += byteArray.length;
        }

        return new WhisperResult(new String(combinedBytes, StandardCharsets.UTF_8), language, task);
    }

//    private WhisperResult runInference(float[] inputData) {
//        // Create input tensor
//        Tensor inputTensor = mInterpreter.getInputTensor(0);
//        TensorBuffer inputBuffer = TensorBuffer.createFixedSize(inputTensor.shape(), inputTensor.dataType());
////        printTensorDump("Input Tensor Dump ===>", inputTensor);
//
//        // Create output tensor
//        Tensor outputTensor = mInterpreter.getOutputTensor(0);
//        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputTensor.shape(), DataType.FLOAT32);
////        printTensorDump("Output Tensor Dump ===>", outputTensor);
//
//        // Load input data
//        int inputSize = inputTensor.shape()[0] * inputTensor.shape()[1] * inputTensor.shape()[2] * Float.BYTES;
//        ByteBuffer inputBuf = ByteBuffer.allocateDirect(inputSize);
//        inputBuf.order(ByteOrder.nativeOrder());
//        for (float input : inputData) {
//            inputBuf.putFloat(input);
//        }
//
//        String signature_key = "serving_en";
//
//        // To test mel data as a input directly
////        try {
////            byte[] bytes = Files.readAllBytes(Paths.get("/data/user/0/com.example.tfliteaudio/files/mel_spectrogram.bin"));
////            inputBuf = ByteBuffer.wrap(bytes);
////        } catch (IOException e) {
////            throw new RuntimeException(e);
////        }
//
//        inputBuffer.loadBuffer(inputBuf);
//
////        Log.d(TAG, "Before inference...");
//        // Run inference
//
//        try {
//
//            mInterpreter.runSignature(inputBuffer.getBuffer(), outputBuffer.getBuffer(),signature_key);
//
//        } catch (Exception ex) {
//
//            return new WhisperResult("","en", Whisper.Action.TRANSCRIBE);
//        }
//
////        Log.d(TAG, "After inference...");
//
//        // Retrieve the results
//        int outputLen = outputBuffer.getIntArray().length;
//        Log.d(TAG, "output_len: " + outputLen);
//        StringBuilder result = new StringBuilder();
//        for (int i = 0; i < outputLen; i++) {
//            int token = outputBuffer.getBuffer().getInt();
//            if (token == mWhisperUtil.getTokenEOT())
//                break;
//
//            // Get word for token and Skip additional token
//            if (token < mWhisperUtil.getTokenEOT()) {
//                //String word = mWhisperUtil.getWordFromToken(token);
//                //Log.d(TAG, "Adding token: " + token + ", word: " + word);
//                byte[] wordBytes = mWhisperUtil.getWordFromToken(token);
//                result.append(new String(wordBytes,StandardCharsets.UTF_8));
//            } else {
//                if (token == mWhisperUtil.getTokenTranscribe())
//                    Log.d(TAG, "It is Transcription...");
//
//                if (token == mWhisperUtil.getTokenTranslate())
//                    Log.d(TAG, "It is Translation...");
//
//                //String word = mWhisperUtil.getWordFromToken(token);
//                byte[] wordBytes = mWhisperUtil.getWordFromToken(token);
//                //Log.d(TAG, "Skipping token: " + token + ", word: " + word);
//            }
//        }
//
//        //return result.toString();
//        return new WhisperResult(result.toString(),"en", Whisper.Action.TRANSCRIBE);
//    }

private WhisperResult runInference(float[] inputData) {
    Log.d("Whisper", "Signatures " + Arrays.toString(mInterpreter.getSignatureKeys()));

    // Create input tensor
    Tensor inputTensor = mInterpreter.getInputTensor(0);

    // Create output tensor
    Tensor outputTensor = mInterpreter.getOutputTensor(0);
    TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputTensor.shape(), DataType.FLOAT32);

    // Load input data
    int inputSize = inputTensor.shape()[0] * inputTensor.shape()[1] * inputTensor.shape()[2] * Float.BYTES;
    ByteBuffer inputBuffer = ByteBuffer.allocateDirect(inputSize);
    inputBuffer.order(ByteOrder.nativeOrder());
    for (float input : inputData) {
        inputBuffer.putFloat(input);
    }

    String signature_key = "serving_en";
//    if (mAction == Whisper.Action.TRANSLATE) {
//        if (Arrays.asList(mInterpreter.getSignatureKeys()).contains("serving_translate"))
//            signature_key = "serving_translate";
//    } else if (mAction == Whisper.ACTION_TRANSCRIBE) {
//        if (Arrays.asList(mInterpreter.getSignatureKeys()).contains("serving_transcribe_lang") && mLangToken != -1)
//            signature_key = "serving_transcribe_lang";
//        else if (Arrays.asList(mInterpreter.getSignatureKeys()).contains("serving_transcribe"))
//            signature_key = "serving_transcribe";
//    }

    Map<String, Object> inputsMap = new HashMap<>();
    String[] inputs = mInterpreter.getSignatureInputs(signature_key);
    inputsMap.put(inputs[0], inputBuffer);
//    if (signature_key.equals("serving_transcribe_lang")) {
//        Log.d(TAG, "Serving_transcribe_lang " + mLangToken);
//        IntBuffer langTokenBuffer = IntBuffer.allocate(1);
//        langTokenBuffer.put(mLangToken);
//        langTokenBuffer.rewind();
//        inputsMap.put(inputs[1], langTokenBuffer);
//    }

    Map<String, Object> outputsMap = new HashMap<>();
    String[] outputs = mInterpreter.getSignatureOutputs(signature_key);
    outputsMap.put(outputs[0], outputBuffer.getBuffer());

    // Run inference
    try {
        mInterpreter.runSignature(inputsMap, outputsMap, signature_key);
    } catch (Exception e) {
        return new WhisperResult("", "", Whisper.Action.TRANSCRIBE);
    }

    // Retrieve the results
    ArrayList<InputLang> inputLangList = InputLang.getLangList();
    String language = "";
    Whisper.Action task = null;
    int outputLen = outputBuffer.getIntArray().length;
    Log.d(TAG, "output_len: " + outputLen);
    List<byte[]> resultArray = new ArrayList<>();
    for (int i = 0; i < outputLen; i++) {
        int token = outputBuffer.getBuffer().getInt();
        if (token == mWhisperUtil.getTokenEOT())
            break;

        // Get word for token and Skip additional token
        if (token < mWhisperUtil.getTokenEOT()) {
            byte[] wordBytes = mWhisperUtil.getWordFromToken(token);
            resultArray.add(wordBytes);
        } else {
            if (token == mWhisperUtil.getTokenTranscribe()) {
                Log.d(TAG, "It is Transcription...");
                task = Whisper.Action.TRANSCRIBE;
            }

            if (token == mWhisperUtil.getTokenTranslate()) {
                Log.d(TAG, "It is Translation...");
                task = Whisper.Action.TRANSLATE;
            }

            if (token >= 50259 && token <= 50357) {
                language = InputLang.getLanguageCodeById(inputLangList, token);
                Log.d(TAG, "Detected language code: " + language);
            }
            byte[] wordBytes = mWhisperUtil.getWordFromToken(token);
            Log.d(TAG, "Skipping token: " + token + ", word: " + new String(wordBytes, StandardCharsets.UTF_8));
        }
    }

    // Calculate the total length of the combined byte array
    int totalLength = 0;
    for (byte[] byteArray : resultArray) {
        totalLength += byteArray.length;
    }

    // Combine the byte arrays into a single byte array
    byte[] combinedBytes = new byte[totalLength];
    int offset = 0;
    for (byte[] byteArray : resultArray) {
        System.arraycopy(byteArray, 0, combinedBytes, offset, byteArray.length);
        offset += byteArray.length;
    }

    return new WhisperResult(new String(combinedBytes, StandardCharsets.UTF_8), language, task);
}


}
