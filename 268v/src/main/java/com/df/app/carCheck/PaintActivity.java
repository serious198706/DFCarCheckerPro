package com.df.app.carCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import com.df.app.R;
import com.df.app.service.util.AppCommon;
import com.df.library.entries.Action;
import com.df.library.entries.CarSettings;
import com.df.library.entries.ListedPhoto;
import com.df.library.entries.PhotoEntity;
import com.df.library.entries.PosEntity;
import com.df.app.paintView.ExteriorPaintView;
import com.df.app.paintView.InteriorPaintView;
import com.df.library.service.views.PaintView;
import com.df.app.service.Adapter.PaintPhotoListAdapter;
import com.df.library.util.Common;
import com.df.library.entries.UserInfo;
import com.df.library.util.Helper;
import com.df.library.util.SlidingUpPanelLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.df.library.util.Helper.getBitmapHeight;
import static com.df.library.util.Helper.getBitmapWidth;
import static com.df.library.util.Helper.setTextView;

/**
 * Created by 岩 on 13-12-20.
 *
 * 绘制的activity
 */
public class PaintActivity extends Activity {
    private ExteriorPaintView exteriorPaintView;
    private InteriorPaintView interiorPaintView;
    private String currentPaintView;

    // 当前绘图类型
    private int currentType = 0;

    // 一个HashMap
    private Map<String, View> map = new HashMap<String, View>();

    // 绘图类的父类
    PaintView paintView;

    // 滑出的照片列表
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private boolean isExpanded = false;
    private ArrayList<ListedPhoto> listedPhotos;
    private PaintPhotoListAdapter adapter;

    // 绘制类型
    public enum PaintType {
        EX_PAINT, IN_PAINT, NOVALUE;

        public static PaintType paintType(String str)
        {
            try {
                return valueOf(str);
            }
            catch (Exception ex) {
                return NOVALUE;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paint_layout);

        // 根据CarSettings的figure设定图片
        int figure = Integer.parseInt(CarSettings.getInstance().getFigure());
        Bitmap bitmap = getBitmapFromFigure(figure, "IN");

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        layoutParams.gravity = Gravity.CENTER;

        // 初始化绘图View
        interiorPaintView = (InteriorPaintView) findViewById(R.id.interior_paint_view);
        interiorPaintView.init(bitmap, InteriorLayout.posEntities, new InteriorPaintView.OnAddEmptyPhoto() {
            @Override
            public void onAddEmptyPhoto(PosEntity posEntity) {
                addPhotoToList();
            }
        });
        interiorPaintView.setType(Common.DIRTY);
        interiorPaintView.setLayoutParams(layoutParams);

        // 根据CarSettings的figure设定图片
        figure = Integer.parseInt(CarSettings.getInstance().getFigure());
        bitmap = getBitmapFromFigure(figure, "EX");

        layoutParams = new LinearLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        layoutParams.gravity = Gravity.CENTER;

        // 初始化绘图View
        exteriorPaintView = (ExteriorPaintView) findViewById(R.id.exterior_paint_view);
        exteriorPaintView.init(bitmap, ExteriorLayout.posEntities, new ExteriorPaintView.OnAddEmptyPhoto() {
            @Override
            public void onAddEmptyPhoto(PosEntity posEntity) {
                addPhotoToList();
            }
        });
        exteriorPaintView.setType(Common.COLOR_DIFF);
        exteriorPaintView.setLayoutParams(layoutParams);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentPaintView = extras.getString("PAINT_TYPE");

            switch (PaintType.paintType(currentPaintView)) {
                // 外观检查绘图
                case EX_PAINT:
                    setExPaintLayout();
                    break;
                // 内饰检查绘图
                case IN_PAINT:
                    setInPaintLayout();
                    break;
            }
        }

        map.put("EX_PAINT", exteriorPaintView);
        map.put("IN_PAINT", interiorPaintView);

        paintView = (PaintView)map.get(currentPaintView);

        // 填充照片列表
        initPhotoList();

        // 取消按钮
        Button cancelButton = (Button)findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertUser(R.string.cancel_confirm);
            }
        });

        // 完成按钮
        Button doneButton = (Button)findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reallyDeleteItems();
                notifyPhotoList();
                finish();
            }
        });

        ImageView imageView = (ImageView)findViewById(R.id.expandImage);

        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));
        slidingUpPanelLayout.setAnchorPoint(0.1f);
        slidingUpPanelLayout.setDragView(imageView);
        slidingUpPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                paintView.setEnabled(false);
            }

            @Override
            public void onPanelExpanded(View panel) {
                ImageView imageView = (ImageView) findViewById(R.id.expandImage);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.expander_open_holo_light);
                imageView.setImageBitmap(bitmap);

                paintView.setEnabled(false);
            }

            @Override
            public void onPanelCollapsed(View panel) {
                ImageView imageView = (ImageView) findViewById(R.id.expandImage);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.expander_close_holo_light);
                imageView.setImageBitmap(bitmap);

                paintView.setEnabled(true);
            }

            @Override
            public void onPanelAnchored(View panel) {
                ImageView imageView = (ImageView) findViewById(R.id.expandImage);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.expander_open_holo_light);
                imageView.setImageBitmap(bitmap);

                paintView.setEnabled(false);
            }
        });
    }

    /**
     * 初始化照片列表（根据不同的布局）
     */
    private void initPhotoList() {
        switch (PaintType.paintType(currentPaintView)) {
            case EX_PAINT:
                setTextView(getWindow().getDecorView(), R.id.listTitle, "外观缺陷照列表");
                break;
            case IN_PAINT:
                setTextView(getWindow().getDecorView(), R.id.listTitle, "内饰缺陷照列表");
                break;
        }

        ListView photoListView = (ListView)findViewById(R.id.paintPhotoList);

        listedPhotos = new ArrayList<ListedPhoto>();

        // 找出未删除的照片，放入一个list
        List<PhotoEntity> noneDeletePhotoEntities = new ArrayList<PhotoEntity>();

        for(PhotoEntity photoEntity : paintView.getPhotoEntities()) {
            if(!photoEntity.getModifyAction().equals(Action.DELETE))
                noneDeletePhotoEntities.add(photoEntity);
        }

        int length = noneDeletePhotoEntities.size();

        for(int i = 0; i < length; i++) {
            PhotoEntity photoEntity = noneDeletePhotoEntities.get(i);
            try {
                JSONObject jsonObject = new JSONObject(photoEntity.getJsonString());
                int type = jsonObject.getJSONObject("PhotoData").getInt("type");

                ListedPhoto listedPhoto = new ListedPhoto(i, photoEntity, type);
                listedPhotos.add(listedPhoto);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        adapter = new PaintPhotoListAdapter(this, listedPhotos, new PaintPhotoListAdapter.OnDeleteItem() {
            @Override
            public void onDeleteItem(int position) {
            paintView.getPosEntities().get(position).setDelete(true);
            paintView.invalidate();
            adapter.getItem(position).setDelete(true);
            adapter.notifyDataSetChanged();
            }
        });

        photoListView.setAdapter(adapter);
    }

    /**
     * 真正删除需要删除的图片
     */
    private void reallyDeleteItems() {
        for(int i = adapter.getCount() - 1; i >= 0; i--) {
            ListedPhoto listedPhoto = adapter.getItem(i);

            if(!listedPhoto.isDelete()) {
                continue;
            }

            int position = listedPhoto.getIndex();

            if(CarCheckActivity.isModify()) {
                int index = paintView.getPhotoEntities().indexOf(listedPhoto.getPhotoEntity());
                PhotoEntity photoEntity = paintView.getPhotoEntities().get(index);

                if(photoEntity != null) {
                    photoEntity.setModifyAction(Action.DELETE);

                    try {
                        JSONObject jsonObject = new JSONObject(photoEntity.getJsonString());
                        jsonObject.put("Action", Action.DELETE);
                        photoEntity.setJsonString(jsonObject.toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if(position < paintView.getPhotoEntities().size()) {
                    PhotoEntity photoEntity = paintView.getPhotoEntities().get(position);
                    PhotoFaultLayout.photoListAdapter.removeItem(photoEntity);
                }

                paintView.getPhotoEntities().remove(position);
            }

            paintView.getPosEntities().remove(position);
        }

        paintView.invalidate();
        PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();
    }

    /**
     * 取消刚刚的所有操作
     */
    private void cancelOperations() {
        for(ListedPhoto listedPhoto : adapter.getItems()) {
            int position = listedPhoto.getIndex();

            paintView.getPosEntities().get(position).setDelete(false);
            adapter.getItem(position).setDelete(false);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     *  设置为外观布局
     */
    private void setExPaintLayout() {
        interiorPaintView.setVisibility(View.GONE);
        exteriorPaintView.setVisibility(View.VISIBLE);

        setTextView(getWindow().getDecorView(), R.id.currentItem, getResources().getString(R.string.exterior));

        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT,RadioGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(15, 10, 15, 10);

        // 选择当前绘图类型
        RadioGroup radioGroup = new RadioGroup(this);

        RadioButton colorDiffRadio = new RadioButton(this);
        colorDiffRadio.setText(R.string.exterior_color_diff);
        colorDiffRadio.setButtonDrawable(R.drawable.radio_button);
        Drawable img = getResources().getDrawable( R.drawable.out_color_diff);
        colorDiffRadio.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null);
        colorDiffRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    currentType = Common.COLOR_DIFF;
                    exteriorPaintView.setType(currentType);
                }
            }
        });
        colorDiffRadio.setLayoutParams(params);

        RadioButton scratchRadio = new RadioButton(this);
        scratchRadio.setText(R.string.exterior_scratch);
        scratchRadio.setButtonDrawable(R.drawable.radio_button);
        img = getResources().getDrawable(R.drawable.out_scratch);
        scratchRadio.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        scratchRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    currentType = Common.SCRATCH;
                    exteriorPaintView.setType(currentType);
                }
            }
        });
        scratchRadio.setLayoutParams(params);

        RadioButton transRadio = new RadioButton(this);
        transRadio.setText(R.string.exterior_trans);
        transRadio.setButtonDrawable(R.drawable.radio_button);
        img = getResources().getDrawable(R.drawable.out_trans);
        transRadio.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        transRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    currentType = Common.TRANS;
                    exteriorPaintView.setType(currentType);
                }
            }
        });
        transRadio.setLayoutParams(params);

        RadioButton scrapeRadio = new RadioButton(this);
        scrapeRadio.setText(R.string.exterior_scrape);
        scrapeRadio.setButtonDrawable(R.drawable.radio_button);
        img = getResources().getDrawable( R.drawable.out_scrape);
        scrapeRadio.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        scrapeRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    currentType = Common.SCRAPE;
                    exteriorPaintView.setType(currentType);
                }

            }
        });
        scrapeRadio.setLayoutParams(params);

        RadioButton otherRadio = new RadioButton(this);
        otherRadio.setText(R.string.exterior_other);
        otherRadio.setButtonDrawable(R.drawable.radio_button);
        img = getResources().getDrawable(R.drawable.out_other);
        otherRadio.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        otherRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    currentType = Common.OTHER;
                    exteriorPaintView.setType(currentType);
                }
            }
        });
        otherRadio.setLayoutParams(params);

        radioGroup.addView(colorDiffRadio, 0);
        radioGroup.addView(scratchRadio, 1);
        radioGroup.addView(transRadio, 2);
        radioGroup.addView(scrapeRadio, 3);
        radioGroup.addView(otherRadio, 4);
        radioGroup.setOrientation(LinearLayout.HORIZONTAL);
        radioGroup.check(colorDiffRadio.getId());

        // 设置绘图类型按钮居中
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        radioGroup.setLayoutParams(layoutParams);
        
        LinearLayout paintMenu = (LinearLayout) findViewById(R.id.paintType);
        paintMenu.addView(radioGroup);
    }

    /**
     * 设置为内饰图布局
     */
    private void setInPaintLayout() {
        interiorPaintView.setVisibility(View.VISIBLE);
        exteriorPaintView.setVisibility(View.GONE);

        setTextView(getWindow().getDecorView(), R.id.currentItem, getResources().getString(R.string.interior));

        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT,RadioGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(20, 10, 20, 10);

        // 选择当前绘图类型
        RadioGroup radioGroup = new RadioGroup(this);

        RadioButton dirtyRadio = new RadioButton(this);
        dirtyRadio.setText(R.string.interior_dirty);
        dirtyRadio.setButtonDrawable(R.drawable.radio_button);
        Drawable img = getResources().getDrawable( R.drawable.out_scrape);
        dirtyRadio.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        dirtyRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                currentType = b ? Common.DIRTY : Common.BROKEN;

                interiorPaintView.setType(currentType);
            }
        });
        dirtyRadio.setLayoutParams(params);


        RadioButton brokenRadio = new RadioButton(this);
        brokenRadio.setText(R.string.interior_broken);
        brokenRadio.setButtonDrawable(R.drawable.radio_button);
        img = getResources().getDrawable( R.drawable.out_trans);
        brokenRadio.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        brokenRadio.setLayoutParams(params);

        radioGroup.addView(dirtyRadio);
        radioGroup.addView(brokenRadio);
        radioGroup.setOrientation(LinearLayout.HORIZONTAL);
        radioGroup.check(dirtyRadio.getId());

        // 设置绘图类型按钮居中
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        radioGroup.setLayoutParams(layoutParams);

        LinearLayout paintMenu = (LinearLayout) findViewById(R.id.paintType);
        paintMenu.addView(radioGroup);
    }

    /**
     * 选择取消时提醒用户
     * @param msgId
     */
    private void alertUser(final int msgId) {
        paintView = (PaintView)map.get(currentPaintView);

        View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(msgId);
        content.setTextSize(20f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, getResources().getString(R.string.alert));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view1)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(msgId == R.string.cancel_confirm) {
                            // 退出
                            cancelOperations();
                            paintView.cancel();
                            finish();
                        } else if(msgId == R.string.clear_confirm) {
                            paintView.clear();
                        }
                    }
                })
                .create();

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 获取绘图父类实体
        paintView = (PaintView)map.get(currentPaintView);

        PosEntity posEntity = paintView.getPosEntity();

        switch (requestCode) {
            case Common.ADD_COMMENT_FOR_EXTERIOR_AND_INTERIOR_PHOTO: {
                    // 从备注界面返回时，先添加备注
                    Bundle bundle = data.getExtras();
                    posEntity.setComment(bundle.getString("COMMENT"));

                    // 再生成PhotoEntity，并添加到列表
                    addPhotoToList();
                }
                break;
            case Common.MODIFY_PAINT_COMMENT: {
                    Bundle bundle = data.getExtras();
                    String comment = bundle.getString("COMMENT");

                    PhotoEntity photoEntity = PhotoLayout.commentModEntity;
                    Helper.updateComment(photoEntity, comment);

                    photoEntity = PhotoLayout.listedPhoto.getPhotoEntity();
                    Helper.updateComment(photoEntity, comment);

                    PhotoLayout.paintPhotoListAdapter.notifyDataSetChanged();
                    PhotoLayout.notifyDataSetChanged();
                }
            default:
                Log.d("DFCarChecker", "拍摄故障！！");
                break;
        }
    }

    /**
     * 生成photoEntity，并添加到对应的列表
     */
    private void addPhotoToList() {
        PhotoEntity photoEntity = generatePhotoEntity(paintView.getPosEntity());

        paintView.getPhotoEntities().add(photoEntity);

        // 此处为index，使用size时要-1
        ListedPhoto listedPhoto = new ListedPhoto(paintView.getPosEntities().size() - 1,
                photoEntity, paintView.getPosEntity().getType());

        adapter.add(listedPhoto);
        adapter.notifyDataSetChanged();
    }

    /**
     * 完成时，将photoEntity都添加到照片list，并更新list
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
     * 生成photoEntity
     * @param posEntity
     * @return
     */
    private PhotoEntity generatePhotoEntity(PosEntity posEntity) {

        int startX, startY, endX, endY;
        int radius = 0;

        // 获取绘图父类实体
        paintView = (PaintView)map.get(currentPaintView);

        startX = posEntity.getStartX();
        startY = posEntity.getStartY();
        endX = posEntity.getEndX();
        endY = posEntity.getEndY();

        // 如果是“变形”，即圆
        if(posEntity.getType() == 3 || posEntity.getType() == 7) {
            // 计算半径
            int dx = Math.abs(endX - startX);
            int dy = Math.abs(endY- startY);
            int dr = (int)Math.sqrt(dx * dx + dy * dy);

            // 计算圆心
            int x0 = (startX + endX) / 2;
            int y0 = (startY + endY) / 2;

            startX = x0;
            startY = y0;
            endX = endY = 0;
            radius = dr / 2;
        }

        // 组建PhotoEntity
        PhotoEntity photoEntity = new PhotoEntity();

        photoEntity.setName(paintView.getTypeName());
        photoEntity.setFileName(posEntity.getImageFileName());
        if(photoEntity.getFileName().equals("")) {
            photoEntity.setThumbFileName("");
        } else {
            photoEntity.setThumbFileName(posEntity.getImageFileName().substring(0, posEntity.getImageFileName().length() - 4) + "_t.jpg");
        }
        photoEntity.setComment(posEntity.getComment());
        photoEntity.setIndex(PhotoLayout.photoIndex++);

        // 如果是走了这段代码，则一定是添加照片
        // 如果是修改模式，则Action就是add
        if(CarCheckActivity.isModify()) {
            photoEntity.setModifyAction(Action.ADD);
        } else {
            photoEntity.setModifyAction(Action.MODIFY);
        }

        // 组织JsonString
        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoData = new JSONObject();

            jsonObject.put("Group", paintView.getGroup());
            jsonObject.put("Part", "fault");

            photoData.put("type", posEntity.getType());
            photoData.put("startX", startX);
            photoData.put("startY", startY);
            photoData.put("endX", endX);
            photoData.put("endY", endY);
            photoData.put("width", getBitmapWidth(AppCommon.photoDirectory, posEntity.getImageFileName()));
            photoData.put("height", getBitmapHeight(AppCommon.photoDirectory, posEntity.getImageFileName()));
            photoData.put("radius", radius);
            photoData.put("comment", posEntity.getComment());

            jsonObject.put("PhotoData", photoData);
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("UserId", UserInfo.getInstance().getId());
            jsonObject.put("Key", UserInfo.getInstance().getKey());
            jsonObject.put("Action", photoEntity.getModifyAction());
            jsonObject.put("Index", photoEntity.getIndex());
        } catch (Exception e) {
            Log.d("DFCarChecker", "Json组织错误：" + e.getMessage());
        }

        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }

    /**
     * 根据不同的检测步骤和figure，返回不同的图片
     * @param figure
     * @param step
     * @return
     */
    private Bitmap getBitmapFromFigure(int figure, String step) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        String name = "";

        if(step.equals("EX")) {
            // 外观图
            switch (figure) {
                case 0:
                case 1:
                    name = "r3d4";
                    break;
                case 2:
                    name = "r3d2";
                    break;
                case 3:
                    name = "r2d2";
                    break;
                case 4:
                    name = "r2d4";
                    break;
                case 5:
                    name = "van_o";
                    break;
            }
        } else {
            // 内饰图
            switch (figure) {
                case 0:
                case 1:
                    name = "d4s4";
                    break;
                case 2:
                    name = "d2s4";
                    break;
                case 3:
                    name = "d2s4";
                    break;
                case 4:
                    name = "d4s4";
                    break;
                case 5:
                    name = "van_i";
                    break;
            }
        }

        return BitmapFactory.decodeFile(AppCommon.utilDirectory + name, options);
    }

    @Override
    public void onBackPressed() {
        alertUser(R.string.cancel_confirm);
    }

}
