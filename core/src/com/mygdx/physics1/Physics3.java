package com.mygdx.physics1;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Physics3 extends ApplicationAdapter implements InputProcessor {
    SpriteBatch batch;
    Sprite logo_sprite;
    Texture logo_texture;
    World world;
    Body logo_body;
    Body screen_edge_body;

    Box2DDebugRenderer debugRenderer;
    Matrix4 debugMatrix;
    OrthographicCamera camera;
    BitmapFont font;

    float torque = 0.0f;
    boolean drawSprite = true;
    final float PIXELS_TO_METERS = 100f;

    @Override
    public void create () {

        batch = new SpriteBatch();
        logo_texture = new Texture("badlogic.jpg");
        logo_sprite = new Sprite(logo_texture);
        logo_sprite.setPosition(
                -Gdx.graphics.getWidth() / 2,
                -Gdx.graphics.getHeight() / 2
        );

        // Create a world with no gravity.
        world = new World(new Vector2(0, -1f), true);

        // Create body in world.
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(
                // Body's position is going to be off from the sprite's...
                (logo_sprite.getX() + logo_sprite.getWidth() / 2) / PIXELS_TO_METERS,
                (logo_sprite.getY() + logo_sprite.getHeight() / 2) / PIXELS_TO_METERS
        );
        logo_body = world.createBody(bodyDef);

        // Define the position of the physics shape.
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(
                logo_sprite.getWidth() / 2 / PIXELS_TO_METERS,
                logo_sprite.getHeight() / 2 / PIXELS_TO_METERS
        );

        // Define physical properties like density and restitution.
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = polygonShape;
        fixtureDef.density = 0.1f;
        fixtureDef.restitution = 0.5f;
        logo_body.createFixture(fixtureDef);
        polygonShape.dispose();

        // Wall body.
        BodyDef bodyDef2 = new BodyDef();
        bodyDef2.type = BodyDef.BodyType.StaticBody;
        float w = Gdx.graphics.getWidth() / PIXELS_TO_METERS;
        // So that we can see edge in debug renderer, set height to 50 pixels above bottom.
        float h = Gdx.graphics.getHeight() / PIXELS_TO_METERS - 50 / PIXELS_TO_METERS;
        bodyDef2.position.set(0, 0);

        FixtureDef fixtureDef2 = new FixtureDef();
        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(-w/2, -h/2, w/2, -h/2);
        fixtureDef2.shape = edgeShape;

        screen_edge_body = world.createBody(bodyDef2);
        screen_edge_body.createFixture(fixtureDef2);
        edgeShape.dispose();

        Gdx.input.setInputProcessor(this);

        // Box2DDebugRenderer allows us to see the physics simulation controlling the scene
        debugRenderer = new Box2DDebugRenderer();
        font = new BitmapFont();
        font.setColor(Color.BLACK);
        camera = new OrthographicCamera(
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );
    }

    private float ellapsed = 0;

    @Override
    public void render () {
        camera.update();

        // Step the physics simulation forward at a rate of 60 Hz.
        world.step(1f/60f, 6, 2);

        // Apply torque to physics body.
        logo_body.applyTorque(torque, true);

        // Update sprite position.
        logo_sprite.setPosition(
                logo_body.getPosition().x * PIXELS_TO_METERS - logo_sprite.getWidth() / 2,
                logo_body.getPosition().y * PIXELS_TO_METERS - logo_sprite.getHeight() / 2
        );
        logo_sprite.setRotation((float) Math.toDegrees(logo_body.getAngle()));

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);

        // Scale down the sprite batch's projection matrix to box2d size.
        debugMatrix = batch.getProjectionMatrix().cpy().scale(
                PIXELS_TO_METERS,
                PIXELS_TO_METERS, 0
        );

        batch.begin();
        if (drawSprite) {
            batch.draw(
                    logo_sprite, logo_sprite.getX(), logo_sprite.getY(),
                    logo_sprite.getOriginX(), logo_sprite.getOriginY(),
                    logo_sprite.getWidth(), logo_sprite.getHeight(),
                    logo_sprite.getScaleX(), logo_sprite.getScaleY(),
                    logo_sprite.getRotation()
            );
        }
        font.draw(
                batch,
                "Restitution: " + logo_body.getFixtureList().first().getRestitution(),
                -Gdx.graphics.getWidth() / 2,
                Gdx.graphics.getHeight() / 2
        );
        batch.end();

        // Now render the physics world using the scaled-down matrix.
        // Just for debugging.
        debugRenderer.render(world, debugMatrix);
    }

    @Override
    public void dispose () {
        logo_texture.dispose();
        world.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        // On right or left arrow set the velocity at a fixed rate in thatdirection
        if(keycode == Input.Keys.RIGHT)
            logo_body.setLinearVelocity(1f, 0f);
        if(keycode == Input.Keys.LEFT)
            logo_body.setLinearVelocity(-1f, 0f);

        if(keycode == Input.Keys.UP)
            logo_body.applyForceToCenter(0f, 10f, true);
        if(keycode == Input.Keys.DOWN)
            logo_body.applyForceToCenter(0f, -10f, true);

        // On brackets ( [ ] ) apply torque, either clock or counterclockwise
        if(keycode == Input.Keys.RIGHT_BRACKET)
            torque += 0.1f;
        if(keycode == Input.Keys.LEFT_BRACKET)
            torque -= 0.1f;

        // Remove the torque using backslash /
        if(keycode == Input.Keys.BACKSLASH)
            torque = 0.0f;

        // If user hits spacebar, reset everything back to normal
        if(keycode == Input.Keys.SPACE) {
            logo_body.setLinearVelocity(0f, 0f);
            logo_body.setAngularVelocity(0f);
            torque = 0f;
            logo_sprite.setPosition(0f,0f);
            logo_body.setTransform(0f,0f,0f);
        }

        if(keycode == Input.Keys.COMMA) {
            logo_body.getFixtureList().first().setRestitution(
                    logo_body.getFixtureList().first().getRestitution() - 0.1f
            );
        }
        if(keycode == Input.Keys.PERIOD) {
            logo_body.getFixtureList().first().setRestitution(
                    logo_body.getFixtureList().first().getRestitution() + 0.1f
            );
        }

        // The ESC key toggles the visibility of the sprite
        // Allow user to see physics debug info
        if(keycode == Input.Keys.ESCAPE)
            drawSprite = !drawSprite;

        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }


    // On touch we apply force from the direction of the users touch.
    // This could result in the object "spinning"
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        logo_body.applyForce(1f, 1f, screenX, screenY, true);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
