package dev.sadat.androide.views;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import dev.sadat.androide.listeners.EditorTouchCallback;
import dev.sadat.androide.listeners.EditorViewTouchListener;
import dev.sadat.androide.views.blocks.Block;

public class EditorView extends View implements EditorTouchCallback {

	private boolean isInit = false;

	private float gridWidth = 1;
	private final int GRID_GAP = 20;
	private int numHLines;
	private int numVLines;

	private Rect background;
	private Paint backgroundPaint;
	private Paint gridLinePaint;

	private float[] delta;
	private float zoomLevel = 1;

	private boolean isInsideChild = false;
	private int childIndex = -1;
	private ArrayList<Block> debug;

	private EditorViewTouchListener listener;

	private static CodeBlockPaints paints;
	
	public EditorView(Context context) {
		this(context, null, 0);
	}

	public EditorView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public EditorView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		listener = new EditorViewTouchListener();
		listener.setTouchCallback(this);
		this.setOnTouchListener(listener);
		delta = new float[] { 0, 0 };
		this.setWillNotDraw(false);
	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// Call Initialization only once
		if (isInit == false)
			initialize(canvas);
		canvas.drawRect(background, backgroundPaint);
		drawGridLines(canvas);
		canvas.save();
		canvas.translate(delta[0], delta[1]);
		canvas.scale(zoomLevel, zoomLevel);
		// TODO Code Block Drawing
		for (int i=debug.size()-1; i>=0;i--){
			debug.get(i).draw(canvas);
		}
			canvas.restore();
	}

	private void drawGridLines(Canvas canvas) {
		for (int i = 0; i <= numHLines; i++)
			canvas.drawLine(0, GRID_GAP * i, super.getWidth(), GRID_GAP * i, gridLinePaint);
		for (int i = 0; i < numVLines; i++)
			canvas.drawLine(GRID_GAP * i, 0, GRID_GAP * i, super.getHeight(), gridLinePaint);
	}

	private void initialize(Canvas canvas) {
		background = new Rect(0, 0, super.getWidth(), super.getHeight());
		backgroundPaint = new Paint();
		backgroundPaint.setColor(Color.DKGRAY);
		backgroundPaint.setStyle(Paint.Style.FILL);
		setGridLineConfig();
		isInit = true;

		paints = new CodeBlockPaints();
		
		debug = new ArrayList<Block>();

		for (int i = 0; i < 600; i++) {
			Block block = new Block(this, 100 * i, 50 * (i % 5), (i%2==0)?paints.BOOLEAN:paints.STRING);
			debug.add(block);
		}

	}

	private void setGridLineConfig() {
		numHLines = super.getHeight() / GRID_GAP;
		numVLines = super.getWidth() / GRID_GAP;
		gridLinePaint = new Paint();
		gridLinePaint.setColor(Color.GRAY);
		gridLinePaint.setStyle(Paint.Style.STROKE);
		gridLinePaint.setStrokeWidth(gridWidth);
	}

	public CodeBlockPaints getPaints(){
		return paints;
	}
	
	@Override
	public boolean motionEvent(int type, float deltaX, float deltaY, float[] coord) {
		coord[0] -= delta[0];
		coord[1] -= delta[1];
		if (childIndex == -1) {
			for (Block v : debug) {
				Rect r = v.getBounds();
				if (r.contains((int)coord[0], (int)coord[1])) {
					isInsideChild = v.onBlockTouched(type, deltaX, deltaY);
					childIndex = debug.indexOf(v);
					invalidate();
					if (isInsideChild = true) {
						break;
					}
				}
			}
		} else if (childIndex > -1) {
			debug.get(childIndex).onBlockTouched(type, deltaX, deltaY);
			invalidate();
		}
		// Do Editor Events if Block Events are done
		if (type == EditorTouchCallback.SCROLL && !isInsideChild) {
			delta[0] += deltaX;
			delta[1] += deltaY;
			childIndex = -99;
			invalidate();
		}else if (type == EditorTouchCallback.ZOOM && !isInsideChild) {
			zoomLevel = deltaX;
			childIndex = -99;
			invalidate();
		}

		// Move this to last
		// Chain the actions together
		if (type == EditorTouchCallback.UNTOUCH) {
			isInsideChild = false;
			childIndex = -1;
		}

		return true;
	}

}