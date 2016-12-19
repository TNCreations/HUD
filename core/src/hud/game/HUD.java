package hud.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

//Tutorial used for all Box2D related stuff:
//https://www.youtube.com/user/samich15?&ab_channel=ConnerAnderson
public class HUD extends ApplicationAdapter implements InputProcessor {

    SpriteBatch batch;
    Stage stage;
    TbsHotbar tbsHotbar;
    TbsGUI tbsGUI;
    TbGUI tbGUI, tbHotbar[] = new TbGUI[4], tbInv[] = new TbGUI[2];
    float PPM = 32;
    float fSpeed;
    float fInvPosX, fInvPosY;
    int nInvY, nInvX;
    int nStamina, nHealth, nThirst, nSanity;
    int nAction;
    private Box2DDebugRenderer b2dr;
    private OrthographicCamera camera;
    private World world;
    private Body player, platform;
    InputMultiplexer multiplexer;
    ShapeRenderer SR;
    boolean isStaminaBuffer = false;

    @Override
    public void create() {

        float nWScreen, nHScreen;
        nWScreen = Gdx.graphics.getWidth();
        nHScreen = Gdx.graphics.getHeight();
        batch = new SpriteBatch();
        SR = new ShapeRenderer();
        nStamina = 200;
        nHealth = 200;
        nThirst = 100;
        nSanity = 100;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, nWScreen / 2, nHScreen / 2);

        world = new World(new Vector2(0, 0), false);
        b2dr = new Box2DDebugRenderer();

        player = createBox(8, 12, 16, 32, false);
        platform = createBox(0, 0, 64, 32, true);

        stage = new Stage();
        tbsHotbar = new TbsHotbar();
        nInvY = Gdx.graphics.getHeight() / 2 - 128;
        nInvX = Gdx.graphics.getWidth() - 64;
        for (int i = 0; i < 4; i++) {
            tbHotbar[i] = new TbGUI("", tbsHotbar, 64, 64);
            tbHotbar[i].setY(nInvY);
            tbHotbar[i].setX(nInvX);
            nInvY += 64;
            stage.addActor(tbHotbar[i]);
        }

        setupGUI();
        btnInvListener();

        //http://stackoverflow.com/questions/30902428/libgdx-stage-input-handling
        InputMultiplexer multiplexer = new InputMultiplexer();
        Gdx.input.setInputProcessor(multiplexer);
        multiplexer.addProcessor(this);
        multiplexer.addProcessor(stage);

    }

    @Override
    public void render() {
        update(Gdx.graphics.getDeltaTime());

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        statsBars();
        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        batch.end();
        stage.act();
        stage.draw();

        b2dr.render(world, camera.combined.scl(PPM));
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width / 2, height / 2);
    }

    @Override
    public void dispose() {
        world.dispose();
        b2dr.dispose();
        batch.dispose();
        stage.dispose();
    }

    public void update(float delta) {
        world.step(1 / 60f, 6, 2);
        inputUpdate(delta);
        cameraUpdate(delta);

        batch.setProjectionMatrix(camera.combined);
    }

    public void inputUpdate(float delta) {
        int nHorizontalForce = 0;
        int nVerticalForce = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            nVerticalForce += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            nVerticalForce -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            nHorizontalForce -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            nHorizontalForce += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            nAction = 0;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {

            if (isStaminaBuffer == false) {
                fSpeed = 2.5f;
                if (nStamina > 0) {
                    nStamina--;
                }
                if (nStamina == 0) {
                    isStaminaBuffer = true;

                }
            }
            if (isStaminaBuffer == true) {
                if (nStamina < 200) {
                    nStamina++;
                }
                if (nStamina == 200) {
                    isStaminaBuffer = false;
                }
                fSpeed = 1.5f;
            }
        } else {
            fSpeed = 1.5f;
            if (nStamina < 200) {
                nStamina++;
            }
            if (nStamina == 200) {
                isStaminaBuffer = false;
            }

        }
        player.setLinearVelocity(nHorizontalForce * fSpeed, player.getLinearVelocity().y);
        player.setLinearVelocity(player.getLinearVelocity().x, nVerticalForce * fSpeed);
    }

    public void cameraUpdate(float delta) {
        Vector3 position = camera.position;
        position.x = player.getPosition().x * PPM;
        position.y = player.getPosition().y * PPM;
        camera.position.set(position);

        camera.update();
    }

    public Body createBox(int nX, int nY, int nWidth, int nHeight, boolean isStatic) {
        Body pBody;
        BodyDef def = new BodyDef();
        if (isStatic) {
            def.type = BodyDef.BodyType.StaticBody;
        } else {
            def.type = BodyDef.BodyType.DynamicBody;
        }
        def.position.set(nX / PPM, nY / PPM);
        def.fixedRotation = true;
        pBody = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(nWidth / 2 / PPM, nHeight / 2 / PPM);
        pBody.createFixture(shape, 1.0f);
        shape.dispose();
        return pBody;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
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
        if (amount == 1 && camera.zoom <= 1.2) {
            camera.zoom += 0.1;
        } else if (amount == -1 && camera.zoom >= 0.4) {
            camera.zoom -= 0.1;
        }
        return false;
    }

    public void handleInput() {
        //https://github.com/libgdx/libgdx/wiki/Orthographic-camera        
        camera.zoom = MathUtils.clamp(camera.zoom, 1.5f, 1.8f);

        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

        camera.position.x = MathUtils.clamp(camera.position.x, effectiveViewportWidth / 2f,
                Gdx.graphics.getWidth() - effectiveViewportWidth / 2f);
        camera.position.y = MathUtils.clamp(camera.position.y, effectiveViewportHeight / 2f,
                Gdx.graphics.getHeight() - effectiveViewportHeight / 2f);
    }

    public void btnInvListener() {
        tbHotbar[0].addListener(new ChangeListener() {
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                System.out.println("Sword");
                nAction = 1;
            }
        });
        tbHotbar[1].addListener(new ChangeListener() {
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                System.out.println("Pickaxe");
                nAction = 2;
            }
        });
        tbHotbar[2].addListener(new ChangeListener() {
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                System.out.println("Axe");
                nAction = 3;
            }
        });
        tbHotbar[3].addListener(new ChangeListener() {
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                System.out.println("Hammer");
                nAction = 4;
            }
        });
        tbInv[0].addListener(new ChangeListener() {
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                tbInv[0].remove();
                stage.addActor(tbInv[1]);
            }
        });
        tbInv[1].addListener(new ChangeListener() {
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                tbInv[1].remove();
                stage.addActor(tbInv[0]);
            }
        });

    }

    //Styling for hotbar buttons
    public class TbsHotbar extends TextButton.TextButtonStyle {

        Skin skin = new Skin();
        TextureAtlas buttonAtlas;

        public TbsHotbar() {
            BitmapFont font = new BitmapFont();
            skin.add("default", font);
            buttonAtlas = new TextureAtlas(Gdx.files.internal("gameAssets.atlas"));
            skin.addRegions(buttonAtlas);

            this.up = skin.getDrawable("inventoryIcon");
            this.down = skin.getDrawable("inventoryIconPressed");
            this.over = skin.getDrawable("inventoryIconHover");
            this.font = skin.getFont("default");
        }
    }

    //Styling for still status icons
    public class TbsGUI extends TextButton.TextButtonStyle {

        Skin skin = new Skin();
        TextureAtlas buttonAtlas;

        public TbsGUI(String sGUI) {
            BitmapFont font = new BitmapFont();
            skin.add("default", font);
            buttonAtlas = new TextureAtlas(Gdx.files.internal("gameAssets.atlas"));
            skin.addRegions(buttonAtlas);
            this.up = skin.getDrawable(sGUI);
            this.font = skin.getFont("default");
        }
    }

    public class TbGUI extends TextButton {

        String sText;

        public TbGUI(String _sText, TextButton.TextButtonStyle _tbs, int nW, int nH) {
            super(_sText, _tbs);
            sText = _sText;
            this.setSize(nW, nH);
            this.addListener(new ClickListener() {
                public void clicked(InputEvent e, float x, float y) {
                    //System.out.println("clicked");
                }
            });
        }
    }

    public void setupGUI() {

        tbsGUI = new TbsGUI("inventory");
        tbInv[1] = new TbGUI(null, tbsGUI, 200, 262);

        tbsGUI = new TbsGUI("bag");
        tbInv[0] = new TbGUI("Inventory", tbsGUI, 64, 64);
        stage.addActor(tbInv[0]);

        tbsGUI = new TbsGUI("healthIcon");
        tbGUI = new TbGUI(null, tbsGUI, 30, 30);
        tbGUI.setY(Gdx.graphics.getHeight() - 40);
        tbGUI.setX(5);
        stage.addActor(tbGUI);

        tbsGUI = new TbsGUI("thirstIcon");
        tbGUI = new TbGUI(null, tbsGUI, 32, 30);
        tbGUI.setY(Gdx.graphics.getHeight() - 78);
        tbGUI.setX(3);
        stage.addActor(tbGUI);

        tbsGUI = new TbsGUI("sanityIcon");
        tbGUI = new TbGUI(null, tbsGUI, 30, 30);
        tbGUI.setY(Gdx.graphics.getHeight() - 116);
        tbGUI.setX(5);
        stage.addActor(tbGUI);
    }

    public void statsBars() {
        SR.begin(ShapeRenderer.ShapeType.Filled);
        if (nHealth <= 75) {
            SR.setColor(Color.FIREBRICK);
        } else {
            SR.setColor(Color.RED);
        }
        SR.rect(35, Gdx.graphics.getHeight() - 32, nHealth, 15);
        if (isStaminaBuffer) {
            SR.setColor(Color.ROYAL);
        } else if (!isStaminaBuffer) {
            SR.setColor(Color.LIME);
        }
        SR.rect(35, Gdx.graphics.getHeight() - 32 - 7, nStamina, 5);
        SR.setColor(Color.SKY);
        SR.rect(35, Gdx.graphics.getHeight() - 69, nThirst, 10);
        SR.setColor(Color.YELLOW);
        SR.rect(35, Gdx.graphics.getHeight() - 105, nSanity, 10);
        SR.end();
    }

}