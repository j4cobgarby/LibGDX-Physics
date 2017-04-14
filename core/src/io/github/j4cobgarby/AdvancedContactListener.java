package io.github.j4cobgarby;

import com.badlogic.gdx.physics.bullet.collision.ContactListener;

/**
 * This class isn't used at the moment. It can call some code whenever two particular objects collide.
 */
public class AdvancedContactListener extends ContactListener {
    @Override
    public boolean onContactAdded(int userValue0, int partId0, int index0, int userValue1, int partId1,
                                  int index1) {
        if (userValue0 != 0) {
        }
        //((ColorAttribute)instances.get(userValue0).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.GREEN);
        if (userValue1 != 0) {
        }
        //((ColorAttribute)instances.get(userValue1).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.GREEN);
        return true;
    } 
}