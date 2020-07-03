package de.uni_ulm.uberuniulm.ui.main;

import android.view.View;

public interface ClickListener {
    void onPositionClicked(int position);

    void onOfferClicked(int position);

    void onEditClicked(int position);

    void onMarkClicked(View view, int position);
}
