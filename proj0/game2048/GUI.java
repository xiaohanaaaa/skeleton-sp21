package game2048;

import ucb.gui2.TopLevel;
import ucb.gui2.LayoutSpec;

import java.util.Observable;
import java.util.Observer;

import java.util.concurrent.ArrayBlockingQueue;

import java.awt.event.KeyEvent;


/** The GUI controller for a 2048 board and buttons.
 *  @author P. N. Hilfinger
 */
class GUI extends TopLevel implements Observer {

    /** Minimum size of board in pixels. */
    private static final int MIN_SIZE = 500;

    /** A new window with given TITLE providing a view of MODEL. */
    GUI(String title, Model model) {
        super(title, true);
        addMenuButton("Game->New", this::newGame);
        addMenuButton("Game->Quit", this::quit);

        addLabel("", "Score", new LayoutSpec("y", 1));

        _model = model;
        _model.addObserver(this);

        _widget = new BoardWidget(model.size());
        add(_widget,
            new LayoutSpec("y", 0,
                           "height", "REMAINDER",
                           "width", "REMAINDER"));

        _widget.requestFocusInWindow();
        _widget.setKeyHandler("keypress", this::keyPressed);
        setPreferredFocus(_widget);
        setScore(0, 0);
    }

    /** Response to "Quit" button click. */
    public void quit(String dummy) {
        _pendingKeys.offer("Quit");
        _widget.requestFocusInWindow();
    }

    /** Response to "New Game" button click. */
    public void newGame(String dummy) {
        _pendingKeys.offer("New Game");
        _widget.requestFocusInWindow();
    }

    /** Respond to the user pressing key E by queuing the key on our
     *  queue of pending keys.*/
    public void keyPressed(String unused, KeyEvent e) {
        String key = keyName(e.getKeyCode());
        if (key != null) {
            _pendingKeys.offer(key);
        }
    }

    /** Return the canonical name ("Up", "Down", "Left", "Right") for key
     *  code CODE, mapping arrow keys and WASD alike. Returns null for any
     *  other key. */
    private static String keyName(int code) {
        switch (code) {
            case KeyEvent.VK_UP: case KeyEvent.VK_W:
                return "Up";
            case KeyEvent.VK_DOWN: case KeyEvent.VK_S:
                return "Down";
            case KeyEvent.VK_LEFT: case KeyEvent.VK_A:
                return "Left";
            case KeyEvent.VK_RIGHT: case KeyEvent.VK_D:
                return "Right";
            default:
                return null;
        }
    }

    /** Return the next pending event, waiting for it as necessary.
     *  Ordinary key presses are reported as the key codes of the
     *  character pressed.  In addition, menu-button clicks result in
     *  the messages "Quit" or "New Game". */
    String readKey() {
        try {
            return _pendingKeys.take();
        } catch (InterruptedException excp) {
            throw new Error("unexpected interrupt");
        }
    }

    /** Set the current score being displayed to SCORE and the current
     *  maximum score to MAXSCORE. */
    public void setScore(int score, int maxScore) {
        setLabel("Score", String.format("Score: %6d / Max score: %6d",
                                        score, maxScore));
    }

    /** The model notifies me that is has changed when its notifyObservers
     *  method is called, because my constructor registered me as an
     *  Observer of the model. */
    @Override
    public void update(Observable model, Object arg) {
        _widget.update(_model);
        setScore(_model.score(), _model.maxScore());
    }

    /** The board widget. */
    private BoardWidget _widget;
    /** The game model being viewed. */
    private Model _model;

    /** Queue of pending key presses. */
    private ArrayBlockingQueue<String> _pendingKeys =
        new ArrayBlockingQueue<>(5);

}
