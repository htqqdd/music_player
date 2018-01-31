package com.example.lixiang.music_player;

/**
 * Created by lixiang on 2017/9/23.
 */

public class Music {
    private String author;
    private String link;
    private String music;
    private String name;
    private String pic;
    private String realPic;
    private String songid;
    private String type;
    public String getRealPic() {
        //网易、QQ、虾米、百度、咪咕、蜻蜓支持
        if (pic!=null) {
            if (pic.contains("?param=100x100")) {//?param=100x100网易
                return pic.substring(0, pic.length() - 14) + "?param=800x800";
            } else if (pic.contains("T002R300x300")) {//QQ音乐pic=http://y.gtimg.cn/music/photo_new/T002R300x300M000003Djf8120GknA.jpg
                return pic.replace("T002R300x300", "T002R800x800");
            } else if (pic.contains("/100/")) {//酷狗音乐pic=http://singerimg.kugou.com/uploadpic/pass/softhead/100/20160908/20160908121727482.jpg
                return pic.replace("/100/", "/200/");
            } else if (pic.contains("w_150,h_150")) {//百度音乐2:pic=http://musicdata.baidu.com/data2/pic/ec271e041469a6af9ef8cff93658be98/67308/67308.jpg@s_1,w_150,h_150
                return pic.replace("w_150,h_150", "w_800,h_800");
            } else if (pic.contains("@s_0,w_240")) {//百度音乐2:pic=http://musicdata.baidu.com/data2/pic/7dce01e6a400a986580b20f848c580a9/271877645/271877645.jpg@s_0,w_240
                return pic.replace("@s_0,w_240", "@s_0,w_800");
            } else if (pic.contains("RsT_200x200")) {//咪咕音乐pic=http://img01.12530.com/music/picture/20170518/27/16/sqMP3645.jpg_RsT_200x200.jpg
                return pic.replace("_RsT_200x200", "_RsT_800x800");
            } else if (pic.contains("!200")) {//蜻蜓音乐pic=http://pic.qingting.fm/2015/1225/2015122512113624.jpg!200
                return pic.replace("!200", "!800");
            }
        }
        return pic;
    }
    public String getMediumPic() {
        if (pic!=null) {
            if (pic.contains("?param=100x100")) {//?param=100x100网易
                return pic.substring(0, pic.length() - 14) + "?param=300x300";
            } else if (pic.contains("300x300")) {//QQ音乐pic=http://y.gtimg.cn/music/photo_new/T002R300x300M000003Djf8120GknA.jpg
                return pic;
            } else if (pic.contains("/100/")) {//酷狗音乐pic=http://singerimg.kugou.com/uploadpic/pass/softhead/100/20160908/20160908121727482.jpg
                return pic.replace("/100/", "/200/");
            } else if (pic.contains("w_150,h_150")) {//百度音乐2:pic=http://musicdata.baidu.com/data2/pic/ec271e041469a6af9ef8cff93658be98/67308/67308.jpg@s_1,w_150,h_150
                return pic.replace("w_150,h_150", "w_300,h_300");
            } else if (pic.contains("@s_0,w_240")) {//百度音乐2:pic=http://musicdata.baidu.com/data2/pic/7dce01e6a400a986580b20f848c580a9/271877645/271877645.jpg@s_0,w_240
                return pic.replace("@s_0,w_240", "@s_0,w_300");
            } else if (pic.contains("RsT_200x200")) {//咪咕音乐pic=http://img01.12530.com/music/picture/20170518/27/16/sqMP3645.jpg_RsT_200x200.jpg
                return pic.replace("RsT_200x200", "RsT_300x300");
            } else if (pic.contains("!200")) {//蜻蜓音乐pic=http://pic.qingting.fm/2015/1225/2015122512113624.jpg!200
                return pic.replace("!200", "!300");
            }
        }
        return pic;
    }
    public String getAuthor() {
        return author;
    }
    public String getLink() {
        return link;
    }
    public String getMusic() {
        return music;
    }
    public String getName() {
        return name;
    }
    public String getPic() {
        return pic;
    }
    public int getSongid() {
        return Integer.valueOf(songid);
    }
    public String getType() {
        switch (type) {
            case "163":
                return "网易云音乐";
            case "qq":
                return "QQ音乐";
            case "kugou":
                return "酷狗音乐";
            case "kuwo":
                return "酷我音乐";
            case "xiami":
                return "虾米音乐";
            case "baidu":
                return "百度音乐";
            case "1ting":
                return "1听音乐";
            case "migu":
                return "咪咕音乐";
            case "lizhi":
                return "荔枝音乐";
            case "qingting":
                return "蜻蜓音乐";
            case "5sing":
                return "5Sing音乐";
            case "soundcloud":
                return "SoundCloud";
            default:
        }
        return type;
    }
}
