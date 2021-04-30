package utp.edu.sms_manger.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.List;

import utp.edu.sms_manger.R;
import utp.edu.sms_manger.model.Sms;

public class SmsRecyclerViewAdapter extends RecyclerView.Adapter<SmsRecyclerViewAdapter.ViewHolder> {

    private List<Sms> smsList;
    private LayoutInflater layoutInflater;
    private ItemClickListener itemClickListener;
    private final DateFormat formatter = DateFormat.getDateTimeInstance();

    public SmsRecyclerViewAdapter(Context context, List<Sms> data) {
        this.layoutInflater = LayoutInflater.from(context);
        this.smsList = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.sms_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Sms sms = smsList.get(position);
        holder.dateTextView.setText(formatter.format(sms.getDate()));
        holder.numberTextView.setText(sms.getNumber());
        holder.messageTextView.setText(sms.getMessage());
    }

    @Override
    public int getItemCount() {
        return smsList.size();
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

    public Sms getItem(int id) {
        return smsList.get(id);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
