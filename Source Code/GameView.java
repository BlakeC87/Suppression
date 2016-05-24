package firestormsoftwarestudio.suppression;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Blake on 3/23/2016.
 */

public class GameView extends View {

    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = SuppressionToDo.screenWidth;
        int height = SuppressionToDo.screenHeight;

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

}