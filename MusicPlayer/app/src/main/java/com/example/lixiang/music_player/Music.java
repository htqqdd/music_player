package com.example.lixiang.music_player;

/**
 * Created by lixiang on 2017/9/23.
 */

public class Music {

    public void setRealPic(String s) {realPic = s;}

    public String getRealPic() {
        if (realPic !=null){
            return realPic;
        }else {
            return pic;
        }
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getSongid() {
        return songid;
    }

    public void setSongid(String songid) {
        this.songid = songid;
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

    public void setType(String type) {
        this.type = type;
    }

    private String author;
    private String link;
    private String music;
    private String name;
    private String pic;
    private String realPic;
    private String songid;
    private String type;

}
