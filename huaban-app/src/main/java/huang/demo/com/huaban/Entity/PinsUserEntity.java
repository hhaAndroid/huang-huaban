package huang.demo.com.huaban.Entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @Title: PinsUserEntity.java
 *
 * @Description:
 *
 * @Company:南京航空航天大学
 *
 * @author：黄海安
 *
 * @date： 16-7-30 下午8:36.
 */
public class PinsUserEntity implements Parcelable {
    private int user_id;
    private String username;
    private String urlname;
    private int created_at;
    private String avatar; //注意：作者使用了拼接，和以前的写法不同,这个解析采用其他不同的方法

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUrlname(String urlname) {
        this.urlname = urlname;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getUrlname() {
        return urlname;
    }

    public int getCreated_at() {
        return created_at;
    }

    public String getAvatar() {
        return avatar;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.user_id);
        dest.writeString(this.username);
        dest.writeString(this.urlname);
        dest.writeInt(this.created_at);
        dest.writeString(this.avatar);
    }

    public PinsUserEntity() {
    }

    protected PinsUserEntity(Parcel in) {
        this.user_id = in.readInt();
        this.username = in.readString();
        this.urlname = in.readString();
        this.created_at = in.readInt();
        this.avatar = in.readString();
    }

    public static final Parcelable.Creator<PinsUserEntity> CREATOR = new Parcelable.Creator<PinsUserEntity>() {
        @Override
        public PinsUserEntity createFromParcel(Parcel source) {
            return new PinsUserEntity(source);
        }

        @Override
        public PinsUserEntity[] newArray(int size) {
            return new PinsUserEntity[size];
        }
    };


    @Override
    public String toString() {
        return "PinsUserEntity{" +
                "user_id=" + user_id +
                ", username='" + username + '\'' +
                ", urlname='" + urlname + '\'' +
                ", created_at=" + created_at +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}
