package dump.t;

import cn.bmob.v3.BmobObject;

/**
 * Created by 26526 on 2017/12/16.
 */

public class addpr0 extends BmobObject {
    // 反馈内容
    private String content;
    // 联系方式
    private String contacts;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }
}
