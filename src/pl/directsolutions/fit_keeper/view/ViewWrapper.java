package pl.directsolutions.fit_keeper.view;

import pl.directsolutions.fit_keeper.R;
import android.view.View;
import android.widget.TextView;

public class ViewWrapper {
	View base;
	TextView details = null;
	TextView label = null;

	ViewWrapper(View base) {
		this.base = base;
	}

	TextView getDetails() {
		if (details == null) {
			details = (TextView) base.findViewById(R.id.row_text_info);
		}
		return (details);
	}

	TextView getLabel() {
		if (label == null) {
			label = (TextView) base.findViewById(R.id.row_text_label);
		}
		return (label);
	}
}
