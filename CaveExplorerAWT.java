import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CaveExplorerAWT {
    public static void main(String[] args) {
        Frame frame = new Frame("Infinite Cave Explorer (AWT Version)");
        GameCanvas gameCanvas = new GameCanvas();
        frame.add(gameCanvas);
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });

        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        gameCanvas.startGameThread();
    }
}

class GameCanvas extends Canvas implements Runnable {

    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 600;
    private static final int FPS = 60;
    private static final long OPTIMAL_TIME = 1000 / FPS;

    private int currentScrollSpeed = 5;
    private long score = 0;
    private boolean isGameOver = false;
    private boolean isPaused = false;

    private Player player;
    private WorldManager worldManager;

    private Thread gameThread;
    private volatile boolean isRunning = false;

    private Image dbImage;
    private Graphics dbg;

    public GameCanvas() {
        this.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (!isGameOver) {
                        isPaused = !isPaused;
                    }
                }
                if (!isGameOver && !isPaused) {
                    if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
                        player.isMovingUp = true;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
                        player.isMovingDown = true;
                    }
                }
                if (isGameOver && e.getKeyCode() == KeyEvent.VK_R) {
                    restartGame();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
                    player.isMovingUp = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
                    player.isMovingDown = false;
                }
            }
        });
        
        initializeGame();
    }

    private void initializeGame() {
        player = new Player(100, CANVAS_HEIGHT / 2);
        score = 0;
        currentScrollSpeed = 5;

        worldManager = new WorldManager(() -> score);
        isGameOver = false;
        isPaused = false;
        this.requestFocus();
    }
    
    private void restartGame() {
        initializeGame();
        startGameThread();
    }
    
    public void startGameThread() {
        if (gameThread == null || !gameThread.isAlive()) {
            isRunning = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            long startTime = System.currentTimeMillis();
            if (!isPaused) {
                updateGame();
            }
            repaint();
            long elapsedTime = System.currentTimeMillis() - startTime;
            long sleepTime = OPTIMAL_TIME - elapsedTime;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateGame() {
        if (isGameOver) {
            isRunning = false;
            return;
        }
        player.update();
        worldManager.update(currentScrollSpeed);
        if (worldManager.isColliding(player)) {
            isGameOver = true;
        }
        score++;
        if (score % 500 == 0 && currentScrollSpeed < 15) {
            currentScrollSpeed++;
        }
    }
    
    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        if (dbImage == null) {
            dbImage = createImage(CANVAS_WIDTH, CANVAS_HEIGHT);
            dbg = dbImage.getGraphics();
        }
        dbg.setColor(getBackground());
        dbg.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        dbg.setColor(getForeground());
        render(dbg);
        g.drawImage(dbImage, 0, 0, this);
    }
    
    private void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        worldManager.draw(g2d);
        player.draw(g2d);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("Score: " + score, 20, 30);
        g2d.drawString("Speed: " + currentScrollSpeed, 20, 60);
        if (isPaused) {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 60));
            String pausedMsg = "PAUSED";
            int msgWidth = g2d.getFontMetrics().stringWidth(pausedMsg);
            g2d.drawString(pausedMsg, (CANVAS_WIDTH - msgWidth) / 2, CANVAS_HEIGHT / 2);
        }
        if (isGameOver) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 60));
            String gameOverMsg = "GAME OVER";
            int msgWidth = g2d.getFontMetrics().stringWidth(gameOverMsg);
            g2d.drawString(gameOverMsg, (CANVAS_WIDTH - msgWidth) / 2, CANVAS_HEIGHT / 2);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("Score: " + score,(CANVAS_WIDTH - msgWidth) / 2 +50, CANVAS_HEIGHT / 3);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            String restartMsg = "Press 'R' to Restart";
            int restartMsgWidth = g2d.getFontMetrics().stringWidth(restartMsg);
            g2d.drawString(restartMsg, (CANVAS_WIDTH - restartMsgWidth) / 2, CANVAS_HEIGHT / 2 + 50);
        }
    }

    // Player Class
    private class Player {
        int x, y, width = 30, height = 20, speed = 5;
        boolean isMovingUp, isMovingDown;
        Player(int x, int y) { this.x = x; this.y = y; }
        void update() {
            if (isMovingUp) y -= speed;
            if (isMovingDown) y += speed;
            if (y < 0) y = 0;
            if (y + height > CANVAS_HEIGHT) y = CANVAS_HEIGHT - height;
        }

        // Ship Drawing
        void draw(Graphics g) {
            g.setColor(Color.CYAN);
            
            
            int[] xPoints = {
                x + width,        // Nose tip
                x + width - 5,    // Top-back of nose
                x,                // Top-front of body / wing tip
                x,                // Bottom-front of body / other wing tip
                x + width - 5     // Bottom-back of nose
            };
            int[] yPoints = {
                y + height / 2,   // Nose tip (middle of height)
                y + height / 4,   // Top-back of nose (1/4 down from top)
                y,                // Top of body / wing (top of height)
                y + height,       // Bottom of body / wing (bottom of height)
                y + (height * 3) / 4 // Bottom-back of nose (3/4 down from top)
            };
            
            g.fillPolygon(xPoints, yPoints, xPoints.length);
            
            //fire effect from the back
            if (!isPaused && (isMovingUp || isMovingDown || score > 0)) { // Only show fire when moving or game started
                g.setColor(Color.ORANGE);
                
                int[] fireX = {x, x - 10, x}; //behind the ship
                int[] fireY = {y + height/4, y + height/2, y + (height*3)/4};
                g.fillPolygon(fireX, fireY, 3);
            }
        }

        Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    }
    
    private class CaveSegment {
        int x, topY, bottomY;
        static final int SEGMENT_WIDTH = 10;
        CaveSegment(int x, int topY, int bottomY) { this.x = x; this.topY = topY; this.bottomY = bottomY; }
    }
    private interface ScoreProvider { long getScore(); }
    // Cave Generator
    private class CaveGenerator {
        private Random rand = new Random();
        private int lastCenterY = CANVAS_HEIGHT / 2;
        private double currentCaveWidth = 200;
        private int centerVolatility = 10;
        CaveSegment generateNextSegment(int currentX, long score) {
            if (currentCaveWidth > 150) currentCaveWidth -= 0.05;
            centerVolatility = 10 + (int)(score / 1000);
            if (centerVolatility > 25) centerVolatility = 25;
            int centerChange = rand.nextInt(centerVolatility * 2 + 1) - centerVolatility;
            int newCenterY = lastCenterY + centerChange;
            if (newCenterY < 100) newCenterY = 100;
            if (newCenterY > CANVAS_HEIGHT - 100) newCenterY = CANVAS_HEIGHT - 100;
            int top = newCenterY - ((int)currentCaveWidth / 2);
            int bottom = newCenterY + ((int)currentCaveWidth / 2);
            lastCenterY = newCenterY;
            return new CaveSegment(currentX, top, bottom);
        }
    }
    private class WorldManager {
        private List<CaveSegment> segments = new ArrayList<>();
        private CaveGenerator generator = new CaveGenerator();
        private ScoreProvider scoreProvider;
        WorldManager(ScoreProvider sp) {
            this.scoreProvider = sp;
            for (int x = 0; x < CANVAS_WIDTH + CaveSegment.SEGMENT_WIDTH; x += CaveSegment.SEGMENT_WIDTH) {
                segments.add(generator.generateNextSegment(x, scoreProvider.getScore()));
            }
        }
        void update(int scrollSpeed) {
            for (CaveSegment seg : segments) seg.x -= scrollSpeed;
            segments.removeIf(seg -> seg.x < -CaveSegment.SEGMENT_WIDTH);
            while (segments.get(segments.size() - 1).x < CANVAS_WIDTH) {
                CaveSegment last = segments.get(segments.size() - 1);
                segments.add(generator.generateNextSegment(last.x + CaveSegment.SEGMENT_WIDTH, scoreProvider.getScore()));
            }
        }
        boolean isColliding(Player p) {
            Rectangle pBounds = p.getBounds();
            for (CaveSegment seg : segments) {
                if (pBounds.getMaxX() > seg.x && pBounds.getX() < seg.x + CaveSegment.SEGMENT_WIDTH) {
                    if (pBounds.getY() < seg.topY || pBounds.getMaxY() > seg.bottomY) return true;
                }
            }
            return false;
        }
        void draw(Graphics g) {
            g.setColor(Color.GRAY);
            Polygon top = new Polygon(), bottom = new Polygon();
            top.addPoint(0, 0); bottom.addPoint(0, CANVAS_HEIGHT);
            for (CaveSegment seg : segments) {
                top.addPoint(seg.x, seg.topY);
                bottom.addPoint(seg.x, seg.bottomY);
            }
            top.addPoint(CANVAS_WIDTH, 0); bottom.addPoint(CANVAS_WIDTH, CANVAS_HEIGHT);
            g.fillPolygon(top);
            g.fillPolygon(bottom);
        }
    }
}