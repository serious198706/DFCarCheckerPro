package com.df.app.util;

import android.content.Context;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.carCheck.BasicInfoLayout;
import com.df.app.carCheck.Integrated2Layout;
import com.df.app.entries.Action;
import com.df.app.entries.PhotoEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by 岩 on 14-4-2.
 */
public class PhotoParser {
    public static void parsePhotoData(Context context, JSONObject photo,
                                      List<PhotoEntity> exteriorPhotos,
                                      List<PhotoEntity> interiorPhotos,
                                      List<PhotoEntity> faultPhotos,
                                      List<PhotoEntity> proceduresPhotos,
                                      List<PhotoEntity> enginePhotos,
                                      List<PhotoEntity> agreementPhotos) throws JSONException {
        parseStandard(context, photo.getJSONObject("exterior").getJSONArray("standard"), exteriorPhotos, R.array.photoForExteriorItems, Common.exteriorPartArray);
        parseStandard(context, photo.getJSONObject("interior").getJSONArray("standard"), interiorPhotos, R.array.photoForInteriorItems, Common.interiorPartArray);

        if(photo.get("procedures") != JSONObject.NULL)
            parseStandard(context, photo.getJSONArray("procedures"), proceduresPhotos, R.array.photoForProceduresItems, Common.proceduresPartArray);

        parseStandard(context, photo.getJSONArray("engineRoom"), enginePhotos, R.array.photoForEngineItems, Common.enginePartArray);

        parseFault(photo, faultPhotos);
        parseTire(context, photo.getJSONObject("tire"), exteriorPhotos);

        if(photo.get("agreement") != JSONObject.NULL)
            parseAgreement(photo.getJSONArray("agreement"), agreementPhotos);
    }

    private static void parseAgreement(JSONArray agreement, List<PhotoEntity> agreementPhotos) throws JSONException {
        for(int i = 0; i < agreement.length(); i++) {
            JSONObject temp = agreement.getJSONObject(i);

            PhotoEntity photoEntity = new PhotoEntity();

            String url = temp.getString("photo");  //     c/1403/12/00715-zmckhyiiyoq.jpg
            String thumbUrl;

            if(!url.equals("")) {
                thumbUrl = Common.getTHUMB_ADDRESS() + url + "?w=150";
                url = Common.getPICTURE_ADDRESS() + url;
            } else {
                thumbUrl = "";
            }

            photoEntity.setFileName(url);
            photoEntity.setThumbFileName(thumbUrl);
            photoEntity.setIndex(temp.getInt("index"));
            photoEntity.setModifyAction(Action.NORMAL);
            photoEntity.setName("协议");
            makeJsonString(photoEntity, temp, "agreement", "");

            agreementPhotos.add(photoEntity);
        }
    }

    private static void parseTire(Context context, JSONObject tire, List<PhotoEntity> exteriorPhotos) throws JSONException {
        String[] tireArray = context.getResources().getStringArray(R.array.tire_items);

        for(int i = 0; i < Common.tirePartArray.length; i++) {
            if(tire.get(Common.tirePartArray[i]) != JSONObject.NULL) {
                JSONObject jsonObject = tire.getJSONObject(Common.tirePartArray[i]);
                addTire(tireArray[i], Common.tirePartArray[i], jsonObject, exteriorPhotos);
                Integrated2Layout.photoShotCount[i] = 1;

                if(Integrated2Layout.buttons[i] != null)
                    Integrated2Layout.buttons[i].setBackgroundResource(R.drawable.tire_pressed);
            }
        }
    }

    private static void addTire(String name, String part, JSONObject temp, List<PhotoEntity> exteriorPhotos) throws JSONException{
        if(temp != JSONObject.NULL) {
            PhotoEntity photoEntity = new PhotoEntity();

            String url = temp.getString("photo");  //     c/1403/12/00715-zmckhyiiyoq.jpg
            String thumbUrl;

            if(!url.equals("")) {
                thumbUrl = Common.getTHUMB_ADDRESS() + url + "?w=150";
                url = Common.getPICTURE_ADDRESS() + url;
            } else {
                thumbUrl = "";
            }

            photoEntity.setFileName(url);
            photoEntity.setThumbFileName(thumbUrl);
            photoEntity.setIndex(temp.getInt("index"));
            photoEntity.setName(name);
            photoEntity.setModifyAction(Action.NORMAL);
            makeJsonString(photoEntity, temp, "tire", part);

            exteriorPhotos.add(photoEntity);

            // 当为报告预览模式时，It2.photoEntityMap为null
            if(Integrated2Layout.photoEntityMap != null) {
                Integrated2Layout.photoEntityMap.put(part, photoEntity);

                if(part.equals("leftFront")) {
                    Integrated2Layout.leftFrontIndex = photoEntity.getIndex();
                } else if(part.equals("rightFront")) {
                    Integrated2Layout.rightFrontIndex = photoEntity.getIndex();
                } else if(part.equals("leftRear")) {
                    Integrated2Layout.leftRearIndex = photoEntity.getIndex();
                } else if(part.equals("rightRear")) {
                    Integrated2Layout.rightRearIndex = photoEntity.getIndex();
                } else if(part.equals("spare")) {
                    Integrated2Layout.spareIndex = photoEntity.getIndex();
                }
            }
        }
    }

    private static void parseStandard(Context context, JSONArray jsonArray, List<PhotoEntity> photoEntities, int stringArrayId, String[] partArray) throws JSONException {
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject temp = jsonArray.getJSONObject(i);

            PhotoEntity photoEntity = new PhotoEntity();

            String part = temp.getString("part");

            String[] exteriorPart = context.getResources().getStringArray(stringArrayId);

            for(int j = 0; j < partArray.length; j++) {
                if(part.equals(partArray[j])) {
                    photoEntity.setName(exteriorPart[j]);
                }
            }

            String url = temp.getString("photo");  //     c/1403/12/00715-zmckhyiiyoq.jpg
            String thumbUrl;

            if(!url.equals("")) {
                thumbUrl = Common.getTHUMB_ADDRESS() + url + "?w=150";
                url = Common.getPICTURE_ADDRESS() + url;
            } else {
                thumbUrl = "";
            }

            photoEntity.setFileName(url);
            photoEntity.setThumbFileName(thumbUrl);
            photoEntity.setIndex(temp.getInt("index"));
            photoEntity.setModifyAction(Action.NORMAL);

            String group;

            if(stringArrayId == R.array.photoForExteriorItems) {
                group = "exterior";
            } else if(stringArrayId == R.array.photoForInteriorItems) {
                group = "interior";
            } else if(stringArrayId == R.array.photoForProceduresItems) {
                group = "procedures";
            } else if(stringArrayId == R.array.photoForEngineItems) {
                group = "engineRoom";
            } else {
                group = "";
            }

            makeJsonString(photoEntity, temp, group, "standard");

            photoEntities.add(photoEntity);
        }
    }

    /**
     * 解析fault部分
     * @param photo
     * @throws JSONException
     */
    private static void parseFault(JSONObject photo, List<PhotoEntity> faultPhotos) throws JSONException {
        if(photo == JSONObject.NULL) {
            return;
        }

        if(photo.getJSONObject("exterior").get("fault") != JSONObject.NULL)
            addFault(photo.getJSONObject("exterior").getJSONArray("fault"), faultPhotos, "fault");

        if(photo.getJSONObject("interior").get("fault") != JSONObject.NULL)
            addFault(photo.getJSONObject("interior").getJSONArray("fault"), faultPhotos, "fault");

        if(photo.getJSONObject("frame").get("front") != JSONObject.NULL)
            addFault(photo.getJSONObject("frame").getJSONArray("front"), faultPhotos, "front");

        if(photo.getJSONObject("frame").get("rear") != JSONObject.NULL)
            addFault(photo.getJSONObject("frame").getJSONArray("rear"), faultPhotos, "rear");

        if(photo.get("otherFault") != JSONObject.NULL) {
            addFault(photo.getJSONArray("otherFault"), faultPhotos, "");
        }
    }

    /**
     * 将jsonArray中的照片加入faultPhotos
     * @param jsonArray array
     * @throws JSONException
     */
    private static void addFault(JSONArray jsonArray, List<PhotoEntity> faultPhotos, String part) throws JSONException {
        if(jsonArray == JSONObject.NULL) {
            return;
        }

        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject temp = jsonArray.getJSONObject(i);

            PhotoEntity photoEntity = new PhotoEntity();

            String name;
            String group;
            if(temp.has("type")) {
                switch (temp.getInt("type")) {
                    case 1:
                        name = "色差";
                        group = "exterior";
                        break;
                    case 2:
                        name = "划痕";
                        group = "exterior";
                        break;
                    case 3:
                        name = "变形";
                        group = "exterior";
                        break;
                    case 4:
                        name = "刮蹭";
                        group = "exterior";
                        break;
                    case 5:
                        name = "其它";
                        group = "exterior";
                        break;
                    case 6:
                        name = "脏污";
                        group = "interior";
                        break;
                    case 7:
                        name = "破损";
                        group = "interior";
                        break;
                    default:
                        name = "";
                        group = "";
                        break;
                }
            } else if(temp.has("issueId")) {
                name = "结构缺陷";
                group = "frame";
            } else {
                name = "其他缺陷";
                group = "otherFault";
            }

            photoEntity.setName(name);

            String url = temp.getString("photo");  //     c/1403/12/00715-zmckhyiiyoq.jpg
            String thumbUrl;

            if(!url.equals("")) {
                thumbUrl = Common.getTHUMB_ADDRESS() + url + "?w=150";
                url = Common.getPICTURE_ADDRESS() + url;
            } else {
                thumbUrl = "";
            }

            photoEntity.setFileName(url);
            photoEntity.setThumbFileName(thumbUrl);
            photoEntity.setComment(temp.getString("comment"));
            photoEntity.setIndex(temp.getInt("index"));
            photoEntity.setModifyAction(Action.NORMAL);
            makeJsonString(photoEntity, temp, group, part);

            faultPhotos.add(photoEntity);
        }
    }

    private static void makeJsonString(PhotoEntity photoEntity, JSONObject photoData, String group, String part) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("CarId", BasicInfoLayout.carId);
        jsonObject.put("UserId", MainActivity.userInfo.getId());
        jsonObject.put("Key", MainActivity.userInfo.getKey());
        jsonObject.put("Action", photoEntity.getModifyAction());
        jsonObject.put("Index", photoEntity.getIndex());

        if(!group.equals(""))
            jsonObject.put("Group", group);

        if(!part.equals(""))
            jsonObject.put("Part", part);

        photoData.remove("photo");
        photoData.remove("index");

        jsonObject.put("PhotoData", photoData);

        photoEntity.setJsonString(jsonObject.toString());
    }
}
