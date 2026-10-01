package com.example.proyectoandroid.ui.chat;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoandroid.data.model.Message;
import com.example.proyectoandroid.databinding.ItemMessageOtherBinding;
import com.example.proyectoandroid.databinding.ItemMessageOwnBinding;
import com.example.proyectoandroid.util.DateFormatter;

import java.util.Objects;

/** Historial del chat: burbuja a la derecha para mensajes propios y a la izquierda para ajenos. */
public class MessageAdapter extends ListAdapter<Message, RecyclerView.ViewHolder> {

    private static final int VIEW_OWN = 0;
    private static final int VIEW_OTHER = 1;

    private final String currentUid;

    public MessageAdapter(String currentUid) {
        super(DIFF_CALLBACK);
        this.currentUid = currentUid;
    }

    @Override
    public int getItemViewType(int position) {
        return isOwn(getItem(position)) ? VIEW_OWN : VIEW_OTHER;
    }

    private boolean isOwn(Message message) {
        return currentUid != null && currentUid.equals(message.getSenderId());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_OWN) {
            return new OwnHolder(ItemMessageOwnBinding.inflate(inflater, parent, false));
        }
        return new OtherHolder(ItemMessageOtherBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = getItem(position);
        if (holder instanceof OwnHolder) {
            ((OwnHolder) holder).bind(message);
        } else {
            ((OtherHolder) holder).bind(message);
        }
    }

    static class OwnHolder extends RecyclerView.ViewHolder {
        private final ItemMessageOwnBinding binding;

        OwnHolder(ItemMessageOwnBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message) {
            binding.tvSender.setText(message.getSenderName());
            binding.tvText.setText(message.getText());
            binding.tvTime.setText(DateFormatter.format(message.getTimestamp()));
        }
    }

    static class OtherHolder extends RecyclerView.ViewHolder {
        private final ItemMessageOtherBinding binding;

        OtherHolder(ItemMessageOtherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message) {
            binding.tvSender.setText(message.getSenderName());
            binding.tvText.setText(message.getText());
            binding.tvTime.setText(DateFormatter.format(message.getTimestamp()));
        }
    }

    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK = new DiffUtil.ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return Objects.equals(oldItem.getText(), newItem.getText())
                    && Objects.equals(oldItem.getSenderName(), newItem.getSenderName())
                    && Objects.equals(oldItem.getTimestamp(), newItem.getTimestamp());
        }
    };
}
