package com.df.app.service.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.df.app.carCheck.AccidentResultLayout;
import com.df.app.carCheck.ExteriorLayout;
import com.df.app.carCheck.InteriorLayout;
import com.df.app.carCheck.PhotoFaultLayout;
import com.df.app.entries.Issue;
import com.df.app.entries.IssuePhoto;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.paintview.FramePaintView;
import com.df.app.util.Common;

import java.util.ArrayList;
import java.util.List;

import com.df.app.R;
import com.df.app.util.Helper;

import static com.df.app.util.Helper.setTextView;
import static com.df.app.util.Helper.showView;

/**
 * Created by 岩 on 13-12-24.
 *
 * 问题查勘adapter
 */
public class IssueListAdapter extends BaseAdapter {
    private List<Issue> items;
    private Context context;

    private Dialog mPictureDialog;
    private FramePaintView framePaintView;
    private View rootView;
    private ListView issuePhotoListView;
    private IssuePhotoListAdapter issuePhotoListAdapter;

    public IssueListAdapter(Context context, List<Issue> items) {
        this.context = context;
        this.items = items;
    }

    public List<Issue> getItems() {
        return items;
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
            final Switch issueSwitch = (Switch) view.findViewById(R.id.issue_switch);
            TextView issueDesc = (TextView) view.findViewById(R.id.issue_desc);

            if (issueSwitch != null) {
                issueSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        // 如果该issue当前为“否”，并且有视角
                        if(!b && issue.getSelect().equals("否") && !issue.getView().equals("")) {
                            // 清除此issue的数据
                            closeIssue(issueSwitch, issue);
                        } else if(issue.getSelect().equals("是") && !issue.getView().equals("")) {
                            // 弹出绘制界面
                            drawIssuePoint(issueSwitch, issue, false);
                        } else if(issue.getView().equals("")) {
                            issue.setSelect(b ? "否" : "是");
                        }
//
//
//
//
//
//
//
//
//                        if(b == issue.getSelect().equals("否")) {
//                            closeIssue(issueSwitch, issue);
//                        }
//
//                        if(b && !issue.getView().equals("")) {
//                            // 弹出绘制界面
//                            drawIssuePoint(issueSwitch, issue, false);
//                        }
//
//                        // 选择完成后，将对应问题的select更新
//                        issue.setSelect(b ? "否" : "是");
                    }
                });


//                issueSwitch.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if(issue.getSelect().equals("否")) {
//                            closeIssue(issueSwitch, issue);
//                        }
//
//                        if(!issue.getSelect().equals("否") && !issue.getView().equals("")) {
//                            // 弹出绘制界面
//                            drawIssuePoint(issueSwitch, issue, false);
//                        } else if(!issue.getSelect().equals("否") && issue.getView().equals("")) {
//                            issue.setSelect("是");
//                        }
//
//                        // 选择完成后，将对应问题的select更新
//                        issue.setSelect(issue.getSelect().equals("否") ? "否" : "是");
//                    }
//                });


                // 为了防止缓存现象，要重设置一下switch
                issueSwitch.setChecked(issue.getSelect().equals("否"));

                if (issue.getPosEntities().size() > 0) {
                    issueDesc.setTextColor(Color.BLUE);
                    issueDesc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            drawIssuePoint(issueSwitch, issue, false);
                        }
                    });
                } else {
                    issueDesc.setTextColor(Color.BLACK);
                    issueDesc.setOnClickListener(null);
                }
            }

            if(issueDesc != null){
                issueDesc.setText(issue.getDesc());
            }
        }

        return view;
    }



    /**
     * 对应某个问题，进行绘制
     * @param issueSwitch 开关，在做不同的操作时，对开关做不同的操作
     * @param issue 问题条目
     * @param delete 是否为删除操作
     */
    private void drawIssuePoint(final Switch issueSwitch, final Issue issue, final boolean delete) {
        rootView = LayoutInflater.from(context).inflate(R.layout.issue_paint_layout, null);

        showView(rootView, R.id.deleteAlert, delete);

        TextView title = (TextView)rootView.findViewById(R.id.currentItem);
        title.setText(delete ? R.string.deleteRecord : R.string.issue_back);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(delete) {
                    // 如果取消删除，则要将将switch改为“否”
                    //issueSwitch.setChecked(true);
                    issue.setSelect("否");
                } else {
                    alertUser(R.string.cancel_confirm);
                }

                mPictureDialog.dismiss();
            }
        });

        TextView issueDesc = (TextView)rootView.findViewById(R.id.issueDesc);
        issueDesc.setText(issue.getDesc());

        showView(rootView, R.id.done, !delete);
        showView(rootView, R.id.deleteButton, delete);

        // 确认按钮
        Button doneButton = (Button)rootView.findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioGroup radioGroup = (RadioGroup)rootView.findViewById(R.id.serious);

                RadioButton radioButton = (RadioButton)rootView.findViewById(radioGroup
                        .getCheckedRadioButtonId());

                // 如果未选择缺陷等级，不能关闭对话框
                if (radioButton == null) {
                    Toast.makeText(context, "请选择缺陷等级！", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 如果选择了，就保存
                issue.setSerious(radioButton.getText().toString());
                issue.setSelect("否");
                mPictureDialog.dismiss();
            }
        });

        // 确认删除按钮
        Button deleteButton = (Button)rootView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (PhotoEntity photoEntity : issue.getPhotoEntities()) {
                    if (issue.getView().equals("F")) {
                        AccidentResultLayout.photoEntitiesFront.remove(photoEntity);
                    } else {
                        AccidentResultLayout.photoEntitiesRear.remove(photoEntity);
                    }

                    PhotoFaultLayout.photoListAdapter.removeItem(photoEntity);
                }

                PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();

                for (PosEntity posEntity : issue.getPosEntities()) {
                    if (issue.getView().equals("F")) {
                        AccidentResultLayout.posEntitiesFront.remove(posEntity);
                    } else {
                        AccidentResultLayout.posEntitiesRear.remove(posEntity);
                    }
                }

                issue.getPhotoEntities().clear();
                issue.getPosEntities().clear();
                issue.setSerious("");
                issue.setSelect("是");
                mPictureDialog.dismiss();
            }
        });

        List<IssuePhoto> issuePhotos = new ArrayList<IssuePhoto>();

        for(int i = 0; i < issue.getPhotoEntities().size(); i++) {
            PhotoEntity photoEntity = issue.getPhotoEntities().get(i);
            IssuePhoto temp = new IssuePhoto(i, photoEntity.getThumbFileName(), photoEntity.getComment());
            issuePhotos.add(temp);
        }

        issuePhotoListAdapter = new IssuePhotoListAdapter(context, issuePhotos, issue,
                new IssuePhotoListAdapter.OnDeleteItem() {
                    @Override
                    public void onDeleteItem(int position) {
                        // 删除条目
                        issuePhotoListAdapter.remove(position);
                        issuePhotoListAdapter.notifyDataSetChanged();

                        // 删除issue中的posEntity
                        issue.getPosEntities().remove(position);

                        // 删除照片列表里的照片
                        PhotoEntity temp = issue.getPhotoEntities().get(position);
                        PhotoFaultLayout.photoListAdapter.removeItem(temp);
                        PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();

                        // 现删除issue本身的照片
                        issue.getPhotoEntities().remove(temp);

                        framePaintView.invalidate();
                    }
                });

        issuePhotoListView = (ListView)rootView.findViewById(R.id.issuePhotoList);
        issuePhotoListView.setAdapter(issuePhotoListAdapter);
        issuePhotoListAdapter.notifyDataSetChanged();

        List<PosEntity> posEntitiesFront = AccidentResultLayout.posEntitiesFront;
        List<PosEntity> posEntitiesRear = AccidentResultLayout.posEntitiesRear;

        // 初始化绘图View
        framePaintView = (FramePaintView) rootView.findViewById(R.id.image);

        if(issue.getView().equals("F")) {
            framePaintView.init(context, AccidentResultLayout.previewBitmapFront,
                    issue, issuePhotoListAdapter, posEntitiesFront, "F");
        } else {
            framePaintView.init(context, AccidentResultLayout.previewBitmapRear,
                    issue, issuePhotoListAdapter, posEntitiesRear, "R");
        }

        RadioGroup radioGroup = (RadioGroup)rootView.findViewById(R.id.serious);
        if(issue.getSerious().equals("轻微")) {
            radioGroup.check(R.id.light);
        } else if (issue.getSerious().equals("严重")) {
            radioGroup.check(R.id.heavy);
        } else {
            radioGroup.clearCheck();
        }

        // 选择当前绘图类型（结构检查只有一个）
        framePaintView.setType(Common.COLOR_DIFF);

        mPictureDialog = new Dialog(context, android.R.style.Theme_Holo_Light);
        mPictureDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mPictureDialog.setContentView(rootView);
        mPictureDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                AccidentResultLayout.framePaintPreviewViewFront.invalidate();
                AccidentResultLayout.framePaintPreviewViewRear.invalidate();

                notifyPhotoList();

                notifyDataSetChanged();
            }
        });
        mPictureDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if(delete) {
                    // 如果取消删除，则要将将switch改为“否”
                    //issueSwitch.setChecked(true);
                    issue.setSelect("否");
                } else {
                    alertUser(R.string.cancel_confirm);
                }

                mPictureDialog.dismiss();
            }
        });
        mPictureDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mPictureDialog.show();
    }

    /**
     * 关闭并删除此项目所对应的所有照片与pos
     */
    private void closeIssue(final Switch issueSwitch, final Issue issue) {
        drawIssuePoint(issueSwitch, issue, true);
    }

    /**
     * 通知照片列表，有照片更新
     */
    private void notifyPhotoList() {
        // 清空，然后将各自的photoEntity加入列表
        PhotoFaultLayout.photoListAdapter.clear();

        for(PhotoEntity photoEntity : ExteriorLayout.photoEntities)
            PhotoFaultLayout.photoListAdapter.addItem(photoEntity);

        for(PhotoEntity photoEntity : InteriorLayout.photoEntities)
            PhotoFaultLayout.photoListAdapter.addItem(photoEntity);

        for(PhotoEntity photoEntity : AccidentResultLayout.photoEntitiesFront) {
            PhotoFaultLayout.photoListAdapter.addItem(photoEntity);
        }

        for(PhotoEntity photoEntity : AccidentResultLayout.photoEntitiesRear) {
            PhotoFaultLayout.photoListAdapter.addItem(photoEntity);
        }

        PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();
    }

    /**
     * 在弹出页面点击取消时提醒用户是否要取消
     * @param msgId 车辆id
     */
    private void alertUser(final int msgId) {
        View view1 = ((Activity)context).getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(msgId);
        content.setTextSize(20f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, context.getResources().getString(R.string.alert));

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view1)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(msgId == R.string.cancel_confirm) {
                            // 退出
                            framePaintView.cancel();
                            mPictureDialog.dismiss();
                            issuePhotoListAdapter.notifyDataSetChanged();
                        } else if(msgId == R.string.clear_confirm) {
                            framePaintView.clear();
                        }
                    }
                })
                .create();

        dialog.show();
    }
}
