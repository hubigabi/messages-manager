package utp.edu.manager.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import utp.edu.manager.model.Message;
import utp.edu.manager.model.MessageType;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM message")
    Single<List<Message>> getAll();

    @Query("SELECT * FROM message WHERE messageType = :type  ORDER BY date DESC LIMIT :limit")
    Single<List<Message>> getByTypeWithLimit(MessageType type, int limit);

    @Insert
    Completable insert(Message message);

}
