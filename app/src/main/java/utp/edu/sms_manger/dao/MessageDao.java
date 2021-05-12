package utp.edu.sms_manger.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import utp.edu.sms_manger.model.Message;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM message")
    List<Message> getAll();

    @Insert
    void insert(Message message);

}
