package de.uni_ulm.uberuniulm.ui.chat;

import android.content.Context;
import android.util.Pair;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.ref.WeakReference;
import java.util.List;

import de.uni_ulm.uberuniulm.R;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> implements View.OnClickListener{
    private final ChatClickListener listener;
    private Context mContext;
    private List<Pair<String, String>> mUsers;

    public UserAdapter(Context mContext, List<Pair<String, String>> mUsers, ChatClickListener listener){
        this.listener=listener;
        this.mUsers=mUsers;
        this.mContext=mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.chat_item, parent, false);

        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pair<String,String> user= mUsers.get(position);
        holder.username.setText(user.second);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/"+user.first+".jpg");
        Glide.with(mContext)
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
        return mUsers.size();
    }

    @Override
    public void onClick(View v) {
        int position=(Integer) v.getTag();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView username;
        public ImageView profileImage;
        private WeakReference<ChatClickListener> listenerRef;

        public ViewHolder(View itemView){
            super(itemView);

            listenerRef= new WeakReference<>(listener);

            username=itemView.findViewById(R.id.chatItemTitle);
            profileImage=itemView.findViewById(R.id.chatItemUserImage);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listenerRef.get().onChatClicked(getAdapterPosition());
        }
    }
}
