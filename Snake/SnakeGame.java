import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

public class SnakeGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
}

class GameFrame extends JFrame {
    public GameFrame() {
        setTitle("Snake Joaoz - ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        GamePanel panel = new GamePanel(30, 20, 24); // cols, rows, cellSize
        add(panel);
        pack();
        setLocationRelativeTo(null);
    }
}

class GamePanel extends JPanel implements ActionListener {
    private final int cols; // largura em células
    private final int rows; // altura em células
    private final int cell; // tamanho do pixel da célula
    private final int width; // pixels
    private final int height; // pixels

    private final LinkedList<Point> snake = new LinkedList<>();
    private Point food;
    private Direction dir = Direction.RIGHT;
    private boolean running = false;
    private Timer timer;
    private final Random rnd = new Random();
    private int score = 0;
    private int speed = 120; // delay ms

    private enum Direction {UP, DOWN, LEFT, RIGHT}

    public GamePanel(int cols, int rows, int cell) {
        this.cols = cols;
        this.rows = rows;
        this.cell = cell;
        this.width = cols * cell;
        this.height = rows * cell;
        setPreferredSize(new Dimension(width + 200, height)); // adiciona painel lateral
        setBackground(new Color(7, 20, 34));
        setFocusable(true);
        initGame();
        setupKeyBindings();
        // foca automaticamente no painel de jogo para permitir controles sem clicar
        requestFocusInWindow();
    }

    private void initGame() {
        snake.clear();
        snake.add(new Point(cols/2, rows/2));
        snake.add(new Point(cols/2 -1, rows/2));
        snake.add(new Point(cols/2 -2, rows/2));
        dir = Direction.RIGHT;
        placeFood();
        score = 0;
        running = true;
        if(timer != null) timer.stop();
        timer = new Timer(speed, this);
        timer.start();
    }

    private void placeFood() {
        while(true) {
            Point p = new Point(rnd.nextInt(cols), rnd.nextInt(rows));
            boolean coll = false;
            for(Point s : snake) if(s.equals(p)) { coll = true; break; }
            if(!coll) { food = p; return; }
        }
    }

    private void setupKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("UP"), "up");
        im.put(KeyStroke.getKeyStroke("DOWN"), "down");
        im.put(KeyStroke.getKeyStroke("LEFT"), "left");
        im.put(KeyStroke.getKeyStroke("RIGHT"), "right");
        im.put(KeyStroke.getKeyStroke("W"), "up");
        im.put(KeyStroke.getKeyStroke("S"), "down");
        im.put(KeyStroke.getKeyStroke("A"), "left");
        im.put(KeyStroke.getKeyStroke("D"), "right");
        im.put(KeyStroke.getKeyStroke("SPACE"), "restart");
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "pause");

        am.put("up", new AbstractAction(){ public void actionPerformed(ActionEvent e){ if(dir!=Direction.DOWN) dir = Direction.UP; }});
        am.put("down", new AbstractAction(){ public void actionPerformed(ActionEvent e){ if(dir!=Direction.UP) dir = Direction.DOWN; }});
        am.put("left", new AbstractAction(){ public void actionPerformed(ActionEvent e){ if(dir!=Direction.RIGHT) dir = Direction.LEFT; }});
        am.put("right", new AbstractAction(){ public void actionPerformed(ActionEvent e){ if(dir!=Direction.LEFT) dir = Direction.RIGHT; }});
        am.put("restart", new AbstractAction(){ public void actionPerformed(ActionEvent e){ initGame(); repaint(); }});
        am.put("pause", new AbstractAction(){ public void actionPerformed(ActionEvent e){ togglePause(); }});
    }

    private void togglePause() {
        if(!running) { running = true; timer.start(); }
        else { running = false; timer.stop(); }
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(running) gameStep();
    }

    private void gameStep() {
        Point head = new Point(snake.getFirst());
        switch(dir) {
            case UP: head.y -= 1; break;
            case DOWN: head.y += 1; break;
            case LEFT: head.x -= 1; break;
            case RIGHT: head.x += 1; break;
        }

        if(head.x < 0 || head.y < 0 || head.x >= cols || head.y >= rows) {
            gameOver(); return;
        }

        for(Point s : snake) if(s.equals(head)) { gameOver(); return; }

        snake.addFirst(head);

        if(head.equals(food)) {
            score += 10;
            placeFood();
            if(speed > 40) {
                speed -= 4;
                timer.setDelay(speed);
            }
        } else {
            snake.removeLast();
        }

        repaint();
    }

    private void gameOver() {
        running = false;
        timer.stop();
        int opt = JOptionPane.showOptionDialog(this, "Game Over! Pontuação: " + score + "\nDeseja reiniciar?",
                "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
        if(opt == JOptionPane.YES_OPTION) initGame();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelX = 0;
        int panelY = 0;

        g2.setColor(new Color(6, 18, 36));
        g2.fillRect(panelX, panelY, cols*cell, rows*cell);

        g2.setColor(new Color(255,255,255,18));
        for(int i=0;i<=cols;i++) g2.drawLine(panelX + i*cell, panelY, panelX + i*cell, panelY + rows*cell);
        for(int j=0;j<=rows;j++) g2.drawLine(panelX, panelY + j*cell, panelX + cols*cell, panelY + j*cell);

        g2.setColor(new Color(255,98,120));
        fillRoundedCell(g2, food.x, food.y);

        boolean head = true;
        for(Point s : snake) {
            if(head) {
                g2.setColor(new Color(0, 230, 160));
                fillRoundedCell(g2, s.x, s.y);
                head = false;
            } else {
                g2.setColor(new Color(13, 209, 163));
                fillRoundedCell(g2, s.x, s.y);
            }
        }

        int sideX = cols*cell + 12;
        int pad = 12;
        g2.setColor(new Color(3,10,18));
        g2.fillRoundRect(sideX - 6, pad - 6, 180, height - 2*pad + 12, 12, 12);

        g2.setColor(new Color(180, 220, 230));
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString("Shop SLJ - Cobrinha", sideX + 8, 30);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.setColor(new Color(160,200,210));
        g2.drawString("Pontuação: " + score, sideX + 8, 60);
        g2.drawString("Velocidade: " + Math.max(1, (220 - speed)/10), sideX + 8, 85);
        g2.drawString("Tamanho: " + snake.size(), sideX + 8, 110);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(140,170,180));
        g2.drawString("Controles:", sideX + 8, 150);
        g2.drawString("Setas / WASD - mover", sideX + 8, 170);
        g2.drawString("SPACE - reiniciar", sideX + 8, 190);
        g2.drawString("ESC - pausar", sideX + 8, 210);

        if(!running) {
            String msg = "PAUSADO";
            g2.setFont(new Font("SansSerif", Font.BOLD, 28));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(msg);
            g2.setColor(new Color(255,255,255,160));
            g2.drawString(msg, panelX + (cols*cell - w)/2, panelY + rows*cell/2);
        }

        g2.dispose();
    }

    private void fillRoundedCell(Graphics2D g2, int gx, int gy) {
        int x = gx * cell + 4;
        int y = gy * cell + 4;
        int s = cell - 8;
        g2.fillRoundRect(x, y, s, s, 8, 8);
    }
}