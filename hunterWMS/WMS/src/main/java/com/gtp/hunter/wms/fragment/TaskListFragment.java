package com.gtp.hunter.wms.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gtp.hunter.R;
import com.gtp.hunter.structure.WrapContentLinearLayoutManager;
import com.gtp.hunter.structure.adapter.TaskRecyclerViewAdapter;
import com.gtp.hunter.wms.interfaces.TaskFragmentListener;
import com.gtp.hunter.wms.model.Action;
import com.gtp.hunter.wms.model.ViewTaskStub;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link TaskFragmentListener}
 * interface.
 */
public class TaskListFragment extends Fragment {

    private CopyOnWriteArrayList<ViewTaskStub> tasks;
    private TaskFragmentListener mListener;
    private RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TaskListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tasks = new CopyOnWriteArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasklist, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, LinearLayoutManager.VERTICAL);

            recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(context));
//            recyclerView.setItemAnimator(new NoAnimationItemAnimator());
            recyclerView.addItemDecoration(dividerItemDecoration);
            recyclerView.setAdapter(new TaskRecyclerViewAdapter(tasks, mListener));
        }
        return view;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof TaskFragmentListener) {
            mListener = (TaskFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement TaskFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.recyclerView.setAdapter(Objects.requireNonNull(this.recyclerView.getAdapter()));
    }

    public void addTasks(List<ViewTaskStub> tl) {
        for (ViewTaskStub task : tl)
            if (!(task.isCancel() || task.isCancel_task()) && !this.tasks.contains(task))
                this.tasks.add(task);
        this.sortTasks();
    }

    private void sortTasks() {
        ViewTaskStub[] a = new ViewTaskStub[this.tasks.size()];
        a = this.tasks.toArray(a);
        //Collections.sort(this.tasks); //WON'T WORK ON GALAXY TAB A6
        Arrays.sort(a);
        this.tasks.clear();
        this.tasks.addAll(Arrays.asList(a));
        this.refresh();
    }

    public void removeTaskById(UUID taskId) {
        for (int i = 0; i < this.tasks.size(); i++) {
            ViewTaskStub task = this.tasks.get(i);

            if (task.getId().equals(taskId)) {
                this.tasks.remove(i);
                Objects.requireNonNull(this.recyclerView.getAdapter()).notifyItemRemoved(i);
                break;
            }
        }
        this.refresh();
    }

    public void clearTasks() {
        this.tasks.clear();
        this.refresh();
    }

    public int getTaskCount() {
        return this.tasks.size();
    }

    public boolean containsTask(ViewTaskStub t) {
        return this.tasks.contains(t);
    }

    private void refresh() {
        FragmentActivity act = getActivity();
        if (act != null && this.recyclerView != null) {
            TaskRecyclerViewAdapter ad = (TaskRecyclerViewAdapter) this.recyclerView.getAdapter();

            if (ad != null)
                act.runOnUiThread(ad::notifyDataSetChanged);
        }
    }

    public void scrollToTask(String taskTitle) {
        for (int i = 0; i < this.tasks.size(); i++) {
            ViewTaskStub task = this.tasks.get(i);

            if (task.getDoccode().contains(taskTitle.toUpperCase())) {
                recyclerView.scrollToPosition(i);
                break;
            }
        }
    }

    public ViewTaskStub findTaskById(UUID taskId) {
        for (int i = 0; i < this.tasks.size(); i++) {
            ViewTaskStub task = this.tasks.get(i);

            if (task.getId().equals(taskId)) {
                return this.tasks.get(i);
            }
        }
        return null;
    }

    public void removeTaskByActionParam(String actionId, String param) {
        for (int i = 0; i < this.tasks.size(); i++) {
            ViewTaskStub task = this.tasks.get(i);

            for (Action a : task.getActions()) {
                if (a.getId().equals(actionId) && a.getParams().equals(param)) {
                    this.tasks.remove(i);
                    Objects.requireNonNull(this.recyclerView.getAdapter()).notifyItemRemoved(i);
                    break;
                }
            }
        }
        this.refresh();
    }
}
