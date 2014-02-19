package com.df.app.CarCheck;

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
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.MyScrollView;
import com.df.app.util.Common;
import com.df.app.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.df.app.util.Helper.getEditViewText;
import static com.df.app.util.Helper.getSpinnerSelectedText;
import static com.df.app.util.Helper.setEditViewText;
import static com.df.app.util.Helper.setSpinnerSelectionWithString;
import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 13-12-25.
 */
public class Integrated2Layout extends LinearLayout {
    private static View rootView;

    long currentTimeMillis;

    // 正在拍摄的轮胎
    private String currentTire;

    private int[] photoShotCount = {0, 0, 0, 0, 0};

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
            R.id.backCorner_spinner,
            R.id.discBox_spinner,
            R.id.storageCorner_spinner,
            R.id.newFuse_spinner,
            R.id.fuse_spinner,
            R.id.engineRoom_spinner};

    private static Map<String, Integer> tireMap;
    static {
        tireMap = new HashMap<String, Integer>();
        tireMap.put("leftFront", 0);
        tireMap.put("rightFront", 1);
        tireMap.put("leftRear", 2);
        tireMap.put("rightRear", 3);
        tireMap.put("spare", 4);
    }

    private Button leftFrontButton;
    private Button rightFrontButton;
    private Button leftRearButton;
    private Button rightRearButton;
    private Button spareButton;

    private MyScrollView scrollView;

    public enum PaintType {
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

    private Map<String, PhotoEntity> photoEntityMap;

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

        for(int i = 0; i < spinnerIds.length; i++) {
            setSpinnerColor(spinnerIds[i], Color.RED);
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
                takePhotoForTires("左前");
            }
        });

        rightFrontButton = (Button)findViewById(R.id.rightFront_button);
        rightFrontButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTire = "rightFront";
                takePhotoForTires("右前");
            }
        });

        leftRearButton = (Button)findViewById(R.id.leftRear_button);
        leftRearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTire = "leftRear";
                takePhotoForTires("左后");
            }
        });

        rightRearButton = (Button)findViewById(R.id.rightRear_button);
        rightRearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTire = "rightRear";
                takePhotoForTires("右后");
            }
        });

        spareButton = (Button)findViewById(R.id.spare_button);
        spareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTire = "spare";
                takePhotoForTires("备胎");
            }
        });

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
    }

    private void takePhotoForTires(String tire) {
        Toast.makeText(rootView.getContext(), "正在拍摄" + tire + "轮", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        currentTimeMillis = System.currentTimeMillis();
        Uri fileUri = Helper.getOutputMediaFileUri(Long.toString(currentTimeMillis) + ".jpg"); //
        // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        ((Activity)getContext()).startActivityForResult(intent, Common.PHOTO_FOR_TIRES);
    }

    public void saveTirePhoto() {
        Helper.setPhotoSize(Long.toString(currentTimeMillis) + ".jpg", 800);
        Helper.generatePhotoThumbnail(Long.toString(currentTimeMillis) + ".jpg", 400);

        PhotoEntity photoEntity = generatePhotoEntity();

        // 如果此轮胎已经有照片，则替换之
        if(photoShotCount[tireMap.get(currentTire)] == 1) {
            View view1 = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.popup_layout, null);
            TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
            TextView content = new TextView(view1.getContext());
            content.setText(R.string.tireReplace);
            content.setTextSize(20f);
            contentArea.addView(content);

            setTextView(view1, R.id.title, getResources().getString(R.string.alert));

            AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                    .setView(view1)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // 确定替换，将之前的全部清除，再重新添加一遍
                            PhotoExteriorLayout.photoListAdapter.clear();

                            for(PhotoEntity photoEntity1 : photoEntityMap.values()) {
                                PhotoExteriorLayout.photoListAdapter.addItem(photoEntity1);
                            }

                            PhotoExteriorLayout.photoListAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // 不替换，则无视这张图片
                        }
                    })
                    .create();

            dialog.show();
        } else {
            if(photoEntityMap.containsKey(currentTire))
                photoEntityMap.remove(currentTire);
            else
                photoEntityMap.put(currentTire, photoEntity);

            PhotoExteriorLayout.photoListAdapter.addItem(photoEntity);
            PhotoExteriorLayout.photoListAdapter.notifyDataSetChanged();
            photoShotCount[tireMap.get(currentTire)]++;
        }
    }

    private PhotoEntity generatePhotoEntity() {
        // 组织JsonString
        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();

            Button button;
            switch(PaintType.paintType(currentTire)) {
                case leftFront:
                    button = leftFrontButton;
                    break;
                case rightFront:
                    button = rightFrontButton;
                    break;
                case leftRear:
                    button = leftRearButton;
                    break;
                case rightRear:
                    button = rightRearButton;
                    break;
                case spare:
                    button = spareButton;
                    break;
                default:
                    button = null;
                    break;
            }

            photoJsonObject.put("x", button.getX());
            photoJsonObject.put("y", button.getY());

            jsonObject.put("Group", "tire");
            jsonObject.put("Part", currentTire);
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
        } catch (JSONException e) {

        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName(Long.toString(currentTimeMillis) + ".jpg");
        photoEntity.setThumbFileName(Long.toString(currentTimeMillis) + "_t.jpg");
        photoEntity.setJsonString(jsonObject.toString());
        photoEntity.setName(currentTire);

        return photoEntity;
    }

    public static String getSpareTireSelection() {
        return getSpinnerSelectedText(rootView, R.id.spareTire_spinner);
    }

    public static void setSpareTireSelection(String selction) {
        setSpinnerSelectionWithString(rootView, R.id.spareTire_spinner, selction);
    }

    private static void setSpinnerColor(int spinnerId, int color) {
        Spinner spinner = (Spinner) rootView.findViewById(spinnerId);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i >= 1)
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.RED);
                else
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);

                // 当选择项为“无”时，还应为黑色字体
                if(adapterView.getSelectedItem().toString().equals("无")) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public JSONObject generateFloodedJSONObject() throws JSONException {
        JSONObject flooded = new JSONObject();

        flooded.put("cigarLighter", getSpinnerSelectedText(rootView, R.id.cigarLighter_spinner));
        flooded.put("seatBelts", getSpinnerSelectedText(rootView, R.id.seatBelts_spinner));
        flooded.put("ashtray", getSpinnerSelectedText(rootView, R.id.ashtray_spinner));
        flooded.put("rearSeats", getSpinnerSelectedText(rootView, R.id.rearSeats_spinner));
        flooded.put("spareTireGroove", getSpinnerSelectedText(rootView, R.id.spareTireGroove_spinner));
        flooded.put("trunkCorner", getSpinnerSelectedText(rootView, R.id.trunkCorner_spinner));
        flooded.put("audio", getSpinnerSelectedText(rootView, R.id.audioHorn_spinner));
        flooded.put("seatSlide", getSpinnerSelectedText(rootView, R.id.seatSlide_spinner));
        flooded.put("ecu", getSpinnerSelectedText(rootView, R.id.ecu_spinner));
        flooded.put("roof", getSpinnerSelectedText(rootView, R.id.roof_spinner));
        flooded.put("backCorner", getSpinnerSelectedText(rootView, R.id.backCorner_spinner));
        flooded.put("discBox", getSpinnerSelectedText(rootView, R.id.discBox_spinner));
        flooded.put("storageCorner", getSpinnerSelectedText(rootView, R.id.storageCorner_spinner));
        flooded.put("newFuse", getSpinnerSelectedText(rootView, R.id.newFuse_spinner));
        flooded.put("fuse", getSpinnerSelectedText(rootView, R.id.fuse_spinner));
        flooded.put("engineRoom", getSpinnerSelectedText(rootView, R.id.engineRoom_spinner));

        return flooded;
    }

    private void fillFloodWithJSONObject(JSONObject flooded) throws JSONException {
        setSpinnerSelectionWithString(rootView, R.id.cigarLighter_spinner, flooded.getString("cigarLighter"));
        setSpinnerSelectionWithString(rootView, R.id.seatBelts_spinner, flooded.getString("seatBelts"));
        setSpinnerSelectionWithString(rootView, R.id.ashtray_spinner, flooded.getString("ashtray"));
        setSpinnerSelectionWithString(rootView, R.id.rearSeats_spinner, flooded.getString("rearSeats"));
        setSpinnerSelectionWithString(rootView, R.id.spareTireGroove_spinner, flooded.getString("spareTireGroove"));
        setSpinnerSelectionWithString(rootView, R.id.trunkCorner_spinner, flooded.getString("trunkCorner"));
        setSpinnerSelectionWithString(rootView, R.id.audioHorn_spinner, flooded.getString("audio"));
        setSpinnerSelectionWithString(rootView, R.id.seatSlide_spinner, flooded.getString("seatSlide"));
        setSpinnerSelectionWithString(rootView, R.id.ecu_spinner, flooded.getString("ecu"));
        setSpinnerSelectionWithString(rootView, R.id.roof_spinner, flooded.getString("roof"));
        setSpinnerSelectionWithString(rootView, R.id.backCorner_spinner, flooded.getString("backCorner"));
        setSpinnerSelectionWithString(rootView, R.id.discBox_spinner, flooded.getString("discBox"));
        setSpinnerSelectionWithString(rootView, R.id.storageCorner_spinner, flooded.getString("storageCorner"));
        setSpinnerSelectionWithString(rootView, R.id.newFuse_spinner, flooded.getString("newFuse"));
        setSpinnerSelectionWithString(rootView, R.id.fuse_spinner, flooded.getString("fuse"));
        setSpinnerSelectionWithString(rootView, R.id.engineRoom_spinner, flooded.getString("engineRoom"));
    }

    public JSONObject generateTiresJSONObject() throws JSONException{
        JSONObject tires = new JSONObject();

        tires.put("leftFront", getEditViewText(rootView, R.id.leftFront_edit));
        tires.put("rightFront", getEditViewText(rootView, R.id.rightFront_edit));
        tires.put("leftRear", getEditViewText(rootView, R.id.leftRear_edit));
        tires.put("rightRear", getEditViewText(rootView, R.id.rightRear_edit));
        tires.put("spare", getEditViewText(rootView, R.id.spare_edit));
        tires.put("formatMatch", getSpinnerSelectedText(rootView, R.id.formatMatch_spinner));
        tires.put("patternMatch", getSpinnerSelectedText(rootView, R.id.patternMatch_spinner));

        return tires;
    }

    private void fillTiresWithJSONObject(JSONObject tires) throws JSONException{
        setEditViewText(rootView, R.id.leftFront_edit, tires.getString("leftFront"));
        setEditViewText(rootView, R.id.rightFront_edit, tires.getString("rightFront"));
        setEditViewText(rootView, R.id.leftRear_edit, tires.getString("leftRear"));
        setEditViewText(rootView, R.id.rightRear_edit, tires.getString("rightRear"));
        setEditViewText(rootView, R.id.spare_edit, tires.getString("spare"));
        setSpinnerSelectionWithString(rootView, R.id.formatMatch_spinner, tires.getString("formatMatch"));
        setSpinnerSelectionWithString(rootView, R.id.patternMatch_spinner, tires.getString("patternMatch"));
    }

    public String generateCommentString() {
        return getEditViewText(rootView, R.id.it2_comment_edit);
    }

    private void fillCommentWithString(String comment) {
        setEditViewText(rootView, R.id.it2_comment_edit, comment);
    }

    public PhotoEntity generateSketch() {
        ImageView imageView = (ImageView)findViewById(R.id.tire_image);

        Bitmap bitmap = null;

        try {
            bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
            FileOutputStream out = new FileOutputStream(Common.photoDirectory + "tire_sketch");
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        } catch (JSONException e) {

        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName("tire_sketch");
        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }


    public void fillInData(JSONObject flooded, JSONObject tires, String comment2) throws JSONException {
        fillFloodWithJSONObject(flooded);
        fillTiresWithJSONObject(tires);
        fillCommentWithString(comment2);
    }

    public String checkAllFields() {
        if(photoShotCount[0] == 0) {
            return tireMap.keySet().toArray(new String[0])[0];
        }

        if(photoShotCount[1] == 0) {
            return tireMap.keySet().toArray(new String[0])[1];
        }

        if(photoShotCount[2] == 0) {
            return tireMap.keySet().toArray(new String[0])[2];
        }

        if(photoShotCount[3] == 0) {
            return tireMap.keySet().toArray(new String[0])[3];
        }

        return "";
    }

    public void locateTirePart() {
        final Button button = (Button)findViewById(R.id.leftFront_button);
        button.setFocusable(true);
        button.requestFocus();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, button.getBottom());
            }
        });
    }
}
