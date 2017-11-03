package com.mygdx.physics1;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Physics1 extends ApplicationAdapter {
	SpriteBatch batch;
	Sprite logoSprite;
	Texture logoTexture;
	World world;
	Body logoBody;
	
	@Override
	public void create () {
		batch = new SpriteBatch();

		// We need a logo_sprite since the logo_texture is going to move.
		logoTexture = new Texture("badlogic.jpg");
		logoSprite = new Sprite(logoTexture);

		// Center the sprite in the top/middle of screen
		logoSprite.setPosition(
				Gdx.graphics.getWidth() / 2 - logoSprite.getWidth() / 2,
				Gdx.graphics.getHeight() / 2
		);

		// Create a physics world, passing in the gravity.
		// Allow it to sleep while inactive.
		world = new World(new Vector2(0, -98f), true);

		// Create a logo_body definition, which defines the physics object type and position.
		BodyDef logoBodyDef = new BodyDef();
		logoBodyDef.type = BodyDef.BodyType.DynamicBody;

		// Use 1 to 1 dimensions - so 1 unit in the physics engine is 1 pixel.
		// Set the logo_body to the same position as the sprite.
		logoBodyDef.position.set(logoSprite.getX(), logoSprite.getY());

		// Create a logo_body in the world using this definition.
		logoBody = world.createBody(logoBodyDef);

		// Define the position of the physics shape.
		PolygonShape logoShape = new PolygonShape();

		// Set the physics polygon to a box with the same dimensions as our sprite.
		logoShape.setAsBox(logoSprite.getWidth() / 2, logoSprite.getHeight() / 2);

		// Define physical properties like density and restitution.
		FixtureDef logoFixtureDef = new FixtureDef();
		logoFixtureDef.shape = logoShape;
		logoFixtureDef.density = 1f;

		Fixture logoFixture = logoBody.createFixture(logoFixtureDef);

		// Shape is the only disposable asset here.
		logoShape.dispose();
	}

	@Override
	public void render () {

		// Advance the world by the amount of time elapsed since the last frame.
		// Don't do this in render in a real game.
		world.step(Gdx.graphics.getDeltaTime(), 6, 2);

		// Update the sprite position to its updated  body.
		logoSprite.setPosition(logoBody.getPosition().x, logoBody.getPosition().y);

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(logoSprite, logoSprite.getX(), logoSprite.getY());
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		logoTexture.dispose();
		world.dispose();
	}
}
