 package io.github.j4cobgarby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Player {
    GameObject object;

    /* stats */
    private final float speed;

    /* constants */
    private final float forwardMultiplier = 1f;
    private final float strafeMultiplier = 0.9f;
    private final float backMultiplier = 0.85f;
    private final int velocityDeltaScalar = 50;
    private final float jumpForce = 15;
    private final float sprintScalar = 1.3f;
    
    Vector3 resultantVelocity = new Vector3();

    private final int forwardKey;
    private final int backKey;
    private final int leftKey;
    private final int rightKey;
    
    private final int jumpKey;
    
    private float camYRot = 0; // The rotation for the corresponding first person camera
    private float camXRot = 0;
    
    private Vector3 tmp0 = new Vector3();

    /* movement variables */
    // in degrees
    private float angleY;
    private float angleX;

    /** The simplest Player constructor.
     * </hr>
     *  It just spawns a player with some default values at a specific
     *  position.
     */
    public Player(Vector3 spawn) {
        object = Main.constructors.get("player").construct();
        object.transform.trn(spawn);
        object.body.proceedToTransform(object.transform);
 
        // Stop it falling over and such
        object.body.setSleepingThresholds(0, 0);
        object.body.setAngularFactor(0);
        
        object.body.setFriction(0.8f);
        object.body.setRestitution(0);

        Main.dynamicsWorld.addRigidBody(object.body);

        speed = 1.5f;
        angleY = 0;

        forwardKey = Input.Keys.W;
        backKey    = Input.Keys.S;
        leftKey    = Input.Keys.A;
        rightKey   = Input.Keys.D;
        
        jumpKey    = Input.Keys.SPACE;
    }

    public void handleInput(float delta) {
        if (Gdx.input.isKeyPressed(forwardKey)) {
            forward(resultantVelocity);
        }
        if (Gdx.input.isKeyPressed(backKey)) {
            back(resultantVelocity);
        }
        if (Gdx.input.isKeyPressed(leftKey)) {
            left(resultantVelocity);
        }
        if (Gdx.input.isKeyPressed(rightKey)) {
            right(resultantVelocity);
        }
        
        final float deltax = -Gdx.input.getDeltaX() * 0.3f;
        turnY(deltax);
        camYRot += deltax;
        Main.cam.rotate(Vector3.Y, deltax);
        
        
        final float deltay = Gdx.input.getDeltaY() * 0.3f;
        
        Main.cam.rotate(Main.cam.direction.cpy().scl(1, 0, 1).rotate(Vector3.Y.cpy(), 90), -camXRot);
        
        // Work on this:
        camXRot += deltay;
        camXRot = MathUtils.clamp(camXRot, -89, 89);
    	angleX = camXRot;
    	
    	// Rotate by delta y, on the axis perpendicular to the camera's direction.
    	Main.cam.rotate(Main.cam.direction.cpy().scl(1, 0, 1).rotate(Vector3.Y.cpy(), 90), camXRot);
        
        resultantVelocity.scl(delta).scl(velocityDeltaScalar);
        
        if (Gdx.input.isKeyJustPressed(jumpKey)) {
        	jump();
        }

        object.body.setLinearVelocity(new Vector3(
        		(object.body.getLinearVelocity().x + resultantVelocity.x) / 2, 
        		object.body.getLinearVelocity().y, 
        		(object.body.getLinearVelocity().z + resultantVelocity.z) / 2));
   }

    /* Movement methods */

    public void forward(Vector3 to) {
    	to = moveAtAngle(to, 0, forwardMultiplier * (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) ? sprintScalar : 1));
    }

    public void back(Vector3 to) {
    	to = moveAtAngle(to, 180, backMultiplier);
    }

    public void left(Vector3 to) {
    	to = moveAtAngle(to, 90, strafeMultiplier);
    }

    public void right(Vector3 to) {
    	to = moveAtAngle(to, 270, strafeMultiplier);
    }
    
    private Vector3 moveAtAngle(Vector3 to, float deg, float directionMultiplier) {
    	to.z += ((float) Math.cos((angleY + deg) * (Math.PI / 180))) * speed * directionMultiplier;
    	to.x += ((float) Math.sin((angleY + deg) * (Math.PI / 180))) * speed * directionMultiplier;
    	
    	return to;
    }

    public boolean isOnFloor() {
    	return false;
    }
    
    public void jump() {
    	System.out.println(isOnFloor());
        object.body.applyCentralImpulse(new Vector3(0, jumpForce, 0));
    }

    public void turnY(float deg) {
    	angleY += deg;
    }
    
    public void turnX(float deg) {
    	angleX += deg;	
    }

    public float getAngleY() {
        return angleY;
    }

    public void setAngleY(float angle) {
        this.angleY = angle;
    }
    
    public Matrix4 getTransform() {
    	return object.body.getWorldTransform();
    }
    
    public Vector3 getPos() {
    	return getTransform().getTranslation(tmp0);
    }
    
    public String getDebugInfo() {
    	return String.format(
    	   "Global translation: %s\n"
    	 + "Collision flags:    %d\n"
    	 + "Friction:           %f\n"
    	 + "Restitution:        %f\n"
    	 + "Linear velocity:    %s\n"
    	 + "Local inertia:      %s\n",
    	 object.body.getCenterOfMassPosition().toString(),
    	 object.body.getCollisionFlags(),
    	 object.body.getFriction(),
    	 object.body.getRestitution(),
    	 object.body.getLinearVelocity().toString(),
    	 object.body.getLocalInertia().toString());
    }

	public Vector3 getResultantVelocity() {
		return resultantVelocity;
	}

	public void setResultantVelocity(Vector3 resultantVelocity) {
		this.resultantVelocity = resultantVelocity;
	}

	public float getCamYRot() {
		return camYRot;
	}

	public void setCamYRot(float camYRot) {
		this.camYRot = camYRot;
	}

	public float getCamXRot() {
		return camXRot;
	}

	public void setCamXRot(float camXRot) {
		this.camXRot = camXRot;
	}

	public float getAngleX() {
		return angleX;
	}

	public void setAngleX(float angleX) {
		this.angleX = angleX;
	}

	public float getSpeed() {
		return speed;
	}
}
