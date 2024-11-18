package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social.Messages;
import com.example.social.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter{
    Context context;
    ArrayList<Messages> messagesArrayList;

    int ITEM_SEND=1;
    int ITEM_RECIEVE=2;

    public MessagesAdapter(Context context, ArrayList<Messages> messagesArrayList) {
        this.context = context;
        this.messagesArrayList = messagesArrayList;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==ITEM_SEND)
        {
            View view= LayoutInflater.from(context).inflate(R.layout.sender_chat_layout,parent,false);
            return new SenderViewHolder(view);
        }
        else
        {
            View view= LayoutInflater.from(context).inflate(R.layout.receiver_chat_layout,parent,false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages messages=messagesArrayList.get(position);
        if(holder.getClass() == SenderViewHolder.class)
        {
            SenderViewHolder viewHolder=(SenderViewHolder)holder;
            viewHolder.textViewMessageSender.setText(messages.getMessage());
            viewHolder.timeOfMessageSender.setText(messages.getCurrentTime());
        }
        else
        {
            ReceiverViewHolder viewHolder=(ReceiverViewHolder)holder;
            viewHolder.textViewMessageReceiver.setText(messages.getMessage());
            viewHolder.timeOfMessageReceiver.setText(messages.getCurrentTime());
        }
    }

    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }
    @Override
    public int getItemViewType(int position) {
        Messages messages=messagesArrayList.get(position);
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messages.getSenderId()))

        {
            return  ITEM_SEND;
        }
        else
        {
            return ITEM_RECIEVE;
        }
    }
    class SenderViewHolder extends RecyclerView.ViewHolder
    {
        TextView textViewMessageSender;
        TextView timeOfMessageSender;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessageSender=itemView.findViewById(R.id.senderMessage);
            timeOfMessageSender=itemView.findViewById(R.id.timeOfMessageSender);
        }
    }
    class ReceiverViewHolder extends RecyclerView.ViewHolder
    {
        TextView textViewMessageReceiver;
        TextView timeOfMessageReceiver;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessageReceiver=itemView.findViewById(R.id.receiverMessage);
            timeOfMessageReceiver=itemView.findViewById(R.id.timeOfMessageReceiver);
        }
    }
}
