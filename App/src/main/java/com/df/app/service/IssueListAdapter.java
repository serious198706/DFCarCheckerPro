package com.df.app.service;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.CarCheck.AccidentResultLayout;
import com.df.app.CarCheck.PhotoFaultLayout;
import com.df.app.R;
import com.df.app.entries.Issue;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.paintview.FramePaintPreviewView;
import com.df.app.paintview.FramePaintView;
import com.df.app.util.Common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-24.
 */
public class IssueListAdapter extends BaseAdapter {
    private ArrayList<Issue> items;
    private Context context;

    private AlertDialog mPictureDialog;
    private FramePaintView framePaintView;
    private View rootView;
    private List<PosEntity> posEntitiesFront;
    private List<PosEntity> posEntitiesRear;
    private List<PhotoEntity> photoEntitiesFront;
    private List<PhotoEntity> photoEntitiesRear;

    public IssueListAdapter(Context context, ArrayList<Issue> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Issue getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.issue_list_item, null);
        }

        final Issue issue = items.get(position);

        if (issue != null) {
            Switch issueSwitch = (Switch) view.findViewById(R.id.issue_switch);
            TextView issueDesc = (TextView) view.findViewById(R.id.issue_desc);
            if (issueSwitch != null) {
                issueSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(b && issue.getPopup().equals("Y")) {
                            // 弹出绘制界面
                            Toast.makeText(context, "此项需要绘制", Toast.LENGTH_SHORT).show();
                            drawIssuePoint(issue.getView(), issue.getId(), issue.getDesc());
                        }
                    }
                });
            }

            if(issueDesc != null){
                issueDesc.setText(issue.getDesc());
            }
        }

        return view;
    }

    private void drawIssuePoint(String sight, int issueId, String comment) {
        rootView = LayoutInflater.from(context).inflate(R.layout.issue_paint_layout, null);

        posEntitiesFront = AccidentResultLayout.posEntitiesFront;
        posEntitiesRear = AccidentResultLayout.posEntitiesRear;

        photoEntitiesFront = AccidentResultLayout.photoEntitiesFront;
        photoEntitiesRear = AccidentResultLayout.photoEntitiesRear;

        // 初始化绘图View
        framePaintView = (FramePaintView) rootView.findViewById(R.id.image);

        if(sight.equals("F")) {
            framePaintView.init(AccidentResultLayout.previewBitmapFront, posEntitiesFront, "F",
                    issueId, comment);
        } else {
            framePaintView.init(AccidentResultLayout.previewBitmapRear, posEntitiesRear, "R",
                    issueId, comment);
        }

        // 选择当前绘图类型（结构检查只有一个）
        framePaintView.setType(Common.COLOR_DIFF);

        mPictureDialog = new AlertDialog.Builder(context)
                .setView(rootView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PhotoFaultLayout.updateUi();
                    }
                })
                .setCancelable(false)
                .create();

        mPictureDialog.show();
    }

    public long getCurrentTimeMillis() {
        return framePaintView.getCurrentTimeMillis();
    }
}
