package zlobniyslaine.ru.ficbook.moshis;

import java.util.Date;

public class Message {
    public String id;
    public String thread_id;
    public Integer type;
    public Integer user_id;
    public String text;
    public Date date_sent;
    public Boolean own;
    public ChatUser user;
}