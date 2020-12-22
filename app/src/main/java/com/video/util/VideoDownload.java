package com.video.util;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Request;
import okhttp3.Response;

public class VideoDownload extends Download {
    private static final VideoDownload videoUtil = new VideoDownload();

    public static VideoDownload getInstance() {
        return videoUtil;
    }

    public void writeXY(Context context, int x, int y) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("x", x);
        editor.putInt("y", y);
        editor.commit();
    }

    public void setUpdate(Context context, String content) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("update", content);
        editor.commit();
    }

    public String getUpdate(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return sharedPreferences.getString("update", "");
    }

    public int getX(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("x", 0);
    }

    public int getY(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("y", 0);
    }

    public boolean checkStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public boolean checkAlertWindowsPermission(Context context) {
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = 24;
            arrayOfObject1[1] = Binder.getCallingUid();
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1));
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void parse(final Context context, final String url) {
        try {
            if (!checkStoragePermission(context)) {
                Toast.makeText(context, "没有存储权限", Toast.LENGTH_SHORT).show();
                return;
            }
            Pattern pattern = Patterns.WEB_URL;
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                final String downloadUrl = matcher.group(0);
                if (downloadUrl.contains("douyin")) {
                    onStartDownload(context);
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Log.e("TAG", "downloadUrl:" + downloadUrl);
                                Request request = new Request.Builder().url(downloadUrl).build();
                                Response response = okHttpClient.newCall(request).execute();
                                String redirect_url = response.request().url().toString();
                                Log.e("TAG", "redirectUrl:" + redirect_url);
                                //https://share.huoshan.com/pages/item/index.html?item_id=6835104462252576003&tag=0&timestamp=1593335702&watermark=2&media_type=4&share_ht_uid=0&did=54563595676&iid=1503786138343918&utm_medium=huoshan_android&tt_from=copy_link&app=live_stream&utm_source=copy_link&schema_url=sslocal%3A%2F%2Fitem%3Fid%3D6835104462252576003
                                //https://www.iesdouyin.com/share/video/6857130135997484288/?region=CN&mid=6853242868393036551&u_code=m8i1m7d6&titleType=title&utm_source=copy_link&utm_campaign=client_share&utm_medium=android&app=aweme
                                String item_id = redirect_url.split("video/")[1].split("/\\?")[0];
                                Log.e("TAG", "item_id:" + item_id);
                                //https://share.huoshan.com/api/item/info?item_id=6835104462252576003
                                //https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids=6857130135997484288
                                String share_url = "https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids=" + item_id;
                                request = new Request.Builder().url(share_url).build();
                                response = okHttpClient.newCall(request).execute();
                                String msg = response.body().string();
                                Log.e("TAG", "msg:" + msg);
                                //{"status_code":0,"item_list":[{"share_url":"https://www.iesdouyin.com/share/video/6857130135997484288/?region=&mid=6853242868393036551&u_code=48&titleType=title",
                                // "text_extra":[],"is_preview":0,"is_live_replay":false,"desc":"怎么还给我删了呢？快来加我！","cha_list":null,"aweme_type":4,"author":{"avatar_larger":{"uri":"douyin-user-file/6857400743943471104",
                                // "url_list":["https://p26-dy.byteimg.com/aweme/1080x1080/douyin-user-file/6857400743943471104.jpeg?from=4010531038","https://p3-dy-ipv6.byteimg.com/aweme/1080x1080/douyin-user-file/6857400743943471104.jpeg?from=4010531038","https://p29-dy.byteimg.com/aweme/1080x1080/douyin-user-file/6857400743943471104.jpeg?from=4010531038"]},"avatar_thumb":{"uri":"douyin-user-file/6857400743943471104","url_list":["https://p29-dy.byteimg.com/aweme/100x100/douyin-user-file/6857400743943471104.jpeg?from=4010531038","https://p26-dy.byteimg.com/aweme/100x100/douyin-user-file/6857400743943471104.jpeg?from=4010531038","https://p6-dy-ipv6.byteimg.com/aweme/100x100/douyin-user-file/6857400743943471104.jpeg?from=4010531038"]},"followers_detail":null,"geofencing":null,"policy_version":null,"short_id":"162229082","signature":"苟日新，日日新，又日新。","avatar_medium":{"uri":"douyin-user-file/6857400743943471104",
                                // "url_list":["https://p3-dy-ipv6.byteimg.com/aweme/720x720/douyin-user-file/6857400743943471104.jpeg?from=4010531038","https://p29-dy.byteimg.com/aweme/720x720/douyin-user-file/6857400743943471104.jpeg?from=4010531038",
                                // "https://p26-dy.byteimg.com/aweme/720x720/douyin-user-file/6857400743943471104.jpeg?from=4010531038"]},"unique_id":"","platform_sync_info":null,"type_label":null,"uid":"81231350390","nickname":"只有鱼刺"},"aweme_id":"6857130135997484288","music":{"duration":7,"position":null,"status":1,"id":6853242868393037000,"author":"小曲同学","cover_large":{"uri":"318470005eb3ff71b902b","url_list":["https://p3-dy-ipv6.byteimg.com/aweme/1080x1080/318470005eb3ff71b902b.jpeg?from=4010531038","https://p6-dy-ipv6.byteimg.com/aweme/1080x1080/318470005eb3ff71b902b.jpeg?from=4010531038","https://p9-dy.byteimg.com/aweme/1080x1080/318470005eb3ff71b902b.jpeg?from=4010531038"]},"play_url":{"uri":"http://p9-dy.byteimg.com/obj/ies-music/6853242870653143821.mp3","url_list":["http://p9-dy.byteimg.com/obj/ies-music/6853242870653143821.mp3","http://p1-dy.byteimg.com/obj/ies-music/6853242870653143821.mp3"]},"cover_thumb":{"uri":"318470005eb3ff71b902b","url_list":["https://p9-dy.byteimg.com/aweme/100x100/318470005eb3ff71b902b.jpeg?from=4010531038","https://p29-dy.byteimg.com/aweme/100x100/318470005eb3ff71b902b.jpeg?from=4010531038","https://p3-dy-ipv6.byteimg.com/aweme/100x100/318470005eb3ff71b902b.jpeg?from=4010531038"]},"mid":"6853242868393036551","title":"@小曲同学创作的原声","cover_hd":{"uri":"318470005eb3ff71b902b","url_list":["https://p3-dy-ipv6.byteimg.com/aweme/1080x1080/318470005eb3ff71b902b.jpeg?from=4010531038","https://p6-dy-ipv6.byteimg.com/aweme/1080x1080/318470005eb3ff71b902b.jpeg?from=4010531038","https://p9-dy.byteimg.com/aweme/1080x1080/318470005eb3ff71b902b.jpeg?from=4010531038"]},"cover_medium":{"uri":"318470005eb3ff71b902b","url_list":["https://p3-dy-ipv6.byteimg.com/aweme/720x720/318470005eb3ff71b902b.jpeg?from=4010531038","https://p9-dy.byteimg.com/aweme/720x720/318470005eb3ff71b902b.jpeg?from=4010531038","https://p29-dy.byteimg.com/aweme/720x720/318470005eb3ff71b902b.jpeg?from=4010531038"]}},"statistics":{"aweme_id":"6857130135997484288","comment_count":2353,"digg_count":30244,"play_count":0},"create_time":1596550025,"share_info":{"share_weibo_desc":"#在抖音，记录美好生活#怎么还给我删了呢？快来加我！","share_desc":"在抖音，记录美好生活","share_title":"怎么还给我删了呢？快来加我！"},"video_labels":null,"video_text":null,"forward_id":"0","long_video":null,"risk_infos":{"warn":false,"type":0,"content":""},"author_user_id":81231350390,"image_infos":null,"comment_list":null,"label_top_text":null,"promotions":null,
                                // "video":{"play_addr":{"url_list":["https://aweme.snssdk.com/aweme/v1/playwm/?video_id=v0200f1e0000bskmmv0o772k770k6iag&ratio=720p&line=0"],"uri":"v0200f1e0000bskmmv0o772k770k6iag"},"width":720,"dynamic_cover":{"uri":"tos-cn-p-0015/14964b7a6d1c47f1841501a46e6b5a9a_1596550028","url_list":["https://p3-dy-ipv6.byteimg.com/obj/tos-cn-p-0015/14964b7a6d1c47f1841501a46e6b5a9a_1596550028?from=2563711402_large","https://p6-dy-ipv6.byteimg.com/obj/tos-cn-p-0015/14964b7a6d1c47f1841501a46e6b5a9a_1596550028?from=2563711402_large","https://p29-dy.byteimg.com/obj/tos-cn-p-0015/14964b7a6d1c47f1841501a46e6b5a9a_1596550028?from=2563711402_large"]},"has_watermark":true,"duration":7300,"vid":"v0200f1e0000bskmmv0o772k770k6iag","cover":{"uri":"tos-cn-p-0015/2f7b82e27c3d480c8a293298d03dc558","url_list":["https://p26-dy.byteimg.com/img/tos-cn-p-0015/2f7b82e27c3d480c8a293298d03dc558~c5_300x400.jpeg?from=2563711402_large","https://p29-dy.byteimg.com/img/tos-cn-p-0015/2f7b82e27c3d480c8a293298d03dc558~c5_300x400.jpeg?from=2563711402_large","https://p6-dy-ipv6.byteimg.com/img/tos-cn-p-0015/2f7b82e27c3d480c8a293298d03dc558~c5_300x400.jpeg?from=2563711402_large"]},"height":1280,"origin_cover":{"uri":"tos-cn-p-0015/401283d4ba6d443281f295b771369c39_1596550026","url_list":["https://p26-dy.byteimg.com/tos-cn-p-0015/401283d4ba6d443281f295b771369c39_1596550026~tplv-dy-360p.jpeg?from=2563711402","https://p9-dy.byteimg.com/tos-cn-p-0015/401283d4ba6d443281f295b771369c39_1596550026~tplv-dy-360p.jpeg?from=2563711402","https://p3-dy-ipv6.byteimg.com/tos-cn-p-0015/401283d4ba6d443281f295b771369c39_1596550026~tplv-dy-360p.jpeg?from=2563711402"]},"ratio":"540p","bit_rate":null},"duration":7300,"geofencing":null,"group_id":6857130135997484000}],"ab_type":1,"extra":{"now":1597542693000,"logid":"2020081609513301019806022055FA68BD"}}
                                String video_url = msg.split("play_addr")[1].split("url_list\":\\[\"")[1].split("\"]")[0];
                                Log.e("TAG", "video_url:" + video_url);
                                video_url = video_url.replace("playwm", "play");
                                Log.e("TAG", "video_url=" + video_url);
                                downloadMp4(context, video_url);
                            } catch (Exception e) {
                                onErrorDownload(context);
                                e.printStackTrace();
                            }
//                            try {
//                                //https://v.douyin.com/JMC4s3t/
//                                WebClient webClient = new WebClient(BrowserVersion.CHROME);
//                                webClient.getOptions().setJavaScriptEnabled(true);
//                                webClient.getOptions().setRedirectEnabled(true);
//                                webClient.getOptions().setActiveXNative(false);
//                                webClient.getOptions().setCssEnabled(false);
//                                webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
//                                webClient.getOptions().setThrowExceptionOnScriptError(false);
//                                webClient.setAjaxController(new NicelyResynchronizingAjaxController());
//                                webClient.getOptions().setTimeout(2000);
//                                webClient
//                                        .addRequestHeader(
//                                                "User-Agent",
//                                                "Mozilla/5.0 (iPhone; CPU iPhone OS 12_1_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/16D57 Version/12.0 Safari/604.1");
//                                HtmlPage rootPage = webClient.getPage(downloadUrl);
//                                webClient.waitForBackgroundJavaScript(2000);
//                                String html = rootPage.asXml();
//                                Document doc = Jsoup.parse(html);
////                                Element video = doc.getElementById("theVideo");
//                                Element video = doc.getElementsByClass("video-player--ORxFE hide--1XNRY").get(0);
//                                String url = video.attr("src");
//                                url = url.replace("playwm", "play");
//                                Log.e("TAG", "url=" + url);
//                                downloadMp4(context, url);
//                                webClient.closeAllWindows();
//                            } catch (Exception e) {
//                                onErrorDownload(context);
//                                e.printStackTrace();
//                            }
                        }
                    }.start();
                } else if (downloadUrl.contains("huoshan")) {
                    //https://share.huoshan.com/hotsoon/s/OhMjrF4Uv98/
                    onStartDownload(context);
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Log.e("TAG", "downloadUrl:" + downloadUrl);
                                Request request = new Request.Builder().url(downloadUrl).build();
                                Response response = okHttpClient.newCall(request).execute();
                                String redirect_url = response.request().url().toString();
                                Log.e("TAG", "redirectUrl:" + redirect_url);
                                //https://share.huoshan.com/pages/item/index.html?item_id=6835104462252576003&tag=0&timestamp=1593335702&watermark=2&media_type=4&share_ht_uid=0&did=54563595676&iid=1503786138343918&utm_medium=huoshan_android&tt_from=copy_link&app=live_stream&utm_source=copy_link&schema_url=sslocal%3A%2F%2Fitem%3Fid%3D6835104462252576003
                                String item_id = redirect_url.split("item_id=")[1].split("&")[0];
                                Log.e("TAG", "item_id:" + item_id);
                                //https://share.huoshan.com/api/item/info?item_id=6835104462252576003
                                String share_url = "https://share.huoshan.com/api/item/info?item_id=" + item_id;
                                request = new Request.Builder().url(share_url).build();
                                response = okHttpClient.newCall(request).execute();
                                String msg = response.body().string();
                                Log.e("TAG", "msg:" + msg);
                                String video_id = msg.split("video_id=")[1].split("&")[0];
                                Log.e("TAG", "video_id:" + video_id);
                                String real_url = "http://hotsoon.snssdk.com/hotsoon/item/video/_playback/?video_id=" + video_id;
                                Log.e("TAG", "real_url:" + real_url);
                                downloadMp4(context, real_url);
                            } catch (Exception e) {
                                onErrorDownload(context);
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } else if (downloadUrl.contains("kuaishou")) {
                    onStartDownload(context);
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Log.e("TAG", "start .....");
                                WebClient webClient = new WebClient(BrowserVersion.CHROME);
                                webClient.getOptions().setRedirectEnabled(true);
                                webClient.getOptions().setCssEnabled(false);
                                webClient.getOptions().setActiveXNative(false);
                                webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                                webClient.getOptions().setThrowExceptionOnScriptError(false);
                                webClient.getOptions().setTimeout(3000);
                                webClient
                                        .addRequestHeader(
                                                "User-Agent",
                                                "Mozilla/5.0 (iPhone; CPU iPhone OS 12_1_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/16D57 Version/12.0 Safari/604.1");
                                HtmlPage rootPage = webClient.getPage(downloadUrl);
                                String html = rootPage.asXml();
                                //Log.e("TAG", html);
                                Log.e("TAG", "srcNoMark:" + html.contains("srcNoMark"));
//                                Log.e("TAG", html.split("srcNoMark")[0]);
//                                Log.e("TAG", html.split("srcNoMark")[1]);
                                Log.e("TAG", html.split("srcNoMark\":\"")[1]);
                                // String srcNoMark=html.split("srcNoMark&#34;:&#34;")[2].split("&#34;")[0];
                                String srcNoMark = html.split("srcNoMark\":\"")[2].split("\"}")[0];
                                Log.e("TAG", "srcNoMarkUrl:" + srcNoMark);
                                downloadMp4(context, srcNoMark);
                                // Document doc = Jsoup.parse(html);
                                webClient.closeAllWindows();
                            } catch (Exception e) {
                                onErrorDownload(context);
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            } else {
                Toast.makeText(context, "请输入正确的链接!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            onErrorDownload(context);
            e.printStackTrace();
        }
    }
}
