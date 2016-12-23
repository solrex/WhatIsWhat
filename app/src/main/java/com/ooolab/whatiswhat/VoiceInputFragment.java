package com.ooolab.whatiswhat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.speech.VoiceRecognitionService;

import java.util.ArrayList;

/*enum VSRErrors {
    NETWORK_TIMEOUT(1, "error_on_network_timeout"),
    NETWORK_UNKNOWN(2, "error_on_network_unknown"),
    AUDIO_RECORDING(3, "error_on_audio_recording"),
    SERVER_RESPONSE(4, "error_on_server_response"),
    CLIENT_UNKNOWN(5, "error_on_client_unknown"),
    SPEECH_TIMEOUT(6, "error_on_speech_timeout"),
    NO_RESULT(7, "error_on_no_result"),
    RECOGNIZER_BUSY(8, "error_on_recognizer_busy"),
    INSUFFICIENT_PERMISSIONS(9, "error_on_insufficient_permissions");

    private final int code;
    private final String message;

    private VSRErrors(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code + ": " + message;
    }
}*/

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VoiceInputFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VoiceInputFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VoiceInputFragment extends Fragment implements RecognitionListener {
    private SpeechRecognizer mSpeechRecognizer;
    private TextView mRecogTextView;
    private MicrophoneView mMicButtonView;

    private OnRecogResultListener mListener;

    public VoiceInputFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VoiceInputFragment.
     */
    // TODO: Rename and change types and number of parameters
//    public static VoiceInputFragment newInstance(String param1, String param2) {
//        VoiceInputFragment fragment = new VoiceInputFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext(), new ComponentName(getContext(), VoiceRecognitionService.class));
        mSpeechRecognizer.setRecognitionListener(this);
    }

    private void startRecognition() {
        Intent intent = new Intent();
        mSpeechRecognizer.startListening(intent);
        mRecogTextView.setText(R.string.voice_input_intro);
    }


    public void stopRecognition() {
        mSpeechRecognizer.stopListening();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_voice_input, container, false);
        mRecogTextView = (TextView) v.findViewById(R.id.voice_input_partial);
        mMicButtonView = (MicrophoneView) v.findViewById(R.id.mic_btn);
        mMicButtonView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopRecognition();
            }
        });
        startRecognition();
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRecogResultListener) {
            mListener = (OnRecogResultListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mSpeechRecognizer.destroy();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnRecogResultListener {
        // TODO: Update argument type and name
        void onRecogResult(Bundle bundle);
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {
        mMicButtonView.animateRadius(v);
    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {
        Bundle bundle = new Bundle();
        bundle.putInt("error", i);
        if (mListener != null) {
            mListener.onRecogResult(bundle);
        }
    }

    @Override
    public void onResults(Bundle bundle) {
        if (mListener != null) {
            mListener.onRecogResult(bundle);
        }
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        ArrayList<String> partialResults = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (partialResults.size() > 0) {
            mRecogTextView.setText(partialResults.get(0));
        }
    }

    @Override
    public void onEvent(int eventType, Bundle bundle) {
    }

}
