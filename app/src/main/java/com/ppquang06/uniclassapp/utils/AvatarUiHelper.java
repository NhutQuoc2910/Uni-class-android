package com.ppquang06.uniclassapp.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.ppquang06.uniclassapp.R;

public final class AvatarUiHelper {

    private AvatarUiHelper() {
        // Utility class
    }

    /**
     * Loads the avatar into the topAppBar's ivAvatar ImageView.
     * Ensures visibility is VISIBLE, clears any vector tint, and sets zero padding
     * so that Glide circleCrop image renders cleanly. If the URL is empty, resets
     * to the default person icon with proper tint and padding.
     */
    public static void loadTopBarAvatar(@Nullable ImageView ivAvatar, @Nullable String rawAvatarUrl) {
        if (ivAvatar == null) return;
        Context context = ivAvatar.getContext();
        if (context == null) return;

        ivAvatar.setVisibility(View.VISIBLE);
        String resolvedUrl = UriFileMultipartHelper.resolveFullFileUrl(rawAvatarUrl);

        if (!TextUtils.isEmpty(resolvedUrl)) {
            ivAvatar.setImageTintList(null);
            ivAvatar.clearColorFilter();
            ivAvatar.setPadding(0, 0, 0, 0);
            Glide.with(context)
                    .load(resolvedUrl)
                    .circleCrop()
                    .placeholder(R.drawable.bg_circle_aura)
                    .error(R.drawable.bg_circle_aura)
                    .into(ivAvatar);
        } else {
            int p = (int) (6 * context.getResources().getDisplayMetrics().density);
            ivAvatar.setPadding(p, p, p, p);
            ivAvatar.setImageResource(R.drawable.ic_person_outline);
            ivAvatar.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.p2_lavender)));
        }
    }
}
