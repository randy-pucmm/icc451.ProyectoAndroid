package com.example.proyectoandroid.ui.chat;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyectoandroid.data.model.Message;
import com.example.proyectoandroid.databinding.ItemImageOtherBinding;
import com.example.proyectoandroid.databinding.ItemImageOwnBinding;
import com.example.proyectoandroid.databinding.ItemMessageOtherBinding;
import com.example.proyectoandroid.databinding.ItemMessageOwnBinding;
import com.example.proyectoandroid.util.DateFormatter;

import java.util.Locale;
import java.util.Objects;

/**
 * Historial del chat: burbuja a la derecha para mensajes propios y a la izquierda para ajenos.
 * Cuatro view types: texto/imagen x propio/ajeno.
 */
public class MessageAdapter extends ListAdapter<Message, RecyclerView.ViewHolder> {

    private static final int VIEW_OWN_TEXT = 0;
    private static final int VIEW_OTHER_TEXT = 1;
    private static final int VIEW_OWN_IMAGE = 2;
    private static final int VIEW_OTHER_IMAGE = 3;

    private final String currentUid;

    public MessageAdapter(String currentUid) {
        super(DIFF_CALLBACK);
        this.currentUid = currentUid;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        boolean own = currentUid != null && currentUid.equals(message.getSenderId());
        boolean image = Message.TYPE_IMAGE.equals(message.getType());
        if (image) {
            return own ? VIEW_OWN_IMAGE : VIEW_OTHER_IMAGE;
        }
        return own ? VIEW_OWN_TEXT : VIEW_OTHER_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_OWN_TEXT:
                return new OwnTextHolder(ItemMessageOwnBinding.inflate(inflater, parent, false));
            case VIEW_OWN_IMAGE:
                return new OwnImageHolder(ItemImageOwnBinding.inflate(inflater, parent, false));
            case VIEW_OTHER_IMAGE:
                return new OtherImageHolder(ItemImageOtherBinding.inflate(inflater, parent, false));
            default:
                return new OtherTextHolder(ItemMessageOtherBinding.inflate(inflater, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = getItem(position);
        if (holder instanceof OwnTextHolder) {
            ((OwnTextHolder) holder).bind(message);
        } else if (holder instanceof OtherTextHolder) {
            ((OtherTextHolder) holder).bind(message);
        } else if (holder instanceof OwnImageHolder) {
            ((OwnImageHolder) holder).bind(message);
        } else {
            ((OtherImageHolder) holder).bind(message);
        }
    }

    static String initialOf(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        return name.trim().substring(0, 1).toUpperCase(Locale.getDefault());
    }

    static class OwnTextHolder extends RecyclerView.ViewHolder {
        private final ItemMessageOwnBinding binding;

        OwnTextHolder(ItemMessageOwnBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message) {
            binding.tvSender.setText(message.getSenderName());
            binding.tvText.setText(message.getText());
            binding.tvTime.setText(DateFormatter.format(message.getTimestamp()));
        }
    }

    static class OtherTextHolder extends RecyclerView.ViewHolder {
        private final ItemMessageOtherBinding binding;

        OtherTextHolder(ItemMessageOtherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message) {
            binding.tvSender.setText(message.getSenderName());
            binding.tvAvatar.setText(initialOf(message.getSenderName()));
            binding.tvText.setText(message.getText());
            binding.tvTime.setText(DateFormatter.format(message.getTimestamp()));
        }
    }

    static class OwnImageHolder extends RecyclerView.ViewHolder {
        private final ItemImageOwnBinding binding;

        OwnImageHolder(ItemImageOwnBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message) {
            binding.tvSender.setText(message.getSenderName());
            binding.tvTime.setText(DateFormatter.format(message.getTimestamp()));
            Glide.with(binding.ivImage).load(message.getImageUrl()).centerCrop().into(binding.ivImage);
        }
    }

    static class OtherImageHolder extends RecyclerView.ViewHolder {
        private final ItemImageOtherBinding binding;

        OtherImageHolder(ItemImageOtherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message) {
            binding.tvSender.setText(message.getSenderName());
            binding.tvAvatar.setText(initialOf(message.getSenderName()));
            binding.tvTime.setText(DateFormatter.format(message.getTimestamp()));
            Glide.with(binding.ivImage).load(message.getImageUrl()).centerCrop().into(binding.ivImage);
        }
    }

    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK = new DiffUtil.ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return Objects.equals(oldItem.getType(), newItem.getType())
                    && Objects.equals(oldItem.getText(), newItem.getText())
                    && Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl())
                    && Objects.equals(oldItem.getSenderName(), newItem.getSenderName())
                    && Objects.equals(oldItem.getTimestamp(), newItem.getTimestamp());
        }
    };
}
