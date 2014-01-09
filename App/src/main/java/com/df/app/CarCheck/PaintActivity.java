package com.df.app.CarCheck;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.paintview.ExteriorPaintView;
import com.df.app.paintview.InteriorPaintView;
import com.df.app.paintview.PaintView;
import com.df.app.util.Common;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.df.app.util.Helper.setPhotoSize;


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

    // 当用户退出时，进行的选择
    boolean choise = false;

    // 标志是否修改过
    boolean modified = false;

    public enum PaintType {
        FRAME_PAINT, EX_PAINT, IN_PAINT, NOVALUE;

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
        int figure = Integer.parseInt(BasicInfoLayout.mCarSettings.getFigure());
        Bitmap bitmap = getBitmapFromFigure(figure, "IN");

        // 初始化绘图View
        interiorPaintView = (InteriorPaintView) findViewById(R.id.interior_paint_view);
        interiorPaintView.init(bitmap, InteriorLayout.posEntities);
        interiorPaintView.setType(Common.DIRTY);

        // 根据CarSettings的figure设定图片
        figure = Integer.parseInt(BasicInfoLayout.mCarSettings.getFigure());
        bitmap = getBitmapFromFigure(figure, "EX");

        // 初始化绘图View
        exteriorPaintView = (ExteriorPaintView) findViewById(R.id.exterior_paint_view);
        exteriorPaintView.init(bitmap, ExteriorLayout.posEntities);
        exteriorPaintView.setType(Common.COLOR_DIFF);

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


        //sketchPhotoEntities = CarCheckBasicInfoFragment.sketchPhotoEntities;

        map.put("EX_PAINT", exteriorPaintView);
        map.put("IN_PAINT", interiorPaintView);

        paintView = (PaintView)map.get(currentPaintView);

        Button undoButton = (Button)findViewById(R.id.undo);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintView.undo();
            }
        });

        Button redoButton = (Button)findViewById(R.id.redo);
        redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintView.redo();
            }
        });

        Button clearButton = (Button)findViewById(R.id.clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertUser(R.string.clear_confirm);
            }
        });

        Button cancelButton = (Button)findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertUser(R.string.cancel_confirm);
            }
        });

        Button doneButton = (Button)findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generatePhotoEntities();
                finish();
            }
        });
    }

    // 设置为外观布局
    private void setExPaintLayout() {
        interiorPaintView.setVisibility(View.GONE);
        exteriorPaintView.setVisibility(View.VISIBLE);

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

        radioGroup.addView(colorDiffRadio, 0);
        radioGroup.addView(scratchRadio, 1);
        radioGroup.addView(transRadio, 2);
        radioGroup.addView(scrapeRadio, 3);
        radioGroup.addView(otherRadio, 4);
        radioGroup.setOrientation(LinearLayout.HORIZONTAL);
        radioGroup.setLayoutParams(new RadioGroup.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        radioGroup.check(colorDiffRadio.getId());

        LinearLayout paintMenu = new LinearLayout(this);
        paintMenu.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        paintMenu.addView(radioGroup);

        LinearLayout root = (LinearLayout) findViewById(R.id.root);
        root.addView(paintMenu);
    }

    // 设置为内饰图布局
    private void setInPaintLayout() {
        interiorPaintView.setVisibility(View.VISIBLE);
        exteriorPaintView.setVisibility(View.GONE);

        // 选择当前绘图类型
        RadioGroup radioGroup = new RadioGroup(this);

        RadioButton dirtyRadio = new RadioButton(this);
        dirtyRadio.setText(R.string.interior_dirty);
        dirtyRadio.setButtonDrawable(R.drawable.radio_button);
        Drawable img = getResources().getDrawable( R.drawable.in_dirty);
        dirtyRadio.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        dirtyRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                currentType = b ? Common.DIRTY : Common.BROKEN;

                interiorPaintView.setType(currentType);
            }
        });

        RadioButton brokenRadio = new RadioButton(this);
        brokenRadio.setText(R.string.interior_broken);
        brokenRadio.setButtonDrawable(R.drawable.radio_button);
        img = getResources().getDrawable( R.drawable.in_broken);
        brokenRadio.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);

        radioGroup.addView(dirtyRadio);
        radioGroup.addView(brokenRadio);
        radioGroup.setOrientation(LinearLayout.HORIZONTAL);
        radioGroup.check(dirtyRadio.getId());

        LinearLayout paintMenu = new LinearLayout(this);
        paintMenu.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        paintMenu.addView(radioGroup);

        LinearLayout root = (LinearLayout) findViewById(R.id.root);
        root.addView(paintMenu);
    }


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        paintView = (PaintView)map.get(currentPaintView);
//
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                // 用户确认返回上一层
//                alertUser(R.string.out_cancel_confirm);
//                return true;
//            case R.id.action_done:
//                // 提交数据
//                captureResultImage();
//                break;
//            case R.id.action_cancel:
//                // 用户确认放弃更改
//                alertUser(R.string.out_cancel_confirm);
//                break;
//            case R.id.action_clear:
//                // 用户确认清除数据
//                alertUser(R.string.out_clear_confirm);
//                modified = true;
//                break;
//            case R.id.action_undo:
//                // 回退
//                paintView.undo();
//                modified = true;
//                break;
//            case R.id.action_redo:
//                // 重做
//                paintView.redo();
//                modified = true;
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    // 提醒用户
    private void alertUser(final int msgId) {
        paintView = (PaintView)map.get(currentPaintView);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.alert);
        builder.setMessage(msgId);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(msgId == R.string.cancel_confirm) {
                    // 退出
                    paintView.cancel();
                    finish();
                } else if(msgId == R.string.clear_confirm) {
                    paintView.clear();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 获取绘图父类实体
        paintView = (PaintView)map.get(currentPaintView);

        switch (resultCode) {
            case Activity.RESULT_OK:
                // 对拍摄的照片做缩小化处理
                setPhotoSize(paintView.getPosEntity().getImageFileName(), 800);
                break;
            case Activity.RESULT_CANCELED:
                // 如果取消了拍摄，将照片名称置空
                paintView.getPosEntity().setImageFileName("");
                break;
            default:
                Log.d("DFCarChecker", "拍摄故障！！");
                break;
        }
    }

    private void generatePhotoEntities() {
        int startX, startY, endX, endY;
        int radius = 0;

        // 获取绘图父类实体
        paintView = (PaintView)map.get(currentPaintView);

        // 照片集合
        List<PhotoEntity> photoEntities = paintView.getPhotoEntities();

        photoEntities.clear();

        List<PosEntity> posEntities = paintView.getPosEntities();

        for(int i = 0; i < posEntities.size(); i++) {
            // 获取坐标
            PosEntity posEntity = posEntities.get(i);

            startX = posEntity.getStartX();
            startY = posEntity.getStartY();
            endX = posEntity.getEndX();
            endY = posEntity.getEndY();

            // 如果是“变形”，即圆
            if(posEntity.getType() == 3) {
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

            // 组织JsonString
            JSONObject jsonObject = new JSONObject();

            try {
                JSONObject photoJsonObject = new JSONObject();

                jsonObject.put("Group", paintView.getGroup());
                jsonObject.put("Part", "fault");

                photoJsonObject.put("type", posEntity.getType());
                photoJsonObject.put("startX", startX);
                photoJsonObject.put("startY", startY);
                photoJsonObject.put("endX", endX);
                photoJsonObject.put("endY", endY);
                photoJsonObject.put("radius", radius);

                jsonObject.put("PhotoData", photoJsonObject);
                jsonObject.put("UniqueId", BasicInfoLayout.carId);
                jsonObject.put("UserId", MainActivity.userInfo.getId());
                jsonObject.put("Key", MainActivity.userInfo.getKey());
            } catch (Exception e) {
                Log.d("DFCarChecker", "Json组织错误：" + e.getMessage());
            }

            // 组建PhotoEntity
            PhotoEntity photoEntity = new PhotoEntity();

            String fileName = (posEntity.getImageFileName() == null) ? "" : posEntity
                    .getImageFileName();

            photoEntity.setFileName(fileName);
            photoEntity.setName(paintView.getGroup());
            photoEntity.setJsonString(jsonObject.toString());

            // 暂时不加入照片池，只放入各自的List，等保存时再提交
            photoEntities.add(photoEntity);
        }
    }


    // 根据不同的检测步骤和figure，返回不同的图片
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

        return BitmapFactory.decodeFile(Common.utilDirectory + name, options);
    }

    @Override
    public void onBackPressed() {
        alertUser(R.string.cancel_confirm);
    }

}
