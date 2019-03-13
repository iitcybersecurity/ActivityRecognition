package iit.cnr.it.gatheringapp.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import iit.cnr.it.gatheringapp.R;
import iit.cnr.it.gatheringapp.adapters.ActionsAdapter;
import iit.cnr.it.gatheringapp.model.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ActionFragmentList.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ActionFragmentList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActionFragmentList extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private RecyclerView actionsRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private final static int col_number = 3;

    public ActionFragmentList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ActionFragmentList.
     */
    // TODO: Rename and change types and number of parameters
    public static ActionFragmentList newInstance(String param1, String param2) {
        ActionFragmentList fragment = new ActionFragmentList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_action_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            actionsRecyclerView = (RecyclerView) getView().findViewById(R.id.actions_recycler_view);
            actionsRecyclerView.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(getContext());
            actionsRecyclerView.setLayoutManager(layoutManager);
            actionsRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                    DividerItemDecoration.VERTICAL));
            List<Action> input = retrieveAvailableActions(view.getContext());
            mAdapter = new ActionsAdapter(input, view.getContext());
            actionsRecyclerView.setAdapter(mAdapter);
        } catch (Exception e) {
            Log.e("VIEW ERROR", "Could not create view " + view.getContentDescription());
            e.printStackTrace();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onActionListFragmentInteraction(uri);
        }
    }

    private List<Action> retrieveAvailableActions(Context context) {
        String[] descriptions = {"Free Texting Sitting",
                "Free Texting Walking",
                "Guided Texting Sitting",
                "Guided Texting Walking",
                "Web Browsing",
                "Unlock From Table",
                "Unlock From Pocket",
                "Unlock From Bag"};
        String[] labels = {"FREE_TEXT_SIT",
                "FREE_TEXT_WALK",
                "GUIDED_TEXT_SIT",
                "GUIDED_TEXT_WALK",
                "WEB_BROWSING",
                "TABLE_UNLOCK",
                "POCKET_UNLOCK",
                "BAG_UNLOCK"};

        String[] previewIds = {
                "ic_baseline_textsms_24px",
                "ic_baseline_textsms_24px",
                "ic_baseline_textsms_24px",
                "ic_baseline_textsms_24px",
                "ic_baseline_public_24px",
                "ic_baseline_screen_lock_portrait_24px",
                "ic_baseline_screen_lock_portrait_24px",
                "ic_baseline_screen_lock_portrait_24px"
        };

        String[] instructionsIds = {
                "free_text_sitting",
                "free_text_sitting",
                "free_text_sitting",
                "free_text_sitting",
                "free_text_sitting",
                "free_text_sitting",
                "free_text_sitting",
                "free_text_sitting",
                "free_text_sitting"
        };
        List<Action> actions = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            Action newAction = new Action();
            newAction.setLabel(labels[i]);
            newAction.setDescription(descriptions[i]);
            newAction.setPreviewResourceName(previewIds[i]);
            newAction.setInstructionsResourceName(instructionsIds[i]);
            actions.add(newAction);
        }
        return actions;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onActionListFragmentInteraction(Uri uri);
    }
}
