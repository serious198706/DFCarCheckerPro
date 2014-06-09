package com.df.kia.service.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.KeyEvent;
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
import com.df.kia.carCheck.AccidentResultLayout;
import com.df.kia.carCheck.CarCheckActivity;
import com.df.kia.carCheck.ExteriorLayout;
import com.df.kia.carCheck.InteriorLayout;
import com.df.kia.carCheck.PhotoFaultLayout;
import com.df.library.entries.Action;
import com.df.library.entries.Issue;
import com.df.library.entries.ListedPhoto;
import com.df.library.entries.PhotoEntity;
import com.df.library.entries.PosEntity;
import com.df.kia.paintView.FramePaintView;
import com.df.kia.service.util.AppCommon;

import java.util.ArrayList;
import java.util.List;

import com.df.kia.R;
import com.df.library.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

import static com.df.library.util.Helper.setTextView;
import static com.df.library.util.Helper.showView;

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
    private AlertDialog dialog;

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
            Switch issueSwitch = (Switch) view.findViewById(R.id.issue_switch);
            TextView issueDesc = (TextView) view.findViewById(R.id.issue_desc);

            if (issueSwitch != null) {
                issueSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(issue.isLastSelect() == b) {
                            return;
                        }

                        // 如果该issue当前为“否”，并且有视角
                        if(!b && issue.getSelect().equals("否") &&
                                (issue.getView().equals("F") || issue.getView().equals("R"))) {
                            // 清除此issue的数据
                            closeIssue(issue);
                        } else if(!issue.getSelect().equals("否") &&
                                (issue.getView().equals("F") || issue.getView().equals("R"))) {
                            // 弹出绘制界面
                            drawIssuePoint(issue, false);
                        } else if(issue.getView().equals("") || issue.getView().equals("null")) {
                            issue.setSelect(b ? "否" : "是");
                            issue.setLastSelect(b);
                        }
                    }
                });

                // 为了防止缓存现象，要重设置一下switch
                issueSwitch.setChecked(issue.getSelect().equals("否"));
                issue.setLastSelect(issue.getSelect().equals("否"));

                if (issue.getPosEntities().size() > 0) {
                    issueDesc.setTextColor(Color.BLUE);
                    issueDesc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            drawIssuePoint(issue, false);
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
     * @param issue 问题条目
     * @param delete 是否为删除操作
     */
    private void drawIssuePoint(final Issue issue, final boolean delete) {
        rootView = LayoutInflater.from(context).inflate(R.layout.issue_paint_layout, null);

        showView(rootView, R.id.deleteAlert, delete);

        TextView title = (TextView)rootView.findViewById(R.id.currentItem);
        title.setText(delete ? R.string.deleteRecord : R.string.issue_back);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(delete) {
                    issue.setLastSelect(true);
                    issue.setSelect("否");

                    mPictureDialog.dismiss();
                } else {
                    issue.setLastSelect(false);
                    alertUser(R.string.cancel_confirm, issue);
                }
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

                if(issue.getPosEntities().size() == 0) {
                    Toast.makeText(context, "请绘制缺陷位置！", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 如果选择了，就保存
                issue.setSerious(radioButton.getText().toString());
                issue.setSelect("否");
                issue.setLastSelect(true);

                reallyDeleteItems(issue);
                mPictureDialog.dismiss();
            }
        });

        // 全部删除 - 确认删除按钮
        Button deleteButton = (Button)rootView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(ListedPhoto listedPhoto : issuePhotoListAdapter.getItems()) {
                    listedPhoto.setDelete(true);
                }

                reallyDeleteItems(issue);

                issue.getPhotoEntities().clear();
                issue.getPosEntities().clear();
                issue.setSerious("");
                issue.setSelect("是");
                issue.setLastSelect(false);
                mPictureDialog.dismiss();
            }
        });

        List<ListedPhoto> listedPhotos = new ArrayList<ListedPhoto>();

        for(int i = 0; i < issue.getPhotoEntities().size(); i++) {
            PhotoEntity photoEntity = issue.getPhotoEntities().get(i);
            ListedPhoto temp = new ListedPhoto(i, photoEntity);
            listedPhotos.add(temp);
        }

        issuePhotoListAdapter = new IssuePhotoListAdapter(context, listedPhotos, issue, delete, new IssuePhotoListAdapter.OnDeleteItem() {
            @Override
            public void onDeleteItem(int position) {
                issue.getPosEntities().get(position).setDelete(true);
                framePaintView.invalidate();
                issuePhotoListAdapter.getItem(position).setDelete(true);
                issuePhotoListAdapter.notifyDataSetChanged();
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

        // 如果是delete模式，禁用一些修改功能
        for(int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(!delete);
        }

        framePaintView.setEnabled(!delete);

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
        mPictureDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                // 返回按钮有按下和抬起两种状态，如果不判断抬起状态，则会触发两次下面的代码
                if(i == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    if(delete) {
                        issue.setLastSelect(true);
                        issue.setSelect("否");

                        mPictureDialog.dismiss();
                    } else {
                        issue.setLastSelect(false);
                        alertUser(R.string.cancel_confirm, issue);
                    }
                }

                return true;
            }
        });
        mPictureDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mPictureDialog.show();
    }

    /**
     * 点击确定按钮后，删除需要删除的图片
     * @param issue
     */
    private void reallyDeleteItems(Issue issue) {
        for(int i = issuePhotoListAdapter.getCount() - 1; i >= 0; i--) {
            ListedPhoto listedPhoto = issuePhotoListAdapter.getItem(i);

            if(!listedPhoto.isDelete()) {
                continue;
            }

            int position = listedPhoto.getIndex();

            // 删除posEntity
            issue.getPosEntities().remove(position);

            if(CarCheckActivity.isModify()) {
                // 更新查勘结果中的图片
                if(issue.getView().equals("F")) {
                    AccidentResultLayout.photoEntitiesFront.get(position).setModifyAction(Action.DELETE);
                    AccidentResultLayout.posEntitiesFront.remove(position);
                    AccidentResultLayout.framePaintPreviewViewFront.invalidate();
                }
                else {
                    AccidentResultLayout.photoEntitiesRear.get(position).setModifyAction(Action.DELETE);
                    AccidentResultLayout.posEntitiesRear.remove(position);
                    AccidentResultLayout.framePaintPreviewViewRear.invalidate();
                }

                // 删除照片列表里的照片
                PhotoEntity temp = issue.getPhotoEntities().get(position);

                PhotoEntity temp1 = PhotoFaultLayout.photoListAdapter.getItem(temp);
                temp1.setModifyAction(Action.DELETE);

                try {
                    JSONObject jsonObject = new JSONObject(temp1.getJsonString());
                    jsonObject.put("Action", Action.DELETE);
                    temp1.setJsonString(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                issue.getPhotoEntities().remove(position);
                PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();

                // 现删除issue本身的照片
                issue.getPhotoEntities().remove(temp);
            } else {
                // 更新查勘结果中的图片
                if(issue.getView().equals("F")) {
                    removePhotoEntity(AccidentResultLayout.photoEntitiesFront, AccidentResultLayout.posEntitiesFront, position);
                    AccidentResultLayout.framePaintPreviewViewFront.invalidate();
                }
                else {
                    removePhotoEntity(AccidentResultLayout.photoEntitiesRear, AccidentResultLayout.posEntitiesRear, position);
                    AccidentResultLayout.framePaintPreviewViewRear.invalidate();
                }

                // 删除照片列表里的照片
                PhotoEntity temp = issue.getPhotoEntities().get(position);
                PhotoFaultLayout.photoListAdapter.removeItem(temp);
                issue.getPhotoEntities().remove(position);
                PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();

                // 现删除issue本身的照片
                issue.getPhotoEntities().remove(temp);
            }

            // 更新绘图
            framePaintView.invalidate();
        }
    }

    private void removePhotoEntity(List<PhotoEntity> photoEntities, List<PosEntity> posEntities,  int position) {
        photoEntities.remove(position);
        posEntities.remove(position);
    }

    /**
     * 取消之前的操作
     * @param issue
     */
    private void cancelOperations(Issue issue) {
        for(ListedPhoto listedPhoto : issuePhotoListAdapter.getItems()) {
            int position = listedPhoto.getIndex();

            issue.getPosEntities().get(position).setDelete(false);
            issuePhotoListAdapter.getItem(position).setDelete(false);
            issuePhotoListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 关闭并删除此项目所对应的所有照片与pos
     */
    private void closeIssue(final Issue issue) {
        drawIssuePoint(issue, true);
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

        for(PhotoEntity photoEntity : AccidentResultLayout.photoEntitiesFront)
            PhotoFaultLayout.photoListAdapter.addItem(photoEntity);

        for(PhotoEntity photoEntity : AccidentResultLayout.photoEntitiesRear)
            PhotoFaultLayout.photoListAdapter.addItem(photoEntity);

        for(PhotoEntity photoEntity : PhotoFaultLayout.photoEntities)
            PhotoFaultLayout.photoListAdapter.addItem(photoEntity);

        PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();
    }

    /**
     * 在弹出页面点击取消时提醒用户是否要取消
     * @param msgId 车辆id
     * @param issue
     */
    private void alertUser(final int msgId, final Issue issue) {
        View view1 = ((Activity)context).getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(msgId);
        content.setTextSize(20f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, context.getResources().getString(R.string.alert));

        dialog = new AlertDialog.Builder(context)
                .setView(view1)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPictureDialog.show();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(msgId == R.string.cancel_confirm) {
                            // 退出
                            cancelOperations(issue);
                            framePaintView.cancel();
                            mPictureDialog.dismiss();
                            dialog.dismiss();
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
