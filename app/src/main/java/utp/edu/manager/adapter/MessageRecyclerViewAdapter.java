package utp.edu.manager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.List;

import utp.edu.manager.R;
import utp.edu.manager.model.Message;

public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {

    private List<Message> messageList;
    private LayoutInflater layoutInflater;
    private ItemClickListener itemClickListener;
    private final DateFormat formatter = DateFormat.getDateTimeInstance();

    public MessageRecyclerViewAdapter(Context context, List<Message> data) {
        this.layoutInflater = LayoutInflater.from(context);
        this.messageList = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.message_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.dateTextView.setText(formatter.format(message.getDate()));
        holder.numberTextView.setText(message.getNumber());
        holder.messageTextView.setText(message.getText());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView dateTextView;
        TextView numberTextView;
        TextView messageTextView;

        ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            numberTextView = itemView.findViewById(R.id.number_text_view);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null)
                itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public Message getItem(int id) {
        return messageList.get(id);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
