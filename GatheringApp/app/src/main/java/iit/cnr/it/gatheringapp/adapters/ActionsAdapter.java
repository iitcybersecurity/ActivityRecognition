package iit.cnr.it.gatheringapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import iit.cnr.it.gatheringapp.R;
import iit.cnr.it.gatheringapp.model.Action;

import java.util.List;

public class ActionsAdapter extends RecyclerView.Adapter<ActionsAdapter.ViewHolder>  {

    private List<Action> values;
    private ActionsAdapterCallback mActionsAdapterCallback;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView txtHeader;
        public ImageView imgHeader;
        public LinearLayout rowContainer;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            txtHeader = (TextView) v.findViewById(R.id.action_description);
            imgHeader = (ImageView) v.findViewById(R.id.action_preview);
            rowContainer = (LinearLayout) v.findViewById(R.id.action_container);
        }
    }

    public void add(int position, Action item) {
        values.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        values.remove(position);
        notifyItemRemoved(position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ActionsAdapter(List<Action> myDataset, Context context) {
        values = myDataset;
        try {
            this.mActionsAdapterCallback = ((ActionsAdapterCallback) context);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ActionsAdapterCallback.");
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ActionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v =
                inflater.inflate(R.layout.row_actions, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Action currentAction = values.get(position);
        Context context = holder.txtHeader.getContext();
        final String label = currentAction.getLabel();
        final String description = currentAction.getDescription();
        final String previewResourceName = currentAction.getPreviewResourceName();
        holder.txtHeader.setText(description);
        int previewId = context.getResources().getIdentifier(previewResourceName, "drawable", context.getPackageName());
        holder.imgHeader.setImageResource(previewId);

        holder.rowContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActionTraining(currentAction);
                } catch (ClassCastException exception) {
                    exception.printStackTrace();
                }
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }

    private void startActionTraining(Action action) {
        //TODO implement call to new action
        Log.i("START_ACTION", action.getLabel());
        mActionsAdapterCallback.onMethodCallback(action);
    }


    public static interface ActionsAdapterCallback {
        void onMethodCallback(Action action);
    }
}
