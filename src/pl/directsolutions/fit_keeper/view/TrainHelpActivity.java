package pl.directsolutions.fit_keeper.view;

import pl.directsolutions.fit_keeper.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class TrainHelpActivity  extends Activity {
	
	private TextView textView;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.train_help);
        
        textView = (TextView)findViewById(R.id.TrainHelpTextView);
        textView.setText(getString(R.string.help_tips));
    }
}