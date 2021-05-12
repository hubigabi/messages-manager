package utp.edu.manager.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import utp.edu.manager.model.Message;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM message")
    Single<List<Message>> getAll();

    @Insert
    Completable insert(Message message);

}
