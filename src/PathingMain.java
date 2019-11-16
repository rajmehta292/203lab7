import processing.core.PApplet;
import processing.core.PImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.*;

public class PathingMain extends PApplet
{
    private static enum GridValues
    {
        BACKGROUND,
        OBSTACLE,
        GOAL,
        SEARCHED
    };

    private int current_image;
    private long next_time;
    private static final int TILE_SIZE = 32;
    private static final int ANIMATION_TIME = 100;
    private static final int ROWS = 15;
    private static final int COLS = 20;
    private Point wPos;
    private boolean drawPath = false;
    private PImage background;
    private PImage obstacle;
    private PImage goal;
    private List<PImage> imgs;
    private List<Point> path;
    private List<Point> m_travelledPosition = new ArrayList<>();
    private GridValues[][] grid;

    public void settings()
    {
        size(640,480);
    }
	
    //runs once to set up world
    public void setup()
    {
        path = new LinkedList<>();
        wPos = new Point(2, 2);
        imgs = new ArrayList<>();
        imgs.add(loadImage("images/wyvern1.bmp"));
        imgs.add(loadImage("images/wyvern2.bmp"));
        imgs.add(loadImage("images/wyvern3.bmp"));
        background = loadImage("images/grass.bmp");
        obstacle = loadImage("images/vein.bmp");
        goal = loadImage("images/water.bmp");
        grid = new GridValues[ROWS][COLS];
        initialize_grid(grid);
        current_image = 0;
        next_time = System.currentTimeMillis() + ANIMATION_TIME;
    }

    //set up a 2D grid to show world
    private static void initialize_grid(GridValues[][] grid)
    {
        for (int row = 0; row < grid.length; row++)
        {
            for (int col = 0; col < grid[row].length; col++)
            {
                grid[row][col] = GridValues.BACKGROUND;
            }
        }

        //set up some obstacles
        for (int row = 2; row < 8; row++)
        {
            grid[row][row + 5] = GridValues.OBSTACLE;
        }

        for (int row = 8; row < 12; row++)
        {
            grid[row][19 - row] = GridValues.OBSTACLE;
        }

        for (int col = 1; col < 8; col++)
        {
            grid[11][col] = GridValues.OBSTACLE;
        }

        grid[13][14] = GridValues.GOAL;
    }

    private void next_image()
    {
        current_image = (current_image + 1) % imgs.size();
    }

    /* runs over and over */
    public void draw()
    {
        // A simplified action scheduling handler
        long time = System.currentTimeMillis();
        if (time >= next_time)
        {
            next_image();
            next_time = time + ANIMATION_TIME;
        }
        draw_grid();
        draw_path();
        image(imgs.get(current_image), wPos.x * TILE_SIZE, wPos.y * TILE_SIZE);
    }

    private void draw_grid()
    {
        for (int row = 0; row < grid.length; row++)
        {
            for (int col = 0; col < grid[row].length; col++)
            {
                draw_tile(row, col);
            }
        }
    }

    private void draw_path()
    {
        if (drawPath)
        {
            for (Point p : path)
            {
                fill(128, 0, 0);
                rect(p.x * TILE_SIZE + TILE_SIZE * 3 / 8, p.y * TILE_SIZE + TILE_SIZE * 3 / 8, TILE_SIZE / 4, TILE_SIZE / 4);
            }
        }
    }

   private void draw_tile(int row, int col)
   {
      switch (grid[row][col])
      {
         case BACKGROUND:
            image(background, col * TILE_SIZE, row * TILE_SIZE);
            break;
         case OBSTACLE:
            image(obstacle, col * TILE_SIZE, row * TILE_SIZE);
            break;
         case SEARCHED:
            fill(0, 128);
            rect(col * TILE_SIZE + TILE_SIZE / 4,row * TILE_SIZE + TILE_SIZE / 4,TILE_SIZE / 2, TILE_SIZE / 2);
            break;
         case GOAL:
            image(goal, col * TILE_SIZE, row * TILE_SIZE);
            break;
      }
   }

   public static void main(String args[])
   {
      PApplet.main("PathingMain");
   }

   public void keyPressed()
   {
      if (key == ' ')
      {
         path.clear();
         initialize_grid(grid);
         moveOnce(wPos, grid, path);
      }
      else if( (key == 'p') || (key == 'P') )
      {
         drawPath ^= true;
      }
      else if( (key == 'c') || (key == 'C') )
      {
         this.m_travelledPosition.clear();
         path.clear();
         initialize_grid(grid);
      }
   }

   private boolean moveOnce(Point pos, GridValues[][] grid, List<Point> path)
   {
        Point rightN = new Point(pos.x , pos.y );
        Point RIGHT = new Point(1, 0);
        Point DOWN = new Point(0, 1);
        Point LEFT = new Point(-1, 0);
        Point UP = new Point(0, -1);
        this.m_travelledPosition.addAll(Arrays.asList(RIGHT,LEFT,DOWN,UP));
        this.dfsTravel(rightN, 0, path, 0);
		return false;
   }
   
    private boolean dfsTravel( Point pos,  int tracker, List<Point> path, int pindex)
    {
        Point newPos = new Point(pos.x + this.m_travelledPosition.get(tracker).x, pos.y + this.m_travelledPosition.get(tracker).y);
        //test if this is a valid grid cell
        if( withinBounds(newPos, grid)  &&
            ( grid[newPos.y][newPos.x] != GridValues.OBSTACLE ) &&
            ( grid[newPos.y][newPos.x] != GridValues.SEARCHED)
		)
        {
            path.add(pos);
            //check if my right neighbor is the goal
            if ( grid[newPos.y][newPos.x] == GridValues.GOAL)
			{
                path.add(newPos);
                return true;
            }
            grid[newPos.y][newPos.x] = GridValues.SEARCHED;
            pos = newPos;
            this.dfsTravel(pos, 0, path, pindex + 1);
        }
        else
        {
            //Right,Left,Down,Up
            if(tracker + 1 <= 3)
            {
			    this.dfsTravel(pos, tracker+1, path, pindex);
			}
            else 
			{
                pindex--;
                pos = path.get(pindex);
                path.remove(path.get(pindex));
                this.dfsTravel(pos, 0, path, pindex);
            }
        }
        return false;
    }

   private static boolean withinBounds(Point p, GridValues[][] grid)
   {
      return( (p.y >= 0 ) &&
              (p.y < grid.length) &&
              (p.x >= 0) &&
              (p.x < grid[0].length) 
	  		) ;
   }
}
