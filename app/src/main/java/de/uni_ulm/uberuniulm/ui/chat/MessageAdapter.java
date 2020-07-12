package de.uni_ulm.uberuniulm.ui.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.ride.Chat;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private static final int MSG_TYPE_RIGHT=1, MSG_TYPE_LEFT=0;
    private Context context;
    private List<Chat> chat;
    private String userId;

    private FirebaseUser user;

    public MessageAdapter(Context context, List<Chat> chat){
        this.chat=chat;
        this.context=context;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType== MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right,parent, false);
            return new MessageAdapter.ViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left,parent, false);
            return new MessageAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chat mchat= chat.get(position);
        holder.message.setText(mchat.getMessage());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/"+mchat.getSender()+".jpg");
        Glide.with(context)
                .load(profileImageRef)
                .centerCrop()
                .skipMemoryCache(true) //2
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.start_register_profile_photo)
                .transform(new CircleCrop())
                .into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return chat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView message;
        public ImageView profileImage;

        public ViewHolder(View itemView){
            super(itemView);

            message= itemView.findViewById(R.id.chatItemMessageText);
            profileImage= itemView.findViewById(R.id.chatItemUserImage);
        }
    }

    @Override
    public int getItemViewType(int position){
        SharedPreferences pref = new ObscuredSharedPreferences(context, context.getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        userId = pref.getString("UserKey", "");

        if(chat.get(position).getSender().equals(userId)){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }
}
