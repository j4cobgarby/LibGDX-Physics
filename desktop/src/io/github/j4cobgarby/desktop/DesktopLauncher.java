package io.github.j4cobgarby.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import io.github.j4cobgarby.Main;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		float resScalar = 1500f; // <- This is the window height

		config.width  = Math.round(1.5f * resScalar);
		config.height = Math.round(resScalar);

		new LwjglApplication(new Main(), config);
	}
}
