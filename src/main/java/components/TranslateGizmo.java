package components;

import com.avi.Mario.editor.PropertiesWindow;
import com.avi.Mario.jade.MouseListener;

public class TranslateGizmo extends Gizmo {
    public TranslateGizmo(Sprite arrowSprite, PropertiesWindow propertiesWindow) {
        super(arrowSprite, propertiesWindow);
    }

    @Override
    public void update(float dt) {
        if(activeGameObject != null) {
            if(xAxisActive && !yAxisActive) {
                activeGameObject.transform.position.x -= MouseListener.getWorldDx();
                activeGameObject.transform.position.y -= MouseListener.getWorldDy();
            }
        }
        super.update(dt);
    }

}
