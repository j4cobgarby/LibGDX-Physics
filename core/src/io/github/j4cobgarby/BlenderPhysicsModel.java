package io.github.j4cobgarby;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;

public class BlenderPhysicsModel {
	public btRigidBody body;
	private Model model;
	public ModelInstance instance;
	
	/** Creates an object who's collision object is <b>the same</b> as its display object.
	 * <hr/>
	 *  This is rarely <i>as</i> optimised as using a different collision object to the
	 *  display object, but if the display object is a very simple one e.g. constructed
	 *  mainly of primitives; or if it would be hard to create a simpler version, then
	 *  this constructor is probably the way to go.
	 */
	public BlenderPhysicsModel(String filename) {
		model = Resources.Assets.assets.get(filename, Model.class);
		instance = new ModelInstance(model);
		
		AdvancedMotionState motionState = new AdvancedMotionState();
		motionState.transform = instance.transform;
		
		btRigidBodyConstructionInfo constInf = new btRigidBodyConstructionInfo
				(0, null, Bullet.obtainStaticNodeShape(model.nodes));
		
		body = new btRigidBody(constInf);
		body.setMotionState(motionState);
		
		Main.dynamicsWorld.addRigidBody(body);
	}
	
	/** Creates an object who's collision object is <b>different</b> to its display object.
	 * <hr/>
	 *  This is useful for optimisation - if the display object has many triangles, it may
	 *  be a good idea to create a simplified version for the collision object.
	 */
	public BlenderPhysicsModel(String modelFilename, String collisionFilename) {
		model = Resources.Assets.assets.get(modelFilename, Model.class);
		instance = new ModelInstance(model);
		
		AdvancedMotionState motionState = new AdvancedMotionState();
		motionState.transform = instance.transform;
		
		btRigidBodyConstructionInfo constInf = new btRigidBodyConstructionInfo
				(0, null, Bullet.obtainStaticNodeShape(Resources.Assets.assets.get(collisionFilename, Model.class).nodes));
		
		body = new btRigidBody(constInf);
		body.setMotionState(motionState);
		
		Main.dynamicsWorld.addRigidBody(body);
	}
}
