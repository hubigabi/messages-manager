package utp.edu.manager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import utp.edu.manager.adapter.MessageRecyclerViewAdapter;
import utp.edu.manager.database.AppDatabase;
import utp.edu.manager.model.Message;
import utp.edu.manager.model.MessageType;

public class DisplayMessagesActivity extends AppCompatActivity {

    private static final String TAG = DisplayMessagesActivity.class.getSimpleName();
    private MessageRecyclerViewAdapter messageAdapter;
    private List<Message> messagesSent = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_messages);

        RecyclerView messageRecyclerView = findViewById(R.id.messages_sent_recycler_view);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(messageRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        messageRecyclerView.addItemDecoration(itemDecoration);

        messageAdapter = new MessageRecyclerViewAdapter(this, messagesSent);
        messageAdapter.setClickListener((view, position) -> {
            Message message = messageAdapter.getItem(position);
            Toast.makeText(this, message.getText(), Toast.LENGTH_SHORT).show();
        });
        messageRecyclerView.setAdapter(messageAdapter);

        Disposable disposable = AppDatabase.getInstance(getApplicationContext()).messageDao().getByTypeWithLimit(MessageType.SENT, 50)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(messages -> {
                    messagesSent.clear();
                    messagesSent.addAll(messages);
                    messageAdapter.notifyDataSetChanged();
                }, throwable -> Log.e(TAG, "Unable to get messages", throwable));
    }
}