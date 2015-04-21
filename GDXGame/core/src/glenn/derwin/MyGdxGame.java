package glenn.derwin;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class MyGdxGame implements ApplicationListener {
    private Texture playerImage, asteroidImage, starImage, windImage;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Rectangle player;
    private Array<Rectangle> asteroids;
    private Array<Rectangle> stars;
    private Array<Rectangle> winds;
    private long lastAsteroid, lastStar, lastWind;
    private int score, lives;
    private Sound hit;

    private int screenWidth = 800;
    private int screenHeight = 480;

    @Override
    public void create() {

        asteroidImage = new Texture(Gdx.files.absolute("C:/Users/Glenn/GDXGame/desktop/myassets/asteroid.png"));
        playerImage = new Texture(Gdx.files.absolute("C:/Users/Glenn/GDXGame/desktop/myassets/player.png"));
        windImage = new Texture(Gdx.files.absolute("C:/Users/Glenn/GDXGame/desktop/myassets/wind.png"));
        starImage = new Texture(Gdx.files.absolute("C:/Users/Glenn/GDXGame/desktop/myassets/star.png"));

        //asteroidImage = new Texture(Gdx.files.internal("asteroid.png"));
        //playerImage = new Texture(Gdx.files.internal("player.png"));
        //starImage = new Texture(Gdx.files.internal("star.png"));
        //windImage = new Texture(Gdx.files.internal("wind.png"));

        hit = Gdx.audio.newSound(Gdx.files.absolute("C:/Users/Glenn/GDXGame/desktop/myassets/hit.wav"));

        //hit = Gdx.audio.newSound(Gdx.files.internal("hit.wav"));

        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        player = new Rectangle();
        player.x = screenWidth / 2 - 64 / 2;
        player.y = 20;
        player.width = 64;
        player.height = 64;

        lives = 5;
        score = 0;

        asteroids = new Array<Rectangle>();
        stars = new Array<Rectangle>();
        winds = new Array<Rectangle>();
        spawnAsteroid();
        spawnStar();
        spawnWind();
    }

    private void spawnAsteroid() {
        Rectangle asteroid = new Rectangle();
        asteroid.x = MathUtils.random(0, screenWidth-64);
        asteroid.y = screenHeight;
        asteroid.width = 64;
        asteroid.height = 64;
        asteroids.add(asteroid);
        lastAsteroid = TimeUtils.nanoTime();
    }

    private void spawnStar() {
        Rectangle star = new Rectangle();
        star.x = MathUtils.random(0, screenWidth - 5);
        star.y = screenHeight;
        star.width = 1;
        star.height = MathUtils.random(0, 5);
        stars.add(star);
        lastStar = TimeUtils.nanoTime();
    }

    private void spawnWind() {
        Rectangle wind = new Rectangle();
        wind.x = MathUtils.random(0, screenWidth-64);
        wind.y = screenHeight;
        wind.width = 64;
        wind.height = 64;
        winds.add(wind);
        lastWind = TimeUtils.nanoTime();
    }


    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);


        batch.begin();

        for(Rectangle star : stars) {
            batch.draw(starImage, star.x, star.y);
        }

        batch.draw(playerImage, player.x, player.y);

        for(Rectangle asteroid: asteroids) {
            batch.draw(asteroidImage, asteroid.x, asteroid.y);
        }

        for(Rectangle wind: winds) {
            batch.draw(windImage, wind.x, wind.y);
        }

        font.draw(batch, "Lives: " + lives, 20, screenHeight - 20);
        font.draw(batch, "Score: " + score, 100, screenHeight - 20);

        batch.end();

        if(Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            player.x = touchPos.x - 64 / 2;
        }

        if(Gdx.input.isKeyPressed(Keys.LEFT)) player.x -= 400 * Gdx.graphics.getDeltaTime();
        if(Gdx.input.isKeyPressed(Keys.RIGHT)) player.x += 400 * Gdx.graphics.getDeltaTime();

        if(player.x < 0) player.x = 0;
        if(player.x > screenWidth - 64) player.x = screenWidth - 64;

        if(TimeUtils.nanoTime() - lastAsteroid > 1000000000) spawnAsteroid();
        if(TimeUtils.nanoTime() - lastWind > 2000000000) spawnWind();
        if(TimeUtils.nanoTime() - lastStar > 10000000) spawnStar();

        Iterator<Rectangle> asteroidIter = asteroids.iterator();
        Iterator<Rectangle> starIter = stars.iterator();
        Iterator<Rectangle> windIter = winds.iterator();

        while(asteroidIter.hasNext()) {
            Rectangle asteroid = asteroidIter.next();
            asteroid.y -= 200 * Gdx.graphics.getDeltaTime();

            if(asteroid.y + 64 < 0) { asteroidIter.remove(); }

            if(asteroid.overlaps(player)) {
                hit.play();
                lives--;
                asteroidIter.remove();
            }
        }

        while(windIter.hasNext()) {
            Rectangle wind = windIter.next();
            wind.y -= 200 * Gdx.graphics.getDeltaTime();

            if(wind.y + 64 < 0) { windIter.remove(); }

            if(wind.overlaps(player)) {
                score++;
                windIter.remove();
            }
        }

        while(starIter.hasNext()) {
            Rectangle star = starIter.next();
            star.y -= 200 * Gdx.graphics.getDeltaTime();

            if(star.y + 5 < 0) { starIter.remove(); }
        }
    }

    @Override
    public void dispose() {
        asteroidImage.dispose();
        playerImage.dispose();
        windImage.dispose();
        starImage.dispose();
        batch.dispose();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}
