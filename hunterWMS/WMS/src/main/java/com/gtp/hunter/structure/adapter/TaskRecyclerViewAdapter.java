package com.gtp.hunter.structure.adapter;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.R;
import com.gtp.hunter.wms.interfaces.TaskFragmentListener;
import com.gtp.hunter.wms.model.ViewTaskStub;

import java.util.List;

import timber.log.Timber;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ViewTaskStub} and makes a call to the
 * specified {@link TaskFragmentListener}.
 */
public class TaskRecyclerViewAdapter extends RecyclerView.Adapter<TaskRecyclerViewAdapter.TaskHolder> {

    private final List<ViewTaskStub> tasks;
    private final TaskFragmentListener mListener;

    public TaskRecyclerViewAdapter(List<ViewTaskStub> items, TaskFragmentListener listener) {
        tasks = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);

        return new TaskHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskHolder holder, int position) {
        holder.fillItem(this.tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }


    public class TaskHolder extends RecyclerView.ViewHolder {
        private final View itemView;

        TaskHolder(View itemView) {
            super(itemView);
            //get the inflater and inflate the XML layout for each item
            this.itemView = itemView;
        }

        void fillItem(ViewTaskStub task) {
            TextView txtTitle, txtLine1, txtLine2;
            MaterialButton btnAction;

            itemView.setOnTouchListener(new View.OnTouchListener() {
                private final static int CLICK_ACTION_THRESHOLD = 50;
                private float startX;
                private float startY;
                private long startTime;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getX();
                            startY = event.getY();
                            startTime = SystemClock.elapsedRealtimeNanos();
//                            Timber.d("Time down %d", SystemClock.elapsedRealtimeNanos());
//                            Timber.d("Time down %d", startTime);
                            break;
                        case MotionEvent.ACTION_UP:
                            float endX = event.getX();
                            float endY = event.getY();

//                            Timber.d("Downtime %d", event.getDownTime());
//                            Timber.d("Time up %d", SystemClock.elapsedRealtimeNanos());
//                            Timber.d("Press Time %d", (SystemClock.elapsedRealtimeNanos() - startTime));
                            if (isAClick(startX, endX, startY, endY)) {
                                v.performClick();
                            }
                            break;
                    }
                    return false;
                }

                private boolean isAClick(float startX, float endX, float startY, float endY) {
                    float differenceX = Math.abs(startX - endX);
                    float differenceY = Math.abs(startY - endY);
                    return !(differenceX > CLICK_ACTION_THRESHOLD/* =5 */ || differenceY > CLICK_ACTION_THRESHOLD);
                }
            });
            itemView.setOnClickListener((v) -> mListener.onListFragmentInteraction(task));
            txtTitle = itemView.findViewById(R.id.txtTaskTitle);
            txtLine1 = itemView.findViewById(R.id.txtTaskName);
            txtLine2 = itemView.findViewById(R.id.txtTaskContent);
            btnAction = itemView.findViewById(R.id.btnAction);
            txtTitle.setText(task.getDoccode());
            txtLine1.setText(task.getDocname());
            txtLine2.setText(task.getContents());
            if (task.getActions().length > 0) {
                btnAction.setText(task.getActions()[0].getName());
                btnAction.setOnClickListener(v -> mListener.openAction(task));
            } else {
                mListener.sendMessageNotification(btnAction.getContext().getString(R.string.error_task_no_action), 12000);
                if (BuildConfig.DEBUG)
                    Timber.e("Tasks %s - (%s) has no actions", task.getDocname(), task.getId().toString());
            }
//            if (task.getActions().length > 1) {
//                for (int i = 1; i < task.getActions().length; i++) {
//                    //TODO: Create other Buttons
//                    //remove btnAction Bottom_toBottomOf
//                    //add buttons relative to btnAction
//                }
//            }
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + ((TextView) itemView.findViewById(R.id.txtTaskTitle)).getText() + "'";
        }
    }
}
