package com.mygdx.physics1;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Physics5 extends ApplicationAdapter  {
    SpriteBatch batch;
    Sprite sprite1, sprite2;
    Texture img;
    World world;
    Body body1, body2;
    Body bodyEdgeScreen;

    Matrix4 debugMatrix;
    OrthographicCamera camera;

    final float PIXELS_TO_METERS = 100f;

    final short PHYSICS_ENTITY = 0x1; // 0001
    final short WORLD_ENTITY = 0x1 << 1; // 0010 or 0x2 in hex

    @Override
    public void create () {

        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");

        // Two identical sprites, slightly offset vertically.
        sprite1 = new Sprite(img);
        sprite1.setPosition(
                -Gdx.graphics.getWidth() / 2,
                -Gdx.graphics.getHeight() / 2 + 200
        );

        sprite2 = new Sprite(img);
        sprite2.setPosition(
                -Gdx.graphics.getWidth() / 2 + 20,
                -Gdx.graphics.getHeight() / 2 + 400
        );

        world = new World(new Vector2(0, -1f), true);

        // Sprite 1's physics body
        final BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(
                (sprite1.getX() + sprite1.getWidth() / 2) / PIXELS_TO_METERS,
                (sprite1.getY() + sprite1.getHeight() / 2) / PIXELS_TO_METERS
        );
        body1 = world.createBody(bodyDef);

        // Sprite 2's physics body
        BodyDef bodyDef2 = new BodyDef();
        bodyDef2.type = BodyDef.BodyType.DynamicBody;
        bodyDef2.position.set(
                (sprite2.getX() + sprite2.getWidth() / 2) / PIXELS_TO_METERS,
                (sprite2.getY() + sprite2.getHeight() / 2) / PIXELS_TO_METERS
        );
        body2 = world.createBody(bodyDef2);

        // Both bodies have identical shape.
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(
                sprite1.getWidth() / 2 / PIXELS_TO_METERS,
                sprite1.getHeight() / 2 / PIXELS_TO_METERS
        );

        // Sprite 1
        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.shape = polygonShape;
        fixtureDef1.density = 0.1f;
        fixtureDef1.restitution = 0.5f;
        fixtureDef1.filter.categoryBits = PHYSICS_ENTITY;
        fixtureDef1.filter.maskBits = WORLD_ENTITY;

        // Sprite 2
        FixtureDef fixtureDef2 = new FixtureDef();
        fixtureDef2.shape = polygonShape;
        fixtureDef2.density = 0.1f;
        fixtureDef2.restitution = 0.5f;
        fixtureDef1.filter.categoryBits = PHYSICS_ENTITY;
        fixtureDef1.filter.maskBits = WORLD_ENTITY;

        body1.createFixture(fixtureDef1);
        body2.createFixture(fixtureDef2);
        polygonShape.dispose();

        // Bottom edge of screen.
        BodyDef bodyDef3 = new BodyDef();
        bodyDef3.type = BodyDef.BodyType.StaticBody;
        float w = Gdx.graphics.getWidth() / PIXELS_TO_METERS;
        float h = Gdx.graphics.getHeight() / PIXELS_TO_METERS;
        bodyDef3.position.set(0, 0);

        FixtureDef fixtureDef3 = new FixtureDef();
        fixtureDef3.filter.categoryBits = WORLD_ENTITY;
        fixtureDef3.filter.maskBits = PHYSICS_ENTITY;

        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(-w/2, -h/2, w/2, -h/2);
        fixtureDef3.shape = edgeShape;

        bodyEdgeScreen = world.createBody(bodyDef3);
        bodyEdgeScreen.createFixture(fixtureDef3);
        edgeShape.dispose();

        camera = new OrthographicCamera(
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );
    }

    @Override
    public void render () {
        camera.update();

        // Step the physics simulation forward at a rate of 60 Hz.
        world.step(1f/60f, 6, 2);

        // Update sprite position.
        sprite1.setPosition(
                body1.getPosition().x * PIXELS_TO_METERS - sprite1.getWidth() / 2,
                body1.getPosition().y * PIXELS_TO_METERS - sprite1.getHeight() / 2
        );
        sprite1.setRotation((float) Math.toDegrees(body2.getAngle()));

        sprite2.setPosition(
                body2.getPosition().x * PIXELS_TO_METERS - sprite2.getWidth() / 2,
                body2.getPosition().y * PIXELS_TO_METERS - sprite2.getHeight() / 2
        );
        sprite2.setRotation((float) Math.toDegrees(body1.getAngle()));

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(
                sprite1, sprite1.getX(), sprite1.getY(),
                sprite1.getOriginX(),    sprite1.getOriginY(),
                sprite1.getWidth(),      sprite1.getHeight(),
                sprite1.getScaleX(),     sprite1.getScaleY(),
                sprite1.getRotation()
        );
        batch.draw(
                sprite2, sprite2.getX(), sprite2.getY(),
                sprite2.getOriginX(),    sprite2.getOriginY(),
                sprite2.getWidth(),      sprite2.getHeight(),
                sprite2.getScaleX(),     sprite2.getScaleY(),
                sprite2.getRotation()
        );
        batch.end();
    }

    @Override
    public void dispose () {
        img.dispose();
        world.dispose();
    }
}
