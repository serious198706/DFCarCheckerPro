package com.df.app.carCheck;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.Action;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.customCamera.IPhotoProcessListener;
import com.df.app.service.customCamera.PhotoProcessManager;
import com.df.app.service.customCamera.PhotoTask;
import com.df.app.util.MyScrollView;
import com.df.app.util.Common;
import com.df.app.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.df.app.util.Helper.findAllViews;
import static com.df.app.util.Helper.generatePhotoThumbnail;
import static com.df.app.util.Helper.getBitmapHeight;
import static com.df.app.util.Helper.getBitmapWidth;
import static com.df.app.util.Helper.getEditViewText;
import static com.df.app.util.Helper.getSpinnerSelectedText;
import static com.df.app.util.Helper.setEditError;
import static com.df.app.util.Helper.setEditViewText;
import static com.df.app.util.Helper.setSpinnerSelectionWithString;
import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 13-12-25.
 *
 * 综合检查二，主要包括泡水检查和轮胎检查
 */
public class Integrated2Layout extends LinearLayout implements IPhotoProcessListener {
    private static View rootView;

    long currentTimeMillis;

    // 正在拍摄的轮胎
    private String currentTire;
    private String currentTireName;

    public static int[] photoShotCount = {0, 0, 0, 0, 0};

    private int[] spinnerIds = {
            R.id.cigarLighter_spinner,
            R.id.seatBelts_spinner,
            R.id.ashtray_spinner,
            R.id.rearSeats_spinner,
            R.id.spareTireGroove_spinner,
            R.id.trunkCorner_spinner,
            R.id.audioHorn_spinner,
            R.id.seatSlide_spinner,
            R.id.ecu_spinner,
            R.id.roof_spinner,
            R.id.discBox_spinner,
            R.id.patternMatch_spinner,
            R.id.formatMatch_spinner};

    private static Map<String, Integer> tireMap;
    static {
        tireMap = new HashMap<String, Integer>();
        tireMap.put("leftFront", 0);
        tireMap.put("rightFront", 1);
        tireMap.put("leftRear", 2);
        tireMap.put("rightRear", 3);
        tireMap.put("spare", 4);
    }

    private String[] map = {"leftFront", "rightFront", "leftRear", "rightRear", "spare"};

    private static Button leftFrontButton;
    private static Button rightFrontButton;
    private static Button leftRearButton;
    private static Button rightRearButton;
    private static Button spareButton;

    public static Button[] buttons = {leftFrontButton, rightFrontButton, leftRearButton, rightRearButton, spareButton};

    private MyScrollView scrollView;

    private enum PaintType {
        leftFront, rightFront, leftRear, rightRear, spare, NOVALUE;

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

    public static int leftFrontIndex;
    public static int rightFrontIndex;
    public static int leftRearIndex;
    public static int rightRearIndex;
    public static int spareIndex;

    private EditText leftFrontEdit = (EditText)findViewById(R.id.leftFront_edit);
    private EditText rightFrontEdit = (EditText)findViewById(R.id.rightFront_edit);
    private EditText leftRearEdit = (EditText)findViewById(R.id.leftRear_edit);
    private EditText rightRearEdit = (EditText)findViewById(R.id.rightRear_edit);

    public static Map<String, PhotoEntity> photoEntityMap;

    TextWatcher textWatcher;

    public Integrated2Layout(Context context) {
        super(context);
        init(context);
    }

    public Integrated2Layout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Integrated2Layout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.integrated2_layout, this);

        photoEntityMap = new HashMap<String, PhotoEntity>();

        ImageView tireImage = (ImageView)findViewById(R.id.tire_image);

        Bitmap bitmap = BitmapFactory.decodeFile(Common.utilDirectory + "r3d4");
        tireImage.setImageBitmap(bitmap);

        scrollView = (MyScrollView)findViewById(R.id.root);
        scrollView.setListener(new MyScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {
                if (scrollView.getScrollY() > 5) {
                    showShadow(true);
                } else {
                    showShadow(false);
                }
            }
        });

        for (int spinnerId : spinnerIds) {
            setSpinnerColor(spinnerId);
        }

        Spinner spareSpinner = (Spinner)findViewById(R.id.spareTire_spinner);
        spareSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                findViewById(R.id.spare_edit).setVisibility(i == 0 ? View.VISIBLE : View.INVISIBLE);
                findViewById(R.id.spare_button).setVisibility(i == 0 ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        leftFrontButton = (Button)findViewById(R.id.leftFront_button);
        leftFrontButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTire = "leftFront";
                currentTireName = "左前轮";
                takePhotoForTires();
                //System.out.println(v.getResources().getResourceName(v.getId()));
            }
        });
        buttons[0] = leftFrontButton;

        rightFrontButton = (Button)findViewById(R.id.rightFront_button);
        rightFrontButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTire = "rightFront";
                currentTireName = "右前轮";
                takePhotoForTires();
            }
        });
        buttons[1] = rightFrontButton;

        leftRearButton = (Button)findViewById(R.id.leftRear_button);
        leftRearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTire = "leftRear";
                currentTireName = "左后轮";
                takePhotoForTires();
            }
        });
        buttons[2] = leftRearButton;

        rightRearButton = (Button)findViewById(R.id.rightRear_button);
        rightRearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTire = "rightRear";
                currentTireName = "右后轮";
                takePhotoForTires();
            }
        });
        buttons[3] = rightRearButton;

        spareButton = (Button)findViewById(R.id.spare_button);
        spareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTire = "spare";
                currentTireName = "备胎轮";
                takePhotoForTires();
            }
        });
        buttons[4] = spareButton;

        // 移除输入框的焦点，避免每次输入完成后界面滚动
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        scrollView.setFocusable(true);
        scrollView.setFocusableInTouchMode(true);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });

        CheckBox checkBox = (CheckBox)findViewById(R.id.numbersSame);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editsAssociation(b);
            }
        });

        leftFrontEdit = (EditText)findViewById(R.id.leftFront_edit);
        rightFrontEdit = (EditText)findViewById(R.id.rightFront_edit);
        leftRearEdit = (EditText)findViewById(R.id.leftRear_edit);
        rightRearEdit = (EditText)findViewById(R.id.rightRear_edit);

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                rightFrontEdit.setText(charSequence);
                leftRearEdit.setText(charSequence);
                rightRearEdit.setText(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    /**
     * 对某一轮胎进行拍照
     */
    private void takePhotoForTires() {
        PhotoProcessManager.getInstance().registPhotoProcessListener(this);

        // 如果此轮胎已经有照片，则询问用户是否要替换
        if(photoShotCount[tireMap.get(currentTire)] >= 1) {
            View view1 = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.popup_layout, null);
            TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
            final TextView content = new TextView(view1.getContext());
            content.setText(R.string.tireReplace);
            content.setTextSize(20f);
            contentArea.addView(content);

            setTextView(view1, R.id.title, getResources().getString(R.string.alert));

            AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                    .setView(view1)
                            // 要替换，就启动相机
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(photoEntityMap.get(currentTire) != null) {
                                if(photoEntityMap.get(currentTire).getFileName().contains("http:")) {
                                    currentTimeMillis = System.currentTimeMillis();

                                    Helper.startCamera(getContext(), currentTireName, currentTimeMillis);

                                    //startCamera(tire, Long.toString(currentTimeMillis) + ".jpg");
                                } else {
                                    //startCamera(tire, photoEntityMap.get(currentTire).getFileName());
                                    String fileName = photoEntityMap.get(currentTire).getFileName();
                                    fileName = fileName.substring(0, fileName.length() - 4);

                                    Helper.startCamera(getContext(), currentTireName, Long.parseLong(fileName));
                                }
                            }
                        }
                    })
                            // 不替换
                    .setNegativeButton(R.string.cancel, null)
                    .create();

            dialog.show();
        } else {
            currentTimeMillis = System.currentTimeMillis();
            Helper.startCamera(getContext(), currentTireName, currentTimeMillis);
        }
    }

    @Override
    public void onPhotoProcessFinish(List<PhotoTask> list) {
        if(list == null) {
            return;
        }

        for(PhotoTask photoTask : list) {
            if(photoTask.getState() == PhotoTask.STATE_COMPLETE) {
                saveTirePhoto();
            }
        }
    }

    /**
     * 拍摄完毕并确认后，保存、处理照片并生成photoEntity
     */
    public void saveTirePhoto() {
        // 如果此轮胎已经有照片，则替换之
        if(photoShotCount[tireMap.get(currentTire)] >= 1) {
            // 如果是含有http的文件名（表示为修改模式）
            if(photoEntityMap.get(currentTire).getFileName().contains("http:")) {
                Helper.handlePhoto(Long.toString(currentTimeMillis));

                PhotoEntity photoEntity = PhotoExteriorLayout.photoListAdapter.getItem(photoEntityMap.get(currentTire));
                PhotoEntity temp = generatePhotoEntity();
                photoEntity.setFileName(temp.getFileName());
                photoEntity.setThumbFileName(temp.getThumbFileName());
                photoEntity.setModifyAction(temp.getModifyAction());
                photoEntity.setJsonString(temp.getJsonString());

                if(photoEntityMap.containsKey(currentTire))
                    photoEntityMap.remove(currentTire);
                else
                    photoEntityMap.put(currentTire, temp);

                PhotoExteriorLayout.photoListAdapter.notifyDataSetChanged();
            } else {
                PhotoEntity temp = photoEntityMap.get(currentTire);

                Helper.setPhotoSize(temp.getFileName(), Common.PHOTO_WIDTH);
                Helper.generatePhotoThumbnail(temp.getFileName(), Common.THUMBNAIL_WIDTH);

                PhotoExteriorLayout.photoListAdapter.notifyDataSetChanged();
            }
        }
        // 如果此轮胎没有照片，则生成新的照片
        else {
            Helper.handlePhoto(currentTimeMillis + ".jpg");

            PhotoEntity photoEntity = generatePhotoEntity();

            if(photoEntityMap.containsKey(currentTire))
                photoEntityMap.remove(currentTire);
            else
                photoEntityMap.put(currentTire, photoEntity);

            PhotoExteriorLayout.photoListAdapter.addItem(photoEntity);
            PhotoExteriorLayout.photoListAdapter.notifyDataSetChanged();
            photoShotCount[tireMap.get(currentTire)] = 1;

            // 将拍完照片的轮胎按钮换个图标
            buttons[tireMap.get(currentTire)].setBackgroundResource(R.drawable.tire_pressed);
        }
    }

    /**
     * 生成photoEntity
     */
    private PhotoEntity generatePhotoEntity() {
        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName(Long.toString(currentTimeMillis) + ".jpg");
        photoEntity.setThumbFileName(Long.toString(currentTimeMillis) + "_t.jpg");
        photoEntity.setName(currentTireName);

        // 如果是修改模式，则Action就是modify（每个轮胎照只有一张）
        photoEntity.setModifyAction(Action.MODIFY);

        // 组织JsonString
        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();

            Button button;
            switch(PaintType.paintType(currentTire)) {
                case leftFront:
                    button = leftFrontButton;
                    photoEntity.setIndex(CarCheckActivity.isModify() ? leftFrontIndex : PhotoLayout.photoIndex++);
                    break;
                case rightFront:
                    button = rightFrontButton;
                    photoEntity.setIndex(CarCheckActivity.isModify() ? rightFrontIndex : PhotoLayout.photoIndex++);
                    break;
                case leftRear:
                    button = leftRearButton;
                    photoEntity.setIndex(CarCheckActivity.isModify() ? leftRearIndex : PhotoLayout.photoIndex++);
                    break;
                case rightRear:
                    button = rightRearButton;
                    photoEntity.setIndex(CarCheckActivity.isModify() ? rightRearIndex : PhotoLayout.photoIndex++);
                    break;
                case spare:
                    button = spareButton;
                    photoEntity.setIndex(CarCheckActivity.isModify() ? spareIndex : PhotoLayout.photoIndex++);
                    break;
                default:
                    button = null;
                    break;
            }

            photoJsonObject.put("x", (button.getX() - 280) / 0.6 + button.getWidth() / 2);
            photoJsonObject.put("y", button.getY() / 0.6 + button.getHeight() / 2);
            photoJsonObject.put("width", getBitmapWidth(Long.toString(currentTimeMillis) + ".jpg"));
            photoJsonObject.put("height", getBitmapHeight(Long.toString(currentTimeMillis) + ".jpg"));

            jsonObject.put("Group", "tire");
            jsonObject.put("Part", currentTire);
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("Action", photoEntity.getModifyAction());
            jsonObject.put("Index", photoEntity.getIndex());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }

    /**
     * 获取备胎的选择项
     */
    public static String getSpareTireSelection() {
        return getSpinnerSelectedText(rootView, R.id.spareTire_spinner);
    }

    /**
     * 设置备胎的选择项
     */
    public static void setSpareTireSelection(String selection) {
        setSpinnerSelectionWithString(rootView, R.id.spareTire_spinner, selection);
    }

    /**
     * 设置spinner选择“无”之后的颜色
     */
    private static void setSpinnerColor(int spinnerId) {
        Spinner spinner = (Spinner) rootView.findViewById(spinnerId);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i >= 1)
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.RED);
                else
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);

                // 当选择项为“无”时，还应为黑色字体
                if (adapterView.getSelectedItem().toString().equals("无")) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * 关联四个输入框，在其中1个输入时，更改另外3个的内容
     */
    private void editsAssociation(boolean association) {
        if(!association) {
            leftFrontEdit.removeTextChangedListener(textWatcher);
        } else {
            leftFrontEdit.addTextChangedListener(textWatcher);
        }
    }

    /**
     * 生成泡水检查的JSONObject
     */
    public JSONObject generateFloodedJSONObject() throws JSONException {
        JSONObject flooded = new JSONObject();

        TableLayout tableLayout = (TableLayout)findViewById(R.id.floodTable);
        List<View> views = new ArrayList<View>();
        findAllViews(tableLayout, views, Spinner.class);

        for(View view : views) {
            if(view instanceof Spinner) {
                flooded.put((String)view.getTag(), getSpinnerSelectedText(rootView, view.getId()));
            }
        }

        return flooded;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     */
    private void fillFloodWithJSONObject(JSONObject flooded) throws JSONException {
        TableLayout tableLayout = (TableLayout)findViewById(R.id.floodTable);

        List<View> views = new ArrayList<View>();
        findAllViews(tableLayout, views, Spinner.class);

        for(View view : views) {
            if(view instanceof Spinner) {
                setSpinnerSelectionWithString(rootView, view.getId(), flooded.getString((String)view.getTag()));
            }
        }
    }

    /**
     * 生成轮胎的JSONObject
     */
    public JSONObject generateTiresJSONObject() throws JSONException{
        JSONObject tires = new JSONObject();

        tires.put("leftFront", getEditViewText(rootView, R.id.leftFront_edit));
        tires.put("rightFront", getEditViewText(rootView, R.id.rightFront_edit));
        tires.put("leftRear", getEditViewText(rootView, R.id.leftRear_edit));
        tires.put("rightRear", getEditViewText(rootView, R.id.rightRear_edit));

        if(getSpinnerSelectedText(rootView, R.id.spareTire_spinner).equals("无")) {
            tires.put("spare", "");
        } else {
            tires.put("spare", getEditViewText(rootView, R.id.spare_edit));
        }

        tires.put("formatMatch", getSpinnerSelectedText(rootView, R.id.formatMatch_spinner));
        tires.put("patternMatch", getSpinnerSelectedText(rootView, R.id.patternMatch_spinner));

        return tires;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     */
    private void fillTiresWithJSONObject(JSONObject tires) throws JSONException{
        setEditViewText(rootView, R.id.leftFront_edit, tires.get("leftFront") == JSONObject.NULL ? "" : tires.getString("leftFront"));
        setEditViewText(rootView, R.id.rightFront_edit, tires.get("rightFront") == JSONObject.NULL ? "" : tires.getString("rightFront"));
        setEditViewText(rootView, R.id.leftRear_edit, tires.get("leftRear") == JSONObject.NULL ? "" : tires.getString("leftRear"));
        setEditViewText(rootView, R.id.rightRear_edit, tires.get("rightRear") == JSONObject.NULL ? "" : tires.getString("rightRear"));
        setEditViewText(rootView, R.id.spare_edit, tires.get("spare") == JSONObject.NULL ? "" : tires.getString("spare"));
        setSpinnerSelectionWithString(rootView, R.id.formatMatch_spinner, tires.getString("formatMatch"));
        setSpinnerSelectionWithString(rootView, R.id.patternMatch_spinner, tires.getString("patternMatch"));
    }

    /**
     * 生成综合二备注的JSONObject
     */
    public String generateCommentString() {
        return getEditViewText(rootView, R.id.it2_comment_edit);
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     */
    private void fillCommentWithString(String comment) {
        setEditViewText(rootView, R.id.it2_comment_edit, comment);
    }

    /**
     * 生成轮胎的草图
     */
    public PhotoEntity generateSketch() {
        ImageView imageView = (ImageView)findViewById(R.id.tire_image);

        Bitmap bitmap = null;

        try {
            bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
            FileOutputStream out = new FileOutputStream(Common.photoDirectory + "tire_sketch");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName("tire_sketch");
        photoEntity.setIndex(PhotoLayout.photoIndex++);

        // 不需要修改
        photoEntity.setModifyAction(Action.NORMAL);

        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();

            photoJsonObject.put("width", bitmap.getWidth());
            photoJsonObject.put("height", bitmap.getHeight());

            jsonObject.put("Group", "tire");
            jsonObject.put("Part", "sketch");
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("Action", photoEntity.getModifyAction());
            jsonObject.put("Index", photoEntity.getIndex());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     */
    public void fillInData(JSONObject flooded, JSONObject tires, String comment2) throws JSONException {
        fillFloodWithJSONObject(flooded);
        fillTiresWithJSONObject(tires);
        fillCommentWithString(comment2);
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     */
    public void fillInData(JSONObject flooded, JSONObject tires, JSONObject photos, String comment2) throws JSONException {
        fillInData(flooded, tires, comment2);

        JSONObject tiresPhoto = photos.getJSONObject("tire");
        JSONObject sketch = tiresPhoto.getJSONObject("sketch");
        int index = sketch.getInt("index");

        if(index >= PhotoLayout.photoIndex) {
            PhotoLayout.photoIndex = index + 1;
        }
    }

    /**
     * 检查所有域
     */
    public String checkAllFields() {
        int count;

        // 如果有备胎，则要检查备胎的图片
        if(getSpinnerSelectedText(rootView, R.id.spareTire_spinner).equals("有")) {
            count = photoShotCount.length;
        } else {
            count = photoShotCount.length - 1;
        }

        for(int i = 0; i < count; i++) {
            if(photoShotCount[i] == 0) {
                return map[i];
            }
        }

        if(getEditViewText(rootView, R.id.leftFront_edit).length() > 0 &&
                getEditViewText(rootView, R.id.leftFront_edit).length() < 4) {
            setEditError(rootView, R.id.leftFront_edit, "轮胎标号格式不正确");
            return "edits";
        }

        if(getEditViewText(rootView, R.id.rightFront_edit).length() > 0 &&
                getEditViewText(rootView, R.id.rightFront_edit).length() < 4) {
            setEditError(rootView, R.id.rightFront_edit, "轮胎标号格式不正确");
            return "edits";
        }

        if(getEditViewText(rootView, R.id.leftRear_edit).length() > 0 &&
                getEditViewText(rootView, R.id.leftRear_edit).length() < 4) {
            setEditError(rootView, R.id.leftRear_edit, "轮胎标号格式不正确");
            return "edits";
        }

        if(getEditViewText(rootView, R.id.rightRear_edit).length() > 0 &&
                getEditViewText(rootView, R.id.rightRear_edit).length() < 4) {
            setEditError(rootView, R.id.rightRear_edit, "轮胎标号格式不正确");
            return "edits";
        }

        if(getEditViewText(rootView, R.id.spare_edit).length() > 0 &&
                getEditViewText(rootView, R.id.spare_edit).length() < 4 &&
                getSpinnerSelectedText(rootView, R.id.spareTire_spinner).equals("有")) {
            setEditError(rootView, R.id.spare_edit, "轮胎标号格式不正确");
            return "edits";
        }

        return "";
    }

    /**
     * 当未拍摄轮胎照片时，定位到轮胎部分
     */
    public void locateTirePart() {
        final EditText editText = (EditText)findViewById(R.id.spare_edit);
        editText.setFocusable(true);
        editText.requestFocus();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, editText.getBottom());
            }
        });
    }
}
