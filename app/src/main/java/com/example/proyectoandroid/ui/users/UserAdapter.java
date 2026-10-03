package com.example.proyectoandroid.ui.users;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoandroid.R;
import com.example.proyectoandroid.data.model.User;
import com.example.proyectoandroid.databinding.ItemUserBinding;
import com.example.proyectoandroid.util.Presence;
import com.google.android.material.color.MaterialColors;

import java.util.Objects;

/** Fila de la lista de usuarios: avatar con la inicial, nombre, correo y estado de conexion. */
public class UserAdapter extends ListAdapter<User, UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(@NonNull User user);
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return Objects.equals(oldItem.getUid(), newItem.getUid());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return Objects.equals(oldItem.getDisplayName(), newItem.getDisplayName())
                    && Objects.equals(oldItem.getEmail(), newItem.getEmail())
                    && Objects.equals(oldItem.getPhotoUrl(), newItem.getPhotoUrl())
                    && oldItem.isOnline() == newItem.isOnline()
                    && Objects.equals(oldItem.getLastSeen(), newItem.getLastSeen());
        }
    };

    private final OnUserClickListener listener;

    public UserAdapter(@NonNull OnUserClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    /**
     * El estado "en linea" depende de la hora actual, no solo de los datos: la Activity llama a esto
     * periodicamente para que quien dejo de dar senales pase a desconectado sin esperar un cambio en Firestore.
     */
    public void refreshPresence() {
        notifyItemRangeChanged(0, getItemCount());
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        private final ItemUserBinding binding;

        UserViewHolder(@NonNull ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull User user, @NonNull OnUserClickListener listener) {
            String name = user.getDisplayName() == null ? "" : user.getDisplayName().trim();
            binding.textName.setText(name);
            binding.textSubtitle.setText(user.getEmail());
            AvatarBinder.bind(binding.textInitial, binding.imagePhoto, name, user.getPhotoUrl());
            bindPresence(user);
            binding.getRoot().setOnClickListener(v -> listener.onUserClick(user));
        }

        private void bindPresence(User user) {
            long now = System.currentTimeMillis();
            boolean online = Presence.isOnline(user, now);
            binding.viewOnline.setVisibility(online ? View.VISIBLE : View.GONE);

            if (online) {
                binding.textStatus.setText(R.string.user_status_online);
                binding.textStatus.setTextColor(MaterialColors.getColor(
                        binding.textStatus, androidx.appcompat.R.attr.colorPrimary));
                binding.textStatus.setVisibility(View.VISIBLE);
            } else if (user.getLastSeen() != null) {
                binding.textStatus.setText(DateUtils.getRelativeTimeSpanString(
                        user.getLastSeen().getTime(), now, DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE));
                binding.textStatus.setTextColor(MaterialColors.getColor(
                        binding.textStatus, com.google.android.material.R.attr.colorOnSurfaceVariant));
                binding.textStatus.setVisibility(View.VISIBLE);
            } else {
                binding.textStatus.setVisibility(View.GONE);
            }
        }
    }
}
