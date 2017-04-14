package io.github.j4cobgarby;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;

public class Resources {
	public static class Assets {
		public static AssetManager assets = new AssetManager();
		
		public static void init() {
			assets.load("tests.g3dj", Model.class);
			assets.finishLoading();
		}
	}
}
