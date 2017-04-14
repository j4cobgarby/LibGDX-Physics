package io.github.j4cobgarby;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw.DebugDrawModes;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

public class Main implements ApplicationListener {
	static PerspectiveCamera cam;
    private ModelBatch modelBatch;
    static Environment environment;
    
    static Array<GameObject> instances;
    static Model primitives;
    static BlenderPhysicsModel worldModel;
    static Player player;
    
    static ArrayMap<String, GameObject.Constructor> constructors;

    private btCollisionConfiguration collisionConfig;
    private btDispatcher dispatcher;
    private AdvancedContactListener contactListener;
    private btBroadphaseInterface broadphase;
    static btDynamicsWorld dynamicsWorld;
    private btConstraintSolver constraintSolver;
    
    private int debugMode = DebugDrawModes.DBG_DrawWireframe;
    private DebugDrawer debugDrawer;
    private boolean debug = false;
    
    private boolean slowmo = false;

    private final Color bg = new Color(0x5998ff); // <- Sky blue

    private boolean paused = false;
    
    private BitmapFont mono12;
    private SpriteBatch spriteBatch;
    private SpriteBatch UIBatch;
    
    private Texture cursor;
    private final int cursorSize = 30;

	@Override
	public void create() {
		// Load all the required assets
		Resources.Assets.init();
		
		// Make the cursor not leave the game window, meaning that the delta
		// cursor position is always working
		Gdx.input.setCursorCatched(true);
		
		// Make a font
		FreeTypeFontGenerator fGen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/SmallTypeWriting.ttf"));
		FreeTypeFontParameter param = new FreeTypeFontParameter();
		param.size = 30;
		param.borderWidth = 0.5f;
		param.borderColor = Color.BLACK;
		param.color = Color.WHITE;
		param.kerning = false;
		mono12 = fGen.generateFont(param);
		fGen.dispose();
		
		// Some sprite batches - these are for drawing 2D stuff
		spriteBatch = new SpriteBatch();
		UIBatch = new SpriteBatch();
		
		// A texture for the crosshair
		cursor = new Texture("cursor.png");

		// Initialise the physics engine
		Bullet.init();

		// The ModelBatch draws 3D models in an environment
		modelBatch = new ModelBatch();
		
		// This is the environment which the 3D models are drawn in.
		// Here all the lighting is added.
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1f));
		environment.add(new DirectionalLight().set(1f, 1f, 1f, 1f, -0.8f, -0.3f));

		// Setting up the camera.
		cam = new PerspectiveCamera(100f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.rotate(Vector3.Y, 180);
		cam.near = 0.25f; 
		cam.far = 400f;
		cam.update();
		
		// Some dimensions to create some basic primitive shapes.
		// The Vector3s denote the dimensions such that each component
		//  is the size on that axis.
		Vector3 groundDimensions = new Vector3(1000f, 1f, 1000f);
		Vector3 boxDimensions = new Vector3(1f, 1f, 1f);
		Vector3 platformDimensions = new Vector3(3f, 0.2f, 3f);
		float barrelHeight = 2f;
		float barrelRad = 0.7f;
		float playerRad = 0.5f;
		float playerHeight = 3f;
		float ballRad = 1f;

		// The ModelBuilder means I can create a 'Model', which contains different
		// 'parts'. Each of these parts can be used to create a ModelInstance.
		ModelBuilder mb = new ModelBuilder();
		mb.begin();

		mb.node().id = "ground";
		mb.part("ground", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal,
				new Material(ColorAttribute.createDiffuse(Color.WHITE)))
				.box(groundDimensions.x, groundDimensions.y, groundDimensions.z);

		mb.node().id = "sphere";
		mb.part("sphere", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal,
				new Material(ColorAttribute.createDiffuse(Color.GREEN)))
                .sphere(ballRad * 2, ballRad * 2, ballRad * 2, 100, 100);

		mb.node().id = "box";
		mb.part("box", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal,
				new Material(ColorAttribute.createDiffuse(Color.LIGHT_GRAY)))
				.box(boxDimensions.x, boxDimensions.y, boxDimensions.z);

        mb.node().id = "player";
        mb.part("player", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.RED)))
                .capsule(playerRad, playerHeight, 20);
        
        mb.node().id = "platform";
		mb.part("platform", GL20.GL_TRIANGLES,
                Usage.Position | Usage.Normal,
				new Material(ColorAttribute.createDiffuse(Color.PURPLE)))
				.box(platformDimensions.x, platformDimensions.y, platformDimensions.z);
		
		mb.node().id = "barrel";
		mb.part("barrel", GL20.GL_TRIANGLES,
				Usage.Position | Usage.Normal,
				new Material(ColorAttribute.createDiffuse(Color.RED)))
				.cylinder(barrelRad*2, barrelHeight, barrelRad*2, 16);

		primitives = mb.end();

		constructors = new ArrayMap<String, GameObject.Constructor>(String.class, GameObject.Constructor.class);

		constructors.put("ground",
				new GameObject.Constructor(primitives,
                        "ground", new btBoxShape(groundDimensions.cpy().scl(0.5f)), 0f));

		constructors.put("sphere",
                new GameObject.Constructor(primitives,
                        "sphere", new btSphereShape(ballRad), 0.6f));

		constructors.put("box",
				new GameObject.Constructor(primitives,
                        "box", new btBoxShape(boxDimensions.cpy().scl(0.5f)), 0.2f));

		constructors.put("cone",
                new GameObject.Constructor(primitives,
                        "cone", new btConeShape(0.5f, 2f), 1f));

		constructors.put("capsule",
                new GameObject.Constructor(primitives,
                        "capsule", new btCapsuleShape(.5f, 1f), 1f));

		constructors.put("cylinder",
				new GameObject.Constructor(primitives,
                        "cylinder", new btCylinderShape(new Vector3(.5f, 1f, .5f)), 1f));

		constructors.put("player",
                new GameObject.Constructor(primitives,
                        "player", new btCapsuleShape(playerRad, playerHeight - 1), 2));
		
		constructors.put("platform",
				new GameObject.Constructor(primitives, 
						"platform", new btBoxShape(platformDimensions.cpy().scl(0.5f)), 0)); // 0 mass for static body
		
		constructors.put("barrel", 
				new GameObject.Constructor(primitives, "barrel", new btCylinderShape(new Vector3(barrelRad, barrelHeight/2, barrelRad)), 2));

		collisionConfig = new btDefaultCollisionConfiguration();
		dispatcher = new btCollisionDispatcher(collisionConfig);
		broadphase = new btDbvtBroadphase();
		constraintSolver = new btSequentialImpulseConstraintSolver();
		dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig);
		dynamicsWorld.setGravity(new Vector3(
		        0,
                -20f,
                0));
		contactListener = new AdvancedContactListener();

		player = new Player(new Vector3(0, 3, 2));
		
		instances = new Array<GameObject>();
		  
		debugDrawer = new DebugDrawer();
		debugDrawer.setDebugMode(debugMode);
		
		dynamicsWorld.setDebugDrawer(debugDrawer);
		
		worldModel = new BlenderPhysicsModel("tests.g3dj");
	}

    @SuppressWarnings("unused")
	private void addCubeTri(int size, int z) {
	    /* Creates a triangular structure made of cubes. */

	    for (int rowWidth = size, rowElevation = 1; rowWidth >= 1; rowWidth--, rowElevation++) {
            float highest = (rowWidth / 2f) - 0.5f, lowest = -highest;
            for (float cX = lowest; cX <= highest; cX++) addCubeAt(cX * 1.f, rowElevation * 1.f, z);
	    }

    }

    private void addCubeAt(Vector3 target) {
        GameObject cube = constructors.values[2].construct();

        cube.transform.trn(target);
        cube.body.proceedToTransform(cube.transform);

        instances.add(cube);
        dynamicsWorld.addRigidBody(cube.body);
    }
    
    private void addCubeAt(float x, float y, float z) {
        addCubeAt(new Vector3(x, y ,z));
    }

    private void addBullet(float x, float y, float z, Vector3 impulse) {
        GameObject thisBullet = constructors.values[1].construct();

        thisBullet.transform.trn(x, y, z);
        thisBullet.body.proceedToTransform(thisBullet.transform);
        thisBullet.body.setUserValue(instances.size);

        instances.add(thisBullet);
        dynamicsWorld.addRigidBody(thisBullet.body);

        instances.get(instances.size - 1).body.applyCentralImpulse(impulse);
    }
    
    private void addBullet(Vector3 pos, Vector3 impulse) {
    	addBullet(pos.x, pos.y, pos.z, impulse);
    }

	@Override
	public void render() {
		Gdx.graphics.setTitle(Integer.toString(Gdx.graphics.getFramesPerSecond()) + " fps");
		
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();

        if (Gdx.input.justTouched()) {
        	addBullet(cam.position.cpy().add(cam.direction.cpy().scl(2)), cam.direction.cpy().scl(30));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) paused = !paused;
        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) debug = !debug;
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) slowmo = !slowmo;

		float delta = Math.min(1f / 60f, Gdx.graphics.getDeltaTime());
		if (slowmo) delta *= 0.05f;

		if (!paused)  dynamicsWorld.stepSimulation(delta, 10, 1f / 60f);

        cam.update();

		Gdx.gl.glClearColor(bg.r, bg.g, bg.b, bg.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		
		modelBatch.render(worldModel.instance, environment);
		modelBatch.render(instances, environment);
		
        modelBatch.end();
        
        if (debug) {
        	debugDrawer.begin(cam);
    		dynamicsWorld.debugDrawWorld();
    		debugDrawer.end();
    		
    		final String debugText = String.format(
		          "DEBUG:\n"
		        + "amount of bodies: %d\n"
		        + "PLAYER INFO:\n"
		        + "%s"
		        + "Cursor delta: %s",
		        instances.size,
		        player.getDebugInfo(),
		        Integer.toString(Gdx.input.getDeltaX()) + ", " + Integer.toString(Gdx.input.getDeltaY()));
	        
	        spriteBatch.begin();
	        mono12.draw(spriteBatch, debugText, 20, Gdx.graphics.getHeight() - 40);
	        spriteBatch.end();
	        
        }
        
        UIBatch.begin();
        UIBatch.draw(cursor, Gdx.graphics.getWidth()/2 - cursorSize/2, Gdx.graphics.getHeight()/2 - cursorSize/2, cursorSize, cursorSize);
        UIBatch.end();

        // Updates the player based on keyboard input
        cam.position.set(player.getPos().add(0, 1, 0));
        player.handleInput(delta);
    }
	
	static void turnCam(float deg) {
		Main.cam.rotate(Vector3.Y.cpy(), deg);
	}

	@Override
	public void dispose() {
		for (GameObject obj : instances)
			obj.dispose();
		instances.clear();

		for (GameObject.Constructor ctor : constructors.values())
			ctor.dispose();
		constructors.clear();

		dynamicsWorld.dispose();
		constraintSolver.dispose();
		broadphase.dispose();
		dispatcher.dispose();
		collisionConfig.dispose();

		contactListener.dispose();

		modelBatch.dispose();
		primitives.dispose();
	}

	@Override
	public void pause() {
	    paused = true;
	}

	@Override
	public void resume() {
	    paused = false;
	}

	@Override
	public void resize(int width, int height) {
	}
}